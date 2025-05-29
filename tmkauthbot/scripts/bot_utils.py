import requests
from user import User
from telebot import types
from datetime import datetime, timedelta
from user_status import User_status
from db_utils import db_utils_static

class bot_utils_static:

   
    @staticmethod
    def add_header_worksheet_excel_buffer(sheet):
        sheet['a1'] = "User"
        sheet['b1'] = "ФИО"
        sheet['c1'] = "Status"
        sheet['d1'] = "Is bot?"
        sheet['e1'] = "Date in"
        sheet['f1'] = "Дежурный?"  
    @staticmethod
    def add_row_worksheet_excel_buffer(sheet, row_index, user):
        columns_leters = ['a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','aa','ab','ac','ad','ae','af','ag','ah','ai','aj','ak','al','am','an','ao','ap','aq','ar','as','at','au','av','aw','ax','ay','az']        
        columns_index = 0
        position = f'{columns_leters[columns_index]}{row_index}'
        columns_index +=1
        sheet[f'{position}'] = f'{user[0]}' #user id
        position = f'{columns_leters[columns_index]}{row_index}'
        columns_index +=1
        sheet[f'{position}'] = f'{user[1]}' #user ФИО
        position = f'{columns_leters[columns_index]}{row_index}'
        columns_index +=1
        sheet[f'{position}'] = f'{user[5]}' #user status 
        position = f'{columns_leters[columns_index]}{row_index}'
        columns_index +=1
        sheet[f'{position}'] = f'{user[6]}' #user is_bot
        position = f'{columns_leters[columns_index]}{row_index}'
        columns_index +=1
        sheet[f'{position}'] = f'{user[4]}' #user date_in 
        position = f'{columns_leters[columns_index]}{row_index}'
        columns_index +=1
        sheet[f'{position}'] = f'{user[8]}' #user date_in  

    @staticmethod
    def add_header_worksheet_error_report(sheet):            
        sheet['a1'] = "date" 
        sheet['b1'] = "user name"
        sheet['c1'] = "user telegram_user_id"
        sheet['d1'] = "error"   
                           
    @staticmethod
    def add_row_worksheet_error_report(sheet, row_index, user,report_text):
        columns_leters = ['a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','aa','ab','ac','ad','ae','af','ag','ah','ai','aj','ak','al','am','an','ao','ap','aq','ar','as','at','au','av','aw','ax','ay','az']        
        columns_index = 0
        position = f'{columns_leters[columns_index]}{row_index}' 
        sheet[position] = f'{datetime.today()}'
        columns_index = 1
        position = f'{columns_leters[columns_index]}{row_index}' 
        sheet[position] = user.user_name
        columns_index = 2
        position = f'{columns_leters[columns_index]}{row_index}'                        
        sheet[position] = user.telegram_user_id
        columns_index = 3
        position = f'{columns_leters[columns_index]}{row_index}' 
        sheet[position] = report_text


    # Создание кнопки для авторизации пользователя
    @staticmethod
    def create_button_auth(telegram_user_id, sso_params, bot, id_chat_group = None):
        try:
            data_state=f'{telegram_user_id}'
            if(id_chat_group is not None):
                data_state = f'{telegram_user_id}{id_chat_group}'#-12123243214
            # Генерируем ссылку для аутентификации
            auth_url = f'{sso_params.auth_endpoint}?response_type=code&client_id={sso_params.client_id}&redirect_uri={sso_params.redirect_uri}&scope=openid attribute&state={data_state}'
            
            # Создаем InlineKeyboardMarkup с кнопкой для аутентификации
            keyboard = types.InlineKeyboardMarkup()        
            auth_button = types.InlineKeyboardButton(text='Аутентификация', url=auth_url)
            keyboard.add(auth_button)
            # Отправляем пользователю сообщение с кнопкой для аутентификации
            bot.send_message(telegram_user_id, 'Для аутентификации нажмите кнопку ниже:', reply_markup=keyboard)
        except (Exception)as error:
            print(f'{datetime.today()} ERROR in bot_utils_static.create_button_auth()   description: {error}  ')

    ## Проверка пользователя, установка confirmed/not_confirmed
    ## Если статус confirmed или not_confirmed есть три дня пройти аутентификацию, делаем refrech token
    @staticmethod
    def check_user_set_not_confirmed(cursor, user, sso_params, bot, message_text, count_days_for_ban):         
        try:
            reference_today = datetime.today().date() - timedelta(days = count_days_for_ban) #3 day ago 
            if((user.status == User_status.confirmed.value and user.date_in >= reference_today) or user.status == User_status.not_confirmed.value):                
                response = bot_utils_static.get_refresh_token(user.refresh_token, sso_params)
                if response.status_code == 200:
                    user.employee_token = response.json().get("access_token")
                    user.expires_in = response.json().get("expires_in")
                    user.refresh_token = response.json().get("refresh_token")  
                    user.date_in = datetime.today().date()
                    user.status = User_status.confirmed.value                    
                    bot_utils_static.set_chat_administrator_custom_title(cursor, user, bot)
                else:
                    user.status = User_status.not_confirmed.value
                    text_name = f'Привет {user.user_name}.'
                    if(user.user_name is None):
                        text_name = f'Привет.'
                    ban_day = user.date_in + timedelta(days = count_days_for_ban) 
                    text_day = ban_day.strftime("%d/%m/%Y")
                    text = f'{text_name} {message_text}. Если Вы этого не сделаете, {text_day} вы будете удалены из групп.'                
                    bot.send_message(user.telegram_user_id, text)
                    bot_utils_static.create_button_auth(user.telegram_user_id, sso_params, bot)
            return user
        except Exception as error:            
            print(f'{datetime.today()} ERROR in bot_utils_static.check_user_set_not_confirmed()   description: {error}  ')
            return user

    ## Проверка пользователя, установка baned
    ## Если статус not_confirmed и прошло больше 3 дней устанавливаем статус baned
    @staticmethod
    def check_user_set_baned(cursor, user, bot, count_days_for_ban):
        try:
            reference_for_ban_today = datetime.today().date() - timedelta(days = count_days_for_ban)# 3 day ago
            if(user.status == User_status.not_confirmed.value and user.date_in < reference_for_ban_today):
                user_chat_group_records = db_utils_static.select_user_chat_group_by_tlg_id(cursor, user.telegram_user_id)
                for row_chat_group in user_chat_group_records: 
                    bot_utils_static.set_admin_or_user(bot, row_chat_group[1], user, False)
                    bot.ban_chat_member(row_chat_group[1], user.telegram_user_id) 
                user.status = User_status.baned.value
            return user
        except Exception as error:
           print(f'{datetime.today()} ERROR in bot_utils_static.check_user_set_baned()   description: {error}  ')
           return user

    ## Проверка пользователя, установка baned_for_ever
    ## Если статус baned и прошло больше 30 дней устанавливаем статус baned_for_ever
    @staticmethod
    def check_user_set_baned_for_ever(cursor, user, bot, count_days_for_ban_for_ever):
        try:
            reference_for_ban_for_ever_today = datetime.today().date() - timedelta(days = count_days_for_ban_for_ever)# 30 day ago
            if(user.status == User_status.baned.value and user.date_in < reference_for_ban_for_ever_today):
                user_chat_group_records = db_utils_static.select_user_chat_group_by_tlg_id(cursor, user.telegram_user_id)
                for row_chat_group in user_chat_group_records: 
                    #bot.unban_chat_member(row_chat_group[1], user.telegram_user_id)
                    bot_utils_static.set_admin_or_user(bot, row_chat_group[1], user, False)
                    bot.ban_chat_member(row_chat_group[1], user.telegram_user_id)
                user.status = User_status.baned_for_ever.value
            return user
        except Exception as error:
            print(f'{datetime.today()} ERROR in bot_utils_static.check_user_set_baned_for_ever()   description: {error}  ')
            return user

    # Общие функции
    # Проверка на групповой чат
    @staticmethod
    def is_group_chat(message):
        if(message.chat.type == 'group' or message.chat.type == 'supergroup'):
            return True
        else:
            return False

    # Проверка на индивидуальный чат с ботом
    @staticmethod
    def is_private_chat(message):
        if(message.chat.type == 'private'):
            return True
        else:
            return False

    # Проверка пользователя, что НЕ админ
    @staticmethod
    def is_not_admin(message, comands_admins):
        return bot_utils_static.is_not_admin_by_id(message.from_user.id, comands_admins)

    @staticmethod
    def is_not_admin_by_id(id, comands_admins):
        if(id not in comands_admins):
            return True
        else:
            return False


    # Получение access_token
    @staticmethod
    def get_access_token(code, sso_params):
        token_data = {
            "grant_type": "authorization_code",
            "code": code,
            "client_id": sso_params.client_id,
            "client_secret": sso_params.client_secret,
            "redirect_uri": sso_params.redirect_uri     
            
        }        
        return requests.post(sso_params.token_endpoint, data=token_data)

    # Получение refresh_token
    @staticmethod
    def get_refresh_token(refresh_token, sso_params):
        token_data = {
            "grant_type": "refresh_token",        
            "client_id": sso_params.client_id,
            "client_secret": sso_params.client_secret,
            "redirect_uri": sso_params.redirect_uri,
            "refresh_token": refresh_token
        }
        return requests.post(sso_params.token_endpoint, data=token_data)

    # Запись пользователя из БД переводим в класс
    @staticmethod
    def get_user_from_record(user_record): 
        try:
            telegram_user_id = user_record[0]
            user_name = user_record[1]
            employee_token = user_record[2]
            expires_in = user_record[3]
            date_in = user_record[4]
            status = user_record[5]
            is_bot = user_record[6]
            refresh_token = user_record[7]
            is_on_service=user_record[8]
            employeeguid = user_record[9]
            user = User(telegram_user_id, status, is_bot, user_name, employee_token, expires_in, refresh_token, date_in,is_on_service,employeeguid)        
            return user
        except Exception as error:
            print(f'{datetime.today()} ERROR in bot_utils_static.get_user_from_record()   description: {error}  ')
            return None
  
    # Устанавливаем участника группы админом/пользователем - is_admin=True/is_admin_False
    @staticmethod
    def set_admin_or_user(bot, chat_id, user, is_admin):        
         bot.promote_chat_member(
                chat_id = chat_id,
                user_id = user.telegram_user_id,
                can_change_info = is_admin,
                can_post_messages = is_admin,
                can_edit_messages = is_admin,
                can_delete_messages = is_admin,
                can_invite_users = is_admin,
                can_restrict_members = is_admin,
                can_pin_messages = is_admin,
                can_promote_members = is_admin,
                can_manage_chat = is_admin,
                can_manage_video_chats = is_admin, 
                can_manage_topics = False
                )
    

    @staticmethod
    def set_chat_administrator_custom_title(cursor, user, bot):
        # esta pendiente de hacer
        # собирается сделать
        try:
            user = db_utils_static.select_user_by_id(cursor, user.telegram_user_id)
            user_chat_group_records = db_utils_static.select_user_chat_group_by_tlg_id(cursor, user.telegram_user_id)
            for row_chat_group in user_chat_group_records:
                if(bot.get_chat_member(row_chat_group[1], user.telegram_user_id).status != "creator"):
                    bot_utils_static.set_admin_or_user(bot, row_chat_group[1], user, False)
                    bot_utils_static.set_admin_or_user(bot, row_chat_group[1], user, True)
                    full_name = user.user_name.split(" ")
                    title_name = f'{full_name[0]}'
                    if(len(full_name) > 1):
                        title_name= f'{full_name[0]} {full_name[1]}'                          
                    bot.set_chat_administrator_custom_title(row_chat_group[1], user.telegram_user_id, title_name)#(chat_id,user_id,custon_title)
        except Exception as error:
            print(f'{datetime.today()} ERROR in bot_utils_static.set_chat_administrator_custom_title()   description: {error}  ')
            return False
        return True
                
    # Получаем параметр из команды
    # Например /unban-1234456 функция вернет 1234456
    @staticmethod
    def get_param_from_message(message): 
        splitter_on_command='-' 
        words = message.text.split(splitter_on_command)    
        
        return words[1].replace(" ", "") 


    # if you want to downgrade   on_diuty =False,
    @staticmethod
    def promote_user_is_on_service(cursor, bot, message, is_on_service):
        try:    
            if(message.chat.type != 'private'):
                bot.delete_message(message.chat.id, message.message_id)
                return False
            user_id = bot_utils_static.get_param_from_message(message)        
            user = db_utils_static.select_user_by_id(cursor, user_id)
            if(user == False):
                bot.send_message(message.chat.id,f'Пользователь не найден')
                return False
            user.is_on_service = is_on_service
            db_utils_static.update_user_db(cursor, user)
            bot.send_message(message.chat.id,f'У пользователя {user.telegram_user_id} {user.user_name} изменен признак is_on_service={is_on_service} "Дежурный администратор"')
            return True
        except Exception as error:
            print(f'{datetime.today()} ERROR in bot_utils_static.promote_user_is_on_service()   description: {error}  ')
            return False


    # Разблокировка/unban пользователя во всех группах
    @staticmethod
    def unban_user_in_all_groups(cursor, bot, user):
        try:
            user_chat_group_records = db_utils_static.select_user_chat_group_by_tlg_id(cursor, user.telegram_user_id)
            for row_chat_group in user_chat_group_records: 
                user_telegram = bot.get_chat_member(row_chat_group[1],user.telegram_user_id)                    
                if(user_telegram is not None) and ( user_telegram.status != 'creator'):  
                    bot.unban_chat_member(row_chat_group[1], user.telegram_user_id)
                    #bot_utils_static.set_user_to_admin_in_chatgoup(bot,row_chat_group[1],user)
                    bot_utils_static.set_admin_or_user(bot, row_chat_group[1], user, True)
            return True
        except Exception as error:
            print(f'{datetime.today()} ERROR in bot_utils_static.unban_user_in_all_groups()   description: {error}  ')
            return False


    # Разблокировка/unban пользователя
    # Если пользователь за 3 дня не прошел аутентификацию, стафим статус notconfirmed и даем ему еще 3 дня
    @staticmethod
    def unban_user(cursor, bot, message):
        try:
            if(message.chat.type != 'private'):
                bot.delete_message(message.chat.id, message.message_id)
                return False

            user_id = bot_utils_static.get_param_from_message(message)

            user = db_utils_static.select_user_by_id(cursor, user_id)
            if(user == False):
                bot.send_message(message.chat.id,f'Пользователь не найден')
                return False

            bot_utils_static.unban_user_in_all_groups(cursor, bot, user)

            user.status = User_status.not_confirmed.value
            user.date_in = datetime.today().date()
            db_utils_static.update_user_db(cursor, user)
            bot.send_message(message.chat.id,f'Пользователь {user.telegram_user_id} {user.user_name} разблокирован')
            return True
        except Exception as error:
            print(f'{datetime.today()} ERROR in bot_utils_static.unban_user()   description: {error}  ')
            return False
        
    @staticmethod    
    def ban_user(cursor, bot,user):
        try:
            user_chat_group_records = db_utils_static.select_user_chat_group_by_tlg_id(cursor, user.telegram_user_id)
            for row_chat_group in user_chat_group_records: 
                user_telegram = bot.get_chat_member(row_chat_group[1],user.telegram_user_id)                    
                if(user_telegram is not None) and ( user_telegram.status != 'creator'):  
                    bot.unban_chat_member(row_chat_group[1], user.telegram_user_id)
                    bot.ban_chat_member(row_chat_group[1], user.telegram_user_id)
            user.status=User_status.baned_for_ever
            db_utils_static.update_user_db(cursor, user)        
            return True
        except Exception as error:
            print(f'{datetime.today()} ERROR in bot_utils_static.ban_user()   description: {error}  ')
            return False
   
   
        
        
    
        