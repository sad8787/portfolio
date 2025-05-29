# Используем за основу образ Python 3
FROM python:3.11

# Устанавливаем рабочую директорию контейнера
WORKDIR /app

# Устанавливаем зависимости для Python-приложения
COPY requirements.txt ./
RUN pip install --trusted-host files.pythonhosted.org --no-cache-dir -r requirements.txt 

# Копируем исходный код в рабочую директорию контейнера
COPY /data /app/data
COPY /scripts /app/scripts

# Определение переменных
ENV client_id=value1
ENV client_secret=value2
ENV bd_user=value3
ENV bd_password=value4
ENV bot_token=value5

# Определяем команду для запуска приложения
ENTRYPOINT [ "python", "/app/scripts/bot.py" ]