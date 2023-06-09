import pandas as pd
import numpy as np
from surprise import Dataset, Reader, SVD
from surprise.model_selection import train_test_split
from surprise.accuracy import rmse
from collections import defaultdict
import pickle

# Read the CSV file
all_owned_games_df = pd.read_csv('all_owned_games.csv')

# Set the playtime threshold (e.g., 10 hours)
playtime_threshold = 10 * 60  # Convert 10 hours to minutes

# Apply the playtime threshold and impute missing values
filtered_games_df = all_owned_games_df.copy()
filtered_games_df.loc[filtered_games_df['playtime_forever'] < playtime_threshold, 'playtime_forever'] = 0

# Create a Surprise Dataset
reader = Reader(rating_scale=(0, 100))
collab_data = Dataset.load_from_df(filtered_games_df, reader)

# Split the dataset into train and test sets
train_data, test_data = train_test_split(collab_data, test_size=0.2, random_state=41)

# Train the SVD model
svd_model = SVD()
svd_model.fit(train_data)

# Evaluate the model on the test set
predictions = svd_model.test(test_data)

# Calculate RMSE
rmse_score = rmse(predictions)
print(f"RMSE: {rmse_score}")

# Calculate Precision at K (P@K)
K = 5  # Top-K recommendations
top_k_predictions = defaultdict(list)
for uid, iid, true_r, est, _ in predictions:
    top_k_predictions[uid].append((iid, est))

precision_sum = 0
total_users = 0
for uid, user_ratings in top_k_predictions.items():
    user_ratings.sort(key=lambda x: x[1], reverse=True)
    top_k_items = [iid for iid, _ in user_ratings[:K]]
    relevant_items = filtered_games_df[(filtered_games_df['steam_id'] == uid) & (filtered_games_df['playtime_forever'] >= playtime_threshold)]['app_id'].values
    precision = len(set(top_k_items) & set(relevant_items)) / K
    precision_sum += precision
    total_users += 1
print(total_users)
precision_at_k = precision_sum / total_users
print(f"Precision at {K}: {precision_at_k}")
def get_top_n_recommendations(user_id, top_n=5):
    # Get all possible items
    all_items = filtered_games_df['app_id'].unique()

    # Remove items the user has already rated
    rated_items = filtered_games_df[filtered_games_df['steam_id'] == user_id]['app_id'].values
    items_to_predict = np.setdiff1d(all_items, rated_items)

    # Predict ratings for the remaining items
    predictions = [svd_model.predict(user_id, item_id) for item_id in items_to_predict]

    # Sort the predictions by estimated rating in descending order
    sorted_predictions = sorted(predictions, key=lambda x: x.est, reverse=True)

    # Get the top-N recommended items
    top_n_recommendations = [prediction.iid for prediction in sorted_predictions[:top_n]]

    return top_n_recommendations

# Save the trained model
with open('collaborative_filtering_model.pkl', 'wb') as file:
    pickle.dump(svd_model, file)

# Load the model from file
with open('collaborative_filtering_model.pkl', 'rb') as file:
    loaded_model = pickle.load(file)

# Open the .pkl file in read binary mode
with open('collaborative_filtering_model.pkl', 'rb') as file:
    loaded_object = pickle.load(file)
print(loaded_model)
print(loaded_model.get_params())
"""
# Example usage:
user_id = '76561198903685596'  # Replace with the desired user ID
recommendations = get_top_n_recommendations(user_id, top_n=5)
print(f"Recommended games for user {user_id}:")
for app_id in recommendations:
    print(app_id)
"""
