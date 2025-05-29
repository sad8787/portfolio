from flask import Flask, request, jsonify,url_for
from flask_sqlalchemy import SQLAlchemy
from flask_bcrypt import Bcrypt
from flask_jwt_extended import JWTManager, create_access_token, jwt_required, get_jwt_identity
from configparser import ConfigParser
from itsdangerous import URLSafeTimedSerializer
from models.modelos import Proyecto

ts = URLSafeTimedSerializer('supersecretkey')

def control_Proyecto(data,db,bcrypt):
    print ("control proyecto")
    try:                      
        action=data.get('action')          
    except Exception as err:        
        return jsonify({"success": str( False),"ok": str(False) ,
                    "controlador":data.get('controlador'),
                    "accion":data.get('action'),"status":204,"code": "204"   ,"message": {str(err)}}), 204
    if(action=="add"):
        return add_Proyecto(data,db,bcrypt)
    if(action=="delete"):
        return delete(data,db)
    if(action=="update"):
        return update(data,db)
    if(action=="list"):
        return list_proyectos(data,db)
    if(action=="searchById"):
        return searchById(data,db)   
    if(action=="search"):
        return search(data,db) 
    return jsonify({"success": str( False),"ok": str(False) ,
                    "controlador":data.get('controlador'),
                    "accion":data.get('action'),
                    "status":404,"code": "404",
                    "message": "este servicio no esta implementado"}), 404                       
    
    

def add_Proyecto(data,db,bcrypt):
    try:                              
        name = data.get('name')
        fecha = data.get('fecha')
        url= data.get('url')
        descripcion = data.get('descripcion')   
        if( not fecha) or (not url) or( not descripcion) or (not name):
                return jsonify({"success": str( False),"ok": str(False) ,
                                "controlador":data.get('controlador') ,
                                "accion":data.get('action'),
                                "code": "201"   ,
                                "status": 201,
                                "message":"Todos los datos son requeridos" }), 201          

    except Exception as err:        
            return jsonify({"success": str( False),"ok": str(False) ,
                            "controlador":data.get('controlador') ,
                            "accion":data.get('action'),
                            "code": "201"   ,
                            "status": 201,
                            "message": {str(err)}}), 201
    
    existe = Proyecto.query.filter_by(name=name).first()    
    if (existe) :
        return jsonify({"success": str( False),"ok": str(False) ,
                        "controlador":data.get('controlador') ,
                        "accion":data.get('action'),
                        "code": "200"   ,
                        "status": 200,
                        "message": f'{name} ya está registrado'}), 200
            
    try:       
        new_proyecto = Proyecto(name = name, fecha = fecha, url = url, descripcion = descripcion, activo = True)
        db.session.add(new_proyecto)
        db.session.commit()
        return jsonify({"success": True,"ok": True,
                        "controlador":data.get('controlador') ,
                        "accion":data.get('action'),                        
                        "code": "200",
                        "message": "proyecto adicionado excito."}), 200
    except Exception as err:       
        return jsonify({"success": False,"ok": False,
                        "controlador":data.get('controlador') ,
                        "accion":data.get('action'),
                        "code": "201"   ,"message": str(err)}),201

def delete(data,db):
    try:                      
        id=data.get('id') 
        action=data.get('action') 
        controlador=data.get('controlador')
        proyecto = Proyecto.query.get_or_404(id)
        db.session.delete(proyecto)
        db.session.commit()
       
        return jsonify({"success": str( True),"ok": str(True) ,
                        "controlador": data.get('controlador'),
                        "accion":data.get('action') ,
                        "code": "200"   ,
                        "status": 200,
                        "message": "delete successfully"}), 200               
    
    except Exception as err:        
        return jsonify({"success": str( False),"ok": str(False) ,"controlador":controlador ,"action":action,
                        "status": 201,"code": "201"   ,"message": {str(err)}}), 201

def update(data,db):
    try:  
        id = data.get('id')  
        name = data.get('name') 
        fecha = data.get('fecha')    
        url = data.get('url')     
        activo = data.get('activo')
        if(activo == "true" or activo == "True" or activo==1 or activo=="1" or activo==True):activo=True 
        else: activo=False
        proyecto = Proyecto.query.get_or_404(id)   
        proyecto.name = name
        proyecto.fecha = fecha
        proyecto.url = url
        proyecto.activo = activo
        db.session.commit()
        return jsonify({"success": str( True),"ok": str(True) ,
                        "controlador": data.get('controlador'),
                        "accion":data.get('action') ,
                        "code": "200"   ,
                        "status": 200,
                        "message": f"{data.get('controlador')} updated successfully"}), 200               
    except Exception as err:        
        return jsonify({"success": str( False),"ok": str(False) ,
                        "controlador": data.get('controlador'),
                        "accion":data.get('action') ,
                        "code": "400"   ,
                        "status": 400
                        ,"message": {str(err)}}), 400
    

def list_proyectos(data,db):
    proyectos = Proyecto.query.all()
    list_proyectos=[]
    for p in proyectos:
        list_proyectos.append( {"id": p.id, "name": p.name, "fecha": p.fecha, "activo": p.activo})
    
                   
    return {"success": str( True),"ok": str(True) ,
                        "controlador": data.get('controlador'),
                        "accion":data.get('action') ,
                        "code": "200"   ,
                        "status": 200,"message": "list proyecto",
                    "proyectos":list_proyectos,"len":len(list_proyectos)} ,200   
 
def searchById(data,db):

    ID = data.get('id')
    proyecto = Proyecto.query.filter_by(id=ID).first()
    if(proyecto):
        r= {"id": proyecto.id, 
            "name": proyecto.name, 
            "fecha": proyecto.fecha,
            "url":proyecto.url,
            "descripcion": proyecto.descripcion,
            "activo": proyecto.activo}
        return jsonify({"success": True,
                    "ok": True,
                    "controlador": data.get('controlador'),
                    "accion":data.get('accion'),
                    "code": "200",
                    "status": 200,
                    "message": "encontrado",
                    "proyecto":r,
                    "len":1}), 200
    else:
        return jsonify({"success": False,"ok": False,
                    "controlador": data.get('controlador'),
                    "accion":data.get('accion'),
                    "status": 200,
                    "code": "200",
                    "message": "no encontrado",
                    "proyecto":None,
                    "len":0,}), 200

def search(data,db):
    print("search")
    value= data.get("searchvalue")
    
    proyectos = Proyecto.query.filter(
        (Proyecto.name == value) | (Proyecto.fecha == value)
    ).all()
    
    list_proyectos=[]
    for proyecto in proyectos:
        list_proyectos.append(  {"id": proyecto.id, 
            "name": proyecto.name, 
            "fecha": proyecto.fecha,
            "url":proyecto.url,
            "descripcion": proyecto.descripcion,
            "activo": proyecto.activo})
    print(list_proyectos)
    return jsonify({"success": True,
                    "ok": True,
                    "controlador": data.get('controlador'),
                    "accion":data.get('accion'),
                    "status": 200,
                    "code": "200",
                    "message": "list proyectos",
                    "proyectos":list_proyectos,
                    "len":len(list_proyectos)}), 200    

