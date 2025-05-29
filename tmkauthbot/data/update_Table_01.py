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

    create_user = '''
	ALTER TABLE users 
	ADD COLUMN employeeguid INT;
    '''
    cursor.execute(create_user)
    connection.commit()
except (Exception, psycopg2.Error) as error:
    print("Error while connecting to PostgreSQL: ", error)
finally:
    if connection:
        cursor.close()
        connection.close()
        print("PostgreSQL connection is closed")


