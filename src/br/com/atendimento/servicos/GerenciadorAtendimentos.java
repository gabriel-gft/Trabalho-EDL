package br.com.atendimento.servicos;

import br.com.atendimento.estruturas.Fila;
import br.com.atendimento.estruturas.NoFila;
import br.com.atendimento.estruturas.Pilha;
import br.com.atendimento.modelos.PostoAtendimento;
import br.com.atendimento.modelos.Senha;
import br.com.atendimento.modelos.Pessoa;
import java.util.Random;

public class GerenciadorAtendimentos {

    //CODIGOS ANSI PARA COLORIR OS EVENTOS NO TERMINAL
    private static final String RESET   = "\033[0m";
    private static final String BOLD    = "\033[1m";
    private static final String VERDE   = "\033[32m";
    private static final String AMARELO = "\033[33m";
    private static final String VERMELHO= "\033[31m";
    private static final String CIANO   = "\033[36m";
    private static final String MAGENTA = "\033[35m";

    private static final int MAX_POSTOS = 5; //DEFINE O MAXIMO DE POSTOS
    private static final int MIN_POSTOS_ATIVOS = 3; //NO MINIMO 3 POSTOS ATIVOS SEMPRE
    private static final int FILAS_NORMAIS = 2; //ISSO DEFINE QUE SÃO DUAS FILAS NORMAIS PARA 1 DE PRIORIDADE
    private final Fila filaNormal; //CRIA FILA NORMAL
    private final Fila filaPrioritaria; //CRIA FILA PRIORITARIA

    //AS QUATRO PILHAS ABAIXO FORMAM DUAS FILAS FIFO (DUAS PILHAS CADA)
    private final Pilha<Pessoa> entradaNormal;
    private final Pilha<Pessoa> saidaNormal;
    private final Pilha<Pessoa> entradaPrioritaria;
    private final Pilha<Pessoa> saidaPrioritaria;

    private final Pilha<Pessoa> pessoasAtendidas; //PARA GUARDAR O HISTORICO DE QUEM FOI CHAMADO
    private final PostoAtendimento[] postos; //PARA GUARDAR OS POSTOS DE ATENDIMENTO
    private final Pessoa[] pessoaNoPosto; //PESSOA ATUALMENTE EM CADA POSTO (INDICE = INDICE DO POSTO)
    private final Random random; //RANDOM

    //ARMAZENAM RESULTADO DA ULTIMA CHAMADA PARA QUE O MAIN POSSA ANIMAR
    private Pessoa ultimaPessoaChamada;
    private int ultimoPostoChamado;

    //MAQUINA DE ESTADOS: 0 = Prioridade, 1 = Normal(1), 2 = Normal(2)
    private int turnoAlternancia;
    private int totalAtendidas;
    private int totalDesistencias;

    public GerenciadorAtendimentos() {
        this.filaNormal = new Fila();
        this.filaPrioritaria = new Fila();
        this.entradaNormal = new Pilha<>();
        this.saidaNormal = new Pilha<>();
        this.entradaPrioritaria = new Pilha<>();
        this.saidaPrioritaria = new Pilha<>();
        this.pessoasAtendidas = new Pilha<>();
        this.postos = new PostoAtendimento[MAX_POSTOS];
        this.pessoaNoPosto = new Pessoa[MAX_POSTOS];
        this.random = new Random();
        this.turnoAlternancia = 0; //Inicia garantindo que a Prioridade seja a 1ª
        this.totalAtendidas = 0;
        this.totalDesistencias = 0;

        inicializarPostos();
    }

    private void inicializarPostos() {
        for (int i = 0; i < MAX_POSTOS; i++) {
            //Se o indice for menor que MIN_POSTOS_ATIVOS (3), nasce como TRUE (Ativo)
            postos[i] = new PostoAtendimento(i + 1, i < MIN_POSTOS_ATIVOS);
            pessoaNoPosto[i] = null;
        }
    }

    private void enfileirarNormal(Pessoa pessoa){
        entradaNormal.empilhar(pessoa);
    }

    private Pessoa desenfileirarNormal() {
        if (saidaNormal.estaVazia()) {
            while (!entradaNormal.estaVazia()) {
                saidaNormal.empilhar(entradaNormal.desempilhar());
            }
        }
        return saidaNormal.desempilhar();
    }

    private void enfileirirarPrioritaria(Pessoa pessoa){
        entradaPrioritaria.empilhar(pessoa);
    }

    private Pessoa desenfileirarPrioritaria() {
        if (saidaPrioritaria.estaVazia()) {
            while (!entradaPrioritaria.estaVazia()) {
                saidaPrioritaria.empilhar(entradaPrioritaria.desempilhar());
            }
        }
        return saidaPrioritaria.desempilhar();
    }

    public Pessoa gerarPessoa(char tipo) {
        Pessoa pessoa = (tipo == 'P') ? Pessoa.gerarPrioritaria() : Pessoa.gerarNormal();
        Senha senha = new Senha(tipo);
        pessoa.setSenha(senha);

        if (tipo == 'N') {
            filaNormal.enfileirar(senha.getNumero(), senha.getTipo());
            enfileirarNormal(pessoa);
        } else {
            filaPrioritaria.enfileirar(senha.getNumero(), senha.getTipo());
            enfileirirarPrioritaria(pessoa);
        }
        return pessoa;
    }

    public boolean chamarProximaSenha() {
        int idx = encontrarIndicePostoLivre();
        if (idx == -1) return false;

        Pessoa pessoa = selecionarProximaPessoa();
        if (pessoa == null) return false;

        pessoaNoPosto[idx] = pessoa;
        postos[idx].atender(pessoa.getSenha());
        ultimaPessoaChamada = pessoa;
        ultimoPostoChamado  = postos[idx].getId();

        // EMPILHA AQUI: Garante que a ordem no relatorio final sera estritamente
        // a ordem em que as senhas foram chamadas para atendimento, refletindo a regra 2N:1P.
        pessoasAtendidas.empilhar(pessoa);

        return true;
    }

    //LOGICA: PRECEDENCIA ESTRITA PRIORITARIA (CICLO P -> N1 -> N2)
    private Pessoa selecionarProximaPessoa(){
        boolean temPrioridade = !filaPrioritaria.estaVazia();
        boolean temNormal = !filaNormal.estaVazia();

        if (!temPrioridade && !temNormal) return null;

        if (turnoAlternancia == 0) { // Turno Exclusivo da Prioridade
            if (temPrioridade) {
                turnoAlternancia = 1; // Avanca para comecar a chamar as Normais
                filaPrioritaria.desenfileirar();
                return desenfileirarPrioritaria();
            } else {
                // Se nao tiver prioridade, absorve como o 1º turno Normal e chama
                turnoAlternancia = 2; // Proximo sera o 2º Normal
                filaNormal.desenfileirar();
                return desenfileirarNormal();
            }
        } else { // Turno de chamar a fila Normal
            if (temNormal) {
                turnoAlternancia++;
                if (turnoAlternancia > FILAS_NORMAIS) {
                    turnoAlternancia = 0; // Se ja chamou as 2 normais, reseta pro ciclo da Prioridade
                }
                filaNormal.desenfileirar();
                return desenfileirarNormal();
            } else {
                // Fila normal vazia: Volta a atender prioridade ininterruptamente
                turnoAlternancia = 1;
                filaPrioritaria.desenfileirar();
                return desenfileirarPrioritaria();
            }
        }
    }

    public void finalizarAtendimentos() {
        for (int i = 0; i < MAX_POSTOS; i++) {
            PostoAtendimento posto = postos[i];
            if (!posto.isAtivo() || posto.isLivre()) continue;

            // Simulacao de duracao de atendimento aleatoria
            if (random.nextDouble() > 0.45 && random.nextDouble() > 0.3) {
                // O registro historico não acontece mais aqui para não embaralhar a lista
                pessoaNoPosto[i] = null;
                posto.liberar();
                totalAtendidas++; // Apenas computa que o atendimento terminou
            }
        }
    }

    public void simularDesistencias() {
        if (!filaNormal.estaVazia()
                && random.nextBoolean()
                && random.nextDouble() < 0.15 + random.nextDouble() * 0.35) {

            filaNormal.desenfileirar();
            Pessoa desistente = desenfileirarNormal();
            totalDesistencias++;
            System.out.printf("  " + VERMELHO + "!!" + RESET
                            + " " + BOLD + "%s" + RESET + " (Senha %s) desistiu da fila normal.%n",
                    desistente.getNome(), desistente.getSenha());
        }

        if (!filaPrioritaria.estaVazia()
                && random.nextBoolean()
                && random.nextBoolean()
                && random.nextDouble() < 0.10 + random.nextDouble() * 0.25) {

            filaPrioritaria.desenfileirar();
            Pessoa desistente = desenfileirarPrioritaria();
            totalDesistencias++;
            System.out.printf("  " + VERMELHO + "!!" + RESET
                            + " " + BOLD + "%s" + RESET + " [%s] (Senha %s) desistiu da fila.%n",
                    desistente.getNome(), desistente.getDescricaoPrioridade(), desistente.getSenha());
        }
    }

    public void gerenciarPostos() {
        int totalFila = filaNormal.getTamanho() + filaPrioritaria.getTamanho();
        //Os postos 1, 2 e 3 estao protegidos. O laco so avalia os postos de indice 3 e 4.
        for (int i = MIN_POSTOS_ATIVOS; i < MAX_POSTOS; i++) {
            PostoAtendimento posto = postos[i];
            if (posto.isAtivo() && !posto.isLivre()) continue;

            if (!posto.isAtivo() && totalFila > 5) {
                posto.setAtivo(true);
                System.out.printf("  " + AMARELO + "[+]" + RESET
                                + " Posto %d ativado (alta demanda: %d na fila).%n",
                        posto.getId(), totalFila);
            } else if (posto.isAtivo()
                    && totalFila <= 2
                    && contarPostosAtivos() > MIN_POSTOS_ATIVOS) {
                posto.setAtivo(false);
                System.out.printf("  " + CIANO + "[-]" + RESET
                                + " Posto %d desativado (baixa demanda: %d na fila).%n",
                        posto.getId(), totalFila);
            }
        }
    }

    private PostoAtendimento encontrarPostoLivre() {
        for (PostoAtendimento posto : postos) {
            if (posto.isLivre()) return posto;
        }
        return null;
    }

    private int encontrarIndicePostoLivre() {
        for (int i = 0; i < MAX_POSTOS; i++) {
            if (postos[i].isLivre()) return i;
        }
        return -1;
    }

    private int contarPostosAtivos() {
        int ativos = 0;
        for (PostoAtendimento posto : postos) {
            if (posto.isAtivo()) ativos++;
        }
        return ativos;
    }

    // SIMULADOR VISUAL DE CHAMADAS OTIMIZADO USANDO PONTEIROS REAIS DA FILA
    private String[] calcularProximasDuasSenhas() {
        String[] proximas = {"---", "---"};
        int fakeTurno = this.turnoAlternancia;

        NoFila ptrN = filaNormal.consultarPrimeiro();
        NoFila ptrP = filaPrioritaria.consultarPrimeiro();

        for (int i = 0; i < 2; i++) {
            if (ptrN == null && ptrP == null) break;

            if (fakeTurno == 0) { // Vez Prioridade na simulacao
                if (ptrP != null) {
                    proximas[i] = ptrP.toString();
                    ptrP = ptrP.getProximo();
                    fakeTurno = 1;
                } else {
                    proximas[i] = ptrN.toString();
                    ptrN = ptrN.getProximo();
                    fakeTurno = 2;
                }
            } else { // Vez Normal na simulacao
                if (ptrN != null) {
                    proximas[i] = ptrN.toString();
                    ptrN = ptrN.getProximo();
                    fakeTurno++;
                    if (fakeTurno > FILAS_NORMAIS) {
                        fakeTurno = 0;
                    }
                } else {
                    proximas[i] = ptrP.toString();
                    ptrP = ptrP.getProximo();
                    fakeTurno = 1;
                }
            }
        }
        return proximas;
    }

    public void exibirSenhasAtendidas() {
        System.out.println();
        System.out.println("  ============================================================");
        System.out.println("   ATENDIMENTOS REALIZADOS  (do ultimo para o primeiro)      ");
        System.out.println("  ============================================================");

        if (pessoasAtendidas.estaVazia()) {
            System.out.println("  Nenhuma pessoa foi atendida.");
        } else {
            // O totalAtendidas e usado apenas como base numerica correta de ID.
            int posicao = totalAtendidas;
            while (!pessoasAtendidas.estaVazia()) {
                Pessoa p = pessoasAtendidas.desempilhar();
                System.out.printf("  " + BOLD + "%3do" + RESET
                                + " atendimento -> Senha " + BOLD + "%-4s" + RESET + " | %s%n",
                        posicao--, p.getSenha(), p);
            }
        }

        System.out.println("  ============================================================");
    }

    public boolean filaEPostosVazios() {
        if (!filaNormal.estaVazia() || !filaPrioritaria.estaVazia()) return false;

        for (PostoAtendimento posto : postos) {
            if (posto.isAtivo() && !posto.isLivre()) return false;
        }

        return true;
    }

    public int getTotalAtendidas()       { return totalAtendidas;              }
    public int getTotalDesistencias()    { return totalDesistencias;           }
    public int getTotalFila()            { return filaNormal.getTamanho() + filaPrioritaria.getTamanho(); }
    public int getFilaNormalTamanho()    { return filaNormal.getTamanho();     }
    public int getFilaPrioridadeTamanho(){ return filaPrioritaria.getTamanho();}
    public PostoAtendimento[] getPostos(){ return postos;                      }
    public Pessoa getPessoaNoPosto(int i){ return pessoaNoPosto[i];            }
    public String[] getProximasSenhas()  { return calcularProximasDuasSenhas();}
    public Pessoa getUltimaPessoaChamada(){ return ultimaPessoaChamada;        }
    public int getUltimoPostoChamado()   { return ultimoPostoChamado;          }
}