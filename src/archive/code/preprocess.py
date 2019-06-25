import sys, copy, cv2, scipy, os
import scipy
from skimage.morphology import thin
import numpy as np
from skimage.filters import threshold_sauvola
from PIL import Image
# from label_detection import detect_label

#Sauvola binarization- local thresholding technique: Instead of calculating a single global threshold for the entire image, several thresholds are calculated for every pixel by using specific formulae that take into account the mean and standard deviation of the local neighborhood (defined by a window centered around the pixel)
#Reference: J. Sauvola and M. Pietikainen,"Adaptive document image binarization",Pattern Recognition 33(2), pp. 225-236, 2000. Elsevier. 
#URL: DOI:10.1016/S0031-3203(99)00055-2

def binarize_image(image):
	image_copy_gray = copy.deepcopy(image)
	window_size = 33 #Parameter window_size determines the size of the window that contains the surrounding pixels, window size must be odd and greater than 1
	thresh_sauvola = threshold_sauvola(image, window_size=window_size) #Return matrix of thresholds
	binary_sauvola = image > thresh_sauvola	#Comparing pixel value with the threshold
	image_copy_gray[binary_sauvola == False] = 0  
	image_copy_gray[binary_sauvola == True] = 255
	return image_copy_gray, binary_sauvola

#thins the binary_sauvola
def thin_image(binary_sauvola, image):
	image1 = copy.deepcopy(image)
	#thinning the binary image (black: false, white: true)
	thinned = thin(binary_sauvola==0, max_iter = 9) #binary_sauvola==0 means thin the pixels with value 0(since foreground has value 0)
	#max_iter: Regardless of this parameter, the thinned image is returned immediately if an iteration produces no change. If this parameter is specified it thus sets an upper bound on the number of iterations performed.

	# image_res is a thinned grayscale image(unique values 0 and 255)
	image1[thinned==True]=0
	image1[thinned==False]=255
	return image1
	
#marks different connected components
def mark_labels(image):                                                                  
	#scipy.ndimage.label takes 0 to be background and all non-zero to be foreground, so we need to change image as follows:

	# setting foreground pixels to 1 (initially foreground pixels [black] have value 0 in grayscale format)
	image[image == 0] = 1  
	# setting background pixels to 0
	image[image == 255] = 0

	# array([[1,1,1],[1,1,1],[1,1,1]]) is A structuring element that defines feature connections. 
	# marked_array is an array where elements of each disconnected component are numbered uniquely, num_labels is the number of disconnected components
	marked_array, num_labels = scipy.ndimage.label(image,np.array([[1,1,1],[1,1,1],[1,1,1]]))
	#structuring element ([[0,1,0],[1,1,1],[0,1,0]]) works on our dataset which has thick edges in diagrams. For thin edged diagrams or to be on the safe side, use structuring element ([[1,1,1],[1,1,1],[1,1,1]]) - it might join some nearly connected labels with main diagram but at least the main diagram edges will surely not be lost during disconnection.

	# find_objects returns an array of bounding box coordinates of slices found [((x1,x2), (y1,y2)) , (..)] where x1x2y1y2 are coordinates for bounding box of first slice (ie. object)
	loc_array = scipy.ndimage.find_objects(marked_array)  

	return marked_array, loc_array


#save and disconnects bounding box of labels
def disconnect_label(label_array, loc_array): 

	#slices is a dictionary having index as key and the original image of that disconnected component as value
	slices={ix:label_array[i] for ix, i in enumerate(loc_array)}	#?? images of every disconnected component 

	main_diagram = max(list(slices), key = lambda s: (slices[s].shape[0]*slices[s].shape[1]))	#?? Determining slice having max area
	
	labels={} #labels dictionary: {index: [original image, [lower left corner pixel, Height, width]]} of the component whose area is small enough to be considered as a label
	main_diagrams={} #main_diagrams dictionary: {index: [original image, [lower left corner pixel, height, width]]} of the component whose area is big
	diagram_only = copy.deepcopy(label_array)

	for i, s in enumerate(loc_array):
		(m,n) = s #m: (y.start, y.stop) n:(x.start:x.stop) bounding box coordinates
		list_info=[] #= [lower left corner pixel, height, width]
		list_info.append((n.start, m.stop-1)) #lower left corner pixel (x,y) 
		list_info.append(m.stop-m.start)#height	#Height of slice
		list_info.append(n.stop-n.start)#width	#Width of slice

		if(slices[i].shape[0]>slices[i].shape[1]):
			width = slices[i].shape[1]
			length = slices[i].shape[0]
		else:
			width = slices[i].shape[0]
			length = slices[i].shape[1]
		if(slices[i].shape[0]*slices[i].shape[1]< 0.02*slices[main_diagram].shape[0]*slices[main_diagram].shape[1] and (width/length > 0.1)):
			labels[i]=[slices[i], list_info]

			#temp_array stores the bounding box denoted by current slice s
			temp_array = diagram_only[s]
			temp_array[temp_array == i+1]=0 #erase labels from original diagram
			diagram_only[s] = temp_array
		else:
			main_diagrams[i] = [slices[i], list_info]

	
	#diagram_only stores only labels and diagram_only stores the image where labels are erased
	label_only = label_array - diagram_only
	#marking all labelled pixels uniformly with 1
	label_only[label_only != 0] = 1
	

	# all values in diagram_only which are nonzero are multiplied by 255 and stored in b
	b = diagram_only*255 != 0
	# following 2 lines convert 0 to 255 (as background) and non zero to 0 (foreground). resulting array is diagram_only.
	diagram_only = 255-(255-diagram_only)*b
	diagram_only[diagram_only<255] = 0


	b = label_only*255 != 0
	label_only = 255-(255-label_only)*b
	label_only[label_only<255] = 0

	return diagram_only, label_only, labels


def main():
	img_name = sys.argv[1]   # command line  argument
	path = sys.argv[2]# input image path
	path_op = sys.argv[3] # path to output folder

	# 255: white 0: black
	image = cv2.imread(path+img_name,0)  # 0 indicates reading image in grayscale
	img_name = img_name.split(".")[0]; # extracts 104 from 104.png/104.jpg

	# padding the input image by 5 pixels on all 4 sides
	y=np.ones((5, image.shape[1])).astype(int)*255
	image=np.append(y,image,axis=0)
	image=np.append(image,y,axis=0)
	x=np.ones((image.shape[0],5)).astype(int)*255
	image=np.append(x,image,axis=1)
	image=np.append(image,x,axis=1) 
	scipy.misc.imsave(path_op+img_name+"_afterpadding.png",image)

	binary_image, binary_savoula = binarize_image(image)
	scipy.misc.imsave(path_op+img_name+"_sauvola.png",binary_image)

#------------disconnect before thinning-------------------------------------
	label_array, loc_array= mark_labels(binary_image)

	diagram_only, label_only, labels = disconnect_label(label_array, loc_array)
	scipy.misc.imsave(path_op+img_name+"_disconnected.png", diagram_only)
	scipy.misc.imsave(path_op+img_name+"_label.png", label_only)

	thinned_image = thin_image(diagram_only, binary_image)
	scipy.misc.imsave(path_op+img_name+"_thin.png",thinned_image)   #using thinned_partial	


main()