package br.com.atendimento.estruturas;

public class Pilha<T> {
    private NoPilha<T> topo;
    private int tamanho;

    public Pilha() {
        this.topo = null;
        this.tamanho = 0;
    }

    public void empilhar (T dado) {
        if (estaVazia()) {
            topo = new NoPilha<>(dado);
        } else {
            NoPilha<T> novo_no = new NoPilha<>(dado);
            novo_no.setProximo(topo);
            topo = novo_no;
        }
        tamanho++;
    }

    public T desempilhar() {
        if (estaVazia()) {
            System.out.println("Pilha vazia. Impossível desempilhar.");
            return null;
        }
        tamanho--;
        T dado_temp = topo.getDado();
        topo = topo.getProximo();
        return dado_temp;
    }

    public boolean estaVazia() { return tamanho == 0; }
    public int getTamanho() { return tamanho; }
}