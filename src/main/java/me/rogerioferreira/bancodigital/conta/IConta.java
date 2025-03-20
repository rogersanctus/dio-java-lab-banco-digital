package me.rogerioferreira.bancodigital.conta;

import me.rogerioferreira.bancodigital.cliente.Cliente;

public interface IConta {
	void abrirConta(double saldoInicial);

	void sacar(double valor);

	void depositar(double valor);

	void transferir(double valor, IConta contaDestino);

	double getSaldo();

	void imprimirExtrato();

	Cliente getCliente();

	String getSequencialConta();

	int getAgencia();
}
