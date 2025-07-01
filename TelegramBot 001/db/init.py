# db/init.py

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from db.models import Base
import config

# Construir la URL desde config.py
db_conf = config.DB_CONFIG
DATABASE_URL = f"postgresql://{db_conf['user']}:{db_conf['password']}@{db_conf['host']}/{db_conf['dbname']}"


engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(bind=engine)

def crear_tablas():
    Base.metadata.create_all(bind=engine)


def create_session():
    """Crea una nueva sesión de base de datos."""
    db_conf = config.DB_CONFIG
    DATABASE_URL = f"postgresql://{db_conf['user']}:{db_conf['password']}@{db_conf['host']}/{db_conf['dbname']}"
    engine = create_engine(DATABASE_URL)
    #SessionLocal = sessionmaker(bind=engine)
    return sessionmaker(bind=engine, autoflush=False, autocommit=False)


