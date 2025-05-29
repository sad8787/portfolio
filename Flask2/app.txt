
from flask import Flask,render_template,request , jsonify, render_template, redirect, url_for, session
from flask_sqlalchemy import SQLAlchemy
from flask_bcrypt import Bcrypt
from flask_jwt_extended import JWTManager, create_access_token, jwt_required, get_jwt_identity
from configparser import ConfigParser

from models.modelos import Convocatoria,Proyecto,User,db

from control.controlUser import control_User
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

db.init_app(app)

jwt = JWTManager(app)
bcrypt = Bcrypt(app)









# Rutas

#######################################################################################################################

@jwt_required()
def deleteOrUpdate(data,db):    
    current_user = get_jwt_identity()
    if not current_user['is_admin']:
        return jsonify({"message": "Admin access required"}), 403
    controlador=data.get('controlador')    
    if(controlador=="users"):                
        return control_User(data,db)             
    if(controlador=="proyecto"):
        print("proyecto")     
    if(controlador=="convocatoria"):
        print("convocatoria")



#total
@app.route('/sadiel/api',methods=['GET', 'POST'])
def appDoTotal():
    print("appDoTotal")
    try:
        if (request.method=='POST'):  
            data = request.json
            print(f'post {str(data)} ' )            
            controlador=data.get('controlador')
            action=data.get('action')
            if(action=="delete"or action=="update"):
                return deleteOrUpdate(data,db)            
                
            else:
                if(controlador=="users"):                
                    return control_User(data,db,bcrypt)                    
                    #erroroperacion incorecta
                if(controlador=="proyecto"):
                    print("proyecto")                  
                
                if(controlador=="convocatoria"):
                    print("convocatoria")

                if(controlador=="email"):
                    print("email")
    except Exception as err:
            print(f"Unexpected {err=}, {type(err)=}")
            return jsonify({"success": False,"ok":False,"recurso": controlador,"action":action, "error": str(err),"code":str(err)})

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

###########################################################################



if __name__ == '__main__':
    with app.app_context():
        db.create_all()
    app.run(host='0.0.0.0', port=5000, debug=True)

    #app.run(debug=True)
