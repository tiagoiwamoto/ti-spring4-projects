# Guia Rápido - Lambda SQS Handler com GraalVM

## 🚀 Início Rápido

Este projeto implementa uma função AWS Lambda em Java 21 que processa mensagens do Amazon SQS com compilação nativa usando GraalVM AOT.

### Passo 1: Configurar AWS CLI

```bash
# Instalar AWS CLI (se ainda não tiver)
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# Configurar credenciais
aws configure
```

Forneça:
- AWS Access Key ID
- AWS Secret Access Key
- Região padrão (ex: us-east-1)
- Formato de saída (json)

### Passo 2: Criar Infraestrutura AWS

```bash
# Executar script de setup
./setup-aws.sh
```

Este script criará:
- ✅ Fila SQS principal
- ✅ Dead Letter Queue (DLQ)
- ✅ IAM Role com permissões
- ✅ Políticas de segurança

### Passo 3: Build do Projeto

#### Opção A: Build JVM (mais rápido para desenvolvimento)

```bash
mvn clean package
```

**Resultado:** `target/06-java-lambda-1.0-SNAPSHOT.jar` (~10MB)

#### Opção B: Build Native Image (produção - menor cold start)

```bash
# Instalar GraalVM primeiro
sdk install java 21-graalvm
sdk use java 21-graalvm

# Build nativo
mvn clean package -Pnative
```

**Resultado:** `target/sqs-lambda-handler` (~20MB executável nativo)

### Passo 4: Deploy

```bash
# Deploy automatizado
./deploy.sh
```

Escolha o tipo de build:
1. JVM (java21 runtime)
2. Native Image (provided.al2 runtime)

### Passo 5: Testar

#### Enviar mensagem de teste

```bash
# Carregar configurações
source aws-config.env

# Enviar mensagem de pedido
aws sqs send-message \
  --queue-url $QUEUE_URL \
  --message-body '{
    "type": "ORDER",
    "orderId": "ORD-001",
    "amount": 150.00,
    "customer": {
      "name": "João Silva",
      "email": "joao@example.com"
    }
  }'

# Enviar mensagem de notificação
aws sqs send-message \
  --queue-url $QUEUE_URL \
  --message-body '{
    "type": "NOTIFICATION",
    "message": "Sistema atualizado",
    "timestamp": "2026-01-08T10:00:00Z"
  }'
```

#### Ver logs em tempo real

```bash
aws logs tail /aws/lambda/$FUNCTION_NAME --follow
```

## 📊 Comparação de Performance

| Métrica | JVM | Native Image | Ganho |
|---------|-----|--------------|-------|
| **Cold Start** | 5-10s | 100-200ms | **50x mais rápido** |
| **Memória** | 512MB | 128MB | **4x menos** |
| **Custo** | Alto | Baixo | **60-70% redução** |
| **Tempo Build** | 10s | 2-3min | JVM mais rápido |
| **Tamanho Pacote** | ~10MB | ~20MB | Similar |

## 🎯 Tipos de Mensagens Suportadas

### 1. ORDER (Pedido)
```json
{
  "type": "ORDER",
  "orderId": "ORD-12345",
  "amount": 100.00,
  "customer": {
    "name": "Cliente Nome",
    "email": "cliente@example.com"
  }
}
```

### 2. NOTIFICATION (Notificação)
```json
{
  "type": "NOTIFICATION",
  "message": "Sua mensagem aqui",
  "timestamp": "2026-01-08T10:00:00Z",
  "severity": "INFO"
}
```

### 3. UPDATE (Atualização)
```json
{
  "type": "UPDATE",
  "entityId": "ENT-456",
  "fields": {
    "status": "active",
    "lastModified": "2026-01-08T10:00:00Z"
  }
}
```

## 🛠️ Comandos Úteis

### Desenvolvimento Local

```bash
# Compilar sem testes
mvn clean compile -DskipTests

# Executar testes
mvn test

# Limpar target
mvn clean
```

### Gerenciar Lambda

```bash
# Atualizar código
aws lambda update-function-code \
  --function-name $FUNCTION_NAME \
  --zip-file fileb://target/06-java-lambda-1.0-SNAPSHOT.jar

# Atualizar configuração
aws lambda update-function-configuration \
  --function-name $FUNCTION_NAME \
  --timeout 120 \
  --memory-size 512

# Invocar manualmente
aws lambda invoke \
  --function-name $FUNCTION_NAME \
  --payload fileb://test-event.json \
  response.json

# Ver informações da função
aws lambda get-function --function-name $FUNCTION_NAME
```

### Monitorar SQS

```bash
# Ver atributos da fila
aws sqs get-queue-attributes \
  --queue-url $QUEUE_URL \
  --attribute-names All

# Ver mensagens (sem remover)
aws sqs receive-message \
  --queue-url $QUEUE_URL \
  --max-number-of-messages 10

# Limpar fila
aws sqs purge-queue --queue-url $QUEUE_URL

# Ver DLQ (mensagens com erro)
aws sqs receive-message --queue-url $DLQ_URL
```

### Logs e Métricas

```bash
# Ver últimos logs
aws logs tail /aws/lambda/$FUNCTION_NAME --since 5m

# Buscar erro específico
aws logs filter-log-events \
  --log-group-name /aws/lambda/$FUNCTION_NAME \
  --filter-pattern "ERROR"

# Ver métricas
aws cloudwatch get-metric-statistics \
  --namespace AWS/Lambda \
  --metric-name Invocations \
  --dimensions Name=FunctionName,Value=$FUNCTION_NAME \
  --start-time 2026-01-08T00:00:00Z \
  --end-time 2026-01-08T23:59:59Z \
  --period 3600 \
  --statistics Sum
```

## 🔧 Customização

### Adicionar Nova Lógica de Processamento

Edite `src/main/java/br/com/tiagoiwamoto/lambda/MessageProcessor.java`:

```java
private void processBusinessLogic(JsonNode jsonNode, String messageId, Context context) {
    if (jsonNode.has("type")) {
        String messageType = jsonNode.get("type").asText();
        
        switch (messageType) {
            case "MEU_TIPO" -> processMyCustomType(jsonNode, messageId);
            // ... outros casos
        }
    }
}

private void processMyCustomType(JsonNode jsonNode, String messageId) {
    logger.info("Processando meu tipo customizado: {}", messageId);
    // Sua lógica aqui
}
```

### Adicionar Dependências

Edite `pom.xml`:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>minha-lib</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Configurar GraalVM para Nova Classe

Edite `src/main/resources/META-INF/native-image/reflect-config.json`:

```json
{
  "name": "com.exemplo.MinhaClasse",
  "allDeclaredConstructors": true,
  "allPublicMethods": true
}
```

## 🐛 Troubleshooting

### Erro: "Dependency not found"
```bash
mvn dependency:purge-local-repository
mvn clean install
```

### Erro: "GraalVM not found"
```bash
# Instalar GraalVM
sdk install java 21-graalvm
sdk use java 21-graalvm
java -version  # Verificar
```

### Lambda Timeout
```bash
# Aumentar timeout para 5 minutos
aws lambda update-function-configuration \
  --function-name $FUNCTION_NAME \
  --timeout 300
```

### Mensagens não sendo processadas

Verifique:
1. Event Source Mapping está ativo
2. IAM Role tem permissões corretas
3. Lambda tem tempo suficiente (timeout)
4. Fila SQS existe e está acessível

```bash
# Verificar Event Source Mapping
aws lambda list-event-source-mappings \
  --function-name $FUNCTION_NAME

# Verificar IAM Role
aws iam get-role --role-name $ROLE_NAME

# Verificar fila
aws sqs get-queue-attributes \
  --queue-url $QUEUE_URL \
  --attribute-names All
```

## 📚 Recursos Adicionais

- [AWS Lambda Java](https://docs.aws.amazon.com/lambda/latest/dg/lambda-java.html)
- [GraalVM Native Image](https://www.graalvm.org/latest/reference-manual/native-image/)
- [AWS SQS](https://docs.aws.amazon.com/sqs/)
- [Maven Shade Plugin](https://maven.apache.org/plugins/maven-shade-plugin/)

## 💡 Dicas de Produção

1. **Use Native Image para produção** - Menor custo e cold start
2. **Configure Dead Letter Queue** - Já configurado no setup
3. **Monitore métricas** - CloudWatch Dashboards
4. **Use Reserved Concurrency** - Para controlar custos
5. **Configure Auto Scaling** - Para o SQS
6. **Implemente Circuit Breaker** - Para dependências externas
7. **Use X-Ray** - Para tracing distribuído

```bash
# Habilitar X-Ray
aws lambda update-function-configuration \
  --function-name $FUNCTION_NAME \
  --tracing-config Mode=Active
```

## ✅ Checklist de Deploy

- [ ] AWS CLI configurado
- [ ] Infraestrutura criada (`./setup-aws.sh`)
- [ ] Projeto compilado (`mvn clean package`)
- [ ] Lambda deployado (`./deploy.sh`)
- [ ] Event Source Mapping configurado
- [ ] Teste enviando mensagem
- [ ] Logs funcionando
- [ ] DLQ configurada
- [ ] Monitoring/Alertas configurados

## 📞 Suporte

Para problemas ou dúvidas:
1. Verifique os logs: `aws logs tail /aws/lambda/$FUNCTION_NAME`
2. Consulte a documentação no README.md
3. Verifique troubleshooting acima

