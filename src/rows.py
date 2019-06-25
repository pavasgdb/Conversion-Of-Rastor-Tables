from __future__ import division
import csv
import numpy as np
from operator import itemgetter
from PIL import Image
import pytesseract
import cv2
from numpy import genfromtxt
from junction_detection import adaptive_threshold





def find_lines(threshold, regions=None, direction='horizontal',
               line_scale=15, iterations=0):
    """Finds horizontal and vertical lines by applying morphological
    transformations on an image.
    Parameters
    ----------
    threshold : object
        numpy.ndarray representing the thresholded image.
    regions : list, optional (default: None)
        List of page regions that may contain tables of the form x1,y1,x2,y2
        where (x1, y1) -> left-top and (x2, y2) -> right-bottom
        in image coordinate space.
    direction : string, optional (default: 'horizontal')
        Specifies whether to find vertical or horizontal lines.
    line_scale : int, optional (default: 15)
        Factor by which the page dimensions will be divided to get
        smallest length of lines that should be detected.
        The larger this value, smaller the detected lines. Making it
        too large will lead to text being detected as lines.
    iterations : int, optional (default: 0)
        Number of times for erosion/dilation is applied.
          -------
    dmask : object
        numpy.ndarray representing pixels where vertical/horizontal
        lines lie.
    lines : list
        List of tuples representing vertical/horizontal lines with
        coordinates relative to a left-top origin in
        image coordinate space.
    """
    lines = []

    if direction == 'vertical':
        size = threshold.shape[0] // line_scale
        el = cv2.getStructuringElement(cv2.MORPH_RECT, (1, size))
    elif direction == 'horizontal':
        size = threshold.shape[1] // line_scale
        el = cv2.getStructuringElement(cv2.MORPH_RECT, (size, 1))
    elif direction is None:
        raise ValueError("Specify direction as either 'vertical' or"
                         " 'horizontal'")

    if regions is not None:
        region_mask = np.zeros(threshold.shape)
        for region in regions:
            x, y, w, h = region
            region_mask[y : y + h, x : x + w] = 1
        threshold = np.multiply(threshold, region_mask)

    threshold = cv2.erode(threshold, el)
    threshold = cv2.dilate(threshold, el)
    dmask = cv2.dilate(threshold, el, iterations=iterations)

    try:
        _, contours, _ = cv2.findContours(
            threshold.astype(np.uint8), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    except ValueError:
        # for opencv backward compatibility
        contours, _ = cv2.findContours(
            threshold.astype(np.uint8), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    for c in contours:
        x, y, w, h = cv2.boundingRect(c)
        x1, x2 = x, x + w
        y1, y2 = y, y + h
        if direction == 'vertical':
            lines.append(((x1 + x2) // 2, y2, (x1 + x2) // 2, y1))
        elif direction == 'horizontal':
            lines.append((x1, (y1 + y2) // 2, x2, (y1 + y2) // 2))
    
    return dmask, lines







def get_array(name):
    #name => name of the image

    image =cv2.imread("../input/"+name+".png")
    #adaptive thresholding of the image
    img, threshold = adaptive_threshold(image)
    #getting endpoints of all lines in the image
    h_dmask, h_lines = find_lines(threshold, regions=None, direction='horizontal',line_scale=15, iterations=0)
    v_dmask, v_lines = find_lines(threshold, regions=None, direction='vertical',line_scale=15, iterations=0)

    for l in range(len(h_lines)):
        print(h_lines[l])
    print("/////////////////")
    for l in range(len(v_lines)):
        print(v_lines[l])
    #horizontal and vertical lines merged into one array
    array_rows=np.concatenate((h_lines, v_lines))

    #array_rows = genfromtxt('./CSV/lines_ncert9.csv', delimiter=',')

    n_rows=len(array_rows)
    n_columns=len(array_rows[0])

    #each row in array_row is of the form[x1,y1,x2,y2]
    #transpose give new 2D array with 4 rows consisting of all x1,y1,x2,y2 respectively
    array_rows=array_rows.transpose()
  
    #x_array is a numpy array which contains all the x1 and x2
    #y_array is a numpy array which contains all the y1 and y2
    x_array=np.array([array_rows[0],array_rows[2]])
    y_array=np.array([array_rows[1],array_rows[3]])

    #flatten converts 2D array to 1D array
    x_array=x_array.flatten()
    y_array=y_array.flatten()
    
    #argsort returns the indexes after the elements pf the array are sorted

    x_sorted_indexes=np.argsort(x_array)
    y_sorted_indexes=np.argsort(y_array)
    
    #all the group of elements in x_array and y_array which have difference less than 5 are changed to average of the elements
    #the changes are made in the original coordinates of the lines
    #for x_array
    i=0
    while (i < (len(x_sorted_indexes)-1)):
        if((x_array[x_sorted_indexes[i+1]]-x_array[x_sorted_indexes[i]])<5):
            start=i
            end=i+1
            count=1
            sum=x_array[x_sorted_indexes[i]]
            while(x_array[x_sorted_indexes[end]]-x_array[x_sorted_indexes[start]]<5):
                sum+=x_array[x_sorted_indexes[end]]
                count=count+1
                end=end+1
                if(end==len(x_sorted_indexes)):
                    break
            for j in range(start,end):
                x_array[x_sorted_indexes[j]]=(sum/count)
            i=end-1
        i=i+1
    
    #for y_array

    i=0
    while (i < (len(y_sorted_indexes)-1)):
        if((y_array[y_sorted_indexes[i+1]]-y_array[y_sorted_indexes[i]])<=5):
            start=i
            end=i+1
            count=1
            sum=y_array[y_sorted_indexes[i]]
            while(y_array[y_sorted_indexes[end]]-y_array[y_sorted_indexes[start]]<=5):
                sum+=y_array[y_sorted_indexes[end]]
                count=count+1
                end=end+1
                if(end==len(y_sorted_indexes)):
                    break
            print(start,end)
            for j in range(start,end):
                print(j)
                y_array[y_sorted_indexes[j]]=(round(sum/count))
            i=end-1
        i=i+1

    print("length =",len(y_sorted_indexes))



    #conversion of points back into the original form after they have been edited to make their value equal to average
    x_array=x_array.reshape(2,n_rows)
    y_array=y_array.reshape(2,n_rows)
    #final array with 4 rows each consisting of all x1,y1,x2,y2 respectively
    final_array=np.array([x_array[0],y_array[0],x_array[1],y_array[1]])
    #final array after transpose with each row of the form [x1,y1,x2,y2]
    final_array=final_array.transpose()
    
    print("/////////////////")
    print(final_array)
    return final_array