import requests



def get_product(search:str = "косметика",price_min:int = 0,price_max: int = 5000  ):
    pricemarginNew= f"{price_min};{price_max}"
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

    resp = requests.get(url, params=params, headers=headers)

    if resp.status_code != 200:
        print("❌ Error HTTP:", resp.status_code)
        print(resp.text)
        exit()
    data = resp.json()
    productos = data.get("data", {}).get("products", [])
    return productos



try:
    productos = get_product()
    print("Total products found:", len(productos))
    for p in productos[:5]:
        print("ID:", p["id"])
        print(p)
        print(p["name"], "-", p["salePriceU"] / 100, "₽ \n")
        print("Link:", "https://www.wildberries.ru/catalog/" + str(p["id"]) + "/detail.aspx")

except Exception as e:
    print("❌ Error JSON:", e)


# Example usage of the get_product function