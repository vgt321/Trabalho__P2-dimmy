import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Desafio 2: Reconstruir Linha do Tempo
 *
 * Objetivo:
 *  Dada uma sessionId, reconstruir a sequência de ações (ACTION_TYPE)
 *  realizadas dentro dessa sessão, em ordem cronológica.
 *
 *  - Usa uma Fila (Queue) para preservar a ordem FIFO.
 *  - Retorna uma List<String> com as ações.
 *  - Nunca retorna null.
 */
public class Desafio2_LinhaDoTempo {

    /**
     * Reconstrói a linha do tempo das ações executadas em uma sessão específica.
     *
     * @param caminhoArquivo Caminho para o arquivo CSV de logs.
     * @param sessionId      ID da sessão que será analisada.
     * @return Lista com os ACTION_TYPE em ordem cronológica (nunca null).
     * @throws IOException Caso ocorra erro de leitura no arquivo.
     */
    public List<String> reconstruirLinhaDoTempo(String caminhoArquivo, String sessionId) throws IOException {
        List<String> resultado = new ArrayList<>();
        Queue<String> fila = new LinkedList<>();

        // Validação básica de entrada
        if (sessionId == null || sessionId.isEmpty()) {
            return resultado; // Retorna lista vazia
        }

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo), 16384)) {
            String linha = br.readLine(); // Ignora o cabeçalho
            if (linha == null) return resultado; // Arquivo vazio

            while ((linha = br.readLine()) != null) {
                if (linha.isEmpty()) continue;

                // Divide até 5 colunas (TIMESTAMP, USER_ID, SESSION_ID, ACTION_TYPE, TARGET_RESOURCE)
                String[] partes = linha.split(",", 5);
                if (partes.length < 4) continue;

                String sessaoAtual = partes[2].trim();
                String acao = partes[3].trim();

                // Filtra apenas os eventos da sessionId especificada
                if (sessaoAtual.equals(sessionId)) {
                    fila.add(acao);
                }
            }

            // Transfere as ações da fila para a lista final (mantém ordem FIFO)
            while (!fila.isEmpty()) {
                resultado.add(fila.poll());
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
            throw e;
        }

        return resultado;
    }

    // ═══════════════════════════════════════════════════════════════
    // MÉTODO DE TESTE
    // ════════s═══════════════════════════════════════════════════════
    public static void main(String[] args) {
        Desafio2_LinhaDoTempo desafio = new Desafio2_LinhaDoTempo();
        String arquivo = "forensic_logs_teste.csv";
        String sessao = "session-delta-404";

        System.out.println("===========================================================");
        System.out.println("=        DESAFIO 2: RECONSTRUIR LINHA DO TEMPO            =");
        System.out.println("===========================================================\n");

        try {
            long inicio = System.nanoTime();
            List<String> acoes = desafio.reconstruirLinhaDoTempo(arquivo, sessao);
            long fim = System.nanoTime();

            double tempoMs = (fim - inicio) / 1_000_000.0;

            System.out.println("Sessao analisada: " + sessao);
            System.out.printf("Tempo de execucao: %.3f ms%n", tempoMs);
            System.out.println("Total de acoes encontradas: " + acoes.size());
            System.out.println("------------------------------------------------------------");

            if (acoes.isEmpty()) {
                System.out.println("✓ Nenhuma acao encontrada para essa sessao.");
            } else {
                int i = 1;
                for (String acao : acoes) {
                    System.out.printf("%02d. %s%n", i++, acao);
                }
            }

            System.out.println("===========================================================");

        } catch (IOException e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }
}
