import torch
import cv2
import os


def rotate_frame(frame, angle: int = 90):
    """Gira el frame en un ángulo específico."""
    if angle == 90:
        return cv2.rotate(frame, cv2.ROTATE_90_CLOCKWISE)
    elif angle == 180:
        return cv2.rotate(frame, cv2.ROTATE_180)
    elif angle == 270:
        return cv2.rotate(frame, cv2.ROTATE_90_COUNTERCLOCKWISE)
    else:
        return frame


def detectin(video_path: str = 'Videos/2_1.MOV'):
    # Cargar el modelo YOLOv5 personalizado
    model = torch.hub.load('yolov5', 'custom', path='modelos/custom_yolo2s300/weights/best.pt', source='local')
    #custom_yolo2,#custom_yolo
    cap = cv2.VideoCapture(video_path)
    if not cap.isOpened():
        print(f"Error al abrir el video {video_path}")
        exit()

    fps = cap.get(cv2.CAP_PROP_FPS)

    # Leer un frame para determinar tamaño final tras rotación
    ret, frame = cap.read()
    if not ret:
        print("No se pudo leer el primer frame.")
        cap.release()
        return

    frame_rotated = rotate_frame(frame, 90)
    height, width = frame_rotated.shape[:2]  # Dimensiones reales tras rotación
    cap.set(cv2.CAP_PROP_POS_FRAMES, 0)  # Reinicia video

    save_path = 'resultados/output_rotated_detected.avi'
    os.makedirs(os.path.dirname(save_path), exist_ok=True)
    # ✅ Eliminar archivo si ya existe
    if os.path.exists(save_path):
        os.remove(save_path)

    # Config el VideoWriter para guardar el video procesado
    fourcc = cv2.VideoWriter_fourcc(*'XVID')
    out = cv2.VideoWriter(save_path, fourcc, fps, (width, height))

    cv2.namedWindow('Detección', cv2.WINDOW_NORMAL)
    cv2.resizeWindow('Detección', width, height)

    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break

        # Rotar el frame 90°
        frame_rotated = rotate_frame(frame, 90)

        # Detección con el modelo
        results = model(frame_rotated)
        result_frame = results.render()[0]

        # Mostrar a mitad de tamaño
        display_frame = cv2.resize(result_frame, (width // 8, height // 8))
        cv2.imshow('Detección', display_frame)

        # Guardar frame completo
        out.write(result_frame)

        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    cap.release()
    out.release()
    cv2.destroyAllWindows()

def all_videos(videos):    
    for video in videos:
        detectin(video)

if __name__ == '__main__':
    videos = ["Videos/1.MOV", "Videos/2_1.MOV", "Videos/3_1.MOV", "Videos/3_2.MOV", "Videos/4.MOV", "Videos/4_1.MOV"]
    all_videos(videos)


