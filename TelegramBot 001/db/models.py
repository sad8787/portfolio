# db/models.py

from sqlalchemy import create_engine, Column, Integer, String, Text, DateTime, Boolean, ForeignKey
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship
from datetime import datetime

Base = declarative_base()

# Definición de las tablas del modelo de base de datos
# clients,	messages, accounts, summaries, scripts, sessions, channels, posts, comments, groups, logs,ClientGroupLink

class Client(Base):
    __tablename__ = 'clients'    
    id = Column(Integer, primary_key=True) 
    client_id= Column(String(50), unique=True)  # ID único del cliente
    is_active=True
    username = Column(String(100))
    status = Column(String(20))
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow) # Good addition!
    messages = relationship("Message", back_populates="client")
   
    sessions = relationship("Session", back_populates="client") 
    def __repr__(self):
        return f"<Client(id={self.id}, username='{self.username}, client_id='{self.client_id}')>"

class Message(Base):
    __tablename__ = 'messages'
    id = Column(Integer, primary_key=True)
    from_id = Column(String(100) )
    to_id = Column(String(100) )
    text = Column(Text)
    timestamp = Column(DateTime, default=datetime.utcnow)
    strategy_used = Column(String(50))
    client_id = Column(Integer, ForeignKey('clients.id'))
    client = relationship("Client", back_populates="messages")

class Account(Base):
    __tablename__ = 'accounts'
    id = Column(Integer, primary_key=True)
    telegram_id = Column(String(50), unique=True)
    name = Column(String(100))
    persona_description = Column(Text)
    tone = Column(String(50))
    is_main = Column(Boolean, default=False)

    comments = relationship("Comment", back_populates="account")

class Summary(Base):
    __tablename__ = 'summaries'
    id = Column(Integer, primary_key=True)
    source_type = Column(String(100))  # 'post' o 'msg'
    main_idea = Column(Text)
    keywords = Column(Text)
    tone = Column(String(100))
    content_type = Column(String(100))

class Script(Base):
    __tablename__ = 'scripts'
    id = Column(Integer, primary_key=True)
    name = Column(String(100))
    content = Column(Text)  # Puedes usar JSON string o Markdown aquí
    version = Column(String(100))

class Session(Base):    
    __tablename__ = 'sessions'
    id = Column(Integer, primary_key=True)
    client_id = Column(Integer, ForeignKey('clients.id'))
    script_id = Column(Integer, ForeignKey('scripts.id'), nullable=True)
    is_active = Column(Boolean, default=True)
    started_at = Column(DateTime, default=datetime.utcnow)
    last_update = Column(DateTime, default=datetime.utcnow)

    client = relationship("Client", back_populates="sessions") 
    script = relationship("Script")


class Channel(Base):
    __tablename__ = 'channels'
    id = Column(Integer, primary_key=True)
    name = Column(String(100))
    chat_id = Column(String(100), unique=True)
    link = Column(String(255))
    is_active = Column(Boolean, default=True)

    posts = relationship("Post", backref="channel")

class Post(Base):
    __tablename__ = 'posts'
    id = Column(Integer, primary_key=True)
    channel_id = Column(Integer , ForeignKey('channels.id'))
    message_id = Column(String(100))
    date = Column(DateTime, default=datetime.utcnow)
    text = Column(Text)
    summary_id = Column(Integer, ForeignKey('summaries.id'))

    comments = relationship("Comment", back_populates="post")
    summary = relationship("Summary")

class Comment(Base):
    __tablename__ = 'comments'
    id = Column(Integer, primary_key=True)
    post_id = Column(Integer, ForeignKey('posts.id'))
    account_id = Column(Integer, ForeignKey('accounts.id'))
    type = Column(String(20))  # 'text', 'photo', 'dialog'
    text = Column(Text)
    status = Column(String(20), default="pending")

    post = relationship("Post", back_populates="comments")
    account = relationship("Account", back_populates="comments")

class Group(Base):
    __tablename__ = 'groups'
    id = Column(Integer, primary_key=True)
    name = Column(String(100))
    created_at = Column(DateTime, default=datetime.utcnow)
    is_active = Column(Boolean, default=True)

    # Si deseas relacionarlo con clientes:
    # Relaciones N:M (clientes en grupo)
    clients = relationship("ClientGroupLink", back_populates="group")

class Log(Base):
    __tablename__ = 'logs'
    id = Column(Integer, primary_key=True)
    event_type = Column(String(50))
    details = Column(Text)
    created_at = Column(DateTime, default=datetime.utcnow)

class ClientGroupLink(Base):
    __tablename__ = 'client_group_link'
    id = Column(Integer, primary_key=True)
    client_id = Column(Integer, ForeignKey('clients.id'))
    group_id = Column(Integer, ForeignKey('groups.id'))

    joined_at = Column(DateTime, default=datetime.utcnow)

    client = relationship("Client")
    group = relationship("Group", back_populates="clients")

