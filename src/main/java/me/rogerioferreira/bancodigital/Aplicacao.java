package me.rogerioferreira.bancodigital;

import me.rogerioferreira.bancodigital.cliente.Cliente;
import me.rogerioferreira.bancodigital.conta.Conta;
import me.rogerioferreira.bancodigital.conta.ContaCorrente;
import me.rogerioferreira.bancodigital.conta.ContaPoupanca;

public class Aplicacao {

	public static void main(String[] args) {
		Cliente venilton = new Cliente("Venilton");

		Conta cc = new ContaCorrente(venilton);
		Conta poupanca = new ContaPoupanca(venilton);

		cc.depositar(null, 100);
		cc.transferir(100, poupanca);

		cc.imprimirExtrato();
		poupanca.imprimirExtrato();
	}

}
