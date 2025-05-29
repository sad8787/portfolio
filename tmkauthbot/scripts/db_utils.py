#from user_status import User_status
from user import User
from datetime import datetime

class db_utils_static:
    # Добавляем запись в таблицу users
    @staticmethod
    def insert_user_db(cursor, user):
        insert_user=f'''INSERT INTO users(telegram_user_id, user_name, employee_token, expires_in, date_in, status, is_bot, is_on_service, refresh_token,employeeguid) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s);'''
        data_insert=(user.telegram_user_id, user.user_name, user.employee_token, user.expires_in, user.date_in, user.status, user.is_bot, user.is_on_service, user.refresh_token, user.employeeguid)
        cursor.execute(insert_user, data_insert)
        #if(user.user_name == None and user.employee_token == None and user.expires_in == None and user.refresh_token == None):
          #  insert_user=f'''INSERT INTO users(telegram_user_id, date_in, status, is_bot, is_on_service) VALUES (%s, %s, %s, %s, %s);'''
          #  data_insert=(user.telegram_user_id, user.date_in, user.status.value, user.is_bot, user.is_on_service)
        #else:
          #  insert_user=f'''INSERT INTO users(telegram_user_id, user_name, employee_token, expires_in, date_in, status, is_bot, is_on_service, refresh_token,employeeguid) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s);'''
          #  data_insert=(user.telegram_user_id, user.user_name, user.employee_token, user.expires_in, user.date_in, user.status, user.is_bot, user.is_on_service, user.refresh_token, user.employeeguid)
        #cursor.execute(insert_user, data_insert)


    # Добавляем запись в таблицу user_chat_group
    @staticmethod
    def insert_user_chat_id_db(cursor, user_chat_group):
        insert_user_chat_id= f'''INSERT INTO user_chat_group(telegram_user_id, chat_id) VALUES (%s, %s); '''
        data_insert=(user_chat_group.telegram_user_id, user_chat_group.chat_id)
        cursor.execute(insert_user_chat_id, data_insert)


    # Получаем всех пользователей в таблице users
    @staticmethod
    def select_all_users(cursor):
        sellect_user = f'''SELECT * FROM users '''
        cursor.execute(sellect_user)
        return cursor.fetchall()


    # Получаем всех пользователей в таблице users, но которые есть хотябы в одной группе
    @staticmethod
    def select_all_users_with_group_and_not_duty(cursor):
        sellect_user = f'''SELECT users.telegram_user_id, users.user_name, users.employee_token, users.expires_in, users.date_in, users.status, users.is_bot, users.refresh_token, users.is_on_service,users.employeeguid	
	        FROM users left join user_chat_group on users.telegram_user_id = user_chat_group.telegram_user_id
            WHERE users.is_on_service=False and users.is_bot=False
	        GROUP BY users.telegram_user_id, users.user_name, users.employee_token, users.expires_in, users.date_in, users.status, users.is_bot, users.refresh_token, users.is_on_service,users.employeeguid	
	        HAVING COUNT(user_chat_group.chat_id)>=0'''
        cursor.execute(sellect_user)
        return cursor.fetchall()

 
    # Получем всех пользователей группы
    @staticmethod
    def select_user_by_chat_id(cursor, chat_id):
        select_user_in_group_sql = f'''SELECT 
        users.telegram_user_id,
        users.user_name,
        users.employee_token,
        users.expires_in,
        users.date_in,
        users.status,
        users.is_bot,
        users.refresh_token, 
        users.is_on_service,
        users.employeeguid
                    FROM users
                    INNER JOIN user_chat_group ON users.telegram_user_id=user_chat_group.telegram_user_id
                    WHERE user_chat_group.chat_id = {chat_id};'''
        cursor.execute(select_user_in_group_sql)
        return cursor.fetchall()


    # Получаем группы юзера в таблице user_chat_group
    @staticmethod
    def select_user_chat_group_by_tlg_id(cursor, telegram_user_id):
        select_user_chat_group = f'''SELECT telegram_user_id, chat_id FROM user_chat_group WHERE telegram_user_id ={telegram_user_id} ; '''
        cursor.execute(select_user_chat_group)
        return cursor.fetchall()


    # Получаем все группы юзера в таблице user_chat_group
    @staticmethod
    def select_all_user_chat_group(cursor):
        select_user_chat_group = f'''SELECT telegram_user_id, chat_id FROM user_chat_group; '''
        cursor.execute(select_user_chat_group)
        return cursor.fetchall()


    # Обновляем юзера в таблице users
    @staticmethod        
    def update_user_db(cursor, user):        
        user.date_in = datetime.today().date()
        try:
            update_user = f'''UPDATE users SET employee_token=%s, user_name=%s, expires_in=%s, date_in=%s, status=%s, refresh_token=%s, is_on_service=%s, employeeguid=%s WHERE telegram_user_id=%s;''' 
            data_insert = (user.employee_token, user.user_name, user.expires_in, user.date_in, user.status, user.refresh_token, user.is_on_service,user.employeeguid, user.telegram_user_id)
            cursor.execute(update_user, data_insert)
        except  Exception as error:
            print(f'{user.date_in} Something went wrong in db_utils_static.update_user_db  :{error}')
     


    @staticmethod
    def select_user_by_id(cursor, telegram_user_id):
        check_if_user_exists = f'''SELECT * FROM users WHERE telegram_user_id={telegram_user_id}'''
        cursor.execute(check_if_user_exists)
        user_exists = cursor.fetchone()
        if user_exists is not None:  
            user=User(user_exists[0],user_exists[5], user_exists[6])
            user.telegram_user_id = user_exists[0]
            user.user_name = user_exists[1]
            user.employee_token = user_exists[2]
            user.expires_in = user_exists[3]
            user.date_in = user_exists[4]
            user.status = user_exists[5]
            user.is_bot = user_exists[6]
            user.refresh_token = user_exists[7]
            user.is_on_service = user_exists[8]
            user.employeeguid = user_exists[9]
            return user
        return False


    # Проверяем пользователя в таблице user если нет добавляем
    @staticmethod
    def check_user_exists(cursor, user):
        check_if_user_exists = f'''SELECT * FROM users WHERE telegram_user_id={user.telegram_user_id}'''
        cursor.execute(check_if_user_exists)
        user_exists = cursor.fetchone()
        if user_exists is None:
            db_utils_static.insert_user_db(cursor, user)
        else:
            user.telegram_user_id = user_exists[0]
            user.user_name = user_exists[1]
            user.employee_token = user_exists[2]
            user.expires_in = user_exists[3]
            user.date_in = user_exists[4]
            user.status = user_exists[5]
            user.is_bot = user_exists[6]
            user.refresh_token = user_exists[7]
            user.is_on_service = user_exists[8]
            user.employeeguid = user_exists[9]
        return user


    # Проверяем пользователя в таблице user_chat_group если нет добавляем
    @staticmethod
    def check_user_chat_group_exists(cursor, user_chat_group):
        check_if_user_chat_group_exists = f'''SELECT *	FROM user_chat_group  WHERE telegram_user_id={user_chat_group.telegram_user_id} AND chat_id ={user_chat_group.chat_id}'''
        cursor.execute(check_if_user_chat_group_exists)
        user_chat_group_exists = cursor.fetchone()
        if user_chat_group_exists is None:  
            db_utils_static.insert_user_chat_id_db(cursor, user_chat_group)

    @staticmethod
    def delete_user_from_chat_group(cursor,chat_id,user_id): 
        result=True
        try:
            sql_delete_user_from_chat_group=f'''DELETE FROM user_chat_group WHERE telegram_user_id={user_id} and chat_id={chat_id} '''
            cursor.execute(sql_delete_user_from_chat_group)
        except  Exception as error:
                print(f"Something went wrong in db_utils_static.delete_user_from_group  :{error}")
                result=False
        finally:
             return result

    @staticmethod
    def delete_chat_group(cursor,chat_id): 
        result=True
        try:
            sql_delete_chat_group=f'''DELETE FROM user_chat_group WHERE chat_id={chat_id} '''
            cursor.execute(sql_delete_chat_group)
        except  Exception as error:
                print(f"Something went wrong in db_utils_static.delete_chat_group  :{error}")
                result=False
        finally:
             return result

   


   

