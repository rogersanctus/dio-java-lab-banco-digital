package me.rogerioferreira.bancodigital.excecoes;

public class TransacaoException extends RuntimeException {
  public TransacaoException(String message) {
    super(message);
  }
}
