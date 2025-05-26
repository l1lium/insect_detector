from flask import Flask, request, jsonify, send_file
from flask_cors import CORS
import torch
from PIL import Image, ImageDraw
import io
import os
import uuid
import sqlite3
from model.cnn_detector import InsectDetector
from torchvision import transforms
import imghdr

app = Flask(__name__)
CORS(app)

UPLOAD_FOLDER = './uploads'
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

# Load your model once
device = 'cuda' if torch.cuda.is_available() else 'cpu'
model = InsectDetector(num_classes=10)
model.load_state_dict(torch.load("insect_detector.pth", map_location=device))
model.to(device)
model.eval()

transform = transforms.Compose([
    transforms.Resize((256, 256)),
    transforms.ToTensor(),
])

# Initialize or connect to SQLite DB
def init_db():
    conn = sqlite3.connect('detections.db')
    c = conn.cursor()
    c.execute('''
        CREATE TABLE IF NOT EXISTS detections (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            filename TEXT,
            class_id INTEGER,
            confidence REAL,
            bbox_x REAL,
            bbox_y REAL,
            bbox_w REAL,
            bbox_h REAL
        )
    ''')
    conn.commit()
    conn.close()

init_db()


def save_detection_to_db(filename, class_id, confidence, bbox):
    conn = sqlite3.connect('detections.db')
    c = conn.cursor()
    c.execute('''
        INSERT INTO detections (filename, class_id, confidence, bbox_x, bbox_y, bbox_w, bbox_h)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    ''', (filename, class_id, confidence, *bbox))
    conn.commit()
    conn.close()


def draw_bbox(image, bbox, class_id, confidence):
    draw = ImageDraw.Draw(image)
    x_c, y_c, w, h = bbox
    width, height = image.size
    # bbox coords are normalized [0,1], convert to pixels:
    left = (x_c - w / 2) * width
    top = (y_c - h / 2) * height
    right = (x_c + w / 2) * width
    bottom = (y_c + h / 2) * height
    draw.rectangle([left, top, right, bottom], outline="red", width=3)
    draw.text((left, top - 10), f"ID:{class_id} {confidence:.2f}", fill="red")


@app.route('/predict', methods=['POST'])
def predict():
    if 'file' not in request.files:
        return jsonify({"error": "No file uploaded"}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "No file selected"}), 400

    # Read the file stream once and keep it in memory
    file_bytes = file.read()
    file_stream = io.BytesIO(file_bytes)

    try:
        image = Image.open(file_stream).convert("RGB")
    except Exception:
        return jsonify({"error": "Invalid image"}), 400

    img_t = transform(image).unsqueeze(0).to(device)

    with torch.no_grad():
        class_logits, bbox = model(img_t)
        probs = torch.softmax(class_logits, dim=1)
        confidence, class_id = torch.max(probs, dim=1)
        confidence = confidence.item()
        class_id = class_id.item()
        bbox = bbox[0].cpu().tolist()

    annotated_img = image.copy()
    draw_bbox(annotated_img, bbox, class_id, confidence)

    # Get extension
    ext = os.path.splitext(file.filename)[1].lower()
    if ext not in [".jpg", ".jpeg", ".png"]:
        # Use imghdr on the same bytes we already read
        ext = f".{imghdr.what(None, file_bytes)}" or ".jpg"

    filename = f"{uuid.uuid4()}{ext}"
    file_path = os.path.join(UPLOAD_FOLDER, filename)

    try:
        annotated_img.save(file_path)
    except Exception as e:
        return jsonify({"error": f"Failed to save image: {str(e)}"}), 500

    save_detection_to_db(filename, class_id, confidence, bbox)

    return jsonify({
        "filename": filename,
        "class_id": class_id,
        "confidence": confidence,
        "bbox": bbox,
        "image_path": f"/uploads/{filename}"
    })


@app.route('/uploads/<filename>')
def uploaded_file(filename):
    file_path = os.path.join(UPLOAD_FOLDER, filename)
    if os.path.exists(file_path):
        return send_file(file_path)
    return jsonify({"error": "File not found"}), 404


@app.route('/detections', methods=['GET'])
def get_detections():
    conn = sqlite3.connect('detections.db')
    c = conn.cursor()
    c.execute('SELECT filename, class_id, confidence, bbox_x, bbox_y, bbox_w, bbox_h FROM detections ORDER BY id DESC')
    rows = c.fetchall()
    conn.close()

    detections = []
    for row in rows:
        detections.append({
            "filename": row[0],
            "class_id": row[1],
            "confidence": row[2],
            "bbox": [row[3], row[4], row[5], row[6]]
        })

    return jsonify(detections)


if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000, debug=True)