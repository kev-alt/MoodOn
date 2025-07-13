from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import joblib
import pathlib

# -------- Model Yükleme --------
MODEL_DIR = pathlib.Path(__file__).parent / "model"
try:
    tfidf = joblib.load(MODEL_DIR / "tfidf_vectorizer.joblib")
    clf = joblib.load(MODEL_DIR / "emotion_classifier.joblib")
except FileNotFoundError as e:
    raise RuntimeError(
        "Model dosyaları bulunamadı. Lütfen önce 'train_model.py' dosyasını çalıştırın."
    ) from e

# -------- FastAPI Uygulaması --------
app = FastAPI(
    title="Emotion Classifier API",
    description="TF‑IDF + Logistic Regression temelli duygu sınıflandırma servisi",
    version="1.0.0"
)

# -------- Veri Modelleri --------
class TextIn(BaseModel):
    sentence: str

class PredictionOut(BaseModel):
    emotion: str
@app.get("/")
def root():
    return {"message": "Duygu sınıflandırma servisi çalışıyor ✅"}

# -------- API Endpoint --------
@app.post("/predict", response_model=PredictionOut)
def predict(data: TextIn):
    if not data.sentence.strip():
        raise HTTPException(status_code=400, detail="Boş cümle gönderildi.")
    
    X = tfidf.transform([data.sentence])
    pred = clf.predict(X)[0]
    return {"emotion": pred}
