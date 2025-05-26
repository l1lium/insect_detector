import os
import torch
from torch.utils.data import Dataset
from PIL import Image
import torchvision.transforms as transforms

class YoloDataset(Dataset):
    def __init__(self, image_dir, label_dir, image_size=256):
        self.image_dir = image_dir
        self.label_dir = label_dir
        self.image_size = image_size

        self.images = sorted([
            f for f in os.listdir(image_dir) if f.endswith(".png") or f.endswith(".jpg")
        ])

        self.transform = transforms.Compose([
            transforms.Resize((image_size, image_size)),
            transforms.ToTensor(),
        ])

    def __len__(self):
        return len(self.images)

    def __getitem__(self, index):
        img_name = self.images[index]
        img_path = os.path.join(self.image_dir, img_name)
        label_path = os.path.join(self.label_dir, img_name.replace('.png', '.txt').replace('.jpg', '.txt'))

        image = Image.open(img_path).convert("RGB")
        image = self.transform(image)

        with open(label_path, 'r') as f:
            line = f.readline().strip()
            class_id, x, y, w, h = map(float, line.split())

        class_id = int(class_id)
        bbox = torch.tensor([x, y, w, h], dtype=torch.float32)

        return image, class_id, bbox
