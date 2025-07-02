from flask import Flask, request, jsonify, send_file
import requests
import os
import tempfile

app = Flask(__name__)


def load_deepai_key(path='deepai_key.txt'):
    try:
        with open(path, 'r') as f:
            return f.read().strip()
    except FileNotFoundError:
        print(f"Archivo {path} no encontrado. Define la clave DEEPAI_API_KEY.")
        return None

# DeepAI
DEEPAI_API_KEY = load_deepai_key()
DEEPAI_URL = "https://api.deepai.org/api/nsfw-detector"

if not DEEPAI_API_KEY:
    raise Exception("The DEEPAI_API_KEY is not defined.")


@app.route('/moderate', methods=['POST'])
def moderate_image():
    if 'file' not in request.files:
        return jsonify({'error': 'No file was provided.'}), 400

    file = request.files['file']

    if file.filename == '':
        return jsonify({'error': 'Empty filename.'}), 400

    if file.content_type not in ['image/jpeg', 'image/png']:
        return jsonify({'error': 'Unsupported image format.'}), 400

    try:
        # Crear archivo temporal sin eliminarlo automáticamente
        temp_file = tempfile.NamedTemporaryFile(delete=False)
        temp_file_name = temp_file.name
        try:
            file.save(temp_file_name)
            temp_file.close()  # Cierra para que pueda ser leído en Windows

            with open(temp_file_name, 'rb') as f:
                response = requests.post(
                    DEEPAI_URL,
                    files={'image': (file.filename, f)},
                    headers={'api-key': DEEPAI_API_KEY}
                )
        finally:
            # Borra el archivo temporal
            os.unlink(temp_file_name)       

        data = response.json()
    except Exception as e:
        print(f"Error contacting DeepAI: {e}")
        return jsonify({'error': f'Error contacting DeepAI: {str(e)} '}), 500

    try:
        nsfw_score = data['output']['nsfw_score']
    except KeyError:
        print("Unexpected response from DeepAI: ", data)
        return jsonify({'error': 'Unexpected response from DeepAI.'}), 500

    if nsfw_score > 0.7:
        return jsonify({'status': 'REJECTED', 'reason': 'NSFW content'})
    else:
        return jsonify({'status': 'OK'})


@app.route('/')
def index():
    return send_file('index.html')

if __name__ == '__main__':
    app.run(debug=True)

