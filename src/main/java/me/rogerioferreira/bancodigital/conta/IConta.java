package me.rogerioferreira.bancodigital.conta;

public interface IConta {

	void sacar(double valor);

	void depositar(IConta origem, double valor);

	void transferir(double valor, IConta contaDestino);

	void imprimirExtrato();
}
