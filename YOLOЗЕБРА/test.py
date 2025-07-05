# test.py
import torch
model = torch.hub.load('ultralytics/yolov5', 'yolov5s')  # primer modelo

results = model('https://ultralytics.com/images/zidane.jpg')  # imagen de prueba
results.show()
