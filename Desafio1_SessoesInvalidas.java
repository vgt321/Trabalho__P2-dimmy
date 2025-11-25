import  java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Desafio1_SessoesInvalidas {

     /**
     * @param caminhoArquivo Caminho para o arquivo CSV de logs
     * @return Set contendo os SESSION_ID inválidos (nunca null)
     * @throws IOException Se houver erro ao ler o arquivo
     */
    public Set<String> encontrarSessoesInvalidas(String caminhoArquivo) throws IOException {
        // Capacidade inicial otimizada
        Map<String, Deque<String>> pilhasPorUsuario = new HashMap<>(512);
        Set<String> sessoesInvalidas = new HashSet<>(256);

        // Buffer maior para leitura eficiente
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo), 16384)) {
            br.readLine(); // Pula cabeçalho

            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isEmpty()) continue;

                // Split otimizado: para após 5 colunas
                String[] partes = linha.split(",", 5);
                if (partes.length < 4) continue;

                String userId = partes[1].trim();
                String sessionId = partes[2].trim();
                String actionType = partes[3].trim();

                // Valida campos vazios
                if (userId.isEmpty() || sessionId.isEmpty() || actionType.isEmpty()) {
                    continue;
                }

                // computeIfAbsent: 1 operação em vez de 2
                Deque<String> pilha = pilhasPorUsuario.computeIfAbsent(userId, k -> new ArrayDeque<>());

                if ("LOGIN".equalsIgnoreCase(actionType)) {
                    // ✅ CORREÇÃO: LOGIN ANINHADO marca a sessão ATUAL (nova)
                    // Edital: "a sessão atual (SESSION_ID) é inválida"
                    // sessionId = a sessão sendo processada AGORA = sessão ATUAL
                    if (!pilha.isEmpty()) {
                        sessoesInvalidas.add(sessionId); // ✅ CORRETO!
                    }
                    pilha.push(sessionId);
                    
                } else if ("LOGOUT".equalsIgnoreCase(actionType)) {
                    if (pilha.isEmpty()) {
                        // LOGOUT sem LOGIN prévio
                        sessoesInvalidas.add(sessionId);
                    } else if (pilha.peek().equals(sessionId)) {
                        // LOGOUT correto: remove da pilha
                        pilha.pop();
                    } else {
                        // LOGOUT de sessão diferente do topo
                        sessoesInvalidas.add(sessionId);
                    }
                }
                // Outras ações ignoradas
            }
        }

        // Sessões não encerradas = inválidas
        for (Deque<String> pilha : pilhasPorUsuario.values()) {
            sessoesInvalidas.addAll(pilha);
        }

        return sessoesInvalidas;
    }

    // ═══════════════════════════════════════════════════════════════
    // MÉTODO DE TESTE
    // ═══════════════════════════════════════════════════════════════
    
    public static void main(String[] args) {
        Desafio1_SessoesInvalidas desafio = new Desafio1_SessoesInvalidas();
        String arquivo = "analise-forense-aed.jar";


        System.out.println("=========================================================");
        System.out.println("=       DESAFIO 1: SESSOES INVALIDAS (CORRIGIDO)       =");
        System.out.println("==========================================================\n");

        try {
            long inicio = System.nanoTime();
            Set<String> invalidas = desafio.encontrarSessoesInvalidas(arquivo);
            long fim = System.nanoTime();
            
            double tempoMs = (fim - inicio) / 1_000_000.0;

            System.out.println("=============================================");
            System.out.printf("= Arquivo: %-30s =%n", arquivo);
            System.out.printf("= Tempo: %10.3f ms                  =%n", tempoMs);
            System.out.printf(" Total de sessoes invalidas: %-5d    %n", invalidas.size());
            System.out.println("================================================\n");

            if (invalidas.isEmpty()) {
                System.out.println("✓ Nenhuma sessao invalida encontrada.\n");
            } else {
                System.out.println("Sessoes invalidas detectadas:");
                System.out.println("==============================================");
                System.out.println("= #  = SESSION_ID                          =");
                System.out.println("===============================================");
                
                int i = 1;
                for (String sessionId : invalidas) {
                    System.out.printf("= %-2d = %-36s =%n", i++, sessionId);
                }
                System.out.println("===============================================");
            }

            System.out.println("\n========================================================");
            System.out.println("= CORREÇÃO APLICADA:");
            System.out.println("  LOGIN aninhado marca a sessao ATUAL (nova)");
            System.out.println("  Conforme interpretacao literal do edital:");
            System.out.println("  'a sessao atual (SESSION_ID) e invalida'");
            System.out.println("============================================================");

        } catch (IOException e) {
            System.err.println("= ERRO ao processar arquivo:");
            System.err.println("   " + e.getMessage());
            e.printStackTrace();
        }
    }
}





