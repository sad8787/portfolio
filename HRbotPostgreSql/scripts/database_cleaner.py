import os
import sys
import psycopg2
import configparser
from datetime import date, datetime
from datetime import timedelta
dir_path = os.path.dirname(os.path.realpath(__file__))
config = configparser.ConfigParser()
config.read(f'{dir_path}//..//data//config.ini')
try:
    connection = psycopg2.connect(
        user = config['bd']['user'],
        password = config['bd']['password'],
        host=config['bd']['host'],
        port=config['bd']['port'],
        database=config['bd']['database']
     )        

    cursor = connection.cursor()    
    today = datetime.today().date()      
    day_delta=timedelta(days=60)
    old_day=today-day_delta
    value_date = old_day.strftime('%Y-%m-%d')
    print(value_date)
   
    
    #delete_user ="DELETE FROM users WHERE entry_date <=' "+value+"';"
    #INSERT INTO public.users(	user_id, entry_date) VALUES (15778887, '2023-01-01');
    #INSERT INTO public.users_contacts(	user_id, contact_name, contact_value) VALUES (1, 'mail', 's@de.com');
    #INSERT INTO public.users_answers(user_id, question_id, is_correct)	VALUES (1, 3, 0);
    #INSERT INTO public.users_open_answers(	user_id, question_id, answer)	VALUES (5, 1, 1);
    get_user_id = f'''SELECT user_id FROM users WHERE entry_date<='{value_date}';'''
    cursor.execute(get_user_id)
    users_id = cursor.fetchall()
    print("----")
    print(users_id)
    print("----")
    for row in users_id:
        delete_users_answers= "DELETE FROM users_answers WHERE user_id= "+str(row[0])+";"
        delete_users_contacts="DELETE FROM users_contacts WHERE user_id= "+str(+row[0])+";"
        delete_users_open_answers="DELETE FROM users_open_answers	WHERE user_id= "+str(+row[0])+";"
        delete_user= "DELETE FROM users WHERE user_id = '"+str(+row[0])+"';"
        delete_data=delete_users_answers+delete_users_contacts+delete_users_open_answers+delete_user
        cursor.execute(delete_data)
    
    #cursor.execute(delete_user)
    
    connection.commit()
except (Exception, psycopg2.Error) as error:
    print("Error while connecting to PostgreSQL: ", error)
finally:
    if connection:
        cursor.close()
        connection.close()
        print("PostgreSQL connection is closed")
         