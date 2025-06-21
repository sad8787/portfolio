from filters import apply_filter

def test_filter_greater_than():
    data = [{'price': '100'}, {'price': '200'}, {'price': '300'}]
    result = apply_filter(data, "price>150")
    assert len(result) == 2
