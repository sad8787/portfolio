import os

def guardar_estructura(directorio, archivo_salida):
    with open(archivo_salida, "w", encoding="utf-8") as f:
        for ruta_actual, carpetas, archivos in os.walk(directorio):
            # Nivel de indentación
            nivel = ruta_actual.replace(directorio, "").count(os.sep)
            indentacion = " " * 4 * nivel
            f.write(f"{indentacion}{os.path.basename(ruta_actual)}/\n")

            # Archivos en la carpeta
            for archivo in archivos:
                f.write(f"{indentacion}{' ' * 4}{archivo}\n")


if __name__ == "__main__":
    directorio = "C://portfolio//portfolio//xcs-sample"   # Cambia por el nombre de tu carpeta
    archivo_salida = "C://portfolio//portfolio//xcs-sample//estructura.txt"
    guardar_estructura(directorio, archivo_salida)
    print(f"Estructura guardada en {archivo_salida}")

