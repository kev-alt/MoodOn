import joblib, pandas as pd, pathlib
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import classification_report, confusion_matrix

# ---------- Dosya yolları ----------
DATA_PATH = "dataset_clean.csv"          # <- temiz & lemmatize veri
MODEL_DIR = pathlib.Path("model")
MODEL_DIR.mkdir(exist_ok=True)

TEXT_COL  = "Yorum"
LABEL_COL = "Duygu"

# ---------- 1) Veriyi Böl ----------
df = pd.read_csv(DATA_PATH)
X_train, X_test, y_train, y_test = train_test_split(
    df[TEXT_COL], df[LABEL_COL],
    test_size=0.2,
    random_state=42,
    stratify=df[LABEL_COL]
)

# ---------- 2) TF‑IDF ----------
tfidf = TfidfVectorizer(
    ngram_range=(1, 2),       # unigrams + bigram
    max_features=30_000,
    sublinear_tf=True         # log‑tf
)
X_train_vec = tfidf.fit_transform(X_train)
X_test_vec  = tfidf.transform(X_test)

# ---------- 3) LogisticRegression ----------
clf = LogisticRegression(
        max_iter=1000,
        solver="lbfgs",
        multi_class="multinomial"
     ).fit(X_train_vec, y_train)

# ---------- 4) Değerlendirme ----------
print("Test Accuracy :", clf.score(X_test_vec, y_test))
print("\nClassification Report:\n",
      classification_report(y_test, clf.predict(X_test_vec)))

cv_acc = cross_val_score(
            clf, tfidf.transform(df[TEXT_COL]), df[LABEL_COL], cv=5
         ).mean()
print(f"5‑Fold CV Accuracy: {cv_acc:.3f}")

# ---------- 5) Kaydet ----------
joblib.dump(clf,   MODEL_DIR / "emotion_classifier.joblib")
joblib.dump(tfidf, MODEL_DIR / "tfidf_vectorizer.joblib")
print("✅ Model & vektörizer 'model/' klasörüne kaydedildi.")
