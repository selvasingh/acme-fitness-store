#!/bin/sh
INITIAL_WORKING_DIRECTORY=$(pwd)
cd "$(dirname "$0")"
python ./data/generate_db_migration.py > ./V1_1__insert_products.sql
mv ./*.sql ../acme-catalog/src/main/resources/db/migration
cp ./data/images/* ../acme-catalog/src/main/resources/static/
cd $INITIAL_WORKING_DIRECTORY