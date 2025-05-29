import os
class Bot_config:     
    # конструктор класса
    def __init__(self):
        self.token = os.environ.get('token')
        self.user_OS = os.environ.get('user')
        self.password_OS = os.environ.get('password')
       
        #https://t.me/Python666091Bot
        #self.user_OS = 'postgres'
        #self.password_OS = '123'
        self.host_OS = 'db'
        self.port_OS = 5432
        self.database_OS = 'hrbot_db'
        self.admins_ids = [1622663308, 1371591132, 1555620161]
        