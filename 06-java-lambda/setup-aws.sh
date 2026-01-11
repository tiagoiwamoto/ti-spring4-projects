#!/bin/bash

# Script para criar a infraestrutura AWS necessária para o Lambda SQS Handler
# Autor: Tiago Iwamoto
# Data: 2026-01-08

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Setup AWS Infrastructure para Lambda SQS ===${NC}"

# Variáveis
QUEUE_NAME="mensagens-lambda"
DLQ_NAME="mensagens-lambda-dlq"
ROLE_NAME="lambda-sqs-execution-role"
FUNCTION_NAME="sqs-message-processor"
REGION=${AWS_REGION:-"us-east-1"}

echo -e "${YELLOW}Região AWS: $REGION${NC}"

# 1. Criar Dead Letter Queue (DLQ)
echo -e "\n${GREEN}1. Criando Dead Letter Queue (DLQ)...${NC}"
DLQ_URL=$(aws sqs create-queue \
    --queue-name $DLQ_NAME \
    --region $REGION \
    --query 'QueueUrl' \
    --output text 2>/dev/null || \
    aws sqs get-queue-url \
    --queue-name $DLQ_NAME \
    --region $REGION \
    --query 'QueueUrl' \
    --output text)

DLQ_ARN=$(aws sqs get-queue-attributes \
    --queue-url $DLQ_URL \
    --attribute-names QueueArn \
    --region $REGION \
    --query 'Attributes.QueueArn' \
    --output text)

echo -e "${GREEN}DLQ criada: $DLQ_URL${NC}"
echo -e "${GREEN}DLQ ARN: $DLQ_ARN${NC}"

# 2. Criar SQS Queue principal com DLQ configurada
echo -e "\n${GREEN}2. Criando SQS Queue principal...${NC}"

QUEUE_URL=$(aws sqs create-queue \
    --queue-name $QUEUE_NAME \
    --region $REGION \
    --attributes "{
        \"VisibilityTimeout\": \"300\",
        \"MessageRetentionPeriod\": \"345600\",
        \"ReceiveMessageWaitTimeSeconds\": \"20\",
        \"RedrivePolicy\": \"{\\\"deadLetterTargetArn\\\":\\\"$DLQ_ARN\\\",\\\"maxReceiveCount\\\":\\\"3\\\"}\"
    }" \
    --query 'QueueUrl' \
    --output text 2>/dev/null || \
    aws sqs get-queue-url \
    --queue-name $QUEUE_NAME \
    --region $REGION \
    --query 'QueueUrl' \
    --output text)

QUEUE_ARN=$(aws sqs get-queue-attributes \
    --queue-url $QUEUE_URL \
    --attribute-names QueueArn \
    --region $REGION \
    --query 'Attributes.QueueArn' \
    --output text)

echo -e "${GREEN}Queue criada: $QUEUE_URL${NC}"
echo -e "${GREEN}Queue ARN: $QUEUE_ARN${NC}"

# 3. Criar IAM Role para Lambda
echo -e "\n${GREEN}3. Criando IAM Role para Lambda...${NC}"

TRUST_POLICY=$(cat <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
)

# Criar role (ou obter se já existir)
ROLE_ARN=$(aws iam create-role \
    --role-name $ROLE_NAME \
    --assume-role-policy-document "$TRUST_POLICY" \
    --query 'Role.Arn' \
    --output text 2>/dev/null || \
    aws iam get-role \
    --role-name $ROLE_NAME \
    --query 'Role.Arn' \
    --output text)

echo -e "${GREEN}Role criada: $ROLE_ARN${NC}"

# 4. Criar e anexar política de permissões
echo -e "\n${GREEN}4. Criando política de permissões...${NC}"

POLICY_NAME="lambda-sqs-policy"
ACCOUNT_ID=$(aws sts get-caller-identity --query 'Account' --output text)

POLICY_DOCUMENT=$(cat <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "sqs:ReceiveMessage",
        "sqs:DeleteMessage",
        "sqs:GetQueueAttributes",
        "sqs:ChangeMessageVisibility"
      ],
      "Resource": [
        "$QUEUE_ARN",
        "$DLQ_ARN"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": "arn:aws:logs:$REGION:$ACCOUNT_ID:*"
    }
  ]
}
EOF
)

# Criar política (ou usar existente)
POLICY_ARN=$(aws iam create-policy \
    --policy-name $POLICY_NAME \
    --policy-document "$POLICY_DOCUMENT" \
    --query 'Policy.Arn' \
    --output text 2>/dev/null || \
    echo "arn:aws:iam::$ACCOUNT_ID:policy/$POLICY_NAME")

echo -e "${GREEN}Política criada: $POLICY_ARN${NC}"

# Anexar política à role
aws iam attach-role-policy \
    --role-name $ROLE_NAME \
    --policy-arn $POLICY_ARN 2>/dev/null || true

# Anexar políticas gerenciadas AWS
aws iam attach-role-policy \
    --role-name $ROLE_NAME \
    --policy-arn arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole 2>/dev/null || true

echo -e "${GREEN}Políticas anexadas à role${NC}"

# 5. Aguardar role propagar
echo -e "\n${YELLOW}Aguardando role propagar (10 segundos)...${NC}"
sleep 10

# 6. Salvar configurações
echo -e "\n${GREEN}6. Salvando configurações...${NC}"

cat > aws-config.env << EOF
# Configurações AWS - Gerado em $(date)
export AWS_REGION="$REGION"
export QUEUE_NAME="$QUEUE_NAME"
export QUEUE_URL="$QUEUE_URL"
export QUEUE_ARN="$QUEUE_ARN"
export DLQ_NAME="$DLQ_NAME"
export DLQ_URL="$DLQ_URL"
export DLQ_ARN="$DLQ_ARN"
export ROLE_NAME="$ROLE_NAME"
export ROLE_ARN="$ROLE_ARN"
export POLICY_ARN="$POLICY_ARN"
export FUNCTION_NAME="$FUNCTION_NAME"
export ACCOUNT_ID="$ACCOUNT_ID"
EOF

chmod +x aws-config.env

echo -e "${GREEN}Configurações salvas em aws-config.env${NC}"

# 7. Sumário
echo -e "\n${BLUE}=== Sumário da Infraestrutura Criada ===${NC}"
echo -e "${GREEN}✓ Dead Letter Queue:${NC} $DLQ_NAME"
echo -e "  URL: $DLQ_URL"
echo -e "  ARN: $DLQ_ARN"
echo -e ""
echo -e "${GREEN}✓ SQS Queue Principal:${NC} $QUEUE_NAME"
echo -e "  URL: $QUEUE_URL"
echo -e "  ARN: $QUEUE_ARN"
echo -e "  Visibility Timeout: 300 segundos"
echo -e "  Message Retention: 4 dias"
echo -e "  Max Receive Count: 3 (antes de ir para DLQ)"
echo -e ""
echo -e "${GREEN}✓ IAM Role:${NC} $ROLE_NAME"
echo -e "  ARN: $ROLE_ARN"
echo -e ""
echo -e "${GREEN}✓ IAM Policy:${NC} $POLICY_NAME"
echo -e "  ARN: $POLICY_ARN"
echo -e ""

# 8. Próximos passos
echo -e "\n${BLUE}=== Próximos Passos ===${NC}"
echo -e "1. Execute o build do projeto:"
echo -e "   ${YELLOW}mvn clean package${NC}"
echo -e ""
echo -e "2. Faça o deploy da função Lambda:"
echo -e "   ${YELLOW}./deploy.sh${NC}"
echo -e ""
echo -e "3. Teste enviando uma mensagem:"
echo -e "   ${YELLOW}aws sqs send-message --queue-url $QUEUE_URL --message-body '{\"type\":\"ORDER\",\"orderId\":\"123\"}'${NC}"
echo -e ""
echo -e "4. Veja os logs:"
echo -e "   ${YELLOW}aws logs tail /aws/lambda/$FUNCTION_NAME --follow${NC}"
echo -e ""

echo -e "${GREEN}✓ Setup concluído com sucesso!${NC}"

