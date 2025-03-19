package me.rogerioferreira.bancodigital.banco;

import java.util.List;

import me.rogerioferreira.bancodigital.conta.IConta;

public class Banco {

	private String nome;
	private List<IConta> contas;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public List<IConta> getContas() {
		return contas;
	}

	public void adicionarConta(IConta conta) {
		this.contas.add(conta);
	}

	@Override
	public String toString() {
		return "Banco{" +
				"nome='" + nome + '\'' +
				", quantidade de Contas=" + contas.size() +
				'}';
	}
}
