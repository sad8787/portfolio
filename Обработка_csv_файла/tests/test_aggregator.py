from aggregator import apply_aggregation

def test_aggregation_avg():
    data = [{'score': '10'}, {'score': '20'}, {'score': '30'}]
    result = apply_aggregation(data, "avg=score")
    assert "20.00" in result
