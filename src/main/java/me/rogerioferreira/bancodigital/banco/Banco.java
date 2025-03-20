package me.rogerioferreira.bancodigital.banco;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import me.rogerioferreira.bancodigital.conta.IConta;
import me.rogerioferreira.bancodigital.excecoes.TransacaoException;

public class Banco {

	private String nome;
	private List<IConta> contas = new ArrayList<>();
	private Map<String, Integer> sequencialTransacaoConta = new HashMap<>();
	private Map<ChaveTransacao, ITransacao> transacoes = new TreeMap<>();

	public Banco(String nome) {
		this.nome = nome;
	}

	public String getNome() {
		return nome;
	}

	public List<IConta> getContas() {
		return contas;
	}

	public void adicionarConta(IConta conta) {
		this.sequencialTransacaoConta.put(conta.getSequencialConta(), 0);
		this.contas.add(conta);
	}

	public void adicionarTransacao(IConta conta, ITransacao transacao) {
		var transacoesConta = this.getTransacoesConta(conta);
		var dataHora = OffsetDateTime.now();
		var sequencialTransacoes = this.sequencialTransacaoConta.get(conta.getSequencialConta());

		// Adicionado sequencial de transação por conta para evitar colisões de dataHora
		// para operações muito rápidas
		// que podem ocorrer mesmo fora de ambiente com múltiplas chamadas ocorrendo ao
		// mesmo tempo.
		// !! Uma colisão de chave pode representar a perda de uma Transação !!
		var sequencialConta = String.format("%s:%s", sequencialTransacoes, conta.getSequencialConta());

		var chave = new ChaveTransacao(dataHora, sequencialConta);

		this.sequencialTransacaoConta.put(conta.getSequencialConta(), sequencialTransacoes + 1);

		ITransacao primeiraTransacao = transacoesConta.size() > 0 ? transacoesConta.get(0) : null;

		switch (transacao) {
			case TransacaoAberturaConta abertura -> {
				if (transacoesConta.size() > 0) {
					throw new TransacaoException("A abertura de conta deve ser a primeira transação");
				}

				if (abertura.valorAbertura() < 0) {
					throw new TransacaoException("Valor de abertura não pode ser negativo");
				}

				this.transacoes.put(chave, abertura);
			}
			case TransacaoDeposito deposito -> {
				if (deposito.valor() <= 0) {
					throw new TransacaoException("Valor de deposito inválido");
				}

				if (!(primeiraTransacao instanceof TransacaoAberturaConta)) {
					throw new TransacaoException("Não houve abertura de conta");
				}

				this.transacoes.put(chave, deposito);
			}
			case TransacaoSaque saque -> {
				if (saque.valor() <= 0) {
					throw new TransacaoException("Valor de saque inválido");
				}

				if (!(primeiraTransacao instanceof TransacaoAberturaConta)) {
					throw new TransacaoException("Não houve abertura de conta");
				}

				if (computarTransacoes(conta, transacoesConta) < saque.valor()) {
					throw new TransacaoException("Saldo insuficiente");
				}

				this.transacoes.put(chave, saque);
			}
			case TransacaoTransferencia transferencia -> {
				var contaPara = transferencia.para();

				if (transferencia.valor() <= 0) {
					throw new TransacaoException("Valor de transferencia inválido");
				}

				if (!(primeiraTransacao instanceof TransacaoAberturaConta)) {
					throw new TransacaoException("Não houve abertura de conta");
				}

				boolean contaDestinoExiste = this.contas
						.stream()
						.anyMatch(contaBanco -> contaBanco.getSequencialConta().equals(contaPara.getSequencialConta()));

				if (!contaDestinoExiste) {
					throw new TransacaoException("Conta de destino inexistente");
				}

				if (computarTransacoes(conta, transacoesConta) < transferencia.valor()) {
					throw new TransacaoException("Saldo insuficiente");
				}

				this.transacoes.put(chave, transferencia);

				var contaParaSequencial = contaPara.getSequencialConta();
				var sequencialTransacoesPara = this.sequencialTransacaoConta.get(contaParaSequencial);
				var sequencialPara = String.format("%s:%s", sequencialTransacoesPara, contaParaSequencial);

				this.sequencialTransacaoConta.put(contaParaSequencial, sequencialTransacoesPara + 1);

				this.transacoes.put(new ChaveTransacao(dataHora, sequencialPara),
						new TransacaoTransferencia(transferencia.de(), contaPara, transferencia.valor()));
			}
			default -> {
				throw new TransacaoException("Transação inválida");
			}
		}
	}

	private List<ITransacao> getTransacoesConta(IConta conta) {
		return this.transacoes.entrySet()
				.stream()
				.filter(entry -> {
					var key = entry.getKey();
					var sequencialConta = key.sequencialConta().split(":")[1];

					return sequencialConta.equals(conta.getSequencialConta());
				})
				.map(entry -> entry.getValue())
				.toList();
	}

	private double computarTransacoes(IConta conta, List<ITransacao> transacoes) throws TransacaoException {
		ITransacao primeiraTransacao = transacoes.size() > 0 ? transacoes.get(0) : null;

		if (!(primeiraTransacao instanceof TransacaoAberturaConta)) {
			throw new TransacaoException("Transações inválidas. Não houve abertura de conta");
		}

		double saldo = 0;

		for (var transacao : transacoes) {
			switch (transacao) {
				case TransacaoAberturaConta aberturaConta -> {
					saldo = aberturaConta.valorAbertura();
				}
				case TransacaoDeposito deposito -> {
					saldo += deposito.valor();
				}
				case TransacaoSaque saque -> {
					saldo -= saque.valor();
				}
				case TransacaoTransferencia transferencia -> {
					// Transferindo desta para outra conta
					if (transferencia.de().equals(conta)) {
						saldo -= transferencia.valor();
					} else if (transferencia.para().equals(conta)) {
						// Recebendo transferência
						saldo += transferencia.valor();
					}
				}
				default -> {
				}
			}
		}

		return saldo;
	}

	public double getSaldoConta(IConta conta) throws TransacaoException {
		var transacoes = this.getTransacoesConta(conta);
		return computarTransacoes(conta, transacoes);
	}

	public double getSaldoTotal() throws TransacaoException {
		return this.contas
				.stream()
				.mapToDouble(conta -> getSaldoConta(conta))
				.sum();
	}

	@Override
	public String toString() {
		return "Banco{" +
				"nome='" + nome + '\'' +
				", quantidade de Contas=" + contas.size() +
				'}';
	}
}
