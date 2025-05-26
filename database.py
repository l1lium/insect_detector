from sqlalchemy import create_engine, Column, Integer, Float, String
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

DATABASE_URL = "sqlite:///./detections.db"

engine = create_engine(DATABASE_URL, connect_args={"check_same_thread": False})

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

class Detection(Base):
    __tablename__ = "detections"

    id = Column(Integer, primary_key=True, index=True)
    filename = Column(String, index=True)
    class_id = Column(Integer)
    confidence = Column(Float)
    bbox_x = Column(Float)
    bbox_y = Column(Float)
    bbox_w = Column(Float)
    bbox_h = Column(Float)

def init_db():
    Base.metadata.create_all(bind=engine)
