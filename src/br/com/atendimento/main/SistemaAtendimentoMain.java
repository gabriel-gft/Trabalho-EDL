package br.com.atendimento.main;

import br.com.atendimento.servicos.GerenciadorAtendimentos;
import br.com.atendimento.modelos.Pessoa;
import br.com.atendimento.modelos.PostoAtendimento;
import java.util.Random;

//PONTO DE ENTRADA COM APRESENTACAO ANIMADA NO CONSOLE
//USA CODIGOS ANSI PARA CORES E ANIMACOES (REQUER TERMINAL COMPATIVEL)
public class SistemaAtendimentoMain {

    //CODIGOS ANSI PARA CORES E FORMATACAO
    private static final String RESET   = "\033[0m";
    private static final String BOLD    = "\033[1m";
    private static final String VERDE   = "\033[32m";
    private static final String AMARELO = "\033[33m";
    private static final String VERMELHO= "\033[31m";
    private static final String CIANO   = "\033[36m";
    private static final String MAGENTA = "\033[35m";
    private static final String BRANCO  = "\033[37m";
    private static final String AZUL    = "\033[34m";

    private static final int MAX_ITERACOES = 25;
    private static final int W = 72; //LARGURA INTERNA DO PAINEL (SEM AS BORDAS)

    public static void main(String[] args) throws InterruptedException {
        Random random = new Random();

        //SEQUENCIA DE ABERTURA
        exibirAbertura();

        GerenciadorAtendimentos g = new GerenciadorAtendimentos();

        //PRE-POPULACAO COM PESSOAS ANTES DE ABRIR O ATENDIMENTO
        int iniciais = 6 + random.nextInt(6);
        cls();
        cabecalho("PRE-ABERTURA: CHEGADA DOS PRIMEIROS CLIENTES");
        p("");
        for (int i = 0; i < iniciais; i++) {
            //40% de chance de gerar Prioridade para balancear com o consumo 2N:1P
            char tipo = (random.nextDouble() <= 0.40) ? 'P' : 'N';
            Pessoa pessoa = g.gerarPessoa(tipo);
            animarChegada(pessoa);
            pausar(250);
        }
        pausar(900);

        //LOOP PRINCIPAL DA SIMULACAO
        for (int iter = 1; iter <= MAX_ITERACOES; iter++) {

            //REDESENHA A CENA COMPLETA (ESTADO ATUAL ANTES DOS EVENTOS)
            cls();
            desenharCena(g, iter);
            pausar(450);

            //CABECALHO DA ITERACAO
            p("");
            p(CIANO + BOLD + "  ─── EVENTOS DA ITERACAO " + iter + " ───" + RESET);
            p("");
            pausar(350);

            //1. CHEGADA DE NOVOS CLIENTES
            //NEXTBOOLEAN() DECIDE SE ALGUEM CHEGA (50/50); NEXTINT(5) DECIDE QUANTOS
            int novasSenhas = random.nextBoolean() ? random.nextInt(5) : 0;
            if (novasSenhas > 0) {
                p("  " + AMARELO + BOLD + "[ CHEGADA ]" + RESET + " " + novasSenhas + " pessoa(s) entrando:");
                for (int i = 0; i < novasSenhas; i++) {
                    // FIX ESTATÍSTICO: 35% de chance de gerar Prioridade
                    char tipo = (random.nextDouble() <= 0.35) ? 'P' : 'N';
                    Pessoa pessoa = g.gerarPessoa(tipo);
                    animarChegada(pessoa);
                    pausar(320);
                }
            } else {
                p("  " + BRANCO + "Sem novas chegadas." + RESET);
            }
            pausar(500);

            //2. DESISTENCIAS
            g.simularDesistencias();
            pausar(450);

            //3. FINALIZACOES DE ATENDIMENTO
            g.finalizarAtendimentos();
            pausar(450);

            //4. GESTAO DINAMICA DE POSTOS
            g.gerenciarPostos();
            pausar(400);

            //5. CHAMADAS COM ANIMACAO DE CAMINHADA ATE O POSTO
            int chamados = 0;
            while (g.chamarProximaSenha()) {
                Pessoa quem  = g.getUltimaPessoaChamada();
                int postoId  = g.getUltimoPostoChamado();
                animarCaminhada(quem, postoId);
                chamados++;
                pausar(320);
            }
            if (chamados == 0 && g.getTotalFila() > 0) {
                p("  " + AMARELO + "-- Todos os postos ocupados. Aguardando liberacao." + RESET);
            } else if (chamados == 0) {
                p("  " + BRANCO + "-- Fila vazia. Nenhuma senha para chamar." + RESET);
            }

            //PAUSA MAIOR NO FINAL DE CADA ITERACAO PARA O USUARIO LER
            pausar(1600);

            //CONDICAO DE ENCERRAMENTO ANTECIPADO APOS METADE DAS ITERACOES
            //SO VERIFICA A PARTIR DA METADE PARA GARANTIR TEMPO MINIMO DE SIMULACAO
            if (iter >= MAX_ITERACOES / 2 && g.filaEPostosVazios()) {
                cls();
                desenharCena(g, iter);
                p("");
                lento(VERDE + BOLD + "  >>> Sistema esvaziado na iteracao " + iter
                        + ". Encerrando." + RESET, 25);
                pausar(600);
                break;
            }
        }

        //SE O LOOP ENCERROU POR LIMITE DE ITERACOES COM POSTOS AINDA OCUPADOS,
        //DRENA OS ATENDIMENTOS EM CURSO ATE QUE TODOS OS POSTOS FIQUEM LIVRES
        //ASSIM O RELATORIO FINAL CONTEM TODOS OS ATENDIMENTOS, SEM EXCECAO
        //NAO CHAMA NOVAS SENHAS DA FILA: APENAS ESGOTA QUEM JA ESTAVA SENDO ATENDIDO
        if (!g.todosPostosLivresOuInativos()) {
            cls();
            p("");
            p(AMARELO + BOLD + "  >>> Limite de " + MAX_ITERACOES
                    + " iteracoes atingido. Finalizando atendimentos em curso..." + RESET);
            p("");
            pausar(600);
            while (!g.todosPostosLivresOuInativos()) {
                g.finalizarAtendimentos();
                pausar(400);
            }
            p(VERDE + "  >>> Todos os postos liberados." + RESET);
            pausar(500);
        }

        //ENCERRAMENTO FINAL
        exibirEncerramento(g);
    }

    // =========================================================
    // ANIMACOES E EXIBICAO
    // =========================================================

    //ABERTURA ANIMADA COM TITULO ASCII E LEGENDA
    private static void exibirAbertura() throws InterruptedException {
        cls();
        pausar(400);

        //TITULO EM ASCII ART COM EFEITO DE DIGITACAO
        String[] titulo = {
                "  ╔══════════════════════════════════════════════════════════════╗",
                "  ║                                                              ║",
                "  ║     ░██████╗██╗░██████╗████████╗███████╗███╗   ███╗░█████╗   ║",
                "  ║     ██╔════╝██║██╔════╝╚══██╔══╝██╔════╝████╗ ████║██╔══██╗  ║",
                "  ║     ╚█████╗ ██║╚█████╗    ██║   █████╗  ██╔████╔██║███████║  ║",
                "  ║      ╚═══██╗██║ ╚═══██╗   ██║   ██╔══╝  ██║╚██╔╝██║██╔══██║  ║",
                "  ║     ██████╔╝██║██████╔╝   ██║   ███████╗██║ ╚═╝ ██║██║  ██║  ║",
                "  ║     ╚═════╝ ╚═╝╚═════╝    ╚═╝   ╚══════╝╚═╝     ╚═╝╚═╝  ╚═╝  ║",
                "  ║                                                              ║",
                "  ║            D E   A T E N D I M E N T O   P O R               ║",
                "  ║                      S E N H A S                             ║",
                "  ║                                                              ║",
                "  ╚══════════════════════════════════════════════════════════════╝"
        };

        //IMPRIME O TITULO LINHA POR LINHA COM PAUSA ENTRE ELAS
        for (String linha : titulo) {
            System.out.println(CIANO + BOLD + linha + RESET);
            pausar(150);
        }
        pausar(500);

        //LEGENDA PRINCIPAL
        p("");
        lento("  " + BOLD + "LEGENDA:" + RESET, 30);
        pausar(400);
        lento("  " + BRANCO  + "(o)" + RESET + "  Pessoa Normal", 18);
        pausar(450);
        lento("  " + MAGENTA + "(★)" + RESET + "  Pessoa Prioritaria  "
                + AMARELO + "[IDOS]" + RESET + " Idoso  "
                + MAGENTA + "[GRAV]" + RESET + " Gravida  "
                + AZUL    + "[PCD]"  + RESET + " Pessoa com Deficiencia", 12);
        pausar(250);
        lento("  " + VERDE   + "LIVRE" + RESET + " Posto livre    "
                + VERMELHO + "INAT" + RESET + " Posto inativo", 18);
        pausar(700);

        //ANIMACAO DE PESSOAS NA FILA AGUARDANDO
        p("");
        lento("  Aguardando abertura do atendimento...", 22);
        p("");
        pausar(400);
        String[] frames = {
                "      (o)  (o)  (★)  (o)  (o)  (★)  (o)  (o)      ",
                "      (o)  (o)  (★)  (o)  (o)  (★)  (o)  (o)      ",
                "      (o)  (o)  (★)  (o)  (o)  (★)  (o)  (o)     ",
                "      (o)  (o)  (★)  (o)  (o)  (★)  (o)  (o)      ",
        };
        //LOOP DE ANIMACAO DA FILA OSCILANDO (4 CICLOS)
        for (int ciclo = 0; ciclo < 4; ciclo++) {
            for (String frame : frames) {
                String cor = (frame.contains("★")) ? MAGENTA : BRANCO;
                System.out.print("\r" + cor + frame + RESET + "   ");
                System.out.flush();
                pausar(120);
            }
        }
        System.out.println();
        pausar(500);
        lento(VERDE + BOLD + "  >>> Iniciando o sistema..." + RESET, 30);
        pausar(700);
    }

    //DESENHA O PAINEL COMPLETO DA CENA: FILA + POSTOS + STATUS
    private static void desenharCena(GerenciadorAtendimentos g, int iter) {
        String borda = "═".repeat(W);
        int normais = g.getFilaNormalTamanho();
        int prios   = g.getFilaPrioridadeTamanho();
        String[] prox = g.getProximasSenhas();

        //CABECALHO DO PAINEL
        pf(CIANO + "╔" + borda + "╗" + RESET);
        String cabEsq = "  " + BOLD + "SISTEMA DE GERENCIAMENTO DE SENHAS" + RESET;
        String cabDir = "ITER " + String.format("%02d", iter) + "  ";
        int padCab = W - comprimentoVisivel(cabEsq) - comprimentoVisivel(cabDir);
        pf(CIANO + "║" + RESET + cabEsq + espaco(Math.max(0, padCab)) + cabDir + CIANO + "║" + RESET);
        pf(CIANO + "╠" + borda + "╣" + RESET);

        //LEGENDA RESUMIDA
        String legenda = "  " + BRANCO  + "(o)" + RESET + " Normal  "
                + MAGENTA + "(★)" + RESET + " Prioritario  "
                + AMARELO + "[IDOS]" + RESET + " Idoso  "
                + MAGENTA + "[GRAV]" + RESET + " Gravida  "
                + AZUL    + "[PCD]" + RESET;
        pf(CIANO + "║" + RESET + legenda + espaco(Math.max(0, W - comprimentoVisivel(legenda))) + CIANO + "║" + RESET);
        pf(CIANO + "╠" + borda + "╣" + RESET);

        //SECAO DA FILA DE ESPERA
        String filaStr = "  " + BOLD + "FILA DE ESPERA: " + RESET
                + (normais + prios) + " pessoa(s)"
                + "  (Normal: " + normais + " | Prior.: " + prios + ")";
        pf(CIANO + "║" + RESET + filaStr + espaco(Math.max(0, W - comprimentoVisivel(filaStr))) + CIANO + "║" + RESET);

        //VISUALIZACAO GRAFICA DAS FILAS COM ICONES
        pf(CIANO + "║" + RESET + "  Normal:    " + filaIcones(normais, "(o)", BRANCO) + CIANO + "║" + RESET);
        pf(CIANO + "║" + RESET + "  Prior.:    " + filaIcones(prios,   "(★)", MAGENTA) + CIANO + "║" + RESET);

        String proxStr = "  Proximas:  " + BOLD + prox[0] + RESET + "  e  " + BOLD + prox[1] + RESET;
        pf(CIANO + "║" + RESET + proxStr + espaco(Math.max(0, W - comprimentoVisivel(proxStr))) + CIANO + "║" + RESET);
        pf(CIANO + "╠" + borda + "╣" + RESET);

        //SECAO DOS POSTOS (5 CAIXAS LADO A LADO)
        String postosStr = "  " + BOLD + "POSTOS:" + RESET;
        pf(CIANO + "║" + RESET + postosStr + espaco(Math.max(0, W - comprimentoVisivel(postosStr))) + CIANO + "║" + RESET);

        PostoAtendimento[] postos = g.getPostos();

        //CADA POSTO E REPRESENTADO POR UMA CAIXA DE 5 LINHAS (TOPO/TITULO/ICONE/NOME/INFO/BASE)
        String[] linhas = new String[6];
        for (int k = 0; k < 6; k++) linhas[k] = "  ";

        for (int i = 0; i < postos.length; i++) {
            PostoAtendimento posto = postos[i];
            Pessoa pessoa  = g.getPessoaNoPosto(i);
            String sep     = (i < postos.length - 1) ? "  " : "";

            //MONTA CADA LINHA DA CAIXA DO POSTO COM 10 CHARS DE LARGURA INTERNA
            linhas[0] += "┌──────────┐" + sep;
            linhas[1] += CIANO + "│" + RESET + String.format(" POSTO %-2d ", posto.getId())
                    + CIANO + "│" + RESET + sep;

            if (!posto.isAtivo()) {
                //POSTO INATIVO: EXIBE VERMELHO
                linhas[2] += CIANO + "│" + RESET + "          " + CIANO + "│" + RESET + sep;
                linhas[3] += CIANO + "│" + RESET + VERMELHO + "  INATIVO " + RESET + CIANO + "│" + RESET + sep;
                linhas[4] += CIANO + "│" + RESET + "          " + CIANO + "│" + RESET + sep;
            } else if (posto.isLivre()) {
                //POSTO LIVRE: EXIBE VERDE
                linhas[2] += CIANO + "│" + RESET + "          " + CIANO + "│" + RESET + sep;
                linhas[3] += CIANO + "│" + RESET + VERDE + "   LIVRE  " + RESET + CIANO + "│" + RESET + sep;
                linhas[4] += CIANO + "│" + RESET + "          " + CIANO + "│" + RESET + sep;
            } else if (pessoa != null) {
                //POSTO OCUPADO: EXIBE ICONE, NOME E INFO DA PESSOA
                String icone = pessoa.isPrioritaria() ? MAGENTA + "   (★)   " + RESET
                        : BRANCO  + "   (o)   " + RESET;
                linhas[2] += CIANO + "│" + RESET + icone + " " + CIANO + "│" + RESET + sep;

                //NOME TRUNCADO PARA CABER NA CAIXA (MAX 8 CHARS)
                String nome = pessoa.getNome();
                if (nome.length() > 8) nome = nome.substring(0, 8);
                linhas[3] += CIANO + "│" + RESET + centrar(nome, 10) + CIANO + "│" + RESET + sep;

                //INFORMACAO: MOTIVO SE PRIORITARIA, OU IDADE SE NORMAL
                String info = pessoa.isPrioritaria()
                        ? "[" + abreviar(pessoa.getDescricaoPrioridade()) + "]"
                        : pessoa.getIdade() + " anos";
                linhas[4] += CIANO + "│" + RESET + centrar(info, 10) + CIANO + "│" + RESET + sep;
            } else {
                linhas[2] += CIANO + "│" + RESET + "          " + CIANO + "│" + RESET + sep;
                linhas[3] += CIANO + "│" + RESET + "          " + CIANO + "│" + RESET + sep;
                linhas[4] += CIANO + "│" + RESET + "          " + CIANO + "│" + RESET + sep;
            }
            linhas[5] += "└──────────┘" + sep;
        }

        //IMPRIME TODAS AS LINHAS DOS POSTOS DENTRO DO PAINEL COM PREENCHIMENTO ABSOLUTO
        //Como a estrutura das caixas garante sempre 70 colunas lógicas e W = 72,
        //usamos um espaçamento hardcoded ("  ") para não sofrer distorção de fontes.
        for (String linha : linhas) {
            pf(CIANO + "║" + RESET + linha + "  " + CIANO + "║" + RESET);
        }

        //RODAPE DO PAINEL COM ESTATISTICAS
        pf(CIANO + "╠" + borda + "╣" + RESET);
        String rodapeStr = "  " + VERDE   + "Atendidas: "    + g.getTotalAtendidas()    + RESET
                + "   " + VERMELHO + "Desistencias: " + g.getTotalDesistencias() + RESET
                + "   Na fila: " + g.getTotalFila();
        pf(CIANO + "║" + RESET + rodapeStr + espaco(Math.max(0, W - comprimentoVisivel(rodapeStr))) + CIANO + "║" + RESET);
        pf(CIANO + "╚" + borda + "╝" + RESET);
    }

    //ANIMACAO DE CHEGADA: PESSOA APARECE DIGITADA LETRA POR LETRA
    private static void animarChegada(Pessoa p) throws InterruptedException {
        String icone = p.isPrioritaria() ? MAGENTA + "(★)" + RESET : BRANCO + "(o)" + RESET;
        String info  = p.isPrioritaria()
                ? p.getNome() + " [" + p.getDescricaoPrioridade() + "]"
                : p.getNome() + " (" + p.getIdade() + " anos)";
        lento("    " + icone + " " + VERDE + info + RESET
                + " -> Senha " + BOLD + p.getSenha() + RESET, 14);
    }

    //ANIMACAO DE CAMINHADA: PESSOA SE MOVE DA ESQUERDA ATE O POSTO
    //USA \r PARA SOBRESCREVER A MESMA LINHA A CADA FRAME
    private static void animarCaminhada(Pessoa p, int postoId) throws InterruptedException {
        String icone   = p.isPrioritaria() ? MAGENTA + "(★)" + RESET : BRANCO + "(o)" + RESET;
        String nome    = p.getNome().length() > 9 ? p.getNome().substring(0, 9) : p.getNome();
        String destino = CIANO + "[POSTO " + postoId + "]" + RESET;
        //LARGURA TOTAL DA TRILHA (ESPACO ENTRE A PESSOA E O DESTINO)
        int trilha = 32;
        String prefixo = "  " + AMARELO + ">>" + RESET + " " + icone + " "
                + BOLD + String.format("%-9s", nome) + RESET + " ";

        //A CADA FRAME, A PESSOA AVANCA 3 POSICOES PARA A DIREITA (TRILHA DIMINUI)
        for (int pos = 0; pos <= trilha; pos += 3) {
            StringBuilder linha = new StringBuilder(prefixo);
            //ESPACOS JA PERCORRIDOS (PESSOA AVANCOU)
            for (int s = 0; s < pos; s++) linha.append(' ');
            //PONTOS RESTANTES (CAMINHO AINDA A PERCORRER)
            for (int d = pos; d < trilha; d++) linha.append('.');
            linha.append(' ').append(destino);

            System.out.print("\r" + linha);
            System.out.flush();
            pausar(65);
        }
        System.out.println();
    }

    //ENCERRAMENTO FINAL COM RELATORIO E PILHA DE ATENDIMENTOS
    private static void exibirEncerramento(GerenciadorAtendimentos g) throws InterruptedException {
        cls();
        pausar(300);

        //CABECALHO DO ENCERRAMENTO
        lento(CIANO + BOLD + "  ╔══════════════════════════════════════════════════════════════╗" + RESET, 5);
        lento(CIANO + BOLD + "  ║            ENCERRAMENTO DO ATENDIMENTO                       ║" + RESET, 5);
        lento(CIANO + BOLD + "  ╚══════════════════════════════════════════════════════════════╝" + RESET, 5);
        p("");
        pausar(400);

        //RELATORIO NUMERICO
        lento("  " + VERDE   + "Total de atendimentos realizados : " + BOLD + g.getTotalAtendidas()    + RESET, 20);
        pausar(200);
        lento("  " + VERMELHO + "Total de desistencias registradas: " + BOLD + g.getTotalDesistencias() + RESET, 20);
        pausar(200);
        lento("  " + BRANCO   + "Pessoas ainda aguardando na fila : " + BOLD + g.getTotalFila()         + RESET, 20);
        pausar(400);

        //EXIBE A PILHA DE ATENDIMENTOS (DO ULTIMO AO PRIMEIRO)
        g.exibirSenhasAtendidas();
        pausar(300);

        p("");
        lento(CIANO + BOLD + "  Sistema encerrado. Ate logo!" + RESET, 35);
        p("");
    }

    // =========================================================
    // UTILITARIOS DE DISPLAY
    // =========================================================

    // REMOVE OS CÓDIGOS ANSI PARA CALCULAR O TAMANHO REAL DA STRING VISÍVEL
    private static int comprimentoVisivel(String s) {
        if (s == null) return 0;
        return s.replaceAll("\033\\[[;\\d]*m", "").length();
    }

    //LIMPA A TELA USANDO ESCAPE CODE ANSI
    private static void cls() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    //IMPRIME UMA LINHA COM PRINTLN
    private static void p(String s) { System.out.println(s); }

    //IMPRIME UMA LINHA DENTRO DO PAINEL (PULA LINHA SIMPLES)
    private static void pf(String s) { System.out.println(s); }

    //IMPRIME CABECALHO DE SECAO COM BORDA SIMPLES
    private static void cabecalho(String texto) {
        System.out.println(CIANO + BOLD + "  >>> " + texto + RESET);
    }

    //EFEITO DE DIGITACAO: IMPRIME CARACTER POR CARACTER COM PAUSA
    //IGNORA CODIGOS ANSI NO CALCULO DO DELAY (APENAS CHARS VISIVEIS CAUSAM PAUSA)
    private static void lento(String texto, int ms) throws InterruptedException {
        boolean emCodigo = false;
        for (char c : texto.toCharArray()) {
            System.out.print(c);
            System.out.flush();
            //DETECTA INICIO E FIM DE CODIGO ANSI (COMECA COM \033[, TERMINA COM LETRA)
            if (c == '\033') { emCodigo = true; continue; }
            if (emCodigo) {
                if (Character.isLetter(c)) emCodigo = false;
                continue;
            }
            Thread.sleep(ms);
        }
        System.out.println();
    }

    //PAUSA A EXECUCAO POR MS MILISSEGUNDOS
    private static void pausar(int ms) throws InterruptedException {
        Thread.sleep(ms);
    }

    //GERA UMA STRING DE N ESPACOS PARA ALINHAMENTO NO PAINEL
    private static String espaco(int n) {
        if (n <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(' ');
        return sb.toString();
    }

    //CENTRALIZA UM TEXTO EM UMA LARGURA FIXA (SEM ANSI, PARA CALCULO CORRETO)
    private static String centrar(String texto, int largura) {
        if (texto.length() >= largura) return texto.substring(0, largura);
        int antes = (largura - texto.length()) / 2;
        int depois = largura - texto.length() - antes;
        return espaco(antes) + texto + espaco(depois);
    }

    //ABREVIA O MOTIVO DE PRIORIDADE PARA CABER NA CAIXA DO POSTO (MAX 4 CHARS)
    private static String abreviar(String descricao) {
        if (descricao.startsWith("Idoso")) return "IDOS";
        if (descricao.startsWith("Grav"))  return "GRAV";
        if (descricao.startsWith("PCD"))   return "PCD ";
        return descricao.length() > 4 ? descricao.substring(0, 4) : descricao;
    }

    //GERA LINHA DE ICONES DA FILA COM COR E LIMITE DE 16 ICONES VISIVEIS
    private static String filaIcones(int qtd, String icone, String cor) {
        if (qtd == 0) {
            String vazia = BRANCO + "(vazia)" + RESET;
            return vazia + espaco(Math.max(0, W - 13 - comprimentoVisivel(vazia)));
        }
        StringBuilder sb = new StringBuilder();
        int mostra = Math.min(qtd, 16);
        for (int i = 0; i < mostra; i++) sb.append(cor).append(icone).append(RESET);
        if (qtd > 16) sb.append(AMARELO).append(" +(").append(qtd - 16).append(")").append(RESET);

        int pad = W - 13 - comprimentoVisivel(sb.toString());
        if (pad > 0) sb.append(espaco(pad));
        return sb.toString();
    }
}