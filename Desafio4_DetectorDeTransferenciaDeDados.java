

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Desafio 4: Identificar Picos de Transferência de Dados
 *
 * Detecta quando o volume de dados transferidos aumenta drasticamente.
 * Isso pode indicar roubo de dados (exfiltração).
 */
public class Desafio4_DetectorDeTransferenciaDeDados {

    /**
     * Encontra picos de transferência no arquivo de logs.
     *
     * Um pico acontece quando um evento tem menos bytes que outro evento futuro.
     * Exemplo: Se agora foram 100 bytes e depois foram 500 bytes, isso é um pico!
     *
     * @param caminhoArquivo Caminho do arquivo CSV
     * @return Map com pares: timestamp atual → timestamp do próximo maior
     * @throws IOException Se der erro ao ler o arquivo
     */
    public Map<Long, Long> identificarPicosTransferencia(String caminhoArquivo) throws IOException {

        // ═══════════════════════════════════════════════════════════════
        // PASSO 1: CRIAR AS ESTRUTURAS DE DADOS
        // ═══════════════════════════════════════════════════════════════

        // Cria um Map (dicionário) para guardar os resultados
        // Map funciona como: chave → valor
        // Aqui: timestamp atual → timestamp do próximo maior
        Map<Long, Long> picosEncontrados = new HashMap<>();

        // Cria uma lista para guardar os timestamps (momentos) de cada evento
        // Long é um tipo de número inteiro grande
        // ArrayList é uma lista que pode crescer conforme adicionamos itens
        List<Long> listaDeTimes = new ArrayList<>();

        // Cria uma lista para guardar quantos bytes foram transferidos
        // Esta lista vai estar sincronizada com a lista de timestamps
        // Posição 0 de listaDeTimes corresponde à posição 0 de listaDeBytes
        List<Long> listaDeBytes = new ArrayList<>();


        // ═══════════════════════════════════════════════════════════════
        // PASSO 2: LER O ARQUIVO CSV
        // ═══════════════════════════════════════════════════════════════

        // BufferedReader é uma classe que lê arquivos de texto
        // Usamos ela para ler o arquivo linha por linha
        BufferedReader leitor = new BufferedReader(new FileReader(caminhoArquivo));

        try {
            // Lê a primeira linha do arquivo (cabeçalho)
            // Cabeçalho tem os nomes das colunas: TIMESTAMP, USER_ID, etc
            // Não vamos usar essa linha, só pulamos ela
            String linhaDoArquivo = leitor.readLine();

            // Loop que vai ler TODAS as linhas do arquivo
            // Continua enquanto houver linhas para ler
            // Quando chegar no final, linhaDoArquivo será null
            while ((linhaDoArquivo = leitor.readLine()) != null) {

                // Se a linha está vazia (sem nada), pula para a próxima
                if (linhaDoArquivo.isEmpty()) {
                    continue; // Vai para a próxima iteração do loop
                }

                // split(",") divide a linha toda vez que encontra uma vírgula
                // Exemplo: "100,alice,session1" vira ["100", "alice", "session1"]
                // Isso transforma a linha CSV em um array (vetor) de textos
                String[] colunasDoCSV = linhaDoArquivo.split(",");

                // Verifica se a linha tem pelo menos 7 colunas
                // As 7 colunas do CSV são:
                // 0=TIMESTAMP, 1=USER_ID, 2=SESSION_ID, 3=ACTION_TYPE,
                // 4=TARGET_RESOURCE, 5=SEVERITY_LEVEL, 6=BYTES_TRANSFERRED
                if (colunasDoCSV.length >= 7) {

                    try {
                        // Pega a primeira coluna (índice 0): TIMESTAMP
                        // trim() remove espaços em branco do início e fim
                        // Long.parseLong() transforma texto em número Long
                        String textoDoTimestamp = colunasDoCSV[0].trim();
                        long numeroDoTimestamp = Long.parseLong(textoDoTimestamp);

                        // Pega a sétima coluna (índice 6): BYTES_TRANSFERRED
                        // Começa com 0 porque algumas linhas podem estar vazias
                        long quantidadeDeBytes = 0;

                        // Verifica se a coluna de bytes NÃO está vazia
                        // isEmpty() retorna true se o texto está vazio
                        String textoDosBytes = colunasDoCSV[6].trim();
                        if (!textoDosBytes.isEmpty()) {
                            // Se tem algo, converte para número
                            quantidadeDeBytes = Long.parseLong(textoDosBytes);
                        }

                        // Adiciona os valores nas listas
                        // add() coloca um item no final da lista
                        listaDeTimes.add(numeroDoTimestamp);
                        listaDeBytes.add(quantidadeDeBytes);

                    } catch (NumberFormatException erro) {
                        // Se der erro ao converter texto para número,
                        // simplesmente ignora essa linha e continua
                        // Isso pode acontecer se o arquivo tiver erro
                    }
                }
            }

        } finally {
            // SEMPRE fecha o arquivo no final
            // Isso é importante para não deixar o arquivo "preso"
            leitor.close();
        }

        // Se não conseguiu ler nenhum evento, retorna Map vazio
        // size() retorna quantos itens tem na lista
        if (listaDeTimes.size() == 0) {
            return picosEncontrados;
        }


        // ═══════════════════════════════════════════════════════════════
        // PASSO 3: ALGORITMO DA STACK (PILHA)
        // ═══════════════════════════════════════════════════════════════

        // Stack = Pilha (como pilha de pratos)
        // O último item que você coloca é o primeiro que você tira
        // Isso se chama LIFO (Last In, First Out)
        // Vamos guardar as POSIÇÕES (índices) dos eventos na pilha
        Stack<Integer> pilhaDeIndices = new Stack<>();

        // Loop que percorre os eventos de TRÁS PARA FRENTE
        // Por que de trás pra frente?
        // Porque precisamos saber o que acontece DEPOIS de cada evento!
        //
        // Exemplo: Se temos 5 eventos nas posições [0, 1, 2, 3, 4]
        // Vamos processar na ordem: 4 → 3 → 2 → 1 → 0
        int totalDeEventos = listaDeTimes.size();

        // i começa no último índice e vai diminuindo até 0
        // i-- significa: diminui 1 de i a cada volta do loop
        for (int i = totalDeEventos - 1; i >= 0; i--) {

            // Pega os bytes do evento ATUAL que estamos processando
            // get(i) retorna o item na posição i da lista
            long bytesDoEventoAtual = listaDeBytes.get(i);

            // ───────────────────────────────────────────────────────────
            // Sub-passo 3.1: LIMPAR A PILHA
            // ───────────────────────────────────────────────────────────

            // Vamos remover da pilha os eventos que têm poucos bytes
            // Por que? Porque se o atual tem mais bytes, aqueles
            // nunca serão "próximo maior" de ninguém!
            //
            // Exemplo prático:
            // Pilha tem: [50 bytes, 80 bytes]
            // Evento atual: 100 bytes
            // Ambos (50 e 80) são menores que 100, então removemos os dois!

            // isEmpty() verifica se a pilha está vazia
            // Continua enquanto a pilha NÃO estiver vazia
            while (!pilhaDeIndices.isEmpty()) {

                // peek() olha o topo da pilha SEM remover
                // Retorna o índice do evento que está no topo
                int indiceDoTopo = pilhaDeIndices.peek();

                // Pega os bytes do evento que está no topo da pilha
                long bytesDoTopo = listaDeBytes.get(indiceDoTopo);

                // Compara: o topo tem MENOS OU IGUAL bytes que o atual?
                if (bytesDoTopo <= bytesDoEventoAtual) {
                    // SIM! Então remove da pilha porque não serve mais
                    // pop() remove e retorna o item do topo
                    pilhaDeIndices.pop();
                } else {
                    // NÃO! O topo tem MAIS bytes!
                    // Encontramos o próximo maior! Para o loop
                    break;
                }
            }

            // ───────────────────────────────────────────────────────────
            // Sub-passo 3.2: VERIFICAR SE HÁ PRÓXIMO MAIOR
            // ───────────────────────────────────────────────────────────

            // Se a pilha NÃO está vazia, significa que temos um
            // evento futuro com MAIS bytes que o atual
            if (!pilhaDeIndices.isEmpty()) {

                // O topo da pilha é o próximo evento com mais bytes
                int indiceMaior = pilhaDeIndices.peek();

                // Pega o timestamp do evento ATUAL
                long timeAtual = listaDeTimes.get(i);

                // Pega o timestamp do evento com MAIS bytes
                long timeMaior = listaDeTimes.get(indiceMaior);

                // Adiciona no Map: timeAtual → timeMaior
                // put() adiciona um par chave-valor no Map
                // Isso significa: "No momento timeAtual teve poucos bytes,
                // mas no momento timeMaior teve MUITOS bytes (pico!)"
                picosEncontrados.put(timeAtual, timeMaior);
            }
            // Se a pilha está vazia, NÃO existe próximo maior
            // Neste caso não fazemos nada (conforme o requisito)

            // ───────────────────────────────────────────────────────────
            // Sub-passo 3.3: ADICIONAR EVENTO ATUAL NA PILHA
            // ───────────────────────────────────────────────────────────

            // push() adiciona um item no topo da pilha
            // Guardamos o ÍNDICE (posição) do evento atual
            // Este evento pode ser o "próximo maior" de eventos anteriores!
            pilhaDeIndices.push(i);
        }

        // Retorna o Map com todos os picos que encontramos
        return picosEncontrados;
    }

    // ═══════════════════════════════════════════════════════════════════
    // MÉTODO DE TESTE
    // ═══════════════════════════════════════════════════════════════════

    public static void main(String[] args) {
        // Cria um objeto da classe Desafio4_DetectorDeTransferenciaDeDados
        // new = cria novo objeto
        Desafio4_DetectorDeTransferenciaDeDados meuDesafio = new Desafio4_DetectorDeTransferenciaDeDados();

        // IMPORTANTE: COLOQUE O CAMINHO DO SEU ARQUIVO AQUI!
        String caminhoDoArquivo = "forensic_logs_teste.csv";

        // Imprime um cabeçalho bonito na tela
        System.out.println("================================================================");
        System.out.println("              DESAFIO 4: PICOS DE TRANSFERÊNCIA                 ");
        System.out.println("================================================================\n");

        try {
            // ───────────────────────────────────────────────────────────
            // Medir quanto tempo o programa demora
            // ───────────────────────────────────────────────────────────

            // nanoTime() retorna o tempo atual em nanosegundos
            // 1 segundo = 1.000.000.000 nanosegundos
            long tempoInicio = System.nanoTime();

            // CHAMA O MÉTODO PRINCIPAL que faz todo o trabalho
            Map<Long, Long> resultadoDoPicos = meuDesafio.identificarPicosTransferencia(caminhoDoArquivo);

            // Marca o tempo de término
            long tempoFim = System.nanoTime();

            // Calcula quanto tempo passou
            // Divide por 1.000.000 para converter nanosegundos em milissegundos
            long tempoDecorrido = tempoFim - tempoInicio;
            double tempoEmMilissegundos = tempoDecorrido / 1_000_000.0;

            // ───────────────────────────────────────────────────────────
            // Mostrar informações gerais
            // ───────────────────────────────────────────────────────────

            System.out.println("Arquivo: " + caminhoDoArquivo);
            // printf permite formatar números (%.3f = 3 casas decimais)
            System.out.printf("Tempo: %.3f ms%n", tempoEmMilissegundos);
            System.out.println("Total de picos encontrados: " + resultadoDoPicos.size());
            System.out.println("===========================================================\n");

            // ───────────────────────────────────────────────────────────
            // Mostrar os picos encontrados
            // ───────────────────────────────────────────────────────────

            // Verifica se encontrou algum pico
            if (resultadoDoPicos.isEmpty()) {
                // isEmpty() retorna true se o Map está vazio
                System.out.println("Nenhum pico detectado!");
            } else {
                // Encontrou picos! Vamos mostrar na tela
                System.out.println("Picos identificados:");
                System.out.println("(Timestamp Atual → Próximo Maior)");
                System.out.println("-----------------------------------------------------------");

                // Converter o Map para uma lista para poder ordenar
                // entrySet() retorna todos os pares chave-valor do Map
                List<Map.Entry<Long, Long>> listaDePicos = new ArrayList<>(resultadoDoPicos.entrySet());

                // Ordenar a lista por timestamp (do menor para o maior)
                // Isso deixa em ordem cronológica (do mais antigo pro mais recente)
                listaDePicos.sort(Map.Entry.comparingByKey());

                // Variável para numerar os picos
                int numeroDoPico = 1;

                // Percorre cada pico da lista
                for (Map.Entry<Long, Long> umPico : listaDePicos) {
                    // getKey() retorna a chave (timestamp atual)
                    // getValue() retorna o valor (timestamp próximo maior)
                    long timeAtual = umPico.getKey();
                    long timeMaior = umPico.getValue();

                    // Mostra na tela formatado
                    // %3d = número com 3 espaços
                    // %d = número Long
                    System.out.printf("%3d. %d → %d%n", numeroDoPico, timeAtual, timeMaior);

                    // Incrementa o contador (adiciona 1)
                    numeroDoPico++;

                    // Limitar a mostrar só 10 picos
                    // Se mostrar todos pode ficar muito poluído
                    if (numeroDoPico > 10) {
                        // Calcula quantos picos não foram mostrados
                        int picosRestantes = resultadoDoPicos.size() - 10;
                        System.out.println("... (e mais " + picosRestantes + " picos)");
                        break; // Sai do loop
                    }
                }
            }

        } catch (IOException erro) {
            // Se der erro ao ler o arquivo (arquivo não existe, etc)
            // Mostra a mensagem de erro na tela
            System.err.println("ERRO ao ler arquivo:");
            System.err.println(erro.getMessage());
        }
    }
}