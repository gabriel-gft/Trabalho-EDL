package br.com.atendimento.estruturas;

public class NoFila {
    private int numeroSenha;
    private char tipoSenha; // 'N' ou 'P'
    private NoFila proximo;

    public NoFila(int numeroSenha, char tipoSenha) {
        this.numeroSenha = numeroSenha;
        this.tipoSenha = tipoSenha;
        this.proximo = null;
    }

    public int getNumeroSenha() {
        return numeroSenha;
    }

    public char getTipoSenha() {
        return tipoSenha;
    }

    public NoFila getProximo() {
        return proximo;
    }

    public void setProximo(NoFila proximo) {
        this.proximo = proximo;
    }

    @Override
    public String toString() {
        return tipoSenha + String.valueOf(numeroSenha);
    }
}