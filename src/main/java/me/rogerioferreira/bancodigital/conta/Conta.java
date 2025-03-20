package me.rogerioferreira.bancodigital.conta;

import me.rogerioferreira.bancodigital.banco.Banco;
import me.rogerioferreira.bancodigital.banco.TransacaoAberturaConta;
import me.rogerioferreira.bancodigital.banco.TransacaoDeposito;
import me.rogerioferreira.bancodigital.banco.TransacaoSaque;
import me.rogerioferreira.bancodigital.banco.TransacaoTransferencia;
import me.rogerioferreira.bancodigital.cliente.Cliente;

public abstract class Conta implements IConta {

	protected Banco banco;
	protected Cliente cliente;
	private static final int AGENCIA_PADRAO = 1;
	private static int SEQUENCIAL = 1;

	protected int agencia;

	protected String sequencialConta;

	public Conta(Banco banco, Cliente cliente) {
		var numero = SEQUENCIAL++;

		this.banco = banco;
		this.agencia = Conta.AGENCIA_PADRAO;
		this.cliente = cliente;
		this.sequencialConta = String.valueOf(numero);
	}

	@Override
	public void abrirConta(double saldoInicial) {
		this.banco.adicionarTransacao(this, new TransacaoAberturaConta(saldoInicial));
	}

	@Override
	public void sacar(double valor) {
		this.banco.adicionarTransacao(this, new TransacaoSaque(valor));
	}

	@Override
	public void depositar(double valor) {
		this.banco.adicionarTransacao(this, new TransacaoDeposito(valor));
	}

	@Override
	public void transferir(double valor, IConta contaDestino) {
		this.banco.adicionarTransacao(this, new TransacaoTransferencia(this, contaDestino, valor));
	}

	@Override
	public Cliente getCliente() {
		return this.cliente;
	}

	@Override
	public String getSequencialConta() {
		return this.sequencialConta;
	}

	@Override
	public int getAgencia() {
		return agencia;
	}

	@Override
	public double getSaldo() {
		return this.banco.getSaldoConta(this);
	}

	protected void imprimirInfosComuns() {
		System.out.println(String.format("Titular: %s", this.cliente.nome()));
		System.out.println(String.format("Agencia: %d", this.agencia));
		System.out.println(String.format("Número da Conta: %s", this.sequencialConta));
		System.out.println(String.format("Saldo: %.2f", this.getSaldo()));

		this.banco.imprimirDetalhesExtratoConta(this);

		System.out.println("=== Fim Extrato ===");
		System.out.println();
	}
}
