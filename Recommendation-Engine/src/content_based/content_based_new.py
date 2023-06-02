import pandas as pd
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.model_selection import train_test_split

# Read the CSV files
all_owned_games_df = pd.read_csv('all_owned_games.csv')
top_100_detailed_df = pd.read_csv('top_100_detailed.csv')

# Set the data percentage
data_percentage = 0.1

# Select a subset of the all_owned_games dataset based on the data percentage
subset_all_owned_games_df = all_owned_games_df.sample(frac=data_percentage, random_state=42)

# Filter the subset of all_owned_games_df to include only the top 100 game app IDs
filtered_owned_games_df = subset_all_owned_games_df[subset_all_owned_games_df['app_id'].isin(top_100_detailed_df['appid'])]

# Merge the two dataframes on 'app_id' column
merged_df = pd.merge(filtered_owned_games_df, top_100_detailed_df, left_on='app_id', right_on='appid')

# Select relevant columns for training the recommendation engine
train_df = merged_df[['steam_id', 'app_id', 'playtime_forever', 'categories', 'genres']]

# Create a TF-IDF vectorizer and transform 'categories' and 'genres' columns
vectorizer = TfidfVectorizer()
categories_genres_matrix = vectorizer.fit_transform(train_df['categories'] + ' ' + train_df['genres'])

# Compute cosine similarity matrix
cosine_sim_matrix = cosine_similarity(categories_genres_matrix, categories_genres_matrix)

# Split the dataset into train and test sets
train_data, test_data = train_test_split(train_df, test_size=0.2, random_state=42)

# Function to get recommendations based on cosine similarity
def get_recommendations(user_id, top_n=5):
    # Filter the train data for the given user_id
    user_data = train_data[train_data['steam_id'] == user_id]

    # Get the indices of the user's owned games in the train data
    user_indices = user_data.index.values

    # Compute the mean playtime for each game in the user's owned games
    playtime_means = user_data.groupby('app_id')['playtime_forever'].mean().reset_index()

    # Create an empty dataframe for storing recommended games
    recommendations = pd.DataFrame(columns=['app_id', 'score'])

    # Iterate over the user's owned games
    for index in user_indices:
        # Get the similarity scores for the current game with all other games
        similarity_scores = cosine_sim_matrix[index]

        # Get the indices of the top_n similar games
        similar_game_indices = np.argsort(similarity_scores)[-top_n:]

        # Create a boolean index of valid indices
        valid_indices = train_df.index.isin(similar_game_indices)

        # Filter out games that are not present in the train_data
        recommended_app_ids = train_df.loc[valid_indices, 'app_id']
        scores = similarity_scores[valid_indices]

        # Add the recommended games to the recommendations dataframe
        recommendations = recommendations.append(pd.DataFrame({'app_id': recommended_app_ids, 'score': scores}), ignore_index=True)

    # Merge recommendations with playtime_means to get the mean playtime for recommended games
    recommendations = pd.merge(recommendations, playtime_means, on='app_id')

    # Sort the recommendations by score and playtime
    recommendations = recommendations.sort_values(['score', 'playtime_forever'], ascending=[False, False])

    # Get the top_n recommended games
    recommendations = recommendations.head(top_n)['app_id']

    return recommendations






# Function to calculate accuracy using the test data
def calculate_accuracy():
    total_users = len(test_data['steam_id'].unique())
    correct_recommendations = 0

    for user_id in test_data['steam_id'].unique():
        # Get the top 5 recommendations for the user
        recommendations = get_recommendations(user_id, top_n=5)

        # Check if any of the recommended games are in the user's test data
        user_test_data = test_data[test_data['steam_id'] == user_id]
        recommended_games = set(recommendations.values)
        test_games = set(user_test_data['app_id'].values)

        if recommended_games.intersection(test_games):
            correct_recommendations += 1

    accuracy = correct_recommendations / total_users * 100
    return accuracy

# Calculate and print the accuracy
accuracy = calculate_accuracy()
print(f"Accuracy: {accuracy}%")
