import json
import psycopg2
import os
import configparser

dir_path = os.path.dirname(os.path.realpath(__file__))
config = configparser.ConfigParser()
config.read(f'{dir_path}//..//data//config.ini')

#bd connection
bd_user = config['bd']['user']
bd_password = config['bd']['password']
#bd_user = os.environ.get('bd_user')
#bd_password = os.environ.get('bd_password')

bd_host=config['bd']['host']
bd_port=config['bd']['port']
bd_database=config['bd']['database']


try:    
    connection = psycopg2.connect(
        user = bd_user,
        password = bd_password,
        host = bd_host,
        port = bd_port,
        database = bd_database
        )
    
    cursor = connection.cursor()  


    drop_users = ''' DROP TABLE IF EXISTS users; '''
    cursor.execute(drop_users)

    create_user = '''
    CREATE TABLE users (    
    telegram_user_id BIGINT ,         
    user_name TEXT,   
    employee_token TEXT,
    expires_in INT,
    date_in DATE,
    status TEXT,
    is_bot BOOLEAN,
    refresh_token TEXT,
    is_on_duty BOOLEAN    
    );
    '''
    cursor.execute(create_user)

    drop_user_chat_group = ''' DROP TABLE IF EXISTS user_chat_group; '''
    cursor.execute( drop_user_chat_group)
    create_user_chat_group = '''
    CREATE TABLE user_chat_group (    
    telegram_user_id BIGINT ,         
    chat_id BIGINT     
    );
    '''
    cursor.execute(create_user_chat_group)


    


    connection.commit()


except (Exception, psycopg2.Error) as error:
    print("Error while connecting to PostgreSQL: ", error)
finally:
    if connection:
        cursor.close()
        connection.close()
        print("PostgreSQL connection is closed")


