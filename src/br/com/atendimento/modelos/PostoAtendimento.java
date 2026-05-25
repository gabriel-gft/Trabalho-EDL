package br.com.atendimento.modelos;

public class PostoAtendimento {
    private final int id;
    private boolean ativo;
    private Senha senhaEmAtendimento;

    public PostoAtendimento(int id, boolean ativo) {
        this.id = id;
        this.ativo = ativo;
        this.senhaEmAtendimento = null;
    }

    public int getId() { return id; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public boolean isLivre() { return ativo && senhaEmAtendimento == null; }
    public Senha getSenhaEmAtendimento() { return senhaEmAtendimento; }

    public void atender(Senha senha) { this.senhaEmAtendimento = senha; }
    public void liberar() { this.senhaEmAtendimento = null; }

    public String getStatus() {
        if (!ativo) return "Inativo";

        if (senhaEmAtendimento == null) {
            return "Livre";
        } else {
            return "Ocupado";
        }
    }

    @Override
    public String toString() {
        return String.format("Posto %d [%s]", id, getStatus());
    }
}