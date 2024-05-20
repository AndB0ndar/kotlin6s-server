#!./venv/bin/python3
import requests
import json
import psycopg2
import os


sql_insert_group = f"INSERT INTO groups ("
sql_insert_group += "id, "
sql_insert_group += "group_name, "
sql_insert_group += "group_suffix, "
sql_insert_group += "unit_name, "
sql_insert_group += "unit_course"
sql_insert_group += ") VALUES (%s, %s, %s, %s, %s)"

conn = psycopg2.connect(
    dbname="queue",
    user="queue",
    password="arbon1874",
    host="localhost",
    port="6000"
)

url = "https://mirea.xyz/api/v1.3/"
certain = "groups/certain"


def insert_group(group_id, group_name, group_suffix, unit_name, unit_course):
    cur.execute(sql_insert_group
            , (group_id, group_name, group_suffix, unit_name, unit_course))
    conn.commit()


if __name__ == "__main__":
    print("Start parse ... ", end="")
    cur = conn.cursor()

    # groups = requests.get(f"{url}groups/all").json()  # load all groups
    path = os.path.dirname(os.path.abspath(__file__))
    with open(f"{path}/data.json") as f:
        groups = json.load(f)

    for group_id, group in enumerate(groups):
        group_name = group['groupName']
        group_suffix = group['groupSuffix']

        data = requests.get(
            f"{url}{certain}"
            , params={'name': group_name, 'suffix': group_suffix}
        ).json()

        for group_data in data:
            insert_group(
                group_id
                , group_name
                , group_suffix
                , group_data["unitName"]
                , group_data["unitCourse"]
                )
    cur.close()
    conn.close()
    print("Done")

