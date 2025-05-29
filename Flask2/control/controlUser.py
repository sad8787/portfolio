from flask import Flask, request, jsonify,url_for
from flask_sqlalchemy import SQLAlchemy
from flask_bcrypt import Bcrypt
from flask_jwt_extended import JWTManager, create_access_token, jwt_required, get_jwt_identity
from configparser import ConfigParser
from itsdangerous import URLSafeTimedSerializer

from models.modelos import User
from control.controlEmail import enviar_correo

ts = URLSafeTimedSerializer('supersecretkey')

def control_User(data,db,bcrypt):
    print ("control users")
    try:                      
        action=data.get('action')          
    except Exception as err:        
        return jsonify({"success": str( False),"ok": str(False) ,"controlador": "users","accion":"add_user","code": "201"   ,"message": {str(err)}}), 201
    if(action=="add"):
        return add_user(data,db,bcrypt)
    if(action=="delete"):
        return delete(data,db)
    if(action=="update"):
        return update(data,db)
    if(action=="list"):
        return list_user(data,db)
    if(action=="searchById"):
        return searchById(data,db)   
    if(action=="search"):
        return search(data,db)                        
    if(action=="changePassword"):        
        return changePassword(data,db, bcrypt )
    if(action=="login"):        
        return login(data,db, bcrypt )
    return jsonify({"success": str( False),"ok": str(False) ,
                    "controlador":data.get('controlador'),
                    "accion":data.get('action'),
                    "status":404,"code": "404",
                    "message": "este servicio no esta implementado"}), 404     

def add_user(data,db,bcrypt):
    try:     
            print ("add user")                 
            name = data.get('name')
            email = data.get('email')  
            password = data.get('password')  
            print(f'{email}')
            if not email or not password or not name:
                return jsonify({"success": str( False),"ok": str(False) , "status": 400,
                    "controlador": "users","accion":"add_user","code": "400",
                    "error": "Todos los datos son requeridos"}), 400

    except Exception as err:        
            return jsonify({"success": str( False),"ok": str(False) , "status": 204,
                            "controlador": "users","accion":"add_user","code": "204"
                            ,"message": {str(err)}}), 204
    
    existe = User.query.filter_by(email=email).first()    
    if (existe) :
        if(existe.activo):
            print(f'{str(existe.id)} {str(existe.activo)}')
            return jsonify({"success": str( False),"ok": str(False) , "status": 204,
                            "controlador": "users","accion":"add_user","code": "204",
                            "message": "El correo ya está registrado"}), 204
        else:
            token = ts.dumps(new_user.email, salt='email-confirm-key')
            confirm_url = url_for('confirm_email', token=token, _external=True)        
            asunto = "Confirma tu correo electrónico"
            body=f'''Por favor, haz clic en el siguiente enlace para activar tu cuenta: {confirm_url}'''
            enviar_correo(asunto,new_user.name,new_user.email,body)
            return jsonify({"success": True,"ok": True,"controlador": "users",
                            "accion":"add_user","code": "200","status": 200,
                            "message": "Usuario registrado. Revisa tu correo para activarlo."}), 200
    
    try:
        hashed_password = bcrypt.generate_password_hash(data['password']).decode('utf-8')
        new_user = User(name=data['name'], email=data['email'],password=hashed_password,is_admin=False,activo=False)
        db.session.add(new_user)
        db.session.commit()


        token = ts.dumps(new_user.email, salt='email-confirm-key')
        confirm_url = url_for('confirm_email', token=token, _external=True)
        
        asunto = "Confirma tu correo electrónico"
        body=f'''Por favor, haz clic en el siguiente enlace para activar tu cuenta: {confirm_url}'''
        enviar_correo(asunto,new_user.name,new_user.email,body)
        return jsonify({"success": True,"ok": True,"controlador": "users","accion":"add_user","code": "201","message": "Usuario registrado. Revisa tu correo para activarlo."}), 201

    except Exception as err:       
        return jsonify({"success": False,"ok": False,"controlador": "users","accion":"add_user","code": "201"   ,"message": str(err)}),201

def delete(data,db):
    try:                      
        id=data.get('id') 
        action=data.get('action') 
        controlador=data.get('controlador')
        user = User.query.get_or_404(id)
        db.session.delete(user)
        db.session.commit()
        return jsonify({"success": str( True),"ok": str(True) ,
                        "controlador": controlador,
                        "accion":action,"code": "200" ,
                        "status": 200,
                        "message": "user delete successfully"}), 200              
    
    except Exception as err:        
        return jsonify({"success": str( False),"ok": str(False) ,"controlador":controlador ,"action":action,
                        "code": "204" , "status": 204  ,"message": {str(err)}}), 204

def update(data,db):
    try:  
        id=data.get('id')  
        name =data.get('name') 
        email =data.get('email')     
        is_admin =data.get('is_admin')    
        if(is_admin=="true" or is_admin=="True" or is_admin==1 or is_admin=="1" or is_admin==True):is_admin=True 
        else: is_admin=False
        activo= data.get('activo')
        if(activo=="true" or activo=="True" or activo==1 or activo=="1" or activo==True):activo=True 
        else: activo=False
        user = User.query.get_or_404(id)   
        user.name = name
        user.email = email
        user.is_admin = is_admin
        user.activo=activo
        db.session.commit()
        return jsonify({"success": str( True),"ok": str(True) ,"controlador": "users","accion":"update",
                        "code": "200" ,"status": 200  ,"message": "user updated successfully"}), 200               
    except Exception as err:        
        return jsonify({"success": str( False),"ok": str(False) ,
                        "controlador": "users","accion":"update",
                        "code": "204"   ,"status": 204,
                        "message": {str(err)}}), 204
    

def list_user(data,db):
    users = User.query.all()
    list_user=[]
    for user in users:
        list_user.append( {"id": user.id, "name": user.name, "email": user.email,"is_admin": user.is_admin, "activo": user.activo})
    
                   
    return {"success": True,"ok": True,"controlador": "users",
                    "accion":"list","code": "200","message": "list user",
                    "status": 200,
                    "users":list_user,"len":len(list_user)} ,200   
 
def searchById(data,db):

    ID = data.get('id')
    user = User.query.filter_by(id=ID).first()
    if(user):
        r= {"id": user.id, "name": user.name, "email": user.email,"is_admin": user.is_admin, "activo": user.activo}
        return jsonify({"success": True,"ok": True,"controlador": "users",
                    "accion":"searchById","code": "200","message": "encontrado",
                    "status": 200,
                    "users":r,"len":1}), 200
    else:
        return jsonify({"success": False,"ok": False,"controlador": "users",
                    "accion":"searchById","code": "200","message": "no encontrado",
                    "status": 200,
                    "len":0,}), 200

def search(data,db):
    print("search")
    value= data.get("searchvalue")
    
    users = User.query.filter(
        (User.email == value) | (User.name == value)
    ).all()
    
    list_user=[]
    for user in users:
        list_user.append( {"id": user.id, "name": user.name, "email": user.email})
    print(list_user)
    return jsonify({"success": True,"ok": True,"controlador": "users",
                    "accion":"list","code": "200","message": "list user",
                    "status": 200,
                    "users":list_user,"len":len(list_user)}), 200    

def changePassword(data,db,bcrypt):    
    id=data.get('id')     
    newpassword = data.get('newpassword')     
    oldpassword  =data.get('oldpassword') 

    newpassword = bcrypt.generate_password_hash(newpassword).decode('utf-8')
    oldpassword = bcrypt.generate_password_hash(oldpassword).decode('utf-8') 
    if not id or not newpassword or not oldpassword:
        return jsonify({"success": str( False),"ok": str(False) , "status": 204,
                    "controlador": "users","accion":data.get('accion'),"code": "204",
            "error": "Todos los datos son requeridos"}), 204 
    
    user = User.query.get_or_404(id)  
   
    print(f'{user.id} {user.password} {user.email} old {oldpassword}')
    if user and bcrypt.check_password_hash(user.password, oldpassword):              
            user.password =  newpassword   
            db.session.commit()
            return jsonify({"success": str( True),"ok": str(True) , "status": 200,
                    "controlador": "users","accion":"add_user","code": "200",                
                "message": "Successfully"}),200
    else:        
        return jsonify({"success": str( False),"ok": str(False) , "status": 204,
                    "controlador": "users","accion":data.get('accion'),"code": "204",
                      "error": "Datos incorrectos"})
    
def login(data,db,bcrypt):
    print(f'''login {data}''')
    email = data.get('email')
    password = data.get('password')
    
    # Validate email and password
    if not email or not password:
        return jsonify({"success": str( False),"ok": str(False) , "status": 400,
                    "controlador":data.get('controlador'),
                    "accion":data.get('accion'),"code": "400",
            "message": "Email and password are required"}), 400
    
    # Find the user in the database
    
    user = User.query.filter_by(email=email).first()
   
    print(bcrypt.check_password_hash(user.password, password))
    if user and bcrypt.check_password_hash(user.password, password) and user.activo:
        # Generate JWT token
        access_token = create_access_token(identity={"id": user.id, "is_admin": user.is_admin})
        print(access_token)
        return jsonify({"success": str( True),"ok": str(True),"token":access_token,
                        "status": 200,
                        "controlador":data.get('controlador'),
                        "accion":data.get('accion'),"code": "200",
                        "message": "Ok"}),200
    else:
        return jsonify({"success": str( False),"ok": str(False),
                    "status": 401,
                    "controlador":data.get('controlador'),
                    "accion":data.get('accion'),"code": "401",
                    "message": "Invalid email or password"}), 401