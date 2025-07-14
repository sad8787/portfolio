
from flask import Flask, render_template, request
from llama_cpp import Llama

# ROUTE to MODEL GGUF
ROUTE_MODEL_tiny = "C:\\llama_models\\tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf"
ROUTE_MODEL_2 = "C:\\llama_models\\llama-2-7b-chat.Q4_K_M.gguf"
# Prompt del sistema: define cómo debe comportarse la IA
prompt_sistema = (
        "Ведите себя как агент российского КГБ.\n"
        " Всегда отвечайте по-русски.\n"        
    )

# model
llm_2 = Llama( model_path=ROUTE_MODEL_2,n_ctx=2048, n_threads=8)
print("*****************************************/n")
print("lama_2 redy")
# model
llm_tiny = Llama( model_path=ROUTE_MODEL_tiny, n_ctx=2048,n_threads=8 )# Ajusta según tu CPU
print("*****************************************/n")
print("lama_tiny redy")

app = Flask(__name__)

# "Ответ"
def response_model(prompt_usuario,modelname):    
    
    prompt = (
        prompt_sistema +
        f"Usuario: {prompt_usuario}\n"
        "IA:"
    )
    # Llamada al modelo
    
    if(modelname=="tiny"):
        resultado = llm_tiny(prompt, max_tokens=150, stop=["Usuario:"])
        return resultado["choices"][0]["text"].strip()
    else:
        resultado = llm_2(prompt, max_tokens=150, stop=["Usuario:"])
        return resultado["choices"][0]["text"].strip()
    

@app.route("/", methods=["GET", "POST"])
def index():
    respuesta = ""
    modelo_seleccionado = "tiny"  # valor por defecto
    if request.method == "POST":
        mensaje_usuario = request.form["mensaje"]
        modelo_seleccionado = request.form.get("modelname", "tiny")
        respuesta = response_model(mensaje_usuario, modelo_seleccionado)
    return render_template("index.html", respuesta=respuesta, modelo=modelo_seleccionado)

if __name__ == "__main__":
    app.run(debug=True)
