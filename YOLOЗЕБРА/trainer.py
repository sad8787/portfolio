import os
import sys
import torch



import os
print(os.path.exists('dataset/data.yaml'))
# add yolov5 al path  "train"
yolov5_path = os.path.join(os.path.dirname(__file__), 'yolov5')
if yolov5_path not in sys.path:
    sys.path.append(yolov5_path)

from yolov5.train import run  # Importamos la función de entrenamiento
 


def train_yolo_model():
    run(
        data='dataset/data.yaml',     # Ruta al archivo YAML
        imgsz=640,
        batch=8,
        epochs=300,
        weights='yolov5s.pt',         # Usa yolov5s como punto de partida
        project='modelos',            # Fail save model
        name='custom_yolo2s300',           # name del experimet
        exist_ok=True
    )

if __name__ == '__main__':
    train_yolo_model()





