import numpy as np
import cv2 as cv
from rescale import rescaled
from line_detection import lines
from intersection import intersections
# import preprocess as pre
# import convert
from PIL import Image
 
img = cv.imread('../output/image1_thin.png', cv.IMREAD_UNCHANGED)
resized = rescaled(img)
print('Resized Dimensions : ',resized.shape)
cv.imwrite("imageresized.jpg", resized)
 

image_obj = Image.open("imageresized.jpg")
src = cv.imread("imageresized.jpg", cv.IMREAD_COLOR)
hlines, vlines= lines(src)
points = sorted(intersections(hlines,vlines),key=lambda set: set[0])
points = sorted (points,key=lambda set: set[1])
print(points)
# temp=set(set())
# temp.add(points[0])
# i=1
# for i in range(len(points)):
        
#         if((points[i][1]-points[i-1][1]<10)&(points[i][0]-points[i-1][0]<10)):
#                 continue
#         temp.add(points[i])


# temp=sorted (temp,key=lambda set: set[1])
# i=1
# final=set(set())
# t=set()
# t.add(temp[0])
# for i in range(len(temp)):
#         t.add(temp[i])
#         if(temp[i][1]-temp[i-1][1]>10):
#                 final.add(sorted(t,key=lambda set:set[0])
#                 t.clear()

# print(final)        
                

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
#                 #cropped_image.save("output\image"+str(i)+str(j)+".jpg")
#                 # print (x[j-1],y[i-1],x[j],y[i])
#                 # print()   
#                 html_data += "<td>" + str(pytesseract.image_to_string(cropped_image, config='--psm 6')) + "</td>"
#         html_data += "</tr>"
# html_data += "</table></body></html>"

# with open("output.html", "w") as html_fil:
#     html_fil.write(html_data)

# cv.imwrite('houghlinesP_vert.jpg', img)