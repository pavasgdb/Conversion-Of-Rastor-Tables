import cv2
import numpy as np

img = cv2.imread('with_borders\image1.png')
gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
edges = cv2.Canny(gray,50,150,apertureSize = 3)
minLineLength = 100
maxLineGap = 10
x=set()
y=set()
# lines = set() # holds lines already seen
vlines = cv2.HoughLinesP(vertical,1,np.pi/180,75,minLineLength,maxLineGap)
hlines = cv2.HoughLinesP(horizontal,1,np.pi/180,75,minLineLength,maxLineGap)

for i in range(len(vlines)):   
    for x1,y1,x2,y2 in vlines[i]:
        cv2.line(img,(x1,y1),(x2,y2),(0,255,0),2)
        x.add(x1)
        x.add(x2)
        y.add(y1)
        y.add(y2)
print(x)
print(y)

cv2.imwrite('houghlines5.jpg',img)