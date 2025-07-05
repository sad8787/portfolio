import os
from PIL import Image
import shutil
import re
import cv2



# === CONFIGURATION ===
videos = ["1.MOV", "2_1.MOV","3_1.MOV", "3_2.MOV","4.MOV", "4_1.MOV"]

# === CONVERT VIDEO TO FRAMES ===
def video_to_frames(video_path, output_folder, skip=1,saved_count = 0):
    os.makedirs(output_folder, exist_ok=True)
    cap = cv2.VideoCapture(video_path)
    frame_count = 0    

    while True:
        ret, frame = cap.read()
        if not ret:
            break

        if frame_count % skip == 0:
            # ROTAR 90° A LA DERECHA
            frame_rotated = cv2.rotate(frame, cv2.ROTATE_90_CLOCKWISE)

            frame_name = f"frame_{saved_count:05d}.jpg"
            frame_path = os.path.join(output_folder, frame_name)
            cv2.imwrite(frame_path, frame_rotated)
            saved_count += 1

        frame_count += 1

    cap.release()
    print(f"Total de frames guardados (rotados): {saved_count}")
    return saved_count+1

# === square center ===
def crop_center_square(input_path, output_path):
    img = Image.open(input_path)
    width, height = img.size
    min_dim = min(width, height)
    left = (width - min_dim) // 2
    top = (height - min_dim) // 2
    cropped = img.crop((left, top, left + min_dim, top + min_dim))
    cropped.save(output_path)

# === FUNCIÓN PARA PROCESAR TODOS LOS FRAMES Y RECORTARLOS ===
def crop_all_frames(input_folder, output_folder):
    os.makedirs(output_folder, exist_ok=True)
    for filename in sorted(os.listdir(input_folder)):
        if filename.lower().endswith(('.jpg', '.png')):
            in_path = os.path.join(input_folder, filename)
            out_path = os.path.join(output_folder, filename)
            crop_center_square(in_path, out_path)
    print(f"Todas las imágenes fueron recortadas a cuadrado.")

# === COMPLET EJECUTION  ===
def extract():      
    conti=0
    for video in videos:
        video_path = os.path.join('Videos', video)
        conti = video_to_frames(video_path , 'frames', skip=100,saved_count = conti) 
    crop_all_frames('frames','croppedframes' )       
# ======== rotate ========
def rotate(input_dir:str = "frames", output_dir:str = "trainig"):       

    # Asegurar que el directorio de salida existe
    os.makedirs(output_dir, exist_ok=True)

    # Obtener y ordenar la lista de archivos frame_*.jpg
    archivos = sorted([
        f for f in os.listdir(input_dir) 
        if f.startswith("frame_") and f.endswith(".jpg")
    ])

    # Recorrer 1 de cada 200
    for i, nombre_archivo in enumerate(archivos):
        if i % 200 == 0:
            ruta_original = os.path.join(input_dir, nombre_archivo)
            ruta_destino = os.path.join(output_dir, nombre_archivo)

            # Copiar la imagen original
            shutil.copy2(ruta_original, ruta_destino)

            # Abrir y rotar 90° a la derecha
            imagen = Image.open(ruta_original)
            imagen_rotada = imagen.rotate(-90, expand=True)  # -90 = derecha

            # Guardar con el mismo nombre + "D"
            nombre_sin_ext, ext = os.path.splitext(nombre_archivo)
            nuevo_nombre = f"{nombre_sin_ext}D{ext}"
            ruta_rotada = os.path.join(output_dir, nuevo_nombre)
            imagen_rotada.save(ruta_rotada)

            imagen_rotada = imagen.rotate(-180, expand=True)  # -180 = derecha

            # Guardar con el mismo nombre + "DD"
            nombre_sin_ext, ext = os.path.splitext(nombre_archivo)
            nuevo_nombre = f"{nombre_sin_ext}DD{ext}"
            ruta_rotada = os.path.join(output_dir, nuevo_nombre)
            imagen_rotada.save(ruta_rotada)

            imagen_rotada = imagen.rotate(-270, expand=True)  # -270 = derecha

            # Guardar con el mismo nombre + "DDD"
            nombre_sin_ext, ext = os.path.splitext(nombre_archivo)
            nuevo_nombre = f"{nombre_sin_ext}DDD{ext}"
            ruta_rotada = os.path.join(output_dir, nuevo_nombre)
            imagen_rotada.save(ruta_rotada)

            imagen_rotada = imagen.rotate(-360, expand=True)  # -360 = derecha

            # Guardar con el mismo nombre + "DDD"
            nombre_sin_ext, ext = os.path.splitext(nombre_archivo)
            nuevo_nombre = f"{nombre_sin_ext}DDDD{ext}"
            ruta_rotada = os.path.join(output_dir, nuevo_nombre)
            imagen_rotada.save(ruta_rotada)

    print("✅ Proceso completado.")

# Function to convert video files to frames
if __name__ == "__main__":
    print("Comenzamos")
    extract()