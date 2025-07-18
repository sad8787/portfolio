from moviepy.editor import VideoFileClip
# Ruta al archivo .webm
input_path = "Запись встречи 16.07.2025 15-15-57 - запись.webm"
output_path = "audio.mp3"
try:
    # Cargar el video
    video = VideoFileClip(input_path)
except Exception as e:
    print(e) 
# Extraer y guardar el audio en formato mp3
video.audio.write_audiofile(output_path)
print("Conversión completada:", output_path)

