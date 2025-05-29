from datetime import datetime

class User:
    
    # конструктор класса
    def __init__(self, telegram_user_id, status, is_bot, user_name = None, token = None, expires_in = None, refresh_token = None, date_in = datetime.today().date(),is_on_service = False,employeeguid = None):
        self.telegram_user_id = telegram_user_id
        self.user_name = user_name
        self.employee_token = token
        self.expires_in = expires_in
        self.date_in = date_in
        self.status = status
        self.is_bot = is_bot
        self.refresh_token = refresh_token
        self.is_on_service=is_on_service
        self.employeeguid = employeeguid
        
        