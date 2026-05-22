package br.com.atendimento.modelos;

public class Senha {
    private static int contadorNormal = 1;
    private static int contadorPrioridade = 1;

    private final int numero;
    private final char tipo;

    public Senha(char tipo) {
        this.tipo = tipo;
        this.numero = (tipo == 'N') ? contadorNormal++ : contadorPrioridade++;
    }

    public int getNumero() { return numero; }
    public char getTipo() { return tipo; }
    public String getIdentificador() { return tipo + String.valueOf(numero); }

    public static void resetarContadores() {
        contadorNormal = 1;
        contadorPrioridade = 1;
    }

    @Override
    public String toString() { return getIdentificador(); }
}