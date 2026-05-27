package br.com.atendimento.modelos;

import java.util.Random;

public class Pessoa {

    //LISTA DE NOMES PARA GERACAO ALEATORIA
    private static final String[] NOMES = {
            "Ana", "Bruno", "Carlos", "Daniela", "Eduardo", "Fernanda",
            "Gabriel", "Helena", "Igor", "Juliana", "Klaus", "Larissa",
            "Marcos", "Natalia", "Otto", "Patricia", "Rafael", "Sandra",
            "Thiago", "Ursula", "Vitor", "Wendy", "Yago", "Yasmin", "Zeca",
            "Alice", "André", "Beatriz", "Caio", "Camila", "César", "Clara",
            "Diego", "Elisa", "Enzo", "Fábio", "Felipe", "Flávia", "Gustavo",
            "Hugo", "Isabela", "João", "José", "Karen", "Leonardo", "Lucas",
            "Luana", "Marcela", "Mateus", "Melissa", "Murilo", "Nicole", "Paulo",
            "Pedro", "Renata", "Ricardo", "Roberta", "Rodrigo", "Sabrina", "Samuel",
            "Simone", "Tatiane", "Vinicius", "William", "Xavier", "Aline", "Bianca",
            "Cauã", "Cristina", "Débora", "Emanuel", "Esther", "Francisco", "Giovana",
            "Henrique", "Iara", "Jéssica", "Kelvin", "Lívia", "Mirela", "Nicolas", "Olivia",
            "Priscila", "Quésia", "Raissa", "Sérgio", "Talita", "Ulisses", "Valentina", "Washington",
            "Yuri", "Zilda", "Adriana", "Breno", "Cintia", "Douglas", "Evelyn", "Fabiana",
            "Geovana", "Heitor", "Ivana", "Jonas", "Raphael", "Carlitos", "Alan Patrick"
    };

    //MOTIVOS DE PRIORIDADE
    public static final char IDOSO   = 'I'; //IDADE 65+
    public static final char GRAVIDA = 'G'; //GESTANTE
    public static final char PCD     = 'D'; //PESSOA COM DEFICIENCIA
    public static final char NORMAL  = '-'; //SEM PRIORIDADE

    private static final Random random = new Random();

    private final String nome;
    private final char motivoPrioridade;
    private final int idade;
    private Senha senha; //ASSOCIADA APOS A CRIACAO

    //CONSTRUTOR PARA PESSOA NORMAL - IDADE ENTRE 18 E 64
    public Pessoa() {
        this.nome             = NOMES[random.nextInt(NOMES.length)];
        this.motivoPrioridade = NORMAL;
        this.idade            = 18 + random.nextInt(47);
        this.senha            = null;
    }

    //CONSTRUTOR PARA PESSOA PRIORITARIA COM MOTIVO ESPECIFICO
    //IDOSOS TEM IDADE 65-94, DEMAIS MOTIVOS ENTRE 20 E 59
    public Pessoa(char motivoPrioridade) {
        this.nome             = NOMES[random.nextInt(NOMES.length)];
        this.motivoPrioridade = motivoPrioridade;
        this.idade            = (motivoPrioridade == IDOSO)
                ? 65 + random.nextInt(30)
                : 20 + random.nextInt(40);
        this.senha            = null;
    }

    //GERA UMA PESSOA NORMAL COM ATRIBUTOS ALEATORIOS
    public static Pessoa gerarNormal() {
        return new Pessoa();
    }

    //GERA UMA PESSOA PRIORITARIA COM MOTIVO ALEATORIO ENTRE IDOSO, GRAVIDA E PCD
    public static Pessoa gerarPrioritaria() {
        char[] motivos = {IDOSO, GRAVIDA, PCD};
        return new Pessoa(motivos[random.nextInt(motivos.length)]);
    }

    //RETORNA DESCRICAO LEGIVEL DO MOTIVO DE PRIORIDADE
    public String getDescricaoPrioridade() {
        switch (motivoPrioridade) {
            case IDOSO:   return "Idoso (" + idade + " anos)";
            case GRAVIDA: return "Gravida";
            case PCD:     return "PCD";
            default:      return "";
        }
    }

    public boolean isPrioritaria() { return motivoPrioridade != NORMAL; }
    public void setSenha(Senha senha)          { this.senha = senha; }
    public Senha getSenha()                    { return senha; }
    public String getNome()                    { return nome; }
    public int getIdade()                      { return idade; }
    public char getMotivoPrioridade()          { return motivoPrioridade; }

    @Override
    public String toString() {
        if (isPrioritaria()) {
            return nome + " [" + getDescricaoPrioridade() + "]";
        }
        return nome + " (" + idade + " anos)";
    }
}