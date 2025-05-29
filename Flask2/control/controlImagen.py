from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
#from flask_cors import CORS
import os
from werkzeug.utils import secure_filename
from models.modelos import Imagen

def control_image(data,db,UPLOAD_FOLDER):
    try:                      
        action=data.get('action')          
    except Exception as err:        
        return jsonify({"success": str( False),"ok": str(False) ,
                    "controlador":data.get('controlador'),
                    "accion":data.get('action'),"status":204,"code": "204"   ,"message": {str(err)}}), 204
    if(action=="upload"):
        return upload_image(data,db,UPLOAD_FOLDER)
    if (action== "list"):
        print("list")
        return get_images(data,db,UPLOAD_FOLDER)
    
    if (action== "delete"):
        return delete_images(data,db,UPLOAD_FOLDER)
    
    if (action== "update"):
        return update_images(data,db,UPLOAD_FOLDER)
    if (action== "searchById"):
        return get_imagesById(data,db,UPLOAD_FOLDER)
    return jsonify({"success": str( False),"ok": str(False) ,
                    "controlador":data.get('controlador'),
                    "accion":data.get('action'),
                    "status":404,"code": "404",
                    "message": "este servicio no esta implementado"}), 404


def upload_image(data,db,UPLOAD_FOLDER):
    if 'file' not in request.files:
        return jsonify({"success": str( False),"ok": str(False) ,
                    "controlador":data.get('controlador'),
                    "accion":data.get('action'),
                    "status":404,"code": "404",
                    "message": "No file part",
            "error": "No file part"}), 400

    file = request.files['file']

    if file.filename == '':
        return jsonify({"success": True,
                    "ok": True,
                    "controlador": data.get('controlador'),
                    "accion":data.get('accion'),
                    "status": 400,
                    "code": "400",
                    "message": " error No selected file",
            "error": "No selected file"}), 400

    if file:
        filename = secure_filename(file.filename)        
        filepath = os.path.join(UPLOAD_FOLDER, filename)
        #filepath=UPLOAD_FOLDER+"/"+filename
        # Guardar archivo en el sistema
        file.save(filepath)
        print(f''' up {filepath}''')
        # Guardar información en la base de datos
        
        new_image = Imagen(name=filename, dir_path=filepath)
        db.session.add(new_image)
        db.session.commit()

        return jsonify({"success": True,
                    "ok": True,
                    "id": new_image.id, "name": new_image.name, "path": new_image.dir_path,
                    "controlador": data.get('controlador'),
                    "accion":data.get('accion'),
                    "status": 201,
                    "code": "201",
                    "message": "imagen guardada" }), 201


def get_images(data,db,UPLOAD_FOLDER):
    print ("control imagen get imagen")
    images = Imagen.query.all()

    list_images=[]
    for img in images:    
        list_images.append( {"id": img.id, "name": img.name, "path":img.dir_path.replace(".\static", "\static")})
   
    return jsonify({"success": True,"ok": True,"controlador": data.get('controlador'),
                    "accion":data.get('action'),
                    "code": "200",
                    "status": 200,
                    "message": "Imagen list",
                    "images":list_images,
                    "len":len(list_images)}), 200


def get_imagesById(data,db,UPLOAD_FOLDER):
    ID = data.get('id')
    img = Imagen.query.filter_by(id=ID).first()
    if(img):
        return jsonify({"success": True,"ok": True,"controlador": data.get('controlador'),
                    "accion":data.get('action'),
                    "code": "200",
                    "status": 200,
                    "message": "Imagen list",
                    "image":img,
                    "len":1}), 200
    else:
        return jsonify({"success": False,"ok": False,
                    "controlador": data.get('controlador'),
                    "accion":data.get('accion'),
                    "status": 200,
                    "code": "200",
                    "message": "no encontrado",
                    "image":None,
                    "len":0,}), 200
   
    
               
    

def delete_images(data,db,UPLOAD_FOLDER):
     # Buscar la imagen por ID
    image_id=data.get('id') 
    image = Imagen.query.get(image_id)
    if not image:
        return jsonify({"success": str( False),"ok": str(False) ,
                    "controlador":data.get('controlador'),
                    "accion":data.get('action'),
                    "status":404,"code": "404",
                    "message": "no encontrado"}), 404

    # Intentar eliminar el archivo físico
    try:
        if os.path.exists(image.dir_path):
            os.remove(image.dir_path)
    except Exception as e:
        return jsonify({"success": str( False),"ok": str(False) ,
                    "controlador":data.get('controlador'),
                    "accion":data.get('action'),
                    "status":404,"code": "404",
                    "message":f"No se pudo eliminar el archivo: {str(e)}",
            "error": f"No se pudo eliminar el archivo: {str(e)}"}), 500

    # Eliminar de la base de datos
    db.session.delete(image)
    db.session.commit()

    return jsonify({"success": str( True),"ok": str(True) ,
                    "controlador":data.get('controlador'),
                    "accion":data.get('action'),
                    "status":200,"code": "200",
        "message": "Imagen eliminada con éxito"}), 200

def update_images(data,db,UPLOAD_FOLDER):
    image_id=data.get('id') 
    # Buscar la imagen por ID
    image = Imagen.query.get(image_id)
    if not image:
        return jsonify({"success": str( False),"ok": str(False) ,
                    "controlador":data.get('controlador'),
                    "accion":data.get('action'),
                    "status":404,"code": "404",
                    "message": "no encontrado"}), 404

    # Verificar si hay un archivo en la solicitud
    if 'file' not in request.files:
        return jsonify({"success": str( False),"ok": str(False) ,
                    "controlador":data.get('controlador'),
                    "accion":data.get('action'),
                    "status":404,"code": "404",
                    "message": "No se ha enviado ningún archivo",
                    "error": "No se ha enviado ningún archivo"
                    }), 404        

    file = request.files['file']
    if file.filename == '':
        return jsonify({"success": str( False),"ok": str(False) ,
                    "controlador":data.get('controlador'),
                    "accion":data.get('action'),
                    "status":404,"code": "404",
                    "message": "El archivo está vacío",
            "error": "El archivo está vacío"}), 400

    # Guardar el nuevo archivo
    filename = secure_filename(file.filename)
    new_filepath = os.path.join(UPLOAD_FOLDER, filename)

    try:
        # Eliminar el archivo antiguo si existe
        if os.path.exists(image.dir_path):
            os.remove(image.dir_path)

        # Guardar el nuevo archivo
        file.save(new_filepath)

        # Actualizar la base de datos
        image.name = filename
        image.dir_path = new_filepath
        db.session.commit()

        return jsonify({"success": True,
                    "ok": True,                    
                    "controlador": data.get('controlador'),
                    "accion":data.get('accion'),
                    "status": 200,
                    "code": "200",            
                    "message": "Imagen actualizada con éxito", 
                    "id": image.id, "name": image.name, "path": image.dir_path}), 200
    except Exception as e:
        return jsonify({"success": str( False),"ok": str(False) ,
                    "controlador":data.get('controlador'),
                    "accion":data.get('action'),
                    "status":500,"code": "500",
                    "message": f"No se pudo actualizar la imagen: {str(e)}",
            "error": f"No se pudo actualizar la imagen: {str(e)}"}), 500

#