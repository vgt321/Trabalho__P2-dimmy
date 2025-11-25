# 🧪 Trabalho de Implementação – Análise Forense  
### Estrutura de Dados I  

**Professor/Mestre:** Dimmy  
**Matéria:** Estrutura de Dados I  

---

## 👨‍🎓 **Alunos**

- **Isac Araújo Albuquerque** — *isac.albuquerque@somosicev.com*  
- **Mardson Varela Lima** — *mardson.lima@somosicev.com*  
- **Vyctor Gabriel Machado Tôrres** — *vyctor.gabriel@somosicev.com*  

---

# 📝 **Descrição Geral do Projeto**

Este trabalho consiste na implementação de **cinco desafios de análise forense digital**, utilizando estruturas de dados eficientes como:

- HashMap  
- ArrayList  
- Pilhas (Stack)  
- Filas  
- Grafos (BFS)

O objetivo é analisar um arquivo de logs forenses e detectar padrões suspeitos, como:

- sessões inválidas  
- ações críticas  
- movimentação lateral  
- picos de transferência  
- rastreamento de contaminação entre recursos  

O arquivo utilizado nos desafios segue o formato CSV com as colunas:


---

# 📦 **Desafio 1 – Detecção de Sessões Inválidas**

**Objetivo:**  
Identificar sessões em que o usuário realiza ações críticas sem estar devidamente autenticado.

**Lógica aplicada:**  
- Ler todas as linhas do CSV  
- Verificar se o `ACTION_TYPE` é uma ação sensível  
- Conferir se a sessão correspondente possui login previamente registrado  
- Caso contrário, sinalizar como sessão suspeita  

**Estruturas utilizadas:**  
- HashMap para mapear sessões → status (válida / inválida)  
- Lista para registrar ações  

---

# 📦 **Desafio 2 – Detecção de Ações de Alto Risco**

**Objetivo:**  
Encontrar eventos com nível de severidade elevado e listá-los de forma ordenada.

**Lógica aplicada:**  
- Ler o campo `SEVERITY_LEVEL`  
- Filtrar valores acima de um limiar (ex.: > 7)  
- Ordenar por timestamp  

**Estruturas utilizadas:**  
- PriorityQueue (fila de prioridade)  
- Comparators  

---

# 📦 **Desafio 3 – Movimentação Lateral**

**Objetivo:**  
Detectar situações onde uma única sessão acessa múltiplos recursos distintos, simulando comportamento de ataque lateral.

**Lógica aplicada:**  
- Agrupamento por `SESSION_ID`  
- Contar quantos recursos distintos foram acessados  
- Sessões com variância alta de recursos são consideradas suspeitas  

**Estruturas utilizadas:**  
- HashMap<Session, Set<Resource>>  
- Conjuntos para garantir unicidade  

---

# 📦 **Desafio 4 – Identificação de Picos de Transferência**  
*(Baseado no código enviado)*

**Objetivo:**  
Detectar quando a quantidade de bytes transferida aumenta drasticamente entre eventos, indicando possível **exfiltração de dados**.

### ✔ Lógica empregada
- Ler todas as linhas do CSV  
- Armazenar timestamps e bytes transferidos sincronizados  
- Utilizar **algoritmo com pilha (Stack)** para descobrir o “próximo maior volume”  
- Sempre que um evento posterior tiver mais bytes → é considerado um **pico**  

### ✔ Estruturas utilizadas
- `ArrayList<Long>` para timestamps e bytes  
- `Stack<Integer>` para cálculo dos próximos maiores  
- `HashMap<Long, Long>` com pares:  
  **timestamp_atual → timestamp_do_pico**

### ✔ Complexidade
- Leitura do arquivo: **O(n)**  
- Algoritmo da pilha: **O(n)**  
- Total: **O(n)**  

---

# 📦 **Desafio 5 – Rastrear Contaminação (BFS)**  
*(Baseado no código enviado)*

**Objetivo:**  
Descobrir o caminho de contaminação entre dois recursos, analisando movimentações laterais em sessões.

### ✔ Lógica geral
1. Ler o arquivo CSV  
2. Agrupar eventos por `SESSION_ID`  
3. Construir um grafo direcionado onde cada recurso aponta para o próximo acessado na mesma sessão  
4. Executar **BFS (Busca em Largura)** para encontrar o **menor caminho entre dois recursos**  

### ✔ Estruturas utilizadas
- `Map<String, List<String>>` para o grafo  
- `Queue<String>` para BFS  
- `HashMap<String, String>` para registrar o caminho (pais)  
- Classe interna `LogEvent` para parsing limpo do CSV  

### ✔ Complexidade
- Construção do grafo: **O(n)**  
- BFS: **O(V + E)**  

### ✔ Retorno
- `Optional<List<String>>` contendo o caminho mais curto  
- Caso não exista rota, retorna `Optional.empty()`  

---

# 📂 **Como Executar os Desafios**

```bash
javac *.java
java DesafioX
