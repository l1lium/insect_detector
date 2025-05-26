from pydantic import BaseModel

class DetectionResult(BaseModel):
    class_id: int
    confidence: float
    bbox: list[float]  # [x_center, y_center, width, height]

class DetectionResponse(BaseModel):
    filename: str
    detections: list[DetectionResult]
    annotated_image: str  # base64 encoded image string
