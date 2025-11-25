import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class Desafio3_PriorizarAlertas {

    /**
     * Prioriza os N alertas de maior severidade do arquivo de logs.
     *
     * @param caminhoArquivo Caminho para o arquivo CSV de logs
     * @param n              Número de alertas a serem retornados
     * @return Lista com os N alertas de maior severidade (nunca null)
     * @throws IOException Caso ocorra erro de leitura no arquivo
     */
    public List<Alerta> priorizarAlertas(String caminhoArquivo, int n) throws IOException {
        List<Alerta> resultado = new ArrayList<>();

        // Caso especial: n = 0 (conforme requisito do PDF)
        if (n == 0) {
            return resultado;
        }

        // PriorityQueue com ordem DECRESCENTE de severidade
        // Maior severidade = maior prioridade (requisito do PDF)
        PriorityQueue<Alerta> filaPrioridade = new PriorityQueue<>(
            (a1, a2) -> Integer.compare(a2.getSeverityLevel(), a1.getSeverityLevel())
        );

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo), 16384)) {
            String linha = br.readLine(); // Ignora o cabeçalho
            
            // Caso especial: arquivo vazio (conforme requisito do PDF)
            if (linha == null) {
                return resultado;
            }

            // Processar todas as linhas do log
            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty()) continue;

                String[] campos = linha.split(",");

                // Validação: garantir que a linha tem todos os 7 campos
                if (campos.length >= 7) {
                    try {
                        // Parsing dos 7 campos do CSV
                        long timestamp = Long.parseLong(campos[0].trim());
                        String userId = campos[1].trim();
                        String sessionId = campos[2].trim();
                        String actionType = campos[3].trim();
                        String targetResource = campos[4].trim();
                        int severityLevel = Integer.parseInt(campos[5].trim());
                        long bytesTransferred = Long.parseLong(campos[6].trim());

                        // Criar objeto Alerta e adicionar à fila de prioridade
                        // (conforme requisito do PDF: "crie um objeto Alerta e adicione-o à PriorityQueue")
                        Alerta alerta = new Alerta(
                            timestamp,
                            userId,
                            sessionId,
                            actionType,
                            targetResource,
                            severityLevel,
                            bytesTransferred
                        );

                        filaPrioridade.offer(alerta);

                    } catch (NumberFormatException e) {
                        // Ignora linhas malformadas
                        System.err.println("Linha com formato invalido: " + linha);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
            throw e; // Propaga a exceção conforme requisito
        }

        // Extrair os N primeiros elementos
        // Caso especial: se n > total, retorna todos (conforme requisito do PDF)
        int qtdExtrair = Math.min(n, filaPrioridade.size());

        // Usar poll() n vezes (conforme requisito do PDF)
        for (int i = 0; i < qtdExtrair; i++) {
            Alerta alerta = filaPrioridade.poll(); 
            if (alerta != null) {
                resultado.add(alerta);
            }
        }

        // Sempre retorna lista válida (nunca null, conforme requisito do PDF)
        return resultado;
    }

    // ═══════════════════════════════════════════════════════════════
    // MÉTODOS DE TESTE - VALIDAÇÃO DE TODOS OS REQUISITOS
    // ═══════════════════════════════════════════════════════════════

    public static void main(String[] args) {
        Desafio3_PriorizarAlertas desafio = new Desafio3_PriorizarAlertas();
        String arquivo = "analise-forense-aed.jar";

        System.out.println("===========================================================");
        System.out.println("=          DESAFIO 3: PRIORIZAR ALERTAS                   =");
        System.out.println("=          VALIDACAO DE TODOS OS REQUISITOS DO PDF        =");
        System.out.println("===========================================================\n");

        try {
            // ========== TESTE 1: Top 5 Alertas ==========
            System.out.println("TESTE 1: Top 5 Alertas de Maior Severidade");
            System.out.println("Requisito: Lista ordenada em ordem DECRESCENTE");
            System.out.println("------------------------------------------------------------");
            
            long inicio = System.nanoTime();
            List<Alerta> top5 = desafio.priorizarAlertas(arquivo, 5);
            long fim = System.nanoTime();
            double tempoMs = (fim - inicio) / 1_000_000.0;

            System.out.printf("Tempo de execucao: %.3f ms%n", tempoMs);
            System.out.println("Total de alertas retornados: " + top5.size());
            System.out.println();

            if (top5.isEmpty()) {
                System.out.println("✗ Nenhum alerta encontrado!");
            } else {
                // Verificar ordenação decrescente
                boolean ordenadoCorreto = true;
                for (int i = 0; i < top5.size() - 1; i++) {
                    if (top5.get(i).getSeverityLevel() < top5.get(i + 1).getSeverityLevel()) {
                        ordenadoCorreto = false;
                        break;
                    }
                }
                
                System.out.println("✓ Ordenacao DECRESCENTE: " + 
                    (ordenadoCorreto ? "CORRETA" : "INCORRETA"));
                System.out.println();

                for (int i = 0; i < top5.size(); i++) {
                    Alerta a = top5.get(i);
                    System.out.printf("%02d. [Severidade: %2d] %-20s -> %s%n",
                        (i + 1),
                        a.getSeverityLevel(),
                        a.getActionType(),
                        a.getTargetResource()
                    );
                }
            }

            System.out.println("\n===========================================================\n");

            // ========== TESTE 2: n = 0 ==========
            System.out.println("TESTE 2: n = 0 (REQUISITO: deve retornar lista vazia)");
            System.out.println("------------------------------------------------------------");
            List<Alerta> vazio = desafio.priorizarAlertas(arquivo, 0);
            System.out.println("✓ Tamanho da lista: " + vazio.size());
            System.out.println("✓ E vazio? " + vazio.isEmpty());
            System.out.println("✓ E null? " + (vazio == null ? "SIM (ERRO!)" : "NAO (OK!)"));
            System.out.println("✓ REQUISITO ATENDIDO: " + 
                (vazio != null && vazio.isEmpty() ? "SIM" : "NAO"));

            System.out.println("\n===========================================================\n");

            // ========== TESTE 3: n muito grande ==========
            System.out.println("TESTE 3: n = 1000 (REQUISITO: retornar todos se n > total)");
            System.out.println("------------------------------------------------------------");
            List<Alerta> todos = desafio.priorizarAlertas(arquivo, 1000);
            System.out.println("✓ Retornados: " + todos.size() + " alertas (todos do arquivo)");

            // Verificar ordenacao decrescente rigorosa
            boolean ordenadoCorreto = true;
            for (int i = 0; i < todos.size() - 1; i++) {
                if (todos.get(i).getSeverityLevel() < todos.get(i + 1).getSeverityLevel()) {
                    ordenadoCorreto = false;
                    System.out.println("✗ ERRO: Posicao " + i + 
                        " (sev=" + todos.get(i).getSeverityLevel() + 
                        ") < Posicao " + (i+1) + 
                        " (sev=" + todos.get(i+1).getSeverityLevel() + ")");
                    break;
                }
            }
            System.out.println("✓ Ordenacao DECRESCENTE correta? " + 
                             (ordenadoCorreto ? "SIM" : "NAO"));
            System.out.println("✓ REQUISITO ATENDIDO: " + (ordenadoCorreto ? "SIM" : "NAO"));

            // Mostrar distribuição de severidades (ordem decrescente)
            System.out.println("\nDistribuicao de Severidades (ordem decrescente):");
            Map<Integer, Long> distribuicao = new TreeMap<>(Collections.reverseOrder());
            for (Alerta a : todos) {
                distribuicao.put(a.getSeverityLevel(), 
                    distribuicao.getOrDefault(a.getSeverityLevel(), 0L) + 1);
            }
            for (Map.Entry<Integer, Long> entry : distribuicao.entrySet()) {
                System.out.printf("  Severidade %2d: %3d eventos%n", 
                    entry.getKey(), 
                    entry.getValue());
            }

        } catch (IOException e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// CLASSE ALERTA - Para testes locais
// IMPORTANTE: Na entrega final do JAR, use a classe do analise-forense-api.jar
// e remova esta classe local!
// ═══════════════════════════════════════════════════════════════
class Alerta {
    private final long timestamp;
    private final String userId;
    private final String sessionId;
    private final String actionType;
    private final String targetResource;
    private final int severityLevel;
    private final long bytesTransferred;

    /**
     * Construtor da classe Alerta
     */
    public Alerta(long timestamp, String userId, String sessionId,
                  String actionType, String targetResource,
                  int severityLevel, long bytesTransferred) {
        this.timestamp = timestamp;
        this.userId = userId;
        this.sessionId = sessionId;
        this.actionType = actionType;
        this.targetResource = targetResource;
        this.severityLevel = severityLevel;
        this.bytesTransferred = bytesTransferred;
    }

    // Getters
    public long getTimestamp() {
        return timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getActionType() {
        return actionType;
    }

    public String getTargetResource() {
        return targetResource;
    }

    public int getSeverityLevel() {
        return severityLevel;
    }

    public long getBytesTransferred() {
        return bytesTransferred;
    }

    @Override
    public String toString() {
        return String.format("Alerta[timestamp=%d, user=%s, session=%s, action=%s, resource=%s, severity=%d, bytes=%d]",
            timestamp, userId, sessionId, actionType, targetResource, severityLevel, bytesTransferred);
    }
}