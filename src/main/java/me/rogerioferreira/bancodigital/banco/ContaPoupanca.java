package me.rogerioferreira.bancodigital.banco;

import me.rogerioferreira.bancodigital.cliente.Cliente;
import me.rogerioferreira.bancodigital.conta.Conta;

public class ContaPoupanca extends Conta {

	ContaPoupanca(Banco banco, Cliente cliente) {
		super(banco, cliente);
	}

	@Override
	public void imprimirExtrato() {
		System.out.println("=== Extrato Conta Poupança ===");
		super.imprimirInfosComuns();
	}
}
