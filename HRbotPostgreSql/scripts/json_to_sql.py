import sqlite3 as sql
import os
import json
#{dir_path}//..//
dir_path = os.path.dirname(os.path.realpath(__file__))

#if 'data.db' not in os.listdir('C:/Users/sadie/OneDrive/Documentos/GitHub/HRbot/data/data.db'):    
    #open('C:/Users/sadie/OneDrive/Documentos/GitHub/HRbot/data/data.db', 'w')
if 'data.db' not in os.listdir('{dir_path}//..//data'):
    open('{dir_path}//..//data//data.db', 'w')
    

#conn = sql.connect(r'C:/Users/sadie/OneDrive/Documentos/GitHub/HRbot/data/data.db')
conn = sql.connect(r'{dir_path}//..//data//data.db')
cur = conn.cursor()

drop_questions = '''
DROP TABLE IF EXISTS questions;
'''
cur.execute(drop_questions)

create_questions = '''
CREATE TABLE questions (
question_id INT PRIMARY KEY,
block_id INT,
question_type TEXT,
right_answer TEXT,
question TEXT,
image TEXT
);
'''
cur.execute(create_questions)

#with open('C:/Users/sadie/OneDrive/Documentos/GitHub/HRbot/data/test_data.json', encoding='utf-8') as td:
    #test_data = json.loads(td.read())
with open('{dir_path}//..//data//test_data.json', encoding='utf-8') as td:
    test_data = json.loads(td.read())

for question_id in test_data:
    question_struct = test_data[question_id]

    question_data = (
        question_id,
        question_struct['block_id'],
        question_struct['question_type'],
        question_struct['right_answer'],
        question_struct['question'],
        question_struct['image'] if 'image' in question_struct else None
    )

    insert_question = 'INSERT INTO questions VALUES (?, ?, ?, ?, ?, ?)'

    cur.execute(insert_question, question_data)

print(cur.execute('SELECT * FROM questions').fetchall())

drop_answers = '''
DROP TABLE IF EXISTS answers;
'''
cur.execute(drop_answers)

create_answers = '''
CREATE TABLE answers (
answer_id INT,
question_id INT,
answer TEXT,
PRIMARY KEY (answer_id, question_id)
);
'''
cur.execute(create_answers)

for question_id in test_data:
    answers = test_data[question_id]['choices'] if 'choices' in test_data[question_id] else None

    if not answers:
        continue

    for answer_id, answer_text in answers.items():
        answer = (
            answer_id,
            question_id,
            answer_text
        )

        insert_answer = 'INSERT INTO answers (answer_id, question_id, answer) VALUES (?, ?, ?)'
        cur.execute(insert_answer, answer)

print(cur.execute('SELECT * FROM answers').fetchall())

drop_block_end_data = '''
DROP TABLE IF EXISTS block_end_data;
'''
cur.execute(drop_block_end_data)

create_block_end_data = '''
CREATE TABLE block_end_data (
correct_answers_5 TEXT,
correct_answers_4 TEXT,
correct_answers_3 TEXT,
correct_answers_2 TEXT,
correct_answers_1 TEXT,
correct_answers_0 TEXT,
offer TEXT,
image TEXT
);
'''
cur.execute(create_block_end_data)

#with open('C:/Users/sadie/OneDrive/Documentos/GitHub/HRbot/data/block_end_data.json', encoding='utf-8') as bd:
    #block_end_data = json.loads(bd.read())
with open('{dir_path}//..//data//block_end_data.json', encoding='utf-8') as bd:
    block_end_data = json.loads(bd.read())

to_insert = (
    block_end_data['block_end']['correct_answers_5'],
    block_end_data['block_end']['correct_answers_4'],
    block_end_data['block_end']['correct_answers_3'],
    block_end_data['block_end']['correct_answers_2'],
    block_end_data['block_end']['correct_answers_1'],
    block_end_data['block_end']['correct_answers_0'],
    block_end_data['block_end']['offer'],
    block_end_data['block_end']['image']
)

insert_block_end_data = 'INSERT INTO block_end_data VALUES (?, ?, ?, ?, ?, ?, ?, ?)'

cur.execute(insert_block_end_data, to_insert)

print(cur.execute('SELECT * FROM block_end_data').fetchall())

drop_contacts_structure = '''
DROP TABLE IF EXISTS contacts_structure;
'''
cur.execute(drop_contacts_structure)

create_contacts_structure = '''
CREATE TABLE contacts_structure (
contact_id INTEGER PRIMARY KEY AUTOINCREMENT,
contact_name TEXT,
contact_message TEXT,
reg_exp TEXT
);
'''
cur.execute(create_contacts_structure)

#with open('C:/Users/sadie/OneDrive/Documentos/GitHub/HRbot/data/contacts_structure.json', encoding='utf-8') as cs:
    #contacts_structure = json.loads(cs.read())
with open('{dir_path}//..//data//contacts_structure.json', encoding='utf-8') as cs:
    contacts_structure = json.loads(cs.read())

for contact_str in contacts_structure['elements']:
    contact = (
        contact_str['name'],
        contact_str['chat_name'],
        contact_str['re']
    )

    insert_contact_str = 'INSERT INTO contacts_structure (contact_name, contact_message, reg_exp) VALUES (?, ?, ?)'

    cur.execute(insert_contact_str, contact)

print(cur.execute('SELECT * FROM contacts_structure').fetchall())

create_users = '''
CREATE TABLE IF NOT EXISTS users (
user_id INT PRIMARY KEY,
is_test_active INT,
block_id INT,
questions_order TEXT,
question_index INT,
contact_index INT,
agreement INT
);
'''
cur.execute(create_users)

print(cur.execute('SELECT * FROM users').fetchall())

create_users_contacts = '''
CREATE TABLE IF NOT EXISTS users_contacts (
user_id INT,
contact_name TEXT,
contact_value TEXT,
PRIMARY KEY (user_id, contact_name)
);
'''
cur.execute(create_users_contacts)

print(cur.execute('SELECT * FROM users_contacts').fetchall())

create_users_answers = '''
CREATE TABLE IF NOT EXISTS users_answers (
user_id INT,
question_id INT,
is_correct INT,
PRIMARY KEY (user_id, question_id)
);
'''
cur.execute(create_users_answers)

print(cur.execute('SELECT * FROM users_answers').fetchall())

create_users_open_answers = '''
CREATE TABLE IF NOT EXISTS users_open_answers (
user_id INT,
question_id INT,
answer TEXT,
PRIMARY KEY (user_id, question_id)
);
'''
cur.execute(create_users_open_answers)

print(cur.execute('SELECT * FROM users_open_answers').fetchall())

conn.commit()
