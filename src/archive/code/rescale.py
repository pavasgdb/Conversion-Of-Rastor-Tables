from PIL import Image
import cv2 as cv

def rescaled ( img ):
    scale_percent = 100 # percent of original size
    width = int(img.shape[1] * scale_percent / 100)
    height = int(img.shape[0] * scale_percent / 100)
    dim = (width, height)
    # resize image
    resized = cv.resize(img, dim, interpolation = cv.INTER_CUBIC)
    cv.imwrite("../output/imageresized.png", resized)
    return resized