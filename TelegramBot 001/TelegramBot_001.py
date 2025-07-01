from ast import Await
import signal
import sys
import asyncio
import threading
import time
import random
import json
from datetime import datetime

from db.models import Base, Client, Message, Account, Summary, Session as DBSessionModel, Group, ClientGroupLink, Log
from db.init import crear_tablas, create_session

import config
from telegram import Update
from telegram.ext import ApplicationBuilder, CommandHandler, MessageHandler, ContextTypes, filters

#________________________IA_____________________________________

from openai import OpenAI
client_openai = OpenAI(api_key=config.OPENAI_API_KEY)

#____________________pronts___________________________
#creative_prompts, curious_prompts, humorous_prompts, professional_prompts, analytical_prompts
creative_prompts  = [
    "Могу ли я объединить сторителлинг и личный брендинг для цифровых продаж? Есть ли у вас какие-либо советы?",
    "Я горжусь — кто-то только что сказал мне, что доверяет мне благодаря тому, как я представил себя в сети",
    "Какая прекрасная сессия. Изучение эмоциональных триггеров в продажах заставило меня много размышлять"
    
]
curious_prompts = [
                "I'm totally new here! What's the first thing I should do to start selling online?",
                "Wow, I just talked to my first potential client. I know it’s small, but I’m so excited!",
                "I never thought digital sales could be this interesting. I really liked today’s tips about persuasion techniques!"
               
            ]
humorous_prompts = [
                "Я здесь новичок! Что мне нужно сделать в первую очередь, чтобы начать продавать онлайн?",
                "Я никогда не думал, что цифровые продажи могут быть такими увлекательными. Мне очень понравились сегодняшние советы по методам убеждения! 😎"
                
            ]
professional_prompts = [
                "What’s the fastest way to scale digital sales to five figures a month?",
                "Just closed my first deal using the cold DM strategy from last week’s session!",
                "The framework on customer pain points was 🔥. Applied it today and got great responses."
                
            ]
analytical_prompts = [
    "Может ли кто-нибудь объяснить стратегии цифровых продаж?",
    "Только что провел небольшой A/B-тест на целевых страницах — версия B конвертировалась на 12% лучше!",
    "Оценил подход, основанный на данных, в сегодняшнем тренинге. Хотелось бы больше информации об аналитике."    
     ]


#___________________DB___________________________
from sqlalchemy import create_engine, Column, Integer, String
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker


# Construir la URL desde config.py
db_conf = config.DB_CONFIG
DATABASE_URL = f"postgresql://{db_conf['user']}:{db_conf['password']}@{db_conf['host']}/{db_conf['dbname']}"
engine = create_engine(DATABASE_URL)
Base = declarative_base()
# Crear sesión
Session = sessionmaker(bind=engine)
session = Session()








def stop_bots(signum, frame):
    print("Señal de apagado recibida. Deteniendo bots...")
    for app in apps:
        try:
            asyncio.run(app.shutdown())
        except Exception as e:
            print(f"Error al detener un bot: {e}")
    sys.exit(0)

#______________Telegram Bot Handlers_____________________
#Summarize the text
async def analizar_mensaje(texto: str) -> dict:
    prompt = f"""
    Summarize this text for marketing:
    Text: {texto}
    Return in JSON format: {{'main_idea':..., 'keywords':..., 'tone':..., 'content_type':...}}
    """
    response = client_openai.chat.completions.create( 
        model=config.IA_MODEL, # Or "gpt-3.5-turbo", "gpt-4o"
        messages=[{"role": "user", "content": prompt}],
        response_format={"type": "json_object"} 
    )
    # The response object structure is slightly different too
    return json.loads(response.choices[0].message.content) 

#Generate answer
async def generate_answer(resumen: dict) -> str:
    prompt = f"Generate an engaging comment in {config.LANGUAGE} for a text with this main idea: {resumen['main_idea']} "
    response = client_openai.chat.completions.create( 
        model=config.IA_MODEL,
        messages=[{"role": "user", "content": prompt}]
    )
    # The response object structure is slightly different too
    return response.choices[0].message.content 

#hello
async def mensaje_bienvenida_grupo(nombre_grupo: str) -> str:
    prompt = f"Create a friendly and motivating welcome message for a group called {nombre_grupo} about digital sales training."
    response = client_openai.chat.completions.create(
        model=config.IA_MODEL,
        messages=[{"role": "user", "content": prompt}]
    )
    # Access the content using .content attribute, not dictionary key
    return response.choices[0].message.content 

#Fake student 
async def fake_student(prompts:list[str] = [],texto: str='') -> list:    
    mensajes = []
    if not prompts or prompts==[]:  # If no prompts provided, use default ones
        prompts = professional_prompts
    resumen = await analizar_mensaje(texto)
    x=f"Generate an engaging comment in {config.LANGUAGE} for a text with this main idea: {resumen['main_idea']} "  
   
    texto: str
    for prompt in prompts:
        prompt+= f" {x}"  # Append the main idea to each prompt
        response = client_openai.chat.completions.create( 
            model=config.IA_MODEL,
            messages=[{"role": "user", "content": prompt}]
        )
        # Access the content using .content attribute, not dictionary key
        mensajes.append(response.choices[0].message.content) 
    return mensajes



async def mensaje_handler(update: Update, context: ContextTypes.DEFAULT_TYPE):
    # Initialize bd_connection to None in case an error occurs before it's assigned
    
    bd_connection = None
    try:
        # Ignore messages without text or from non-users
        if not update.message or not update.message.text:
            return

        # Create a new database session (connection) for this request
        bd_connection = session # This correctly creates a session instance

        texto = update.message.text
        resumen = await analizar_mensaje(texto)
        comentario = await generate_answer(resumen)
        student_false= await fake_student(texto = resumen['main_idea'])

        username = update.message.from_user.username
        telegram_user_id = update.message.from_user.id
        

        # Find or create the client
        client = bd_connection.query(Client).filter(Client.username == username).first()
        
        if not client:
            client = Client(client_id=str(telegram_user_id), username=username, status='new', created_at=datetime.utcnow())
            bd_connection.add(client)
            bd_connection.commit() # Commit new client to get its ID immediately
            bd_connection.refresh(client)
        
        # Create and store summary
        summary = Summary(
            source_type='msg',
            main_idea=resumen.get('main_idea', 'N/A'),
            keywords=json.dumps(resumen.get('keywords', [])), # Store list as JSON string
            tone=resumen.get('tone', 'neutral'),
            content_type=resumen.get('content_type', 'text')
        )
        
        bd_connection.add(summary)
        bd_connection.commit() # Commit new client to get its ID immediately
        bd_connection.refresh(summary)
        
        # Commit will happen at the end for all related operations

        # Find or create active session for the client
        session_active = bd_connection.query(DBSessionModel).filter_by(client_id=str(client.id), is_active=True).first()
        
        if not session_active:
            session_active = DBSessionModel(
                client_id=str(client.id),
                is_active=True,
                started_at=datetime.utcnow(),
                last_update=datetime.utcnow()
            )
            
            bd_connection.add(session_active)
            
            # Commit will happen at the end

        # Save the incoming message
        message = Message(
            from_id=str(telegram_user_id),
            to_id=str(context.bot.id), # The bot's own ID
            text=texto,
            timestamp=datetime.utcnow(),
            strategy_used="script",
            client_id=str(client.id)
        )
        
        bd_connection.add(message)
        

        # Log the event
        log_event = Log(
            event_type='message_received',
            details=f"User: @{username} | Text: {texto}",
            created_at=datetime.utcnow()
        )
        
        bd_connection.add(log_event)
        
        # Special handling if the tone is positive: create a group and add fictitious students
        if resumen.get('tone', '').lower() == 'positive':
            group_name = f"Group_{username}_{datetime.utcnow().strftime('%Y%m%d%H%M')}"
            group = Group(name=group_name, created_at=datetime.utcnow(), is_active=True)
            bd_connection.add(group)
            bd_connection.commit() # Commit new group to get its ID
            bd_connection.refresh(group)

            client_group_link = ClientGroupLink(client_id=str(client.id), group_id=str(group.id), joined_at=datetime.utcnow())
            bd_connection.add(client_group_link)
            
            welcome_message = await mensaje_bienvenida_grupo(group_name)
            await update.message.reply_text(f"Great news! You've been added to a training group called **{group_name}**!")
            await update.message.reply_text(welcome_message)

            try:
                admin_chat_id = int(config.ADMIN) # Ensure admin ID is an integer
                await context.bot.send_message(admin_chat_id, f"A new group was created: **{group_name}** with user @{username}")
            except Exception as e:
                print(f"Could not notify admin: {e}")

            # Generate and simulate messages from fictitious students
            fictitious_messages = await fake_student(texto=resumen['main_idea'])
            for idx, fict_msg_text in enumerate(fictitious_messages):
                fictitious_username = f"FictitiousStudent{idx+1}"
                # Find or create fictitious account
                fictitious_account = bd_connection.query(Account).filter_by(telegram_id=fictitious_username).first()
                if not fictitious_account:
                    fictitious_account = Account(
                        telegram_id=fictitious_username,
                        name=fictitious_username,
                        persona_description='Simulated student for group interaction',
                        tone='positive',
                        is_main=False
                    )
                    bd_connection.add(fictitious_account)
                    bd_connection.commit() # Commit to get account ID
                    bd_connection.refresh(fictitious_account)

                # Link fictitious account to the group
                link = ClientGroupLink(client_id=fictitious_account.id, group_id=group.id, joined_at=datetime.utcnow())
                bd_connection.add(link)

                # Save the fictitious message
                fake_message = Message(
                    from_id=0, # Or a dedicated ID for simulated users
                    to_id=context.bot.id, # Bot's ID or the group's ID if sent to group
                    text=fict_msg_text,
                    timestamp=datetime.utcnow(),
                    strategy_used='fictitious',
                    client_id=fictitious_account.id # Associate message with the fictitious client
                )
                bd_connection.add(fake_message)

                # Log the fictitious message
                log_fictitious = Log(
                    event_type='fictitious_message',
                    details=f"[{fictitious_username}]: {fict_msg_text}",
                    created_at=datetime.utcnow()
                )
                bd_connection.add(log_fictitious)

                await update.message.reply_text(f"[{fictitious_username}]: {fict_msg_text}")

        bd_connection.commit() # Final commit for all pending changes in this transaction
        num_random = random.randint(2, 5)            
        time.sleep(num_random)
        await update.message.reply_text(comentario)
       
        for x in student_false:
            num_random = random.randint(4, 10)            
            time.sleep(num_random)
            await context.bot.send_message(chat_id=update.message.chat_id, text = x)
           
        
        
    except Exception as e:
        print(f"An unexpected error occurred in mensaje_handler: {e}")
        if bd_connection: # Only rollback/close if a connection was successfully established
            bd_connection.rollback() # This will now work on the session instance
        await update.message.reply_text("I'm sorry, an error occurred while processing your request. Please try again later.")
    finally:
        if bd_connection: # Ensure bd_connection exists before trying to close it
            bd_connection.close() # This will now work on the session instance
    

async def start(update: Update, context: ContextTypes.DEFAULT_TYPE):
    await update.message.reply_text("¡Hello! ")

async def mensaje_handler_fake_student_1(update: Update, context: ContextTypes.DEFAULT_TYPE):
    
    try:
        chat_type = update.effective_chat.type
        if chat_type in ['group', 'supergroup']:             
            num_random = random.randint(5, 10)            
            time.sleep(num_random)
            chat_id = update.message.chat_id             
            prompt_variantes = {
                "humorous": humorous_prompts,
                "curious": curious_prompts ,
                "creative": creative_prompts
            }

            perfil = random.choice(list(prompt_variantes.keys()))
            prompts = prompt_variantes[perfil]
                      
            texts = await fake_student(prompts,texto = update.message.text)
            for x in texts: 
                num_random = random.randint(3, 5)            
                time.sleep(num_random)
                await context.bot.send_message(chat_id=chat_id, text = x)
        else:
            return
    except Exception as e:
         print(f"An unexpected error occurred in mensaje_handler_fake_student: {e}")


async def mensaje_handler_fake_student_2(update: Update, context: ContextTypes.DEFAULT_TYPE):
    
    try:
        chat_type = update.effective_chat.type
        if chat_type in ['group', 'supergroup']:             
            num_random = random.randint(5, 10)            
            time.sleep(num_random)
            chat_id = update.message.chat_id 
            
            prompt_variantes = {
                "analytical": analytical_prompts,
                "professional": professional_prompts                 
            }
            perfil = random.choice(list(prompt_variantes.keys()))
            prompts = prompt_variantes[perfil]
            texts = await fake_student(prompts,texto=update.message.text)
            for x in texts: 
                num_random = random.randint(4, 10)            
                time.sleep(num_random)
                await context.bot.send_message(chat_id=chat_id, text = x)
        else:
            return
    except Exception as e:
         print(f"An unexpected error occurred in mensaje_handler_fake_student: {e}")

#--------- trhe --------------#

def botBoss():
    asyncio.set_event_loop(asyncio.new_event_loop())  # Create and set a new event loop for this thread
    app = ApplicationBuilder().token(config.TELEGRAM_BOT_TOKEN).build()
    app.add_handler(CommandHandler("start", start))
    app.add_handler(MessageHandler(filters.TEXT & ~filters.COMMAND, mensaje_handler))
    print("Bot de Telegram AI iniciado...")
    app.run_polling()


def tarea_fake_student_1(token, name):
    print(f"Starting {name} ")
    asyncio.set_event_loop(asyncio.new_event_loop())  # Create and set a new event loop for this thread
    app = ApplicationBuilder().token(token).build()
    app.add_handler(CommandHandler("start", start))
    app.add_handler(MessageHandler(filters.TEXT & ~filters.COMMAND, mensaje_handler_fake_student_1))
    print(f"{name} Telegram AI iniciado...")
    app.run_polling()


def tarea_fake_student_2(token, name):
    print(f"Starting {name} ")
    asyncio.set_event_loop(asyncio.new_event_loop())  # Create and set a new event loop for this thread
    app = ApplicationBuilder().token(token).build()
    app.add_handler(CommandHandler("start", start))
    app.add_handler(MessageHandler(filters.TEXT & ~filters.COMMAND, mensaje_handler_fake_student_2))
    print(f"{name} Telegram AI iniciado...")
    app.run_polling()



    

# -------- MAIN APP -------- #
if __name__ == '__main__':

    crear_tablas()
    print("✅ Tablas creadas correctamente.")
    # Crear hilos para los bots
    thread_bot_Boss = threading.Thread(target=botBoss)
    tarea_fake_student_1 = threading.Thread(target=tarea_fake_student_1, args=(config.TELEGRAM_FAKE_STUDENT_TOKEN_1, "FakeStudent1"))
    tarea_fake_student_2 = threading.Thread(target=tarea_fake_student_2, args=(config.TELEGRAM_FAKE_STUDENT_TOKEN_2, "FakeStudent2"))
    thread_bot_Boss.start()
    tarea_fake_student_1.start()
    tarea_fake_student_2.start()


    while True:
        time.sleep(1)