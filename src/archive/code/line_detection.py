import numpy as np
import cv2 as cv
import rescale
import pytesseract
import openpyxl
from openpyxl import load_workbook

def lines( src ):
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
   
    hlines = cv.HoughLinesP(horizontal,1,np.pi/180,100,minLineLength,maxLineGap)
    vlines = cv.HoughLinesP(vertical,1,np.pi/180,100,minLineLength,maxLineGap)
    return hlines,vlines