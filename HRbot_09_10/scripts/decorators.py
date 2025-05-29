#import os
import psycopg2
#import configparser
from bot_config import Bot_config

# Обёртка для взаимодействия методов с ботом
# Обёрнутые ей методы должны возвращать словарь вида:
# {'вызываемый у бота метод': {'название параметра': <значение параметра>, ...}, ...}

bot_config=Bot_config()

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
            user = bot_config.user_OS,
            password = bot_config.password_OS,            
            host = bot_config.host_OS,
            port = bot_config.port_OS,
            database = bot_config.database_OS
        )  
        cursor = connection.cursor()
        try:
            ret = handler(cursor, *args)
            connection.commit()
        finally:
            connection.close()

        return ret

    return wrapper
