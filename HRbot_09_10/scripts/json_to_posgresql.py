#import sqlite3 as sql
import os
import json
import psycopg2
#import configparser
from bot_config import Bot_config
dir_path = os.path.dirname(os.path.realpath(__file__))
bot_config=Bot_config()
# Файл запускается самостоятельно для создания БД в PosgreSQL


class Json_to_posgresql: 
    def __init__(self):
        return None
        
    # конструктор класса
    @staticmethod
    def update_DB(cursor):
        message_text=""
        try:     
            message_text= ''
            #dir_path = os.path.dirname(os.path.realpath(__file__))
            #print(connection.get_dsn_parameters())
            #cursor.execute("SELECT version()")
            #record = cursor.fetchone()
            #print("You are connected to - ", record)


            drop_questions = '''
            DROP TABLE IF EXISTS questions;
            '''
            cursor.execute(drop_questions)

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
            cursor.execute(create_questions)
        
            with open(f'{dir_path}//..//data//test_data.json', encoding='utf-8') as td:
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
        
                insert_question = 'INSERT INTO questions VALUES (%s, %s, %s, %s, %s, %s)'

                cursor.execute(insert_question, question_data)
    
            cursor.execute('SELECT * FROM questions')
            rows = cursor.rowcount
            print(f'questions count = {rows}')

            message_text += f'questions count = {rows}\n'
       

            drop_answers = '''
            DROP TABLE IF EXISTS answers;
            '''
            cursor.execute(drop_answers)

            create_answers = '''CREATE TABLE  answers (
            answer_id INT,
            question_id INT,
            answer TEXT,
            PRIMARY KEY (answer_id, question_id)
            );
            '''
            cursor.execute(create_answers)

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
            
                    insert_answer = 'INSERT INTO answers (answer_id, question_id, answer) VALUES (%s, %s, %s)'
                    cursor.execute(insert_answer, answer) 

            cursor.execute('SELECT * FROM questions')
            rows = cursor.rowcount
            print(f'answers count = {rows}')
            message_text += f'answers count = {rows}\n'

            drop_block_end_data = '''
            DROP TABLE IF EXISTS block_end_data;
            '''
            cursor.execute(drop_block_end_data)

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
            cursor.execute(create_block_end_data)

            with open(f'{dir_path}//..//data//block_end_data.json', encoding='utf-8') as bd:
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

            insert_block_end_data = 'INSERT INTO block_end_data VALUES (%s,%s,%s,%s,%s,%s,%s,%s)'
            cursor.execute(insert_block_end_data, to_insert)
            cursor.execute('SELECT * FROM block_end_data')
            rows = cursor.rowcount
            print(f'block_end_data count = {rows}') 
            message_text += f'block_end_data count = {rows}\n'

            drop_contacts_structure = '''
            DROP TABLE IF EXISTS contacts_structure;
            '''
            cursor.execute(drop_contacts_structure)

            create_contacts_structure = '''
            CREATE TABLE contacts_structure (
            contact_id SERIAL PRIMARY KEY,
            contact_name TEXT,
            contact_message TEXT,
            reg_exp TEXT
            );
            '''
            cursor.execute(create_contacts_structure)

            with open(f'{dir_path}//..//data//contacts_structure.json', encoding='utf-8') as cs:
                contacts_structure = json.loads(cs.read())

            for contact_str in contacts_structure['elements']:
                contact = (
                    contact_str['name'],
                    contact_str['chat_name'],
                    contact_str['re']
                )
                insert_contact_str = 'INSERT INTO contacts_structure (contact_name, contact_message, reg_exp) VALUES (%s, %s, %s)'
                cursor.execute(insert_contact_str, contact)
            cursor.execute('SELECT * FROM contacts_structure')
            rows = cursor.rowcount
            print(f'contacts_structure count = {rows}') 
            message_text += f'contacts_structure count = {rows}\n'

            drop_users = '''
            DROP TABLE IF EXISTS users;
            '''
            cursor.execute(drop_users)

            create_users = '''    
            CREATE TABLE IF NOT EXISTS users (
            user_id INT PRIMARY KEY,
            is_test_active INT,
            block_id INT,
            questions_order TEXT,
            question_index INT,
            contact_index INT,
            agreement INT,
            entry_date DATE
            );
            '''
            cursor.execute(create_users)
            cursor.execute('SELECT * FROM users')
            rows = cursor.rowcount
            print(f'users count = {rows}')
            message_text += f'users count = {rows}\n'

            drop_users_contacts = '''
            DROP TABLE IF EXISTS users_contacts;
            '''
            cursor.execute(drop_users_contacts)

            create_users_contacts = '''
            CREATE TABLE IF NOT EXISTS users_contacts (
            user_id INT,
            contact_name TEXT,
            contact_value TEXT,
            PRIMARY KEY (user_id, contact_name)
            );
            '''
            cursor.execute(create_users_contacts)

            cursor.execute('SELECT * FROM users_contacts')
            rows = cursor.rowcount
            print(f'users_contacts count = {rows}')
            message_text += f'users_contacts count = {rows}\n'

            drop_users_answers = '''
            DROP TABLE IF EXISTS users_answers;
            '''
            cursor.execute(drop_users_answers)

            create_users_answers = '''
            CREATE TABLE IF NOT EXISTS users_answers (
            user_id INT,
            question_id INT,
            is_correct INT,
            PRIMARY KEY (user_id, question_id)
            );
            '''
            cursor.execute(create_users_answers)

            cursor.execute('SELECT * FROM users_answers')
            rows = cursor.rowcount
            print(f'users_answers count = {rows}')
            message_text += f'users_answers count = {rows}\n'

            drop_users_open_answers = '''
            DROP TABLE IF EXISTS users_open_answers;
            '''
            cursor.execute(drop_users_open_answers)

            create_users_open_answers = '''
            CREATE TABLE IF NOT EXISTS users_open_answers (
            user_id INT,
            question_id INT,
            answer TEXT,
            PRIMARY KEY (user_id, question_id)
            );
            '''
            cursor.execute(create_users_open_answers)

            cursor.execute('SELECT * FROM users_open_answers')
            rows = cursor.rowcount
            print(f'users_open_answers count = {rows}')
            message_text += f'users_open_answers count = {rows}\n'

            create_logs = '''
            CREATE TABLE IF NOT EXISTS Logs (
            id SERIAL PRIMARY KEY,
            action VARCHAR(255),
            table_name VARCHAR(255),
            user_id INT,
            timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            '''
            cursor.execute(create_logs)
            message_text += f'CREATE TABLE Logs\n'

            create_function_for_logs = '''
            CREATE OR REPLACE FUNCTION log_deletion()
            RETURNS TRIGGER AS $$
            BEGIN
                -- Вставка записи в таблицу Logs
                INSERT INTO Logs (action, table_name, user_id)
                VALUES ('Удаление записи', TG_TABLE_NAME, OLD.user_id);
    
                -- Возвращение значения NULL, чтобы удаление продолжилось
                RETURN NULL;
            END;
            $$ LANGUAGE plpgsql;
            '''
            cursor.execute(create_function_for_logs)   
            message_text += f'CREATE FUNCTION log_deletion\n'

            create_trigger_for_users = '''
            CREATE TRIGGER users_deletion_trigger
            AFTER DELETE ON users
            FOR EACH ROW
            EXECUTE PROCEDURE log_deletion();
            '''
            cursor.execute(create_trigger_for_users)

            create_trigger_for_users_answers = '''
            CREATE TRIGGER users_answers_deletion_trigger
            AFTER DELETE ON users_answers
            FOR EACH ROW
            EXECUTE PROCEDURE log_deletion();
            '''
            cursor.execute(create_trigger_for_users_answers)

            create_trigger_for_users_contacts = '''
            CREATE TRIGGER users_contacts_deletion_trigger
            AFTER DELETE ON users_contacts
            FOR EACH ROW
            EXECUTE PROCEDURE log_deletion();
            '''
            cursor.execute(create_trigger_for_users_contacts)

            create_trigger_for_users_open_answers = '''
            CREATE TRIGGER users_open_answers_deletion_trigger
            AFTER DELETE ON users_open_answers
            FOR EACH ROW
            EXECUTE PROCEDURE log_deletion();
            '''
            cursor.execute(create_trigger_for_users_open_answers)
            message_text += f'CREATE TRIGGERS\n'

            #connection.commit()
            #message_text= f'ошибок не обнаружено, update_DB():'
        except (Exception, psycopg2.Error) as error:
            print("Error while connecting to PostgreSQL: ", error)
            message_text= f'Error in Json_to_posgresql.update_DB():  {error}'
        #finally:
            #if connection:
                #cursor.close()
                #connection.close()
                #print("PostgreSQL connection is closed")
                #message_text+=" DB connection is closed"
        return message_text


