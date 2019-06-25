import os, csv, math, numpy as np
import scipy
from PIL import Image, ImageDraw

#runs tesseract on the whole image and stores output in a tsv file. config_file stores the configuration to run first pass and is stored in folder /usr/local/share/tessdata/configs/
def tesseract_pass1(img_name, op_name):
	os.system("tesseract "+img_name+" "+op_name+" -tessdata-dir=/usr/local/share/ tsv config_file" )

#runs tesseract on each image independently, in a single run. Single run is possible because all label names are written in file_name, psm=10 for single character
def tesseract_pass2(file_name, op_name):
	os.system("tesseract "+file_name+" "+op_name+" -c tessedit_pageseg_mode=10 tsv" )

#parses output of tesseract pass1 and stores the labels with confidence > confidence_thresh. output is list of dictionaries
def parse_tsv(filename, scale, confidence_thresh):
	
	#reads lines from tsv file and stores heading in a different list
	with open(filename,'r') as tsv:
	    rows = [line.strip().split('\t') for line in tsv]
	detected_labels=[]
	heading = rows[0]
	rows.pop(0)

	#for each row in the tsv file, if the confidence is high, scale down the coordinates, height and width and store it in detected_labels
	for r in rows:
		d=dict(zip(heading, r))
		if(int(d["conf"])>=confidence_thresh):
			d["left"] = int(d["left"])/scale
			d["top"] = int(d["top"])/scale
			d["height"] = int(d["height"])/scale
			d["width"] = int(d["width"])/scale
			detected_labels.append(d)

	print("detected_labels:")
	for l in detected_labels:
		print(str(l["text"]))
	
	return detected_labels

#parses output of tesseract pass1 and stores the labels with confidence > confidence_thresh. output is list of dictionaries
def parse_tsv2(filename, left_labels, labels, confidence_thresh):

	#reads lines from tsv file and stores heading in a different list
	with open(filename,'r') as tsv:
	    rows = [line.strip().split('\t') for line in tsv]
	detected_labels=[]
	heading = rows[0]
	rows.pop(0)

	#for each row in the tsv file, if the confidence is high, store the label and bounding box coordinates are taken from the real input of scipy.labels. (tesseract coordinates are according to the independent image) (coordinates are already in original scale)
	for r in rows:
		d=dict(zip(heading, r))
		if(int(d["conf"]) >= confidence_thresh):
			key = left_labels[int(d["page_num"])-1]
			img, bb = labels[key] #bb: [(x, y), height, width]
			d["left"] = bb[0][0]
			d["top"] = bb[0][1]-bb[1]
			d["height"] = bb[1]
			d["width"] = bb[2]
			detected_labels.append(d)
	return detected_labels

#checks if (x1, y1) and (x2, y2) are close
def isclose(x1, y1, x2, y2, thresh_close):
	#eucledian distance d
	d = math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1))
	# print(d)
	if(d<thresh_close):
		return True
	return False

#join l1 and l2
#TODO: currently joining means appending two labels. This wont work for characters like 'i' where we have to join '.'' above the character 
def join(l1, l2):
	#appends width and text, takes height of the maximum
	l1["text"] = l1["text"]+l2["text"]
	l1["height"] = max(l1["height"], l2["height"])
	l1["width"] = (l2["left"]-l1["left"])+l2["width"]
	return l1

#checks if labels l1 and l2 are continuous and joins them
def iscontinuous(l1, l2, flag, i, j, thresh_close):

	x1, y1, h1, w1 = l1["left"], l1["top"], l1["height"], l1["width"]
	x2, y2, h2, w2 = l2["left"], l2["top"], l2["height"], l2["width"]

	# print(x1, y1, h1, w1, x2, y2, h2, w2)

	#if the bottom right corner of l1 and bottom left corner of l2 are close then join them
	if(isclose(x1+w1, y1, x2, y2, thresh_close)):
		# print("isclose")
		flag[j] = 1 #mark it as joined 
		l1 = join(l1, l2)
	return l1

#joins characters to words
def join_labels(detected_labels, thresh_close):
	#detected _labels is a list of dictionaries
	final_loc = []

	#sorting detected_labels on left point
	sorted_loc=sorted(detected_labels, key=lambda kv: kv["left"])
	
	n = len(sorted_loc)
	flag={i:0 for i in range(n)} #flag=1 for labels that are joined

	for i in range(n):
		if(flag[i]==1):
			continue
		l1=sorted_loc[i]
		for j in range(i+1, n):
			if(flag[j]==1):
				continue
			#check if the labels are continuous then joins it
			l1 = iscontinuous(sorted_loc[i], sorted_loc[j], flag, i, j, thresh_close)
			# print("l1: "+str(l1["text"])+" "+sorted_loc[i]["text"]+" "+sorted_loc[j]["text"])
		#appends the result in the final list
		final_loc.append(l1)
	# print(" len(final_loc): "+str(len(final_loc)))
	return final_loc

#calculates area of a rectangle = [left, bottom, top, right]
def area(rect):
	[left, bottom, top, right] = rect
	return (right-left)*(bottom-top)
	
#checks if the two bounding boxes, rect1 and rect2 overlaps
def overlaps(rect1, rect2):
	#rect: [left, bottom, top, right]
	sa = area(rect1) #area of rect1
	sb = area(rect2) #area of rect2
	si = area([max(rect1[0], rect2[0]), min(rect1[1], rect2[1]), max(rect1[2], rect2[2]), min(rect1[3], rect2[3])]) #area of the intersection of rect1 and rect2

	x = (si*100)/(sb) #percentage of rect2 that is involved in the intersection
	if((rect2[0]>=rect1[0] and rect2[0]<=rect1[3]) or (rect2[3]>=rect1[0] and rect2[3]<=rect1[3])): #checks if the rectangles intersect in x directeion
		if((rect2[1]>=rect1[2] and rect2[1]<=rect1[1]) or (rect2[2]>=rect1[2] and rect2[2]<=rect1[1])): #checks if the rectangles intersect in y directeion
			#if rect2 is small then the percentage of intersection is less
			if((x>=80 and sb >15) or (x>=50 and sb <=15)):
				return True
			return False


#returns sets of labels not detected in tesseract pass1
def remove_detected(detected_labels, labels):
	flag={i:0 for i in labels.keys()}#flag=1 if this label has been detected in pass1
	left_labels=[]
	label_list=list(labels.items())

	#check which label overlaps with the detected labels
	for d in detected_labels:
		#rect: [left, bottom, top, right]
		rect1 = [int(d["left"]), int(d["top"])+int(d["height"]), int(d["top"]), int(d["left"])+int(d["width"]) ]

		#label_list: [[key, [img, [x, y, height, width]]], [key, ...]]
		for list_item in label_list:
			key = list_item[0]
			if(flag[key]==1): #if this label is detected then skip it
				continue

			bb = list_item[1][1] #bounding box
			rect2 = [bb[0][0], bb[0][1], (bb[0][1]-bb[1]), (bb[0][0]+bb[2])]

			#if the two bounding boxes overlap, it means rect2 is detected
			if(overlaps(rect1, rect2)):
				flag[key]=1 #mark it detected
	
	#keys of all labels that are not detected are stored in left_labels
	for l in label_list:
		if(flag[l[0]]!=1):
			left_labels.append(l[0])

	return left_labels

#preprocess label arrays
def process_label(labels):  
	label_image={}

	# appending a row of background pixels -up/down/left/right- to each label
	for i, t in labels.items():
		s=t[0]
		y=np.ones((5, s.shape[1])).astype(int)*255
		# print(s.shape, y.shape)
		s=np.append(y,s,axis=0)
		s=np.append(s,y,axis=0)
		x=np.ones((s.shape[0],5)).astype(int)*255
		
		s=np.append(x,s,axis=1)
		s=np.append(s,x,axis=1)
		
		# converting to 255/0 format (earlier it was in 0/nonzero format denoting background/foreground)
		boolean=s==(i+1)
		segment=255-(255-s)*boolean
		
		label_image[i]=(segment)
	return label_image

	
#resize and saves labels as png images, label_names in text file
def save_label(label_image, path_op, img_name, filename, left_labels, scale, dpi_value):
	
	label_names = ""
	# saving each label as png
	for ix in left_labels:
		scipy.misc.imsave(path_op+img_name+str(ix)+".png", label_image[ix]) 
		resize_image(path_op+img_name+str(ix), scale, dpi_value) #resize images
		label_names += path_op+img_name+str(ix)+"_high.png\n" 
		
	#store the label names in one text file
	f = open(filename,"w")
	f.write(label_names)
	f.close()

#resizes image_name by scaling up by value 'scale' and dpi = dpi_value
def resize_image(image_name, scale, dpi_value):
	im = Image.open(image_name+".png")
	r, c = im.size
	im_resized = im.resize((r*scale, c*scale), Image.BICUBIC)
	im_resized.save(image_name+"_high.png", dpi=dpi_value)

def detect_label(template_folder, path_op, img_name, labels, label_img_name, label_high_imag_name, path_op_labels):

	scale = 4 #image is scaled up by this value
	dpi_value = (1000, 1000) #the resized image has this dpi (dots per inch)
	#image with labels only is scaled up
	resize_image(path_op+img_name+"_label", scale, dpi_value)

	#run tesseract pass 1 on the entire image where only labels are present
	tesseract_pass1(label_high_imag_name, template_folder+img_name+"_tsv")

	confidence_thresh=70
	#parses the output of tesseract 1 and extracts labels with high confidence
	detected_labels = parse_tsv(template_folder+img_name+"_tsv.tsv", scale, confidence_thresh)
	
	# print("labels bounding boxes: ")
	# for ix, l in labels.items():
	# 	print(l[1])

	#left_labels are the labels that are not detected in tesseract pass1
	left_labels=remove_detected(detected_labels, labels)

	# print("left labels bounding box: ")
	# for l in left_labels:
	# 	print(str(labels[l][1]))

	#drawing boxes around labels that are nit detected yet
	im = Image.open(label_img_name)
	draw = ImageDraw.Draw(im)
	for l in left_labels: 
		bb = labels[l][1]
		draw.rectangle((bb[0][0], bb[0][1], bb[0][0]+bb[2], bb[0][1]-bb[1]))
	im.save(path_op+img_name+"_tesseract_pass1.png")


	#if there are any labels left, run tesseract pass2
	if(len(left_labels)!=0):
		#pre-process and store label images, and their names in a text file
		label_image=process_label(labels)
		save_label(label_image, path_op_labels, img_name, template_folder+img_name+"_image_names", left_labels, scale, dpi_value)

		tesseract_pass2(template_folder+img_name+"_image_names", template_folder+img_name+"_tsv2")

		#parse the output of tesseract pass2
		detected_labels2 = parse_tsv2(template_folder+img_name+"_tsv2.tsv", left_labels, labels, confidence_thresh)
		#append the results to resullts of tesseract pass1
		detected_labels= detected_labels+detected_labels2


	for l in (detected_labels):
		print(l["text"])


	thresh_close = 8 #threshold to check if two characters are close enough
	#join characters to words 
	final_labels = join_labels(detected_labels, thresh_close)

	if(len(final_labels) >0):
		#store the final result
		with open(template_folder+img_name+"_label.csv", "w") as f:
			dict_writer = csv.DictWriter(f, final_labels[0].keys())
			dict_writer.writeheader()
			dict_writer.writerows(final_labels)


	for l in final_labels:
		print(str(l["text"])+" "+str(l["conf"])+" "+str(l["left"])+" "+str(l["top"])+" "+str(l["height"])+" "+str(l["width"]))
