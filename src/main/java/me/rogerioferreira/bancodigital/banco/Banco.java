package me.rogerioferreira.bancodigital.banco;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import java.util.TreeMap;

import me.rogerioferreira.bancodigital.cliente.Cliente;
import me.rogerioferreira.bancodigital.conta.IConta;
import me.rogerioferreira.bancodigital.conta.TipoConta;
import me.rogerioferreira.bancodigital.excecoes.TransacaoException;

public class Banco {

	private String nome;
	private List<IConta> contas = new ArrayList<>();
	private Map<String, Integer> sequencialTransacaoConta = new HashMap<>();
	private Map<ChaveTransacao, ITransacao> transacoes = new TreeMap<>();

	public Banco(String nome) {
		this.nome = nome;
	}

	private ChaveTransacao gerarChaveTransacao(OffsetDateTime dataHora, String sequencialConta) {
		// Adicionado sequencial de transação por conta para evitar colisões de dataHora
		// para operações muito rápidas
		// que podem ocorrer mesmo fora de ambiente com múltiplas chamadas ocorrendo ao
		// mesmo tempo.
		// !! Uma colisão de chave pode representar a perda de uma Transação !!

		var sequencialTransacao = this.sequencialTransacaoConta.get(sequencialConta);
		var sequencialTransacaoConta = String.format("%s:%s", sequencialTransacao, sequencialConta);

		this.sequencialTransacaoConta.put(sequencialConta, sequencialTransacao + 1);

		return new ChaveTransacao(dataHora, sequencialTransacaoConta);
	}

	public String getNome() {
		return nome;
	}

	public List<IConta> getContas() {
		return contas;
	}

	public IConta abrirConta(TipoConta tipoConta, Cliente cliente) {
		return this.abrirConta(tipoConta, cliente, null);
	}

	public IConta abrirConta(TipoConta tipoConta, Cliente cliente, Double saldoInicial) {
		var conta = switch (tipoConta) {
			case TipoConta.POUPANCA ->
				new ContaPoupanca(this, cliente);
			case TipoConta.CORRENTE ->
				new ContaCorrente(this, cliente);
			default ->
				new ContaCorrente(this, cliente);
		};

		this.adicionarConta(conta);

		if (saldoInicial != null) {
			conta.abrirConta(saldoInicial);
		}

		return conta;
	}

	private void adicionarConta(IConta conta) {
		this.sequencialTransacaoConta.put(conta.getSequencialConta(), 0);
		this.contas.add(conta);
	}

	public void adicionarTransacao(IConta conta, ITransacao transacao) {
		var transacoesConta = this.getTransacoesConta(conta)
				.map(entry -> entry.getValue())
				.toList();

		var dataHora = OffsetDateTime.now();

		var chave = this.gerarChaveTransacao(dataHora, conta.getSequencialConta());

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
				this.transacoes.put(this.gerarChaveTransacao(dataHora, contaPara.getSequencialConta()),
						new TransacaoTransferencia(transferencia.de(), contaPara, transferencia.valor()));
			}
			default -> {
				throw new TransacaoException("Transação inválida");
			}
		}
	}

	private Stream<Entry<ChaveTransacao, ITransacao>> getTransacoesConta(IConta conta) {
		return this.transacoes.entrySet()
				.stream()
				.filter(entry -> {
					var key = entry.getKey();
					var sequencialConta = key.sequencialConta().split(":")[1];

					return sequencialConta.equals(conta.getSequencialConta());
				});
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
		var transacoes = this.getTransacoesConta(conta)
				.map(entry -> entry.getValue())
				.toList();

		return computarTransacoes(conta, transacoes);
	}

	public double getSaldoTotal() throws TransacaoException {
		return this.contas
				.stream()
				.mapToDouble(conta -> getSaldoConta(conta))
				.sum();
	}

	public void imprimirDetalhesExtratoConta(IConta conta) {
		this.getTransacoesConta(conta)
				.filter(entry -> {
					var chave = entry.getKey();
					var dataHora = chave.dataHora();
					var dataHoraAtual = OffsetDateTime.now();

					// Todas as transações de uma semana atrás até a data atual
					return dataHora.isEqual(dataHoraAtual) || dataHora.isAfter(dataHoraAtual.minusWeeks(1));
				})
				.forEach(entry -> {
					var dataHora = entry.getKey().dataHora();
					var contaTransacao = entry.getKey().sequencialConta().split(":")[1];
					var transacao = entry.getValue();
					var dataHoraFormatada = dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

					switch (transacao) {
						case TransacaoAberturaConta aberturaConta -> {
							System.out.println(
									dataHoraFormatada + " - Abertura de Conta | valor de abertura: " + aberturaConta.valorAbertura());
						}
						case TransacaoDeposito deposito -> {
							System.out.println(dataHoraFormatada + " - Depósito | valor: " + deposito.valor());
						}
						case TransacaoSaque saque -> {
							System.out.println(dataHoraFormatada + " - Saque | valor: " + saque.valor());
						}
						case TransacaoTransferencia transferencia -> {
							var de = transferencia.de();
							var para = transferencia.para();
							var deCliente = de.getCliente().nome();
							var paraCliente = para.getCliente().nome();
							var deConta = de.getSequencialConta();
							var paraConta = para.getSequencialConta();

							// Se quem criou a transação foi quem enviou ("de"), a transferência é "para" a
							// outra parte.
							// Caso contrário, a transferência é vinda "de" a outra parte
							var detalheTransferencia = contaTransacao.equals(deConta) ? "para " + paraCliente + "(" + paraConta + ")"
									: "de " + deCliente + "(" + deConta + ")";

							System.out.println(dataHoraFormatada + " - Transferência \n\t| " + detalheTransferencia + "\n\t| valor: "
									+ transferencia.valor());
						}
						default -> {
						}
					}
				});
	}

	@Override
	public String toString() {
		return "Banco{" +
				"nome='" + nome + '\'' +
				", quantidade de Contas=" + contas.size() +
				'}';
	}
}
