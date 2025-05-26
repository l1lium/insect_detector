import torch
from torchvision import transforms
from PIL import Image
from model.cnn_detector import InsectDetector

device = 'cuda' if torch.cuda.is_available() else 'cpu'

# Load model
model = InsectDetector(num_classes=10)
model.load_state_dict(torch.load("insect_detector.pth", map_location=device))
model.to(device)
model.eval()

# Image transforms (match training)
transform = transforms.Compose([
    transforms.Resize((256, 256)),
    transforms.ToTensor(),
])


def predict(image: Image.Image):
    img_t = transform(image).unsqueeze(0).to(device)
    with torch.no_grad():
        class_logits, bbox = model(img_t)
        probs = torch.softmax(class_logits, dim=1)
        conf, class_id = torch.max(probs, dim=1)
        
        bbox = bbox[0].cpu().tolist()  # [x_center, y_center, w, h]
        
        return {
            'class_id': int(class_id.item()),
            'confidence': float(conf.item()),
            'bbox': bbox,
        }
