# model/cnn_detector.py

import torch
import torch.nn as nn

class InsectDetector(nn.Module):
    def __init__(self, num_classes=10):
        super(InsectDetector, self).__init__()
        self.features = nn.Sequential(
            nn.Conv2d(3, 16, 3, padding=1), nn.ReLU(), nn.MaxPool2d(2),
            nn.Conv2d(16, 32, 3, padding=1), nn.ReLU(), nn.MaxPool2d(2),
            nn.Conv2d(32, 64, 3, padding=1), nn.ReLU(), nn.MaxPool2d(2),
            nn.Conv2d(64, 128, 3, padding=1), nn.ReLU(), nn.MaxPool2d(2),
        )

        self.flatten = nn.Flatten()

        self.classifier = nn.Sequential(
            nn.Linear(128 * 16 * 16, 256),
            nn.ReLU(),
            nn.Linear(256, num_classes),
        )

        self.bbox_regressor = nn.Sequential(
            nn.Linear(128 * 16 * 16, 256),
            nn.ReLU(),
            nn.Linear(256, 4),  # [x_center, y_center, width, height]
        )

    def forward(self, x):
        x = self.features(x)
        x = self.flatten(x)
        class_logits = self.classifier(x)
        bbox = self.bbox_regressor(x)
        return class_logits, bbox
