from yolo_dataset import YoloDataset
from backend.model.cnn_detector import InsectDetector
from torch.utils.data import DataLoader
import torch.nn as nn
import torch.optim as optim
import torch

# Directories
train_img = "data/images/training"
train_lbl = "data/labels/training"
val_img   = "data/images/validation"
val_lbl   = "data/labels/validation"

# Parameters
num_classes = 10
batch_size = 8
image_size = 256
epochs = 100
device = 'cuda' if torch.cuda.is_available() else 'cpu'

# Load datasets
train_dataset = YoloDataset(train_img, train_lbl, image_size)
val_dataset   = YoloDataset(val_img, val_lbl, image_size)

train_loader = DataLoader(train_dataset, batch_size=batch_size, shuffle=True)
val_loader   = DataLoader(val_dataset, batch_size=batch_size)

# Model
model = InsectDetector(num_classes).to(device)
criterion_cls = nn.CrossEntropyLoss()
criterion_bbox = nn.SmoothL1Loss()
optimizer = optim.Adam(model.parameters(), lr=1e-4)

# Training loop
for epoch in range(epochs):
    model.train()
    total_loss = 0
    for imgs, labels, boxes in train_loader:
        imgs, labels, boxes = imgs.to(device), labels.to(device), boxes.to(device)

        optimizer.zero_grad()
        out_cls, out_bbox = model(imgs)
        loss = criterion_cls(out_cls, labels) + criterion_bbox(out_bbox, boxes)
        loss.backward()
        optimizer.step()
        total_loss += loss.item()

    # Validation
    model.eval()
    val_loss = 0
    with torch.no_grad():
        for imgs, labels, boxes in val_loader:
            imgs, labels, boxes = imgs.to(device), labels.to(device), boxes.to(device)
            out_cls, out_bbox = model(imgs)
            val_loss += criterion_cls(out_cls, labels).item() + criterion_bbox(out_bbox, boxes).item()

    print(f"Epoch {epoch+1}/{epochs} | Train Loss: {total_loss:.3f} | Val Loss: {val_loss:.3f}")

# Save final model
torch.save(model.state_dict(), "insect_detector.pth")
