import java.lang.reflect.Method;

public class MainGeral {

    // ⚠️ ATENÇÃO: Verifique e ajuste este caminho para o SEU arquivo de logs!
    // Ele será usado como argumento de teste para cada desafio.
    private static final String ARQUIVO_LOGS_BASE = "analise-forense-aed.jar";

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("=    ANALISE FORENSE: EXECUÇÃO CENTRALIZADA DE DESAFIOS =");
        System.out.println("=========================================================\n");

        // Array COM OS NOMES CORRIGIDOS DE TODAS AS SUAS CLASSES
        String[] nomesClasses = {
            "Desafio1_SessoesInvalidas",
            "Desafio2_LinhaDoTempo",
            "Desafio3_PriorizarAlertas",
            "Desafio4_DetectorDeTransferenciaDeDados",
            "Desafio5RastrearContaminacao" // ✔️ NOME CORRIGIDO
        };

        // Passa o caminho do arquivo para os métodos main de teste
        String[] argsDesafio = new String[]{ARQUIVO_LOGS_BASE};


        for (int i = 0; i < nomesClasses.length; i++) {
            String nomeClasse = nomesClasses[i];
            
            System.out.println("\n" + "=".repeat(70));
            System.out.printf("--- EXECUTANDO DESAFIO %d: %s ---%n", (i + 1), nomeClasse);
            System.out.println("=".repeat(70));

            try {
                // 1. Carrega a classe dinamicamente pelo nome
                Class<?> classeDesafio = Class.forName(nomeClasse);
                
                // 2. Procura o método main(String[] args)
                // O método main é sempre 'static', por isso usamos getDeclaredMethod
                Method metodoMain = classeDesafio.getDeclaredMethod("main", String[].class);
                
                // 3. Invoca o método main estático
                // O primeiro argumento é 'null' porque o método é estático.
                // O segundo argumento é o array de argumentos.
                metodoMain.invoke(null, (Object) argsDesafio);
                
            } catch (ClassNotFoundException e) {
                System.err.println("❌ ERRO: Classe **" + nomeClasse + "** não encontrada! Verifique o nome, o package ou o CLASSPATH.");
            } catch (NoSuchMethodException e) {
                System.err.println("❌ ERRO: Método main(String[] args) não encontrado em " + nomeClasse);
            } catch (Exception e) {
                // Captura a exceção real que ocorreu dentro do método main invocado
                System.err.println("❌ ERRO na execução do Desafio " + (i + 1) + ": " + e.getCause());
                System.err.println("   Detalhe: " + e.getCause().getMessage());
            }
        }

        System.out.println("\n=========================================================");
        System.out.println("=             FIM DA EXECUÇÃO DE TODOS OS DESAFIOS      =");
        System.out.println("=========================================================");
    }
}