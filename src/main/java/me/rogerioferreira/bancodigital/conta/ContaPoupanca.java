package me.rogerioferreira.bancodigital.conta;

import me.rogerioferreira.bancodigital.banco.Banco;
import me.rogerioferreira.bancodigital.cliente.Cliente;

public class ContaPoupanca extends Conta {

	public ContaPoupanca(Banco banco, Cliente cliente) {
		super(banco, cliente);
	}

	public ContaPoupanca(Banco banco, Cliente cliente, double saldoInicial) {
		super(banco, cliente, saldoInicial);
	}

	@Override
	public void imprimirExtrato() {
		System.out.println("=== Extrato Conta Poupança ===");
		super.imprimirInfosComuns();
	}
}
