package me.rogerioferreira.bancodigital.banco;

import java.time.OffsetDateTime;

public record ChaveTransacao(OffsetDateTime dataHora, String sequencialConta) implements Comparable<ChaveTransacao> {

  private String computarChave() {
    return String.format("%s-%s", dataHora.toEpochSecond(), sequencialConta);
  }

  @Override
  public String toString() {
    return computarChave();
  }

  @Override
  public int compareTo(ChaveTransacao o) {
    return computarChave().compareTo(o.computarChave());
  }
}
