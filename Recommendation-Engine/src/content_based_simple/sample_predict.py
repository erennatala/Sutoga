import csv
import logging
import random

import pandas as pd
from sklearn.model_selection import train_test_split

""" Same script but it gives just 10 sample user and predictions"""

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


def recommend(average_hours, game_name, threshold):
    if game_name in average_hours and average_hours[game_name] > threshold:
        return 1
    else:
        return 0




logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')


user_db = pd.read_csv("steam.csv")
game_db = pd.read_csv("content-based.csv")
game_db.drop_duplicates()

# Get a 10% random sample of the user_db
user_db_sample = user_db.sample(frac=0.1, random_state=41)
MINIMUM_GAMES_PLAYED = 5

# Filter out users with insufficient play history
user_counts = user_db_sample.groupby('user_id').size()
sufficient_play_history_users = user_counts[user_counts >= MINIMUM_GAMES_PLAYED].index
filtered_user_db_sample = user_db_sample[user_db_sample['user_id'].isin(sufficient_play_history_users)]

# Split the filtered sample user database into training and testing datasets
training_data, testing_data = train_test_split(filtered_user_db_sample, test_size=0.2, random_state=42)

# Convert the training data DataFrame to a list of dictionaries
training_data = training_data.to_dict(orient="records")

# Train the content-based recommendation algorithm with the training_data
average_hours = train(training_data)


# Set a threshold for recommending games based on average hours played
threshold = 10

# Test the algorithm using the testing_data and calculate the accuracy
correct_predictions = 0
total_predictions = 0
testing_data = testing_data.to_dict(orient="records")
# Print recommendations for some users
sample_size = 10
sample_users = random.sample(testing_data, sample_size)

for row in sample_users:
    """Actual data"""
    user_id = row["user_id"]
    game_name = row["game_name"]
    actual_played = int(row["play"])
    """ Predicted Game """
    predicted_played = recommend(average_hours, game_name, threshold)

    print(f"User: {user_id}, Game: {game_name}")
    print(f"  Predicted played: {predicted_played}")
    print(f"  Actual played: {actual_played}")
    print(f"  Average hours for the game: {average_hours.get(game_name, 0):.2f}")
    print(f"  Threshold: {threshold}")


    if actual_played == predicted_played:
        correct_predictions += 1
        print("  Correct prediction!")
    else:
        print("  Incorrect prediction.")

    total_predictions += 1
    print()
""" Accuracy calculation for 10 game. (It also counts its correct if it predicted that user will not play the game and 
 actually did not play) """
accuracy = correct_predictions / total_predictions
print(f"Accuracy: {accuracy * 100:.2f}%")
