try:
    from PIL import Image
except ImportError:
    import Image
import pytesseract

# If you don't have tesseract executable in your PATH, include the following:
pytesseract.pytesseract.tesseract_cmd = r'<full_path_to_your_tesseract_executable>'
# Example tesseract_cmd = r'C:\Program Files (x86)\Tesseract-OCR\tesseract'

# Simple image to string
print(pytesseract.image_to_string(Image.open('test.jpg')))

# French text image to string
print(pytesseract.image_to_string(Image.open('test.jpg'), lang='eng'))

# Get bounding box estimates
print(pytesseract.image_to_boxes(Image.open('test.jpg')))

# Get verbose data including boxes, confidences, line and page numbers
print(pytesseract.image_to_data(Image.open('test.jpg')))

# Get information about orientation and script detection
print(pytesseract.image_to_osd(Image.open('test.jpg')))

# In order to bypass the internal image conversions, just use relative or absolute image path
# NOTE: If you don't use supported images, tesseract will return error
print(pytesseract.image_to_string('test.jpg'))

# get a searchable PDF
pdf = pytesseract.image_to_pdf_or_hocr('test.jpg', extension='pdf')

# get HOCR output
hocr = pytesseract.image_to_pdf_or_hocr('test.jpg', extension='hocr')