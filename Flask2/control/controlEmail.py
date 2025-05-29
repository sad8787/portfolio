import smtplib
from email.message import EmailMessage
import ssl
from configparser import ConfigParser
import json
from flask import Flask,render_template,request , jsonify, render_template, redirect, url_for, session


def enviar_correo(asunto,nombre,email,body):
    # Configuración del correo
    config = ConfigParser()
    config.read('config.ini')


    remitente = config['email']['email']
    destinatario = email
    contraseña = config['email']['password']
    body=f'''Nombre: {nombre}
    Email: {email} 
     {body}'''
    # Crear el mensaje
    mensaje = EmailMessage()
    mensaje['Subject'] = asunto
    mensaje['From'] = remitente
    mensaje['To'] = destinatario
    mensaje.set_content(body)
    result=False
    try:
        context= ssl.create_default_context()
        # Conectar al servidor SMTP de Microsoft
        with smtplib.SMTP_SSL('smtp.gmail.com', 465,context=context) as servidor:
            #servidor.starttls()  # Iniciar conexión segura
            servidor.login(remitente, contraseña)  # Autenticarse
            servidor.sendmail(remitente,destinatario,mensaje.as_string())
            servidor.send_message(mensaje)  # Enviar correo
            result= True
    except Exception as e:
        print(f"Error al enviar el correo: {e}")
    return result


def control_sendMail(data):
    result={}
    try:
        asunto = data.get('asunto')             
        nombre = data.get('nombre')
        email = data.get('email')  
        body = data.get('body')                    
    except Exception as err:                               
        print(f"Unexpected {err=}, {type(err)=}")
        result={"success": False,"ok":False,"recurso": "send_email", "error": str(err),"code":str(err)}
                    #erroroperacion incorecta        
    try:
        result= enviar_correo(asunto,nombre,email,body)
        message= "NOT successfully"
        if(result):
            message= "successfully"
        
        result={"success": str( result),"ok": str(result) ,
                "controlador": data.get('controlador'),
                    "accion":data.get('accion'),
                    "status": 200,
                    "code": "200",
                    "message": message}                 
    except Exception as err:
        print(f"Unexpected {err=}, {type(err)=}")
        result={"success": str(False),"ok":str(False),"controlador": "email","accion":"email", "error": str(err),"code":str(err) ,"message": "unsuccessfully!"}
    return result

 