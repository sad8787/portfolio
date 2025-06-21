def apply_filter(rows, condition: str):
    import operator

    ops = {
        '>': operator.gt,
        '<': operator.lt,
        '=': operator.eq,
    }

    for op in ops:
        if op in condition:
            column, value = condition.split(op)
            column, value = column.strip(), value.strip()
            comp = ops[op]
            break
    else:
        raise ValueError("Invalid filter operator (use =, < o >)")

    # Detectar tipo numérico si se puede
    def try_cast(val):
        try:
            return float(val)
        except ValueError:
            return val

    value = try_cast(value)

    filtered = []
    for row in rows:
        cell = try_cast(row[column])
        if comp(cell, value):
            filtered.append(row)

    return filtered
