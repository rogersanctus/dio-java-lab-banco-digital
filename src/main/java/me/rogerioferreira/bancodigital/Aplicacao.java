package me.rogerioferreira.bancodigital;

import me.rogerioferreira.bancodigital.banco.Banco;
import me.rogerioferreira.bancodigital.cliente.Cliente;
import me.rogerioferreira.bancodigital.conta.TipoConta;

public class Aplicacao {
	public static void main(String[] args) {
		Cliente joao = new Cliente("João");
		Cliente maria = new Cliente("Maria");

		Banco banco = new Banco("Banco Binário");
		var ccj = banco.abrirConta(TipoConta.CORRENTE, joao, 500.00);
		var cpj = banco.abrirConta(TipoConta.POUPANCA, joao);

		var ccm = banco.abrirConta(TipoConta.CORRENTE, maria, 1000.00);

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
