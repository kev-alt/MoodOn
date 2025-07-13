MoodOn – AI-Powered Emotional Support Mobile Application
MoodOn is an Android-based mobile application developed as a senior capstone project in Computer Engineering. It offers real-time emotional support by analyzing user input through Natural Language Processing (NLP) and generating personalized therapeutic feedback using AI models like GPT-4.

<img width="408" height="903" alt="image" src="https://github.com/user-attachments/assets/61552dec-83f9-4d62-8dec-9a00a0a92d80" />  <img width="410" height="912" alt="image" src="https://github.com/user-attachments/assets/e3561b22-8f7f-4e34-bef8-d251ef751d93" />  <img width="411" height="909" alt="image" src="https://github.com/user-attachments/assets/5963dce2-b0e0-4433-a343-6a11351f9486" />

🌟 Project Overview
MoodOn aims to help users cope with stress, anxiety, and emotional fluctuations by providing an accessible, AI-supported psychological assistant. Users can express themselves via text or voice, and the app performs emotion classification using a trained machine learning model. Based on the identified emotion, a personalized and empathetic response is generated via the OpenAI GPT-4 API.

🧠 Core Features
💬 Real-Time Emotional Chat (via ChatGPT)

✍️ Text & Voice Input Support

📊 Mood Tracking with Weekly Summaries

📔 Digital Diary with Cloud Backup

📁 Past Conversation History

🔐 Secure Login with Firebase Authentication

📡 Offline and Online Sync (Room + Firestore)

🛠️ Technologies Used
📱 Mobile (Frontend)
Android Studio – Development environment

Kotlin + Jetpack Compose – Declarative UI framework

Room Database – Local offline data storage

Retrofit – API integration

🧩 Backend
Python + FastAPI – Lightweight and asynchronous REST API

Scikit-learn + Joblib – Emotion classification model

OpenAI GPT-4.1 API – Therapeutic response generation

☁️ Data Management
Firebase Firestore – Cloud data storage

Firebase Authentication – Secure identity verification


🔍 Emotion Analysis Model
Data Sources: User-generated content from Reddit & Ekşi Sözlük

Preprocessing:

Lowercasing, HTML/emoji/URL removal

Turkish lemmatization (Zemberek NLP)

Vectorization: TF-IDF

Model: Multiclass Logistic Regression

Performance:

Accuracy: ~87%

Balanced Precision, Recall, and F1-Score

📷 Sample Screenshots

<img width="400" height="903" alt="image" src="https://github.com/user-attachments/assets/7ad52d6f-dd13-4b00-9c28-2ce542e8691c" />  <img width="404" height="898" alt="image" src="https://github.com/user-attachments/assets/34979592-f38d-428a-8ef7-7f35765a015e" />  <img width="403" height="900" alt="image" src="https://github.com/user-attachments/assets/8bb23a68-97b9-4783-97f9-11e61283dd2c" />  <img width="408" height="899" alt="image" src="https://github.com/user-attachments/assets/c0b441c9-cff3-40f1-b80b-b195e91a2452" />  <img width="411" height="894" alt="image" src="https://github.com/user-attachments/assets/81ece276-6cc6-418a-a533-ba5ffd05da65" />

📖 Research and Design Foundations
Natural Language Processing (NLP)

Sentiment Analysis using Machine Learning

Prompt Engineering for therapeutic support

Human-Computer Interaction (HCI)

Digital Mental Health & CBT Principles



 




