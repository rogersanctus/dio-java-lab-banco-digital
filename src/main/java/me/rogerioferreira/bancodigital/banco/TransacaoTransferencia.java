package me.rogerioferreira.bancodigital.banco;

import me.rogerioferreira.bancodigital.conta.IConta;

public record TransacaoTransferencia(IConta de, IConta para, double valor) implements ITransacao {
}
