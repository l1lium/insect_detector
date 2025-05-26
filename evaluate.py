from yolo_dataset import YoloDataset
from model.cnn_detector import InsectDetector
from torch.utils.data import DataLoader
import torch
import torch.nn.functional as F

# Directories
test_img = "data/images/testing"
test_lbl = "data/labels/testing"

# Load dataset
dataset = YoloDataset(test_img, test_lbl, 256)
loader = DataLoader(dataset, batch_size=8)
model = InsectDetector(num_classes=10)
model.load_state_dict(torch.load("insect_detector.pth", map_location="cpu"))
model.eval()

# Accuracy
correct = total = 0
with torch.no_grad():
    for imgs, labels, boxes in loader:
        out_cls, _ = model(imgs)
        preds = torch.argmax(out_cls, dim=1)
        correct += (preds == labels).sum().item()
        total += labels.size(0)

print(f"Test Accuracy: {correct / total * 100:.2f}%")
