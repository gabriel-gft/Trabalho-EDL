package br.com.atendimento.estruturas;

public class Fila {
    private NoFila inicio;
    private NoFila fim;
    private int tamanho;

    public Fila() {
        this.inicio = null;
        this.fim = null;
        this.tamanho = 0;
    }

    public boolean estaVazia() {
        return inicio == null;
    }

    public int getTamanho() {
        return tamanho;
    }

    public void enfileirar(int numeroSenha, char tipoSenha) {
        NoFila novo = new NoFila(numeroSenha, tipoSenha);

        if (estaVazia()) {
            inicio = novo;
            fim = novo;
        } else {
            fim.setProximo(novo);
            fim = novo;
        }

        tamanho++;
    }

    public NoFila desenfileirar() {
        if (estaVazia()) {
            return null;
        }

        NoFila removido = inicio;
        inicio = inicio.getProximo();

        if (inicio == null) {
            fim = null;
        }

        removido.setProximo(null);
        tamanho--;

        return removido;
    }

    public NoFila consultarPrimeiro() {
        return inicio;
    }

    public NoFila consultarSegundo() {
        if (inicio != null) {
            return inicio.getProximo();
        }
        return null;
    }
}