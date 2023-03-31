import csv
import matplotlib.pyplot as plt
import numpy as np

"""Training function for just using played hours """
def train(training_data):
    game_hours = {}
    game_counts = {}

    for row in training_data:
        game_name = row["game_name"]
        hours = float(row["hours"])

        if game_name not in game_hours:
            game_hours[game_name] = 0
            game_counts[game_name] = 0

        game_hours[game_name] += hours
        game_counts[game_name] += 1

    average_hours = {game: total_hours / game_counts[game] for game, total_hours in game_hours.items()}
    return average_hours

#just recommends if average hours above threshold (simple)
def recommend(average_hours, game_name, threshold):
    if game_name in average_hours and average_hours[game_name] > threshold:
        return 1
    else:
        return 0


training_file = "training.csv"
testing_file = "test.csv"

# Read data from the CSV files
with open(training_file, "r") as f:
    reader = csv.DictReader(f)
    training_data = [row for row in reader]

with open(testing_file, "r") as f:
    reader = csv.DictReader(f)
    testing_data = [row for row in reader]

# Train the content-based recommendation algorithm with the training_data
average_hours = train(training_data)

# Calculate accuracy for thresholds from 0 to 1000 (to get some idea for optimum threshold)
thresholds = list(range(0, 1001,10))
accuracies = []

for threshold in thresholds:
    correct_predictions = 0
    total_predictions = 0

    for row in testing_data:
        user_id = row["user_id"]
        game_name = row["game_name"]
        actual_played = int(row["play"])
        predicted_played = recommend(average_hours, game_name, threshold)

        if actual_played == predicted_played:
            correct_predictions += 1

        total_predictions += 1

    accuracy = correct_predictions / total_predictions
    #accuracies.append("{:.5f}".format(accuracy))
    accuracies.append(accuracy)

# Plot the accuracies vs thresholds
plt.plot(thresholds, accuracies)
plt.xlabel("Threshold")
plt.ylabel("Accuracy")
plt.title("Accuracy vs. Threshold")
plt.autoscale(enable=True,axis='both',tight=None)
plt.show()
print("Accuracy:" + str(accuracies))
print("Thresholds:" + str(thresholds))
