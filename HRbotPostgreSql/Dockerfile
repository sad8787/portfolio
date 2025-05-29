# Используем за основу образ Python 3
FROM python:3.11

# Устанавливаем рабочую директорию контейнера
WORKDIR /app

# Устанавливаем зависимости для Python-приложения
COPY requirements.txt ./
RUN pip install --trusted-host files.pythonhosted.org --no-cache-dir -r requirements.txt 

# Копируем исходный код в рабочую директорию контейнера
COPY /data /app/data
COPY /resources /app/resources
COPY /scripts /app/scripts

# Передаем параметры
ENV user=value1
ENV password=value2
ENV token=value3


# Определяем команду для запуска приложения
ENTRYPOINT [ "python", "/app/scripts/bot.py" ]

# Открываем порты
#EXPOSE 5432
#EXPOSE 8080
