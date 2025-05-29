import os
import psycopg2
import configparser

# Обёртка для взаимодействия методов с ботом
# Обёрнутые ей методы должны возвращать словарь вида:
# {'вызываемый у бота метод': {'название параметра': <значение параметра>, ...}, ...}
dir_path = os.path.dirname(os.path.realpath(__file__))
config = configparser.ConfigParser()
config.read(f'{dir_path}//..//data//config.ini')

def bot_feedback(handler):
    def wrapper(*args):
        from bot import bot

        ret = handler(*args)

        if ret:
            for feedback_type in ret:
                feedback_values = ret[feedback_type]
                feedback_function = getattr(bot, feedback_type)
                feedback_function(**feedback_values)

        return ret

    return wrapper


def data_handler(handler):
    def wrapper(*args):        
        connection = psycopg2.connect(
            user = os.environ.get('user'),
            password = os.environ.get('password'),
            #user = config['bd']['user'],
            #password = config['bd']['password'],
            host=config['bd']['host'],
            port=config['bd']['port'],
            database=config['bd']['database']
        )  
        cursor = connection.cursor()
        try:
            ret = handler(cursor, *args)
            connection.commit()
        finally:
            connection.close()

        return ret

    return wrapper
