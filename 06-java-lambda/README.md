# AWS Lambda SQS Handler com GraalVM Native Image

Este projeto implementa uma função AWS Lambda em Java que processa mensagens do Amazon SQS, com suporte para compilação nativa usando GraalVM e AOT (Ahead-Of-Time) compilation.

## 🚀 Características

- ✅ Handler Lambda para eventos SQS
- ✅ Compilação nativa com GraalVM
- ✅ Suporte para mensagens JSON e texto
- ✅ Processamento em lote de mensagens
- ✅ Logging estruturado
- ✅ Testes unitários

## 📋 Pré-requisitos

- Java 21 ou superior
- Maven 3.8+
- GraalVM 21+ (para compilação nativa)
- AWS CLI configurado (para deploy)

### Instalando GraalVM

```bash
# Download GraalVM
sdk install java 21-graalvm

# Ou usando SDKMAN
sdk use java 21-graalvm

# Verificar instalação
java -version
native-image --version
```

## 🔨 Build

### Build JAR tradicional (JVM)

```bash
mvn clean package
```

O JAR será gerado em: `target/06-java-lambda-1.0-SNAPSHOT.jar`

### Build Native Image (GraalVM)

```bash
# Build com GraalVM Native Image
mvn clean package -Pnative

# Ou usando o plugin diretamente
mvn clean native:compile
```

O executável nativo será gerado em: `target/sqs-lambda-handler`

## 📦 Deploy

### Deploy via AWS CLI

```bash
# Criar função Lambda (primeira vez)
aws lambda create-function \
  --function-name sqs-message-processor \
  --runtime java21 \
  --handler br.com.tiagoiwamoto.lambda.SqsEventHandler::handleRequest \
  --memory-size 512 \
  --timeout 60 \
  --role arn:aws:iam::YOUR_ACCOUNT:role/lambda-execution-role \
  --zip-file fileb://target/06-java-lambda-1.0-SNAPSHOT.jar

# Atualizar função existente
aws lambda update-function-code \
  --function-name sqs-message-processor \
  --zip-file fileb://target/06-java-lambda-1.0-SNAPSHOT.jar
```

### Deploy Native Image

Para deploy de native image, você precisa criar um Lambda Custom Runtime:

```bash
# Criar bootstrap script
cat > bootstrap << 'EOF'
#!/bin/sh
set -euo pipefail
./sqs-lambda-handler
EOF

chmod +x bootstrap

# Criar ZIP com native image
zip -j function.zip target/sqs-lambda-handler bootstrap

# Deploy
aws lambda create-function \
  --function-name sqs-message-processor-native \
  --runtime provided.al2 \
  --handler function.handler \
  --memory-size 512 \
  --timeout 60 \
  --role arn:aws:iam::YOUR_ACCOUNT:role/lambda-execution-role \
  --zip-file fileb://function.zip
```

## 🔗 Configurar trigger SQS

```bash
# Criar fila SQS (se ainda não existir)
aws sqs create-queue --queue-name mensagens-lambda

# Obter ARN da fila
QUEUE_ARN=$(aws sqs get-queue-attributes \
  --queue-url https://sqs.us-east-1.amazonaws.com/YOUR_ACCOUNT/mensagens-lambda \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' \
  --output text)

# Adicionar trigger SQS ao Lambda
aws lambda create-event-source-mapping \
  --function-name sqs-message-processor \
  --event-source-arn $QUEUE_ARN \
  --batch-size 10 \
  --maximum-batching-window-in-seconds 5
```

## 🧪 Testes

### Executar testes unitários

```bash
mvn test
```

### Testar localmente

```bash
# Criar arquivo de evento de teste
cat > test-event.json << 'EOF'
{
  "Records": [
    {
      "messageId": "test-123",
      "receiptHandle": "test-receipt",
      "body": "{\"type\":\"ORDER\",\"orderId\":\"12345\",\"amount\":100.00}",
      "attributes": {
        "ApproximateReceiveCount": "1"
      },
      "messageAttributes": {},
      "awsRegion": "us-east-1",
      "eventSource": "aws:sqs",
      "eventSourceARN": "arn:aws:sqs:us-east-1:123456789012:test-queue"
    }
  ]
}
EOF

# Testar com AWS SAM Local
sam local invoke -e test-event.json
```

### Enviar mensagem de teste para SQS

```bash
# Enviar mensagem JSON
aws sqs send-message \
  --queue-url https://sqs.us-east-1.amazonaws.com/YOUR_ACCOUNT/mensagens-lambda \
  --message-body '{
    "type": "ORDER",
    "orderId": "12345",
    "amount": 100.00,
    "customer": {
      "name": "João Silva",
      "email": "joao@example.com"
    }
  }'

# Enviar mensagem de notificação
aws sqs send-message \
  --queue-url https://sqs.us-east-1.amazonaws.com/YOUR_ACCOUNT/mensagens-lambda \
  --message-body '{
    "type": "NOTIFICATION",
    "message": "Sistema atualizado com sucesso",
    "timestamp": "2026-01-08T10:00:00Z"
  }'

# Enviar mensagem de atualização
aws sqs send-message \
  --queue-url https://sqs.us-east-1.amazonaws.com/YOUR_ACCOUNT/mensagens-lambda \
  --message-body '{
    "type": "UPDATE",
    "entityId": "456",
    "fields": {
      "status": "active",
      "lastModified": "2026-01-08T10:00:00Z"
    }
  }'
```

## 📊 Estrutura do Projeto

```
06-java-lambda/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/com/tiagoiwamoto/lambda/
│   │   │       ├── SqsEventHandler.java        # Handler principal
│   │   │       └── MessageProcessor.java       # Processador de mensagens
│   │   └── resources/
│   │       └── META-INF/native-image/
│   │           ├── reflect-config.json         # Configuração de reflexão
│   │           ├── serialization-config.json   # Configuração de serialização
│   │           └── resource-config.json        # Configuração de recursos
│   └── test/
│       └── java/
│           └── br/com/tiagoiwamoto/lambda/
│               └── SqsEventHandlerTest.java    # Testes unitários
├── pom.xml                                      # Configuração Maven
└── README.md                                    # Este arquivo
```

## 🎯 Tipos de Mensagens Suportadas

O handler suporta três tipos de mensagens JSON:

### 1. ORDER (Pedido)
```json
{
  "type": "ORDER",
  "orderId": "12345",
  "amount": 100.00
}
```

### 2. NOTIFICATION (Notificação)
```json
{
  "type": "NOTIFICATION",
  "message": "Mensagem de notificação"
}
```

### 3. UPDATE (Atualização)
```json
{
  "type": "UPDATE",
  "entityId": "456"
}
```

## ⚙️ Configuração da IAM Role

A função Lambda precisa de uma role com as seguintes permissões:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "sqs:ReceiveMessage",
        "sqs:DeleteMessage",
        "sqs:GetQueueAttributes"
      ],
      "Resource": "arn:aws:sqs:*:*:*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": "arn:aws:logs:*:*:*"
    }
  ]
}
```

## 🔍 Monitoramento

### Ver logs no CloudWatch

```bash
# Ver logs em tempo real
aws logs tail /aws/lambda/sqs-message-processor --follow

# Ver logs específicos
aws logs filter-log-events \
  --log-group-name /aws/lambda/sqs-message-processor \
  --start-time $(date -d '5 minutes ago' +%s)000
```

### Métricas importantes

- **Invocations**: Número de invocações
- **Duration**: Tempo de execução
- **Errors**: Número de erros
- **Dead Letter Errors**: Mensagens enviadas para DLQ

## 🚀 Performance

### Comparação JVM vs Native Image

| Métrica | JVM | Native Image | Melhoria |
|---------|-----|--------------|----------|
| Cold Start | ~5-10s | ~100-200ms | 50x |
| Memory | 512MB | 128MB | 4x |
| Custo | Maior | Menor | 60-70% |

## 📝 Customização

Para adicionar sua própria lógica de negócio, edite os métodos em `MessageProcessor.java`:

- `processOrder()`: Lógica para processar pedidos
- `processNotification()`: Lógica para notificações
- `processUpdate()`: Lógica para atualizações
- `processTextMessage()`: Lógica para mensagens de texto

## 🐛 Troubleshooting

### Erro de reflexão no Native Image

Adicione as classes necessárias em `reflect-config.json`

### Timeout no Lambda

Aumente o timeout da função:
```bash
aws lambda update-function-configuration \
  --function-name sqs-message-processor \
  --timeout 300
```

### Mensagens não sendo processadas

Verifique:
1. Permissions da IAM Role
2. Event Source Mapping configurado
3. Logs no CloudWatch

## 📚 Recursos

- [AWS Lambda Java](https://docs.aws.amazon.com/lambda/latest/dg/lambda-java.html)
- [GraalVM Native Image](https://www.graalvm.org/latest/reference-manual/native-image/)
- [AWS SQS](https://docs.aws.amazon.com/sqs/)

## 📄 Licença

Este projeto está sob a licença MIT.

