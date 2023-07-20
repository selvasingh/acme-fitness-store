#!/bin/sh
mvn spring-boot:run -Dstart-class=com.microsoft.azure.acme.askforhelp.webapi.BuildVectorStoreApplication -Dspring-boot.run.arguments="--from=$1 --to=$2"