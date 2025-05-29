from pprint import pprint
from tkinter.tix import ROW
from decorators import data_handler, bot_feedback
from telebot import types
from datetime import date
import re


# Метод для запроса ввода контакта (если следующего контакта нет - их заполнение завершается)
@bot_feedback
@data_handler
def send_contact_request(cursor, chat_id, user_id, from_button=False):
    if from_button:
        get_user_agreement = f'SELECT agreement FROM users WHERE user_id = {user_id}'
        cursor.execute(get_user_agreement)
        user_agreement = cursor.fetchall()[0]        
        if user_agreement != 1:
            update_user_agreement = f'UPDATE users SET agreement = 1 WHERE user_id = {user_id}'
            cursor.execute(update_user_agreement)

            reset_contact_index = f'''
                UPDATE users
                SET contact_index = 1 WHERE user_id = {user_id}
                '''
            cursor.execute(reset_contact_index)
        else:
            return

    #get_contact_index = f'SELECT contact_index FROM users WHERE user_id = {user_id}'
    get_contact_index = '''SELECT contact_index FROM users WHERE user_id = ''' + user_id   
    cursor.execute(get_contact_index)  
    contact_index = cursor.fetchone()[0]
    
    if contact_index != -1:
        get_contact_message = '''SELECT contact_message FROM contacts_structure WHERE contact_id = ''' +str(contact_index) +''';'''
        cursor.execute(get_contact_message)        
        contact_message = cursor.fetchone()
        to_send = contact_message

        to_feedback = {'send_message': {'chat_id': chat_id, 'text': to_send}}
        return to_feedback

    to_send = f'Теперь предлагаю пошевелить мозгами и немного поиграть.\nПогнали!'

    keyboard = types.InlineKeyboardMarkup()
    keyboard.add(types.InlineKeyboardButton('Начать тест', callback_data='test_start'))

    to_feedback = {'send_message': {'chat_id': chat_id, 'text': to_send, 'reply_markup': keyboard}}
    return to_feedback


@data_handler
def set_contacts(cursor, user_id, contact, from_start=False):
    if from_start:
        delete_answers = f'DELETE FROM users_answers WHERE user_id = {user_id}'
        cursor.execute(delete_answers)

        delete_open_answers = f'DELETE FROM users_open_answers WHERE user_id = {user_id}'
        cursor.execute(delete_open_answers)

        #delete_contacts = f'DELETE FROM users_contacts WHERE user_id = {user_id}'
        #cursor.execute(delete_contacts)

        #insert or update users
        #insert_user = f'INSERT OR REPLACE INTO users (user_id, contact_index) VALUES ({user_id}, -1)'
        #cursor.execute(insert_user)
        select_user =f'SELECT * FROM users WHERE user_id={user_id}'
        cursor.execute(select_user)
        rows = cursor.rowcount
        if rows < 1 :
            today = date.today();
            insert_user = '''INSERT INTO users (user_id, contact_index,entry_date) VALUES (%s, %s, %s);'''
            datain=(user_id,-1,today)
            cursor.execute(insert_user,datain)
        else:
            today = date.today();
            update_user=''' UPDATE users SET  contact_index = %s, entry_date = %s	WHERE user_id = %s; '''
            datain=(-1,today,user_id)
            cursor.execute(update_user,datain)

        #insert or update users_contacts
        #insert_first_contacts = f'INSERT OR REPLACE INTO users_contacts VALUES ({user_id}, "Telegram", "{contact}")'
        #cursor.execute(insert_first_contacts)
        select_users_contacts =f'SELECT * FROM users_contacts WHERE user_id={user_id}  '
        cursor.execute(select_users_contacts)
        rows = cursor.rowcount
        if rows < 1 :
            insert_first_contacts = '''INSERT INTO users_contacts (user_id, contact_name, contact_value) VALUES  (%s, %s, %s) ;'''
            datain=(user_id,"Telegram",contact)
            cursor.execute(insert_first_contacts,datain)
        else:
            update_users_contacts='''UPDATE users_contacts	SET  contact_value = %s	WHERE user_id = %s  and contact_name = %s; '''
            datain=(contact,user_id,"Telegram")
            cursor.execute(update_users_contacts,datain)   

        return

    get_contact_index = f'SELECT contact_index FROM users WHERE user_id = {user_id}'
    cursor.execute(get_contact_index)
    contact_index = cursor.fetchone()[0]

    get_contact_name = f'SELECT contact_name FROM contacts_structure WHERE contact_id = {contact_index}'
    cursor.execute(get_contact_name)
    contact_name = cursor.fetchone()

    if not contact_name:
        return
    else:
        contact_name = contact_name[0]

    get_contacts_count = f'SELECT COUNT (*) FROM contacts_structure'
    cursor.execute(get_contacts_count)
    contacts_count = cursor.fetchone()[0]

    get_validation = f'SELECT reg_exp FROM contacts_structure WHERE contact_id = {contact_index}'
    cursor.execute(get_validation)
    validation = cursor.fetchone()[0]

    if re.match(validation, contact):
        if contact_index <= contacts_count:
            #insert_contact = f'INSERT OR REPLACE INTO users_contacts VALUES ({user_id}, "{contact_name}", "{contact}")'
            select_users_contacts= f'SELECT all FROM users_contacts WHERE user_id =%s and contact_name =%s '    
            datain=(user_id,contact_name)
            cursor.execute(select_users_contacts,datain)              
            ro=cursor.rowcount
            if ro >0:
                update_users_contacts = f'UPDATE users_contacts SET  contact_value=%s	WHERE  user_id =%s and contact_name =%s'
                datain=(contact,user_id,contact_name)
                cursor.execute(update_users_contacts,datain)
            else:
                insert_users_contacts = f'INSERT INTO users_contacts (user_id, contact_name, contact_value) VALUES (%s, %s, %s)'
                datain=(user_id,contact_name,contact)
                cursor.execute(insert_users_contacts,datain)            

        next_contact_index = contact_index + 1 if contact_index + 1 <= contacts_count else -1

        update_contact_index = f'UPDATE users SET contact_index = {next_contact_index} WHERE user_id = {user_id}'
        cursor.execute(update_contact_index)

        return True

    return False


@data_handler
def user_is_leaving_contacts(cursor, user_id):
    datain=(user_id,-1)
    cursor.execute('SELECT * FROM users  WHERE ( user_id =%s)  AND (contact_index != %s)',datain)     
    rows = cursor.rowcount
    return rows > 0
    #return cursor.execute(f'SELECT COUNT (*) FROM users WHERE user_id = {user_id} AND contact_index != -1').fetchone()[0] > 0
