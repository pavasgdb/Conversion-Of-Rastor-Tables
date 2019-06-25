import pytesseract
from pytesseract import Output
import cv2
img = cv2.imread('without_lines\image3.png')

d = pytesseract.image_to_data(img, output_type=Output.DICT)
n_boxes = len(d['level'])

for i in range(n_boxes):
    (x, y, w, h,t) = (d['left'][i], d['top'][i], d['width'][i], d['height'][i],d['text'][i])
    cv2.rectangle(img, (x, y), (x + w, y + h), (0, 255, 0), 2)
    print (x,y,x+w,y+h,t)

cv2.imshow('img', img)
cv2.waitKey(0)