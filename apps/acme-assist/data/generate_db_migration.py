#!/usr/bin/env python3

import json
import uuid


def single_quote(raw):
    return "'" + raw.replace("'", "''") + "'"

def generate_sql(category):
    data=json.load(open(category,'r+'))

    sql = 'INSERT INTO CATALOG (id,  description,  image_url1,  image_url2,  image_url3,  name,  price,  short_description,  tags)\nVALUES'

    for i, item in enumerate(data, 1):
        image_file = '/static/images/new_%s_%d.jpg' % (category, i)
        row = (
            single_quote(str(uuid.uuid4())),
            single_quote(item['description']),
            single_quote(image_file),
            single_quote(image_file),
            single_quote(image_file),
            single_quote(item['name']),
            str(item['price']),
            single_quote(item['shortDescription']),
            single_quote(','.join(item['tags']))
        )
        sql += '\n(' + ', '.join(row) + '),'

    sql = sql.replace('\\\'', '\'\'')
    sql = sql.rstrip(',')
    sql += ';'

    print(sql)

if __name__ == '__main__':
    generate_sql('data/bikes.json')
    generate_sql('data/accessories.json')
