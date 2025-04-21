import sys
import numpy as np
from PIL import Image
from transformers import AutoImageProcessor, AutoModelForImageClassification
import json

# Map the model outputs to the specified labels
label_mapping = {
    "0": "Crack",
    "1": "Scratch",
    "2": "Tire Flat",
    "3": "Dent",
    "4": "Glass Shatter",
    "5": "Lamp Broken"
}

# Path to the local directory where the model files are saved
model_path = "C:/Users/samim/Desktop/amena/src/main/java/com/example/amena/python"  # Update this path to your local directory

# Load the model and image processor from the local directory
processor = AutoImageProcessor.from_pretrained(model_path)
model = AutoModelForImageClassification.from_pretrained(model_path)

# Load and process the image from Java input
image_path = sys.argv[1]  # Get the image path from the command line argument
image = Image.open(image_path)
inputs = processor(images=image, return_tensors="pt")

# Make predictions
outputs = model(**inputs)
logits = outputs.logits.detach().cpu().numpy()
predicted_class_id = np.argmax(logits)
predicted_proba = np.max(logits)

# Map predicted class ID to human-readable label
predicted_class_name = label_mapping[str(predicted_class_id)]

# Create a result in JSON format
result = {
    "label": predicted_class_name,
}

# Print the result as a JSON string (Java can read this)
print(json.dumps(result))