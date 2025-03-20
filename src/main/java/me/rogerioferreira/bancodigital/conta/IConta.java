package me.rogerioferreira.bancodigital.conta;

public interface IConta {
	void abrirConta(double saldoInicial);

	void sacar(double valor);

	void depositar(double valor);

	void transferir(double valor, IConta contaDestino);

	double getSaldo();

	void imprimirExtrato();

	String getSequencialConta();
}
