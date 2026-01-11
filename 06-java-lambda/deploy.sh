#!/bin/bash

# Script de deploy da função Lambda SQS Handler
# Autor: Tiago Iwamoto
# Data: 2026-01-08

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Variáveis de configuração
FUNCTION_NAME="sqs-message-processor"
HANDLER="br.com.tiagoiwamoto.lambda.SqsEventHandler::handleRequest"
RUNTIME="java21"
MEMORY_SIZE=512
TIMEOUT=60
REGION="us-east-1"

echo -e "${GREEN}=== Deploy Lambda SQS Handler ===${NC}"

# Verificar se AWS CLI está instalado
if ! command -v aws &> /dev/null; then
    echo -e "${RED}AWS CLI não está instalado. Por favor, instale primeiro.${NC}"
    exit 1
fi

# Menu de opções
echo ""
echo "Escolha o tipo de build:"
echo "1) JVM (JAR tradicional)"
echo "2) Native Image (GraalVM)"
read -p "Opção [1-2]: " BUILD_TYPE

if [ "$BUILD_TYPE" = "1" ]; then
    echo -e "${YELLOW}Compilando com Maven (JVM)...${NC}"
    mvn clean package -DskipTests

    PACKAGE_FILE="target/06-java-lambda-1.0-SNAPSHOT.jar"
    RUNTIME_OPTION="java21"

elif [ "$BUILD_TYPE" = "2" ]; then
    echo -e "${YELLOW}Compilando com GraalVM Native Image...${NC}"

    # Verificar se GraalVM está instalado
    if ! command -v native-image &> /dev/null; then
        echo -e "${RED}GraalVM native-image não encontrado. Por favor, instale GraalVM.${NC}"
        exit 1
    fi

    mvn clean package -Pnative -DskipTests

    # Criar bootstrap script
    cat > bootstrap << 'EOF'
#!/bin/sh
set -euo pipefail
./sqs-lambda-handler "${_HANDLER}"
EOF
    chmod +x bootstrap

    # Criar ZIP com native image
    zip -j function.zip target/sqs-lambda-handler bootstrap

    PACKAGE_FILE="function.zip"
    RUNTIME_OPTION="provided.al2"

else
    echo -e "${RED}Opção inválida!${NC}"
    exit 1
fi

# Verificar se o arquivo foi gerado
if [ ! -f "$PACKAGE_FILE" ]; then
    echo -e "${RED}Erro: Arquivo de pacote não encontrado: $PACKAGE_FILE${NC}"
    exit 1
fi

echo -e "${GREEN}Build concluído com sucesso!${NC}"
echo -e "${YELLOW}Tamanho do pacote: $(du -h $PACKAGE_FILE | cut -f1)${NC}"

# Solicitar IAM Role ARN
read -p "Digite o ARN da IAM Role (ou pressione Enter para usar default): " ROLE_ARN

if [ -z "$ROLE_ARN" ]; then
    echo -e "${YELLOW}Tentando obter IAM Role automaticamente...${NC}"
    ROLE_ARN=$(aws iam list-roles --query "Roles[?RoleName=='lambda-execution-role'].Arn" --output text)

    if [ -z "$ROLE_ARN" ]; then
        echo -e "${RED}Não foi possível encontrar a IAM Role. Por favor, forneça o ARN.${NC}"
        exit 1
    fi
fi

echo -e "${GREEN}Usando IAM Role: $ROLE_ARN${NC}"

# Verificar se a função já existe
FUNCTION_EXISTS=$(aws lambda get-function --function-name $FUNCTION_NAME --region $REGION 2>&1 || true)

if echo "$FUNCTION_EXISTS" | grep -q "ResourceNotFoundException"; then
    echo -e "${YELLOW}Criando nova função Lambda...${NC}"

    aws lambda create-function \
        --function-name $FUNCTION_NAME \
        --runtime $RUNTIME_OPTION \
        --handler $HANDLER \
        --memory-size $MEMORY_SIZE \
        --timeout $TIMEOUT \
        --role $ROLE_ARN \
        --zip-file fileb://$PACKAGE_FILE \
        --region $REGION \
        --environment Variables={LOG_LEVEL=INFO}

    echo -e "${GREEN}Função Lambda criada com sucesso!${NC}"
else
    echo -e "${YELLOW}Atualizando função Lambda existente...${NC}"

    aws lambda update-function-code \
        --function-name $FUNCTION_NAME \
        --zip-file fileb://$PACKAGE_FILE \
        --region $REGION

    echo -e "${GREEN}Função Lambda atualizada com sucesso!${NC}"
fi

# Perguntar se deseja configurar trigger SQS
read -p "Deseja configurar trigger SQS? (s/n): " CONFIGURE_SQS

if [ "$CONFIGURE_SQS" = "s" ] || [ "$CONFIGURE_SQS" = "S" ]; then
    read -p "Digite o nome da fila SQS: " QUEUE_NAME

    # Obter ARN da fila
    QUEUE_URL=$(aws sqs get-queue-url --queue-name $QUEUE_NAME --region $REGION --query 'QueueUrl' --output text)
    QUEUE_ARN=$(aws sqs get-queue-attributes --queue-url $QUEUE_URL --attribute-names QueueArn --region $REGION --query 'Attributes.QueueArn' --output text)

    echo -e "${YELLOW}Configurando Event Source Mapping...${NC}"

    aws lambda create-event-source-mapping \
        --function-name $FUNCTION_NAME \
        --event-source-arn $QUEUE_ARN \
        --batch-size 10 \
        --maximum-batching-window-in-seconds 5 \
        --region $REGION

    echo -e "${GREEN}Event Source Mapping configurado com sucesso!${NC}"
fi

echo ""
echo -e "${GREEN}=== Deploy Concluído ===${NC}"
echo ""
echo "Função: $FUNCTION_NAME"
echo "Runtime: $RUNTIME_OPTION"
echo "Região: $REGION"
echo ""
echo "Para testar, envie uma mensagem para a fila SQS:"
echo "aws sqs send-message --queue-url YOUR_QUEUE_URL --message-body '{\"type\":\"ORDER\",\"orderId\":\"12345\"}'"
echo ""
echo "Para ver os logs:"
echo "aws logs tail /aws/lambda/$FUNCTION_NAME --follow"

