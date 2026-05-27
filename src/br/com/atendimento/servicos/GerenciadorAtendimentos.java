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
    private static final int MIN_POSTOS_ATIVOS = 1; //DEFINE O MINIMO DE POSTOS ATIVOS (1)
    private static final int FILAS_NORMAIS = 2; //ISSO DEFINE QUE SÃO DUAS FILAS NORMAIS PARA 1 DE PRIORIDADE
    private final Fila filaNormal; //CRIA FILA NORMAL
    private final Fila filaPrioritaria; //CRIA FILA PRIORITARIA

    //AS QUATRO PILHAS ABAIXO FORMAM DUAS FILAS FIFO (DUAS PILHAS CADA)
    //AGORA ARMAZENAM PESSOA EM VEZ DE SENHA, PARA EXIBIR NOME E MOTIVO
    private final Pilha<Pessoa> entradaNormal; //PARA ENTRAR NA FILA NORMAL
    private final Pilha<Pessoa> saidaNormal; //PARA SAIR DA FILA NORMAL
    private final Pilha<Pessoa> entradaPrioritaria; //PARA ENTRAR NA FILA PRIORITARIA
    private final Pilha<Pessoa> saidaPrioritaria; //PARA SAIR DA FILA PRIORITARIA

    private final Pilha<Pessoa> pessoasAtendidas; //PARA GUARDAR PESSOAS JA ATENDIDAS
    private final PostoAtendimento[] postos; //PARA GUARDAR OS POSTOS DE ATENDIMENTO
    private final Pessoa[] pessoaNoPosto; //PESSOA ATUALMENTE EM CADA POSTO (INDICE = INDICE DO POSTO)
    private final Random random; //RANDOM

    //ARMAZENAM RESULTADO DA ULTIMA CHAMADA PARA QUE O MAIN POSSA ANIMAR
    private Pessoa ultimaPessoaChamada;
    private int ultimoPostoChamado;

    //CONTADOR 2N:1P
    private int normaisDesdeUltimaPrioritaria;
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
        this.postos = new PostoAtendimento[MAX_POSTOS]; //INSTANCIANDO ARRAY DE POSTOS DE ATENDIMENTO COM TAMANHO DO MAXIMO DE POSTOS
        this.pessoaNoPosto = new Pessoa[MAX_POSTOS]; //ARRAY PARALELO AO DE POSTOS: GUARDA QUEM ESTA EM CADA UM
        this.random = new Random();
        this.normaisDesdeUltimaPrioritaria = 0;
        this.totalAtendidas = 0;
        this.totalDesistencias = 0;

        inicializarPostos();
    }

    private void inicializarPostos() {
        for (int i = 0; i < MAX_POSTOS; i++) {
            postos[i] = new PostoAtendimento(i + 1, i < MIN_POSTOS_ATIVOS);
            pessoaNoPosto[i] = null; //NENHUMA PESSOA NO POSTO AO INICIAR
        }
    }

    //EMPILHAR NA PILHA DE ENTRADA
    //A PILHA DE ENTRADAS VAI ACUMULANDO AS SENHAS NOVAS
    private void enfileirarNormal(Pessoa pessoa){
        entradaNormal.empilhar(pessoa);
    }

    //SE A PILHA DE SAIDA ESTIVER VAZIA ELE VAI MOVER TODOS OS DA PILHA DE ENTRADA PARA A PILJA DE SAIDA
    //ISSO PARA INVERtER A ORDEM, DAI O MAIS ANTIGO FICA NO TOPO DA SAIDA
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

    //MESMA LÓGICA DA NORMAL
    private Pessoa desenfileirarPrioritaria() {
        if (saidaPrioritaria.estaVazia()) {
            while (!entradaPrioritaria.estaVazia()) {
                saidaPrioritaria.empilhar(entradaPrioritaria.desempilhar());
            }
        }
        return saidaPrioritaria.desempilhar();
    }

    //GERA UMA NOVA SENHA DEPENDENDO DO TIPO E INSERE NA FILA E NA RESPECTIVA PILHA
    // N = NORMAL
    // P = PRIORITARIA
    //CRIA A PESSOA CORRESPONDENTE E ASSOCIA A SENHA GERADA A ELA
    public Pessoa gerarPessoa(char tipo) {
        //CRIA A PESSOA COM ATRIBUTOS ALEATORIOS CONFORME O TIPO
        Pessoa pessoa = (tipo == 'P') ? Pessoa.gerarPrioritaria() : Pessoa.gerarNormal();
        Senha senha = new Senha(tipo);
        pessoa.setSenha(senha); //VINCULA A SENHA A PESSOA

        if (tipo == 'N') {
            filaNormal.enfileirar(senha.getNumero(), senha.getTipo());
            enfileirarNormal(pessoa);
        } else {
            filaPrioritaria.enfileirar(senha.getNumero(), senha.getTipo());
            enfileirirarPrioritaria(pessoa);
        }
        return pessoa;
    }

    //CHAMA A PROXIMA SENHA PARA O PROXIMO POSTO DISPONIVEL
    //ARMAZENA RESULTADO EM ultimaPessoaChamada E ultimoPostoChamado PARA ANIMACAO NO MAIN
    //PRINT REMOVIDO: O MAIN CONTROLA A EXIBICAO E ANIMACAO DA CHAMADA
    public boolean chamarProximaSenha() {
        int idx = encontrarIndicePostoLivre();
        if (idx == -1) return false;

        Pessoa pessoa = selecionarProximaPessoa();
        if (pessoa == null) return false;

        //REGISTRA A PESSOA NO ARRAY PARALELO E ATENDE NO POSTO
        pessoaNoPosto[idx] = pessoa;
        postos[idx].atender(pessoa.getSenha());
        ultimaPessoaChamada = pessoa;
        ultimoPostoChamado  = postos[idx].getId();
        return true;
    }

    private Pessoa selecionarProximaPessoa(){
        boolean temPrioridade = !filaPrioritaria.estaVazia();
        boolean temNormal = !filaNormal.estaVazia();

        if (!temPrioridade && !temNormal) return null;

        // REGRA 2N:1P: se ja chamou FILAS_NORMAIS normais seguidas E tem prioritaria, chama prioritaria
        if (temPrioridade && normaisDesdeUltimaPrioritaria >= FILAS_NORMAIS) {
            filaPrioritaria.desenfileirar();
            normaisDesdeUltimaPrioritaria = 0;
            return desenfileirarPrioritaria();
        }
        if (temNormal) {
            filaNormal.desenfileirar();
            normaisDesdeUltimaPrioritaria++;
            return desenfileirarNormal();
        }

        // SO RESTAM PRIORITARIAS: chama sem restricao de alternancia
        filaPrioritaria.desenfileirar();
        normaisDesdeUltimaPrioritaria = 0;
        return desenfileirarPrioritaria();
    }

    public void finalizarAtendimentos() {
        //USA INDICE PARA ACESSAR PESSOASNOPOSTO EM SINCRONIA COM POSTOS
        for (int i = 0; i < MAX_POSTOS; i++) {
            PostoAtendimento posto = postos[i];
            if (!posto.isAtivo() || posto.isLivre()) continue;

            if (random.nextDouble() > 0.45 && random.nextDouble() > 0.3) {
                Pessoa pessoa = pessoaNoPosto[i];
                pessoasAtendidas.empilhar(pessoa); //EMPILHA A PESSOA PARA EXIBICAO INVERTIDA NO FIM
                pessoaNoPosto[i] = null; //LIBERA O SLOT DO ARRAY PARALELO
                posto.liberar();
                totalAtendidas++;
                System.out.printf("  " + VERDE + "OK" + RESET
                                + " Posto %d finalizou: " + BOLD + "%s" + RESET
                                + " (Senha %s).%n",
                        posto.getId(), pessoa.getNome(), pessoa.getSenha());
            }
        }
    }

    public void simularDesistencias() {
        //NEXTBOOLEAN() E O PRIMEIRO FILTRO (50% DE CHANCE DE AVALIAR O RESTANTE)
        //O THRESHOLD DE NEXTDOUBLE() VARIA ENTRE 0.15 E 0.50 A CADA CHAMADA
        //TORNANDO A PROBABILIDADE REAL IMPREVISIVEL
        if (!filaNormal.estaVazia()
                && random.nextBoolean()
                && random.nextDouble() < 0.15 + random.nextDouble() * 0.35) {

            filaNormal.desenfileirar();               //REMOVE DA FILA OFICIAL
            Pessoa desistente = desenfileirarNormal(); //REMOVE DA FILA DE OBJETOS (SINCRONIA)
            totalDesistencias++;
            System.out.printf("  " + VERMELHO + "!!" + RESET
                            + " " + BOLD + "%s" + RESET + " (Senha %s) desistiu da fila normal.%n",
                    desistente.getNome(), desistente.getSenha());
        }

        //DOIS NEXTBOOLEAN() DEVEM SER VERDADEIROS (APROX 25% DE PASSAR POR AQUI)
        //TORNANDO DESISTENCIAS PRIORITARIAS MAIS RARAS QUE AS NORMAIS
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
        // BUG CORRIGIDO: i <= MAX_POSTOS causava ArrayIndexOutOfBoundsException (indice 5 nao existe)
        for (int i = MIN_POSTOS_ATIVOS; i < MAX_POSTOS; i++) {
            PostoAtendimento posto = postos[i];
            if (posto.isAtivo() && !posto.isLivre()) continue;
            if (!posto.isAtivo() && totalFila > 5) {
                posto.setAtivo(true);
                System.out.printf("  " + AMARELO + "[+]" + RESET
                                + " Posto %d ativado (alta demanda: %d na fila).%n",
                        posto.getId(), totalFila);
                // BUG CORRIGIDO: contarPostosAtivos sem () era um nome de campo, nao uma chamada de metodo
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

    // RETORNA O PRIMEIRO POSTO ATIVO E LIVRE, OU NULL SE NAO HOUVER
    private PostoAtendimento encontrarPostoLivre() {
        for (PostoAtendimento posto : postos) {
            if (posto.isLivre()) return posto;
        }
        return null;
    }

    //RETORNA O INDICE DO PRIMEIRO POSTO LIVRE, OU -1 SE NAO HOUVER
    //NECESSARIO PARA REGISTRAR A PESSOA EM PESSOASNOPOSTO[INDICE]
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

    private String[] calcularProximasDuasSenhas() {
        String[] proximas = {"---", "---"};
        //COPIAS VIRTUAIS DO ESTADO ATUAL - NAO MODIFICAM AS ESTRUTURAS REAIS
        int fakeCopiaNormais = normaisDesdeUltimaPrioritaria;
        int normalDispon = filaNormal.getTamanho();
        int prioritariaDispon = filaPrioritaria.getTamanho();

        if (normalDispon == 0 && prioritariaDispon == 0) return proximas; //FILAS VAZIAS

        //SIMULA A PRIMEIRA CHAMADA VIRTUAL
        boolean primeiraEhPrioritaria;

        if (prioritariaDispon > 0 && fakeCopiaNormais >= FILAS_NORMAIS) {
            //REGRA 2N:1P DISPARA: A PROXIMA DEVE SER PRIORITARIA
            proximas[0] = filaPrioritaria.consultarPrimeiro().toString();
            primeiraEhPrioritaria = true;
            fakeCopiaNormais = 0;
            prioritariaDispon--;
        } else if (normalDispon > 0) {
            //SITUACAO PADRAO: CHAMA NORMAL
            proximas[0] = filaNormal.consultarPrimeiro().toString();
            primeiraEhPrioritaria = false;
            fakeCopiaNormais++;
            normalDispon--;
        } else {
            //SO RESTAM PRIORITARIAS: CHAMA SEM RESTRICAO
            proximas[0] = filaPrioritaria.consultarPrimeiro().toString();
            primeiraEhPrioritaria = true;
            fakeCopiaNormais = 0;
            prioritariaDispon--;
        }

        //SEM MAIS SENHAS APOS A PRIMEIRA VIRTUAL, RETORNA O QUE TEMOS
        if (normalDispon == 0 && prioritariaDispon == 0) return proximas;

        //USA CONSULTARSEGUNDO() QUANDO AMBAS AS SENHAS VIRIAM DA MESMA FILA
        //OU CONSULTARPRIMEIRO() DA OUTRA FILA QUANDO A FONTE MUDA
        if (prioritariaDispon > 0 && fakeCopiaNormais >= FILAS_NORMAIS) {
            //SEGUNDA TAMBEM DEVE SER PRIORITARIA PELA REGRA 2N:1P
            if (primeiraEhPrioritaria) {
                //PRIMEIRA FOI PRIORITARIA: SEGUNDA E A SEGUINTE DA FILA DE PRIORIDADE
                NoFila seg = filaPrioritaria.consultarSegundo();
                proximas[1] = (seg != null) ? seg.toString() : "---";
            } else {
                //PRIMEIRA FOI NORMAL: SEGUNDA E O TOPO DA FILA DE PRIORIDADE
                proximas[1] = filaPrioritaria.consultarPrimeiro().toString();
            }
        } else if (normalDispon > 0) {
            //SEGUNDA DEVE SER NORMAL
            if (primeiraEhPrioritaria) {
                //PRIMEIRA FOI PRIORITARIA: SEGUNDA E O TOPO DA FILA NORMAL
                proximas[1] = filaNormal.consultarPrimeiro().toString();
            } else {
                //PRIMEIRA FOI NORMAL: SEGUNDA E A PROXIMA DA FILA NORMAL (SE HOUVER)
                NoFila seg = filaNormal.consultarSegundo();
                if (seg != null) {
                    proximas[1] = seg.toString();
                } else if (prioritariaDispon > 0) {
                    //NAO HA SEGUNDA NORMAL, MAS HA PRIORITARIA DISPONIVEL
                    proximas[1] = filaPrioritaria.consultarPrimeiro().toString();
                }
            }
        } else if (prioritariaDispon > 0) {
            //SO RESTAM PRIORITARIAS PARA A SEGUNDA
            if (primeiraEhPrioritaria) {
                NoFila seg = filaPrioritaria.consultarSegundo();
                proximas[1] = (seg != null) ? seg.toString() : "---";
            } else {
                proximas[1] = filaPrioritaria.consultarPrimeiro().toString();
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
            int posicao = totalAtendidas;
            //DESEMPILHA ATE ESVAZIAR: CADA ITERACAO RETIRA A SENHA MAIS RECENTE
            while (!pessoasAtendidas.estaVazia()) {
                Pessoa p = pessoasAtendidas.desempilhar();
                System.out.printf("  " + BOLD + "%3do" + RESET
                                + " atendimento -> Senha " + BOLD + "%-4s" + RESET + " | %s%n",
                        posicao--, p.getSenha(), p);
            }
        }

        System.out.println("  ============================================================");
    }

    //CHECAGENS DE ESTADO E GETTERS

    //RETORNA TRUE SE NAO HA NENHUMA SENHA NA FILA E TODOS OS POSTOS ATIVOS ESTAO LIVRES
    //USADO COMO CONDICAO DE ENCERRAMENTO ANTECIPADO DA SIMULACAO
    public boolean filaEPostosVazios() {
        //SE QUALQUER FILA AINDA TIVER SENHA, O SISTEMA NAO ESTA INATIVO
        if (!filaNormal.estaVazia() || !filaPrioritaria.estaVazia()) return false;

        //VERIFICA SE ALGUM POSTO ATIVO AINDA ESTA ATENDENDO ALGUEM
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