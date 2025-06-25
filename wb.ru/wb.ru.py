from flask import Flask, render_template, request, redirect, url_for, jsonify,render_template
import requests

app = Flask(__name__)

def get_product_data(search: str = "косметика", price_min: int = 0, price_max: int = 5000):
    """
    Fetches product data from the Wildberries API based on search criteria.
    This is the core logic that remains the same.
    """
    pricemarginNew = f"{price_min};{price_max}"
    params = {
        "query": search,
        "resultset": "catalog",
        "sort": "popular",
        "page": 1,
        "appType": 1,
        "curr": "rub",
        "dest": -1257786,
        "pricemarginNew": pricemarginNew
    }
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
        "Accept": "application/json"
    }
    url = "https://search.wb.ru/exactmatch/ru/common/v4/search"

    try:
        resp = requests.get(url, params=params, headers=headers)
        resp.raise_for_status()  # Raise an HTTPError for bad responses (4xx or 5xx)
    except requests.exceptions.RequestException as e:
        print(f"❌ Error during API request: {e}")
        return None

    data = resp.json()
    productos = data.get("data", {}).get("products", [])
    productos.sort(key=lambda x: x.get('salePriceU', 0), reverse=True)
    return productos



#http://127.0.0.1:5000/products?search=batery&price_min=70000&price_max=75000
@app.route('/products', methods=['GET'])
def products_service():
    """
    API endpoint to search for products.
    Allows parameters to be passed via query string.
    Example: /products?search=phone&price_min=1000&price_max=10000
    """
    search = request.args.get('search', "косметика")
    try:
        price_min = int(request.args.get('price_min', 0))
        price_max = int(request.args.get('price_max', 5000))
    except ValueError:
        return jsonify({"error": "price_min and price_max must be integers"}), 400

    products = get_product_data(search=search, price_min=price_min, price_max=price_max)

    if products is None:
        return jsonify({"error": "Could not retrieve product data"}), 500
    if not products:
        return jsonify({"message": "No products found for the given criteria"}), 404

    return jsonify(products)



# --- RUTA CORREGIDA PARA LISTAR LOS PRIMEROS 10 PRODUCTOS EN list.html ---
@app.route('/list')
def list_products():
    """
    Renders a page listing products with pagination based on search and price filters.
    """
    # 1. Get search parameters from the URL (or use defaults)
    search = request.args.get('search', "косметика")
    try:
        price_min = int(request.args.get('price_min', 0))
        price_max = int(request.args.get('price_max', 5000))
    except ValueError:
        return jsonify({"error": "price_min and price_max must be integers"}), 400

    # 2. Get the current page number
    page = request.args.get('page', 1, type=int)
    products_per_page = 10 # Define how many products per page

    # 3. Fetch ALL products that match the search and price criteria
    all_products = get_product_data(search=search, price_min=price_min, price_max=price_max)

    products_to_display = []
    error_message = None
    total_products = 0
    has_next_page = False
    has_prev_page = False

    if all_products is None:
        error_message = "Error al obtener los datos de los productos."
    elif not all_products:
        error_message = "No se encontraron productos para listar con los criterios especificados."
    else:
        total_products = len(all_products)
        start_index = (page - 1) * products_per_page
        end_index = start_index + products_per_page

        # Slice the list to get only the products for the current page
        products_to_display = all_products[start_index:end_index]

        # Determine if there are previous or next pages
        has_prev_page = page > 1
        has_next_page = end_index < total_products

        # Optional: Handle case where a page number is too high
        if not products_to_display and page > 1:
            error_message = "No hay más productos en esta página. Es posible que hayas llegado al final."

    # 4. Render the template and pass all necessary data
    return render_template('list.html',
                           products=products_to_display,
                           error_message=error_message,
                           current_page=page,
                           has_prev_page=has_prev_page,
                           has_next_page=has_next_page,
                           search_query=search, # Pass search/price so pagination links can retain them
                           price_min=price_min,
                           price_max=price_max)
   
@app.route('/')
def index():
    """
    Renders the index page with the search form.
    """
    return render_template('index.html')

if __name__ == '__main__':
    # Run the Flask app
    # In a production environment, use a WSGI server like Gunicorn or uWSGI
    app.run(debug=True, port=5000)
