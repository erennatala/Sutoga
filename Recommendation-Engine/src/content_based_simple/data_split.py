import csv
from collections import defaultdict
import random

input_file = "steam.csv"
training_file = "training.csv"
testing_file = "test.csv"

# Read data from the CSV file
with open(input_file, "r") as f:
    reader = csv.DictReader(f)
    data = [row for row in reader]

# Group data by user_id
user_data = defaultdict(list)
for row in data:
    user_data[row["user_id"]].append(row)

# Split data for each user using the 80-20 rule and store in separate lists
training_data = []
testing_data = []
for user_id, user_rows in user_data.items():
    random.shuffle(user_rows)
    split_index = int(0.8 * len(user_rows))
    training_data.extend(user_rows[:split_index])
    testing_data.extend(user_rows[split_index:])

# Write training data to the "training.csv" file
with open(training_file, "w", newline="") as f:
    writer = csv.DictWriter(f, fieldnames=data[0].keys())
    writer.writeheader()
    writer.writerows(training_data)

# Write testing data to the "test.csv" file
with open(testing_file, "w", newline="") as f:
    writer = csv.DictWriter(f, fieldnames=data[0].keys())
    writer.writeheader()
    writer.writerows(testing_data)
