import sqlite3 as sql
import os
dir_path = os.path.dirname(os.path.realpath(__file__))
#connection = sql.connect(r'C:/Git/chatbot/HRbot/data/data.db')
#connection = sql.connect(r'C:/Users/sadie/OneDrive/Documentos/GitHub/HRbot/data/data.db')
connection = sql.connect(r'{dir_path}//..//data//data.db')
cursor = connection.cursor()

check_if_user_exists = f'SELECT user_id FROM users_contacts WHERE user_id = 646580648'
user_exists = cursor.execute(check_if_user_exists).fetchone()

print(user_exists is not None)
