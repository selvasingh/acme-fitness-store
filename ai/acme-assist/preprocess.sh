#!/bin/sh
mvn spring-boot:run -Dstart-class=com.azure.acme.assist.tools.BuildVectorStoreApplication -Dspring-boot.run.arguments="--from=$1 --to=$2"