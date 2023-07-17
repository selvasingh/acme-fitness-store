#!/bin/sh

./generate_db_migration.py > V1_1__insert_products.sql
mv *.sql ../apps/acme-catalog/src/main/resources/db/migration
cp data/images/* ../apps/acme-catalog/src/main/resources/static/
