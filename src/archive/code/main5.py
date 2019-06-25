try:
    from PIL import Image
except ImportError:
    import Image
import pytesseract

# Get bounding box estimates
print(pytesseract.image_to_boxes(Image.open('test.jpg')))