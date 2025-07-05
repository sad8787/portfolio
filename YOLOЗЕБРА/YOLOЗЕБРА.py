import os
import cv2
from DatasetCreator import video_to_frames,crop_center_square,crop_all_frames,rotate
from trainer import  train_yolo_model
from video90 import detectin, all_videos



if __name__ == "__main__":
    print("Comenzamos")
    videos = ["Videos/1.MOV", "Videos/2_1.MOV", "Videos/3_1.MOV", "Videos/3_2.MOV", "Videos/4.MOV", "Videos/4_1.MOV"]
    
    all_videos(videos)
    
   
    
