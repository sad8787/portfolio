import os
import psycopg2
import configparser
from datetime import datetime

# Обёртка для взаимодействия методов с ботом
# Обёрнутые ей методы должны возвращать словарь вида:
# {'вызываемый у бота метод': {'название параметра': <значение параметра>, ...}, ...}
dir_path = os.path.dirname(os.path.realpath(__file__))
config = configparser.ConfigParser()
config.read(f'{dir_path}//..//data//config.ini')

def data_handler(handler):
    def wrapper(*args):        
        connection = psycopg2.connect(
            #user = os.environ.get('bd_user'),
            #password = os.environ.get('bd_password'),
            user = config['bd']['user'],
            password = config['bd']['password'],
            host=config['bd']['host'],
            port=config['bd']['port'],
            database=config['bd']['database']
        )  
        cursor = connection.cursor()
        ret=False
        try:
            ret = handler(cursor, *args)
            connection.commit()
        except (Exception) as error:
            print(f'{datetime.today()} ERROR in data_handler  Data connection: {error}')
        finally:
            connection.close()
        return ret

    return wrapper
