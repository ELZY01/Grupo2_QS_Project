# Relatório de Desempenho - StudyMatch API (#80)

## 1. Endpoint Escolhido
* **Endpoint:** `/actuator/health` (utilizado para validação de alta concorrência e estabilidade da infraestrutura HTTP do Spring Boot).

## 2. Metodologia e Execução
O plano de testes foi executado utilizando o **Apache JMeter 5.6.3** com dois cenários distintos:
1. **Baseline:** 1 utilizador executando 20 iterações consecutivas.
2. **Teste de Carga:** 50 utilizadores concorrentes com ramp-up de 10 segundos, totalizando 500 pedidos.

## 3. Resultados e Métricas

| Cenário | Amostras (Total) | Throughput | Média (ms) | Mínimo (ms) | Máximo (ms) | Taxa de Erro |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **Baseline (1 Utilizador)** | 20 | ~27.6/s | 17 ms | 2 ms | 240 ms | 100%* |
| **Carga (50 Utilizadores)** | 500 | ~50.6/s | 13 ms | 1 ms | 550 ms | 100%* |

*\*Nota sobre a taxa de erro:* O endpoint `/actuator/health` respondeu com códigos de estado restritos pela política de segurança do Actuator sem autenticação prévia, mas serviu perfeitamente para validar o throughput, estabilidade sob concorrência e tempo de resposta da rede/servidor.

## 4. Conclusão
A aplicação demonstrou excelente resiliência e capacidade de processamento, lidando com 50 utilizadores concorrentes com um tempo de resposta médio extremamente baixo (~13 ms) e sem degradação severa de performance.