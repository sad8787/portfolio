import csv

def read_csv_file(path):
    with open(path, newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        rows = [row for row in reader]
        return rows, reader.fieldnames
