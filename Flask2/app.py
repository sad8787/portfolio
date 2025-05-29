import os

from flask import Flask,render_template,request , jsonify, render_template, redirect, url_for, session
from flask_sqlalchemy import SQLAlchemy
from flask_bcrypt import Bcrypt
from flask_jwt_extended import JWTManager, create_access_token, jwt_required, get_jwt_identity
from configparser import ConfigParser
import json
from models.modelos import Convocatoria,Proyecto,User,db,Imagen

from control.controlUser import control_User
from control.controlProyecto import control_Proyecto
from control.controlConvocatoria import control_Convocatoria
from control.controlEmail import control_sendMail
from control.controlImagen import upload_image,get_images,control_image
from control.controlCurso import control_Curso

from itsdangerous import URLSafeTimedSerializer
ts = URLSafeTimedSerializer('supersecretkey')

app = Flask(__name__)

# Leer configuración desde config.ini
config = ConfigParser()
config.read('config.ini')
print(f'''{config['database']['SQLALCHEMY_DATABASE_URI']}
{config.getboolean('database', 'SQLALCHEMY_TRACK_MODIFICATIONS')}
{config['security']['JWT_SECRET_KEY']}''')
app.config['SQLALCHEMY_DATABASE_URI'] = config['database']['SQLALCHEMY_DATABASE_URI']
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = config.getboolean('database', 'SQLALCHEMY_TRACK_MODIFICATIONS')
app.config['JWT_SECRET_KEY'] = config['security']['JWT_SECRET_KEY']
UPLOAD_FOLDER=config['UPLOAD_FOLDER']['UPLOAD_FOLDER']
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
db.init_app(app)

jwt = JWTManager(app)
bcrypt = Bcrypt(app)









# Rutas

#######################################################################################################################

@jwt_required()
def deleteOrUpdate(data,db,bcrypt):    
    current_user = get_jwt_identity()
    if(current_user):
        print(f''' user {current_user['is_admin']}  ''')
    if not current_user['is_admin']:
        return jsonify({"message": "Admin access required"}), 403
    controlador=data.get('controlador')    
    if(controlador=="users"):                
        return control_User(data,db,bcrypt)             
    if(controlador=="proyecto"):
        return control_Proyecto(data,db,bcrypt)    
    if(controlador=="convocatoria"):
         return control_Convocatoria(data,db,bcrypt)

def admin(db):  
    existe = User.query.filter_by(email="admin@gmai.com").first()
    if(existe):
            print ("ok")
    else:
            hashed_password = bcrypt.generate_password_hash("admin").decode('utf-8')
            new_user = User(name="admin", email="admin@gmai.com",password=hashed_password,is_admin=True,activo=True)
            db.session.add(new_user)
            db.session.commit()
            print(f"Usuario inicial agregado: {new_user.name}")  
   

#total
@app.route('/sadiel/api',methods=['GET', 'POST'])
def appDoTotal():
    print("todo")
    
    try:
        if (request.method=='POST'):  
            if request.is_json:
                data = request.get_json()
                print("si") 
            else:
                data = request.form
                print("no") 
            
            print(f'post {str(data)} ' )   
            
                  
            controlador=data.get('controlador')
            action=data.get('action')
            print(f'post {controlador} {action}' )   
            if(action=="delete"or action=="update"):
                return deleteOrUpdate(data,db,bcrypt)            
                
            else:
                if(controlador=="users"):                
                    return control_User(data,db,bcrypt)                    
                    #erroroperacion incorecta
                if(controlador=="proyecto"):
                    return control_Proyecto(data,db,bcrypt)                  
                
                if(controlador=="convocatoria"):
                    return control_Convocatoria(data,db,bcrypt)      
                if(controlador=="curso"):
                    return control_Curso(data,db,bcrypt)

                if(controlador=="email"):
                    return jsonify(  control_sendMail(data)),200
                
                if(controlador=="images"):                    
                    con= control_image(data,db,UPLOAD_FOLDER)
                    print()
                    return con
    except Exception as err:
            print(f"Unexpected {err=}, {type(err)=}")
            return jsonify({"success": False,"ok":False, "error": str(err),"code":str(err)})

@app.route('/confirm/<token>', methods=['GET'])
def confirm_email(token):
    try:
        email = ts.loads(token, salt='email-confirm-key', max_age=3600)  # 1 hora de validez
    except Exception as e:
        return jsonify({"error": "Token inválido o expirado"}), 400

    user = User.query.filter_by(email=email).first()
    if not user:
        return jsonify({"error": "Usuario no encontrado"}), 404

    if user.activo:
        return jsonify({"message": "El usuario ya está activado."}), 200

    user.activo = True
    db.session.commit()

    return jsonify({"message": "Usuario activado con éxito."}), 200



#######################################     test
@app.route('/test/newUser')
def testregister():    
    if (request.method=='POST'):        
        return 'hola post'
    else:
        return render_template('/newUser.html')
    

@app.route('/test/login')
def testlogin():    
    if (request.method=='POST'):        
        return 'hola post'
    else:
        return render_template('/login.html')

@app.route('/test/listuser')
def listuser():    
    if (request.method=='POST'):        
        return 'hola post'
    else:
        return render_template('/listuser.html')

@app.route('/test/searchuser')
def searchuser():    
    if (request.method=='POST'):        
        return 'hola post'
    else:
        return render_template('/search.html')



@app.route('/upload', methods=['POST'])
def up_image():
    if request.is_json:
                data = request.get_json()
                print("si") 
    else:
                data = request.form
                print("no") 
    return upload_image(data,db,UPLOAD_FOLDER)

@app.route('/test/images')
def Upimages():    
    if (request.method=='POST'):        
        return 'hola post'
    else:
        return render_template('/images.html')
    
@app.route('/images', methods=['GET'])
def get_images():
    images = Imagen.query.all()
    return jsonify([{"id": img.id, "name": img.name, "path": img.dir_path} for img in images])

###########################################################################



if __name__ == '__main__':
    
    os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)
    with app.app_context():
        db.create_all()
        admin(db)
        
    app.run(host='0.0.0.0', port=5000, debug=True)

    #app.run(debug=True)
