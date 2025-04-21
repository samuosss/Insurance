from fastapi import FastAPI
from pydantic import BaseModel
from transformers import pipeline

# Load the model
pipe = pipeline("text-classification", model="tajuarAkash/Health_Insurance_Fraud_detection_using_NLP")

# Initialize FastAPI app
app = FastAPI()

# Define request format
class TextRequest(BaseModel):
    text: str

# Define the prediction endpoint
@app.post("/predict")
def predict(request: TextRequest):
    result = pipe(request.text)
    return {"prediction": result}
