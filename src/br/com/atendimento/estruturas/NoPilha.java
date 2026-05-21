package br.com.atendimento.estruturas;

public class NoPilha<T> {
    private NoPilha<T> proximo;
    private T dado;

    public NoPilha(T dado) {
        this.dado = dado;
    }

    public NoPilha<T> getProximo() {
        return proximo;
    }
    public void setProximo(NoPilha<T> proximo) { this.proximo = proximo; }
    public T getDado() { return dado; }
}