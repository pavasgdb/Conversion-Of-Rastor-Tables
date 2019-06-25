from __future__ import division
import numpy as np
import cv2 as cv
from PIL import Image
import pytesseract
import openpyxl
from openpyxl import load_workbook

 
img = cv.imread('with_borders/image3.png', cv.IMREAD_UNCHANGED)
 
print('Original Dimensions : ',img.shape)
 
scale_percent = 500 # percent of original size
width = int(img.shape[1] * scale_percent / 100)
height = int(img.shape[0] * scale_percent / 100)
dim = (width, height)
# resize image
resized = cv.resize(img, dim, interpolation = cv.INTER_CUBIC)
 
print('Resized Dimensions : ',resized.shape)
 
cv.imshow("Resized image", resized)
cv.imwrite("imageresized.jpg", resized)
cv.waitKey(0)
cv.destroyAllWindows()
 

image_obj = Image.open("imageresized.jpg")

src = cv.imread("imageresized.jpg", cv.IMREAD_COLOR)

if len(src.shape) != 2:
    gray = cv.cvtColor(src, cv.COLOR_BGR2GRAY)
else:
    gray = src

gray = cv.bitwise_not(gray)
bw = cv.adaptiveThreshold(gray, 255, cv.ADAPTIVE_THRESH_MEAN_C, cv.THRESH_BINARY, 15, -2)

horizontal = np.copy(bw)
vertical = np.copy(bw)

cols = horizontal.shape[1]
horizontal_size = int(cols / 30)

horizontalStructure = cv.getStructuringElement(cv.MORPH_RECT, (horizontal_size, 1))
horizontal = cv.erode(horizontal, horizontalStructure)
horizontal = cv.dilate(horizontal, horizontalStructure)

#np.set_printoptions(threshold=np.inf)
cv.imwrite("img_horizontal8.png", horizontal)

h_transpose = np.transpose(np.nonzero(horizontal))
# print("h_transpose")
# print(h_transpose[:100])
#prints [ 56  35] ... [ 56  134]
#and that makes sense, there is an horizontal line more or less in the height 56 like that on the image img_horizontal8.png



rows = vertical.shape[0]
verticalsize = int(rows / 30)
verticalStructure = cv.getStructuringElement(cv.MORPH_RECT, (1, verticalsize))
vertical = cv.erode(vertical, verticalStructure)
vertical = cv.dilate(vertical, verticalStructure)

cv.imwrite("img_vertical8.png", vertical)

v_transpose = np.transpose(np.nonzero(vertical))

# print("v_transpose")
# print(v_transpose[:100])

img = src.copy()

# edges = cv.Canny(vertical,50,150,apertureSize = 3)
minLineLength = 100
maxLineGap = 200
x=set()
y=set()
vlines = cv.HoughLinesP(vertical,1,np.pi/180,100,minLineLength,maxLineGap)
hlines = cv.HoughLinesP(horizontal,1,np.pi/180,100,minLineLength,maxLineGap)
# print(vlines)
# print(hlines)







































# for line in vlines:
#     for x1,y1,x2,y2 in line:
#         cv.line(img,(x1,y1),(x2,y2),(0,255,0),2)
#         x.add(x1)
#         x.add(x2)

# for line in hlines:
#     for x1,y1,x2,y2 in line:
#         cv.line(img,(x1,y1),(x2,y2),(0,255,0),2)
#         y.add(y1)
#         y.add(y2)
# x=sorted(x)
# y=sorted(y)
# print(x)
# print(y)   


# html_data = """
#  <html>
#     <head>
#         <title>
#        Table
#         </title>
#         <style>
#         table, tr, td {
#         border: 1px solid black;
#         }
# </style>
#     </head>
#     <body>
       
#     <table style="border: solid 1px #000000">
#     """


# for i in range(len(y)):
#     if((i>0) & ((y[i]-y[i-1])> 10)):
#         html_data += "<tr>"
#         for j in range(len(x)):
#             if ((j>0) & ((x[j]-x[j-1])>10)):
#                 cropped_image = image_obj.crop((x[j-1],y[i-1],x[j],y[i]))
#                 html_data += "<td>" + str(pytesseract.image_to_string(cropped_image, config='--psm 6')) + "</td>"
#         html_data += "</tr>"
# html_data += "</table></body></html>"

# with open("output.html", "w") as html_fil:
#     html_fil.write(html_data)

cv.imwrite('houghlinesP_vert.jpg', img)