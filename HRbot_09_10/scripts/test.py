#from asyncio.windows_events import NULL
import random
import os
from telebot import types
from decorators import data_handler, bot_feedback


dir_path = os.path.dirname(os.path.realpath(__file__))

# Проверка на активность теста
@data_handler
def test_is_active(cursor, user_id):
    try:        
        active_check = f'''SELECT is_test_active FROM users WHERE user_id = {user_id}'''    
        cursor.execute(active_check)         
        rows=cursor.rowcount  
        if rows<1:
            return False
        is_active = cursor.fetchall()[0]
        if is_active[0] is None:
            return False           
        return is_active[0] ==1 
    except (Exception) as error:          
        return False
    

@data_handler
def get_questions_by_block_id(cursor, block_id):
    get_questions_ids = f'SELECT question_id FROM questions WHERE block_id = {block_id} order by question_id'
    cursor.execute(get_questions_ids)
    resultQuery=cursor.fetchall()    
    questions_ids = [row[0] for row in resultQuery]    
    return questions_ids


@data_handler
def get_random_block_id(cursor):    
    get_blocks_ids = f'SELECT block_id FROM questions'
    cursor.execute(get_blocks_ids)
    result=cursor.fetchall()    
    blocks_ids = [row[0] for row in result]
    blocks_ids = list(set(blocks_ids))
    random_block_id = random.choice(blocks_ids)
    return random_block_id


@data_handler
def start_test_block(cursor, user_id):    
    block_id = get_random_block_id()    
    set_block_id = f'UPDATE users SET block_id = {block_id} WHERE user_id = {user_id}'
    cursor.execute(set_block_id)
    questions = get_questions_by_block_id(block_id)
    questions_order = [str(q) for q in questions]   
    questions_order = ' '.join(questions_order)    
    update_questions_order = f'UPDATE users SET questions_order = %s  WHERE user_id = %s'
    datain=(questions_order,user_id)
    cursor.execute(update_questions_order,datain)
    update_question_index = f'UPDATE users SET question_index = -1 WHERE user_id = {user_id}'
    cursor.execute(update_question_index)
    set_test_active = f'UPDATE users SET is_test_active = 1 WHERE user_id = {user_id}'
    cursor.execute(set_test_active)


@data_handler
def get_question_id_by_index(cursor, user_id, index):   
    get_questions_order = f'SELECT questions_order FROM users WHERE user_id = {user_id}'
    cursor.execute(get_questions_order)
    questions_order = cursor.fetchall()[0][0]
    if questions_order == None:
        return None
    questions_order = questions_order.split()
    if 0 <= index < len(questions_order):
        return questions_order[index]
    else:
        return None


@bot_feedback
@data_handler
def send_question(cursor, chat_id, user_id):    
    get_question_index = f'SELECT question_index FROM users WHERE user_id = {user_id}'
    cursor.execute(get_question_index)
    question_index = cursor.fetchone()[0]
    question_index += 1
    update_question_index = f'UPDATE users SET question_index = {question_index} WHERE user_id = {user_id}'
    cursor.execute(update_question_index)
    question_id = get_question_id_by_index(user_id, question_index)

    if not question_id:
        set_test_inactive = f'''
            UPDATE users
            SET is_test_active = 0 WHERE user_id = {user_id}
            '''
        cursor.execute(set_test_inactive)

        end_test_block(chat_id, user_id)

        to_send = 'С тобой было приятно общаться 🙂'
        to_feedback = {'send_message': {'chat_id': chat_id, 'text': to_send}}
        return to_feedback
    get_question_info = f'SELECT question, question_type, image FROM questions WHERE question_id = {question_id}'
    cursor.execute(get_question_info)
    question, question_type, image_path = cursor.fetchone()
    to_send = question
    to_feedback = {'send_message': {'chat_id': chat_id, 'text': to_send}}

    if question_type == 'close':
        keyboard = types.InlineKeyboardMarkup()
        get_answers_info = f'SELECT answer_id, answer FROM answers WHERE question_id = {question_id}'
        cursor.execute(get_answers_info)
        answers_info = cursor.fetchall()        
        random.shuffle(answers_info)
        for answer_id, answer in answers_info:
            button = types.InlineKeyboardButton(answer,
                                                callback_data=f'test_{answer_id}')
            keyboard.add(button)

        to_feedback = {'send_message': {'chat_id': chat_id, 'text': to_send, 'reply_markup': keyboard}}
    if image_path:        
        image = open(f'{dir_path}//..//resources//{image_path}', 'rb')
        to_feedback['send_photo'] = {'chat_id': chat_id, 'photo': image}

    return to_feedback


@bot_feedback
@data_handler
def handle_choice(cursor, chat_id, user_id, callback):   
    get_question_index = f'SELECT question_index FROM users WHERE user_id = {user_id}'
    cursor.execute(get_question_index)
    question_index = cursor.fetchone()[0]
    question_id = get_question_id_by_index(user_id, question_index)
    picked_choice = callback.data[callback.data.find('_') + 1:]
    get_right_answer_id = f'SELECT right_answer FROM questions WHERE question_id = {question_id}'
    cursor.execute(get_right_answer_id)
    right_answer_id = cursor.fetchone()[0]    
    if right_answer_id == picked_choice:
        is_correct = 1 #True
        to_send = 'Правильно :)'
    else:
        is_correct = 0 #False
        to_send = 'Неправильно :('  
    check_if_answer_exists = f'SELECT * FROM users_answers ' \
                             f'WHERE user_id = {user_id} AND question_id = {question_id}'
    cursor.execute(check_if_answer_exists)
    answer_exists = cursor.fetchone()

    if not answer_exists:
        add_answer = f'INSERT INTO users_answers VALUES (%s, %s,%s)'
        data_int=(user_id,question_id,is_correct)
        cursor.execute(add_answer,data_int)

    to_feedback = {'send_message': {'chat_id': chat_id, 'text': to_send},
                   'delete_message': {'chat_id': chat_id, 'message_id': callback.message.id}}
    return to_feedback


@bot_feedback
@data_handler
def handle_text_answer(cursor, chat_id, user_id, answer):    
    get_question_index = f'SELECT question_index FROM users WHERE user_id = {user_id}'
    cursor.execute(get_question_index)
    question_index = cursor.fetchone()[0]    
    question_id = get_question_id_by_index(user_id, question_index)    
    get_right_answer = f'SELECT right_answer FROM questions WHERE question_id = {question_id}'
    cursor.execute(get_right_answer)
    right_answer = cursor.fetchone()[0]    
    if not right_answer:        
        add_open_answer = f'INSERT INTO users_open_answers VALUES (%s,%s,%s)'
        data_in=(user_id,question_id,answer)
        cursor.execute(add_open_answer,data_in)
        return

    if right_answer.replace(' ', '').lower() == answer.replace(' ', '').lower():
        is_correct = 1 #True
        to_send = 'Правильно :)'
    else:
        is_correct = 0 #False
        to_send = 'Неправильно :('
   
    check_if_answer_exists = f'SELECT *	FROM users_answers WHERE user_id =%s AND question_id =%s'
    data_in=(user_id,question_id)
    cursor.execute(check_if_answer_exists, data_in)
    answer_exists = cursor.fetchone()
    if not answer_exists:
        add_answer = f'INSERT INTO users_answers VALUES (%s, %s,%s)'
        data_in=(user_id,question_id,is_correct)
        cursor.execute(add_answer,data_in)
    to_feedback = {'send_message': {'chat_id': chat_id, 'text': to_send}}
    return to_feedback


@data_handler
def get_correct_answers_count(cursor, user_id):    
    get_incorrect_answers_count = f'SELECT COUNT (*) FROM users_answers WHERE user_id = {user_id} AND is_correct = 1'
    cursor.execute(get_incorrect_answers_count)
    correct_answers_count = cursor.fetchone()[0]
    return correct_answers_count


@bot_feedback
@data_handler
def end_test_block(cursor, chat_id, user_id):   
    to_feedback = {}
    correct_answers_count = get_correct_answers_count(user_id)
    get_block_data = f'SELECT correct_answers_{correct_answers_count}, image, offer FROM block_end_data'
    cursor.execute(get_block_data)
    message, image_path, offer = cursor.fetchall()[0]
    to_send = message
    if correct_answers_count >= 3:        
        image = open(f'{dir_path}//..//resources//{image_path}', 'rb')
        to_feedback['send_photo'] = {'chat_id': chat_id, 'photo': image}
        to_send += '\n' + offer
    to_feedback['send_message'] = {'chat_id': chat_id, 'text': to_send}
    return to_feedback


@data_handler
def question_is_open(cursor, user_id):    
    get_question_index = f'SELECT question_index FROM users WHERE user_id = {user_id}'
    cursor.execute(get_question_index)
    question_index = cursor.fetchone()[0]   
    if question_index is None:
        return True        
    else:
        question_id = get_question_id_by_index(user_id, question_index)        
        if question_id is None:
            return False
        select_question_type=f'SELECT question_type FROM questions WHERE question_id = %s' 
        data_in=(question_id,)
        cursor.execute(select_question_type,data_in)
        is_question_open = cursor.fetchone()[0]       
        return is_question_open == 'open'
  
