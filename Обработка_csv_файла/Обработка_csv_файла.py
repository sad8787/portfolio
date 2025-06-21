import argparse
from csv_utils import read_csv_file
from filters import apply_filter
from aggregator import apply_aggregation
from tabulate import tabulate

def main():
    parser = argparse.ArgumentParser(description="CSV file processor")
    parser.add_argument('--file', required=True, help='Path to the CSV file')
    parser.add_argument('--where', help='Filter condition, e.g., price>300')
    parser.add_argument('--aggregate', help='Aggregation, e.g., avg=price')

    args = parser.parse_args()
    rows, headers = read_csv_file(args.file)

    if args.where:
        rows = apply_filter(rows, args.where)

    if args.aggregate:
        result = apply_aggregation(rows, args.aggregate)
        print(result)
    else:
        print(tabulate(rows, headers="keys", tablefmt="grid"))

if __name__ == "__main__":
    main()

