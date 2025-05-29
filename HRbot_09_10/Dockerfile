ARG ARG_REGISTRY=docker.io/library

# Используем за основу образ Python 3
FROM $ARG_REGISTRY/python:3.11

# Присваиваем аргументы для сборки
ARG ARG_INDEX_URL=https://pypi.python.org/simple
ARG ARG_TRUSTED_HOST=files.pythonhosted.org
ARG ARG_USER=value1
ARG ARG_PASSWORD=value2
ARG ARG_TOKEN=value3

# Устанавливаем рабочую директорию контейнера
WORKDIR /app

# Устанавливаем зависимости для Python-приложения
COPY requirements.txt ./
RUN pip config list
RUN pip install --index-url ${ARG_INDEX_URL} --trusted-host ${ARG_TRUSTED_HOST} --no-cache-dir -r requirements.txt 

# Копируем исходный код в рабочую директорию контейнера
COPY /data /app/data
COPY /resources /app/resources
COPY /scripts /app/scripts

# Передаем параметры
ENV user=$ARG_USER
ENV password=$ARG_PASSWORD
ENV token=$ARG_TOKEN

# Определяем команду для запуска приложения
ENTRYPOINT [ "python", "/app/scripts/bot.py" ]

# Открываем порты
#EXPOSE 5432
#EXPOSE 8080
