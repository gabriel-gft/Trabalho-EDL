package br.com.atendimento.modelos;

public class Senha {
    private static int contadorNormal = 1;
    private static int contadorPrioridade = 1;

    private final int numero;
    private final char tipo;

    public Senha(char tipo) {
        this.tipo = tipo;
        if (tipo == 'N') {
            this.numero = contadorNormal++;
        } else {
            this.numero = contadorPrioridade++;
        }
    }

    public int getNumero() {
        return numero;
    }

    public char getTipo() {
        return tipo;
    }

    public String getIdentificador() {
        return String.valueOf(tipo) + numero;
    }

    @Override
    public String toString() {
        return getIdentificador(); }
}