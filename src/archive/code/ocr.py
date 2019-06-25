from __future__ import division
from numpy import empty
import cv2
from PIL import Image
import pytesseract
import numpy as np

def getText(cropped_image):
    return str(pytesseract.image_to_string(cropped_image))