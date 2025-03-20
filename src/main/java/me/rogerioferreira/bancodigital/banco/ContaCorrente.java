package me.rogerioferreira.bancodigital.banco;

import me.rogerioferreira.bancodigital.cliente.Cliente;
import me.rogerioferreira.bancodigital.conta.Conta;

public class ContaCorrente extends Conta {

	ContaCorrente(Banco banco, Cliente cliente) {
		super(banco, cliente);
	}

	@Override
	public void imprimirExtrato() {
		System.out.println("=== Extrato Conta Corrente ===");
		super.imprimirInfosComuns();
	}

}
