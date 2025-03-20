package me.rogerioferreira.bancodigital;

import me.rogerioferreira.bancodigital.banco.Banco;
import me.rogerioferreira.bancodigital.cliente.Cliente;
import me.rogerioferreira.bancodigital.conta.Conta;
import me.rogerioferreira.bancodigital.conta.ContaCorrente;
import me.rogerioferreira.bancodigital.conta.ContaPoupanca;

public class Aplicacao {
	public static void main(String[] args) {
		Cliente joao = new Cliente("João");
		Cliente maria = new Cliente("Maria");

		Banco banco = new Banco();
		Conta ccj = new ContaCorrente(banco, joao, 500.00);
		Conta cpj = new ContaPoupanca(banco, joao);

		Conta ccm = new ContaCorrente(banco, maria, 1000.00);

		cpj.abrirConta(0.0);
		cpj.depositar(250.00); // cpj 250.00
		cpj.transferir(100.50, ccm); // cpj 149.50 ccm 1100.50

		ccj.depositar(250.00); // ccj 750.00
		ccm.transferir(175.00, ccj); // ccm 925.50 ccj 925.00

		ccj.imprimirExtrato();
		cpj.imprimirExtrato();
		ccm.imprimirExtrato();

		System.out.println("\nSaldo Total no Banco: " + banco.getSaldoTotal());
	}

}
