import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Implementação do Desafio 5: Rastrear Contaminação
 *
 * Este desafio utiliza o algoritmo BFS (Busca em Largura) para encontrar
 * o caminho mais curto entre dois recursos em um grafo de movimentação lateral.
 *
 * Complexidade:
 * - Construção do grafo: O(n) onde n é o número de linhas do log
 * - BFS: O(V + E) onde V é o número de vértices (recursos) e E é o número de arestas
 */
public class Desafio5RastrearContaminacao {

    /**
     * Classe interna para representar um evento de log
     */
    private static class LogEvent {
        long timestamp;
        String userId;
        String sessionId;
        String actionType;
        String targetResource;
        int severityLevel;
        long bytesTransferred;

        public LogEvent(String csvLine) {
            String[] parts = csvLine.split(",");
            this.timestamp = Long.parseLong(parts[0]);
            this.userId = parts[1];
            this.sessionId = parts[2];
            this.actionType = parts[3];
            this.targetResource = parts[4];
            this.severityLevel = Integer.parseInt(parts[5]);
            this.bytesTransferred = Long.parseLong(parts[6]);
        }
    }

    /**
     * Rastreia o caminho de contaminação entre dois recursos usando BFS.
     *
     * @param caminhoArquivo Caminho para o arquivo CSV de logs
     * @param recursoInicial Recurso de origem
     * @param recursoAlvo Recurso de destino
     * @return Optional contendo a lista do caminho mais curto, ou Optional.empty() se não houver caminho
     * @throws IOException Se houver erro ao ler o arquivo
     */
    public static Optional<List<String>> rastrearContaminacao(
            String caminhoArquivo,
            String recursoInicial,
            String recursoAlvo) throws IOException {

        // Passo 1: Construir o grafo de movimentação lateral
        Map<String, List<String>> grafo = construirGrafo(caminhoArquivo);

        // Passo 2: Verificar se os recursos existem no grafo
        if (!grafo.containsKey(recursoInicial)) {
            return Optional.empty();
        }

        // Caso especial: origem e destino são o mesmo
        if (recursoInicial.equals(recursoAlvo)) {
            // Verifica se o recurso existe no log
            if (grafo.containsKey(recursoInicial)) {
                return Optional.of(Collections.singletonList(recursoInicial));
            }
            return Optional.empty();
        }

        // Passo 3: Executar BFS
        return executarBFS(grafo, recursoInicial, recursoAlvo);
    }

    /**
     * Constrói o grafo direcionado de movimentação lateral entre recursos.
     *
     * Lógica:
     * - Agrupa eventos por SESSION_ID
     * - Dentro de cada sessão, cria arestas direcionadas entre recursos acessados sequencialmente
     * - Se uma sessão acessa A, depois B, depois C: cria arestas A->B e B->C
     *
     * @param caminhoArquivo Caminho para o arquivo CSV
     * @return Mapa de adjacências representando o grafo
     * @throws IOException Se houver erro ao ler o arquivo
     */
    private static Map<String, List<String>> construirGrafo(String caminhoArquivo) throws IOException {
        // Mapa para armazenar eventos agrupados por sessão
        Map<String, List<LogEvent>> eventosPorSessao = new LinkedHashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha = br.readLine(); // Pular cabeçalho

            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;

                // Verificar se não é o cabeçalho
                if (linha.startsWith("TIMESTAMP")) continue;

                try {
                    LogEvent evento = new LogEvent(linha);

                    // Agrupar eventos por sessão
                    eventosPorSessao
                            .computeIfAbsent(evento.sessionId, k -> new ArrayList<>())
                            .add(evento);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    // Ignorar linhas malformadas
                    System.err.println("Aviso: Linha ignorada (formato invalido): " + linha);
                }
            }
        }

        // Construir o grafo a partir das sessões
        Map<String, List<String>> grafo = new HashMap<>();

        for (List<LogEvent> eventosNaSessao : eventosPorSessao.values()) {
            // Para cada par consecutivo de eventos na sessão, criar uma aresta
            for (int i = 0; i < eventosNaSessao.size() - 1; i++) {
                String recursoAtual = eventosNaSessao.get(i).targetResource;
                String proximoRecurso = eventosNaSessao.get(i + 1).targetResource;

                // Adicionar aresta direcionada: recursoAtual -> proximoRecurso
                grafo
                        .computeIfAbsent(recursoAtual, k -> new ArrayList<>())
                        .add(proximoRecurso);
            }

            // Garantir que o último recurso também esteja no grafo
            if (!eventosNaSessao.isEmpty()) {
                String ultimoRecurso = eventosNaSessao.get(eventosNaSessao.size() - 1).targetResource;
                grafo.putIfAbsent(ultimoRecurso, new ArrayList<>());
            }
        }

        return grafo;
    }

    /**
     * Executa o algoritmo BFS para encontrar o caminho mais curto.
     *
     * @param grafo Grafo de adjacências
     * @param inicio Recurso inicial
     * @param alvo Recurso alvo
     * @return Optional com o caminho ou Optional.empty() se não houver caminho
     */
    private static Optional<List<String>> executarBFS(
            Map<String, List<String>> grafo,
            String inicio,
            String alvo) {

        // Fila para BFS
        Queue<String> fila = new LinkedList<>();

        // Mapa para rastrear predecessores (para reconstruir o caminho)
        Map<String, String> predecessor = new HashMap<>();

        // Conjunto de visitados
        Set<String> visitados = new HashSet<>();

        // Inicializar BFS
        fila.offer(inicio);
        visitados.add(inicio);
        predecessor.put(inicio, null); // O início não tem predecessor

        // Executar BFS
        while (!fila.isEmpty()) {
            String recursoAtual = fila.poll();

            // Verificar se chegamos ao alvo
            if (recursoAtual.equals(alvo)) {
                return Optional.of(reconstruirCaminho(predecessor, inicio, alvo));
            }

            // Explorar vizinhos
            List<String> vizinhos = grafo.getOrDefault(recursoAtual, Collections.emptyList());
            for (String vizinho : vizinhos) {
                if (!visitados.contains(vizinho)) {
                    visitados.add(vizinho);
                    predecessor.put(vizinho, recursoAtual);
                    fila.offer(vizinho);
                }
            }
        }

        // Alvo não foi alcançado
        return Optional.empty();
    }

    /**
     * Reconstrói o caminho a partir do mapa de predecessores.
     *
     * @param predecessor Mapa de predecessores gerado pelo BFS
     * @param inicio Recurso inicial
     * @param alvo Recurso alvo
     * @return Lista ordenada representando o caminho
     */
    private static List<String> reconstruirCaminho(
            Map<String, String> predecessor,
            String inicio,
            String alvo) {

        LinkedList<String> caminho = new LinkedList<>();
        String recursoAtual = alvo;

        // Reconstruir caminho do alvo até o início
        while (recursoAtual != null) {
            caminho.addFirst(recursoAtual);
            recursoAtual = predecessor.get(recursoAtual);
        }

        return caminho;
    }

    /**
     * Método auxiliar para encontrar o arquivo CSV em diferentes localizações.
     *
     * @param nomeArquivo Nome do arquivo a ser procurado
     * @return Caminho absoluto do arquivo encontrado
     * @throws IOException Se o arquivo não for encontrado
     */
    private static String encontrarArquivo(String nomeArquivo) throws IOException {
        List<String> caminhosTentados = new ArrayList<>();

        // 1. Tenta o diretório atual
        java.io.File f = new java.io.File(nomeArquivo);
        caminhosTentados.add(f.getAbsolutePath());
        if (f.exists()) {
            return f.getAbsolutePath();
        }

        // 2. Tenta o diretório onde está a classe compilada
        try {
            String classpath = Desafio5RastrearContaminacao.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath();

            // Decodifica o caminho (remove %20, etc)
            classpath = java.net.URLDecoder.decode(classpath, "UTF-8");

            f = new java.io.File(new java.io.File(classpath).getParent(), nomeArquivo);
            caminhosTentados.add(f.getAbsolutePath());
            if (f.exists()) {
                return f.getAbsolutePath();
            }
        } catch (Exception e) {
            // Ignora erro e continua tentando outros caminhos
        }

        // 3. Tenta o diretório src (comum em projetos Java)
        f = new java.io.File("src", nomeArquivo);
        caminhosTentados.add(f.getAbsolutePath());
        if (f.exists()) {
            return f.getAbsolutePath();
        }

        // 4. Tenta o diretório raiz do projeto
        f = new java.io.File("..", nomeArquivo);
        caminhosTentados.add(f.getAbsolutePath());
        if (f.exists()) {
            return f.getAbsolutePath();
        }

        // Se não encontrou em nenhum lugar, lança exceção com informações úteis
        StringBuilder mensagemErro = new StringBuilder();
        mensagemErro.append("Arquivo '").append(nomeArquivo).append("' não encontrado!\n");
        mensagemErro.append("Diretório de trabalho atual: ").append(System.getProperty("user.dir")).append("\n");
        mensagemErro.append("\nCaminhos tentados:\n");
        for (String caminho : caminhosTentados) {
            mensagemErro.append("  - ").append(caminho).append("\n");
        }
        mensagemErro.append("\nDica: Coloque o arquivo '").append(nomeArquivo).append("' em um destes locais ou use o caminho absoluto.");

        throw new IOException(mensagemErro.toString());
    }

    /**
     * Método main para testes locais
     */
    public static void main(String[] args) {
        try {
            // Tenta encontrar o arquivo automaticamente
            String arquivo = encontrarArquivo("analise-forense-aed.jar");

            System.out.println("=".repeat(70));
            System.out.println("DESAFIO 5: RASTREAR CONTAMINAÇÃO");
            System.out.println("=".repeat(70));
            System.out.println("Arquivo utilizado: " + arquivo);
            System.out.println("=".repeat(70));
            System.out.println();

            // Teste 1: Caminho existente
            System.out.println("TESTE 1: Caminho de /usr/bin/sshd para /var/secrets/key.dat");
            System.out.println("-".repeat(70));
            Optional<List<String>> resultado1 = rastrearContaminacao(
                    arquivo,
                    "/usr/bin/sshd",
                    "/var/secrets/key.dat"
            );

            if (resultado1.isPresent()) {
                List<String> caminho = resultado1.get();
                System.out.println("✓ Caminho encontrado com " + caminho.size() + " recursos:");
                System.out.println("  " + String.join(" → ", caminho));
            } else {
                System.out.println("✗ Nenhum caminho encontrado");
            }
            System.out.println();

            // Teste 2: Caminho inexistente
            System.out.println("TESTE 2: Caminho inexistente (/recurso/inexistente → /outro/recurso)");
            System.out.println("-".repeat(70));
            Optional<List<String>> resultado2 = rastrearContaminacao(
                    arquivo,
                    "/recurso/inexistente",
                    "/outro/recurso"
            );

            if (resultado2.isPresent()) {
                System.out.println("✗ Caminho encontrado (inesperado): " + resultado2.get());
            } else {
                System.out.println("✓ Nenhum caminho encontrado (comportamento esperado)");
            }
            System.out.println();

            // Teste 3: Mesmo recurso (origem = destino)
            System.out.println("TESTE 3: Mesmo recurso (origem = destino)");
            System.out.println("-".repeat(70));
            Optional<List<String>> resultado3 = rastrearContaminacao(
                    arquivo,
                    "/usr/bin/sshd",
                    "/usr/bin/sshd"
            );

            if (resultado3.isPresent()) {
                System.out.println("✓ Caminho: " + resultado3.get());
            } else {
                System.out.println("✗ Nenhum caminho encontrado");
            }
            System.out.println();

            System.out.println("=".repeat(70));
            System.out.println("TESTES CONCLUÍDOS");
            System.out.println("=".repeat(70));

        } catch (IOException e) {
            System.err.println("=".repeat(70));
            System.err.println("ERRO AO PROCESSAR ARQUIVO");
            System.err.println("=".repeat(70));
            System.err.println(e.getMessage());
            System.err.println("=".repeat(70));
            e.printStackTrace();
        }
    }
}