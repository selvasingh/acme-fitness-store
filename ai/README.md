# Enhance with Azure OpenAI

## Prerequisites
- An Azure subscription with access granted to Azure OpenAI (see more [here](https://customervoice.microsoft.com/Pages/ResponsePage.aspx?id=v4j5cvGGr0GRqy180BHbR7en2Ais5pxKtso_Pz4b1_xUOFA5Qk1UWDRBMjg0WFhPMkIzTzhKQ1dWNyQlQCN0PWcu))


## Prepare Azure OpenAI Service

1. Run the following command to create an Azure OpenAI resource in the the resource group.

   ```bash
   export OPENAI_RESOURCE_NAME=<choose-a-resource-name>
   az cognitiveservices account create \
      -n ${OPENAI_RESOURCE_NAME} \
      -g ${RESOURCE_GROUP} \
      -l ${REGION} \
      --kind OpenAI \
      --sku s0 \
      --custom-domain ${OPENAI_RESOURCE_NAME}   
   ```

1. Create the model deployments for `text-embedding-ada-002` and `gpt-35-turbo` in your Azure OpenAI service.
   ```bash
   az cognitiveservices account deployment create \
      -g ${RESOURCE_GROUP} \
      -n ${OPENAI_RESOURCE_NAME} \
      --deployment-name text-embedding-ada-002 \
      --model-name text-embedding-ada-002 \
      --model-version "2"  \
      --model-format OpenAI \
      --scale-settings-scale-type "Standard"

    az cognitiveservices account deployment create \
      -g ${RESOURCE_GROUP} \
      -n ${OPENAI_RESOURCE_NAME} \
      --deployment-name gpt-35-turbo \
      --model-name gpt-35-turbo \
      --model-version "0301"  \
      --model-format OpenAI \
      --scale-settings-scale-type "Standard"     
   ```


## Build and deploy to Azure Spring Apps

1. Run `cp setup-ai-env-variables-template.sh setup-ai-env-variables.sh` and update the values in `setup-ai-env-variables.sh` with your own values.
1. Prepare the new sample data and images by `./prepare-data.sh`.
1. Redeploy `catalog-service` with the new resources:
    ```bash
    source ./scripts/setup-env-variables.sh
    az spring app deploy --name ${CATALOG_SERVICE_APP} \
    --config-file-pattern catalog/default \
    --source-path apps/acme-catalog \
    --build-env BP_JVM_VERSION=17
    ```
1. Deploy the new ai service `acme-askforhelp` :
    ```bash
    cd ai
    source ./setup-ai-env-variables.sh
    az spring app create --name ${AI_APP} --instance-count 1 --memory 1Gi
    az spring gateway route-config create \
        --name ${AI_APP} \
        --app-name ${AI_APP} \
        --routes-file askforhelp-service.json
    az spring app deploy --name ${AI_APP} \
        --source-path acme-askforhelp \
        --build-env BP_JVM_VERSION=17 \
        --env AZURE_OPENAI_ENDPOINT=${AZURE_OPENAI_ENDPOINT} AZURE_OPENAI_APIKEY=${AZURE_OPENAI_APIKEY} AZURE_OPENAI_CHATDEPLOYMENTID=${AZURE_OPENAI_CHATDEPLOYMENTID} AZURE_OPENAI_EMBEDDINGDEPLOYMENTID=${AZURE_OPENAI_EMBEDDINGDEPLOYMENTID}
    ```