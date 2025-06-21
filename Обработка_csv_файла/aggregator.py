def apply_aggregation(rows, expr: str):
    import statistics

    func_map = {
        'avg': statistics.mean,
        'min': min,
        'max': max,
    }

    try:
        func_name, column = expr.split('=')
        func_name, column = func_name.strip(), column.strip()
        func = func_map[func_name]
    except (ValueError, KeyError):
        raise ValueError("Invalid aggregation format. Use avg=col, min=col, or max=col.")

    try:
        values = [float(row[column]) for row in rows]
    except ValueError:
        raise ValueError(f"The column '{column}' does not contain only numeric values.")

    result = func(values)
    return f"{func_name.upper()} The '{column}' = {result:.2f}"
