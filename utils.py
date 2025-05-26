from PIL import Image, ImageDraw, ImageFont
import io
import base64

def draw_bounding_boxes(image: Image.Image, detections: list):
    draw = ImageDraw.Draw(image)
    for det in detections:
        x_c, y_c, w, h = det['bbox']
        conf = det['confidence']
        class_id = det['class_id']
        
        # Convert from center to box corners
        left = x_c - w / 2
        top = y_c - h / 2
        right = x_c + w / 2
        bottom = y_c + h / 2
        
        # Scale if coordinates are normalized (assuming [0,1])
        width, height = image.size
        left *= width
        right *= width
        top *= height
        bottom *= height
        
        draw.rectangle([left, top, right, bottom], outline="red", width=3)
        draw.text((left, top - 10), f"ID:{class_id} {conf:.2f}", fill="red")
    return image

def pil_image_to_base64(img: Image.Image) -> str:
    buffered = io.BytesIO()
    img.save(buffered, format="JPEG")
    img_str = base64.b64encode(buffered.getvalue()).decode()
    return f"data:image/jpeg;base64,{img_str}"
