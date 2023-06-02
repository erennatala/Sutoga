"""import pandas as pd
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity

def content_based_recommendations():
    # Read the encoded game dataset from the CSV file
    encoded_game_dataset = pd.read_csv('encoded_game_dataset.csv')

    # Define user's game preferences and play time as a dictionary
    user_games = {
        10: 500,  # app_id: play_time
        440: 100,  # app_id: play_time
        550: 30,  # app_id: play_time
        392100: 30  # app_id: play_time
    }

    # Select only the games in the user's library from the encoded game dataset
    user_games_encoding = encoded_game_dataset[encoded_game_dataset['app_id'].isin(user_games.keys())]

    # Compute the weighted average encoding for the user's games based on play time
    user_preferences = np.average(user_games_encoding.drop('app_id', axis=1), axis=0, weights=list(user_games.values()))

    # Compute cosine similarity between the user preferences and all other games
    all_games_features = encoded_game_dataset.drop('app_id', axis=1)
    similarity_scores = cosine_similarity(user_preferences.reshape(1, -1), all_games_features)

    # Get the indices of games sorted by similarity scores
    similar_games_indices = similarity_scores.argsort()[0][::-1]

    # Retrieve the app_ids of the similar games
    similar_games_app_ids = encoded_game_dataset.loc[similar_games_indices, 'app_id']

    # Filter out the user's games from the recommended games
    recommended_games = similar_games_app_ids[~similar_games_app_ids.isin(user_games.keys())]

    # Print the recommended games
    print("Recommended games:")
    print(recommended_games.head(5))  # Change the number to adjust the number of recommendations
    # Compute cosine similarity between the user preferences and all other games
    all_games_features = encoded_game_dataset.drop('app_id', axis=1)
    similarity_scores = cosine_similarity(user_preferences.reshape(1, -1), all_games_features)

    # Create a DataFrame for similarity scores
    similarity_scores_df = pd.DataFrame(similarity_scores.T, index=encoded_game_dataset['app_id'],
                                        columns=['similarity'])

    # Sort by similarity
    sorted_scores_df = similarity_scores_df.sort_values(by='similarity', ascending=False)

    # Filter out the user's games from the recommended games
    recommended_games = sorted_scores_df.index[~sorted_scores_df.index.isin(user_games.keys())]

    # Get the similarity scores for the recommended games
    recommended_scores = sorted_scores_df.loc[recommended_games, 'similarity']

    # Print the recommended games and their similarity scores
    print("Recommended games and their similarity scores:")
    print(pd.DataFrame({'app_id': recommended_games, 'similarity': recommended_scores}).head(5))
content_based_recommendations()"""
import numpy as np
import pandas as pd


# import pandas as pd
# import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
#
# def content_based_recommendations(user_games):
#     # Read the encoded game dataset from the CSV file
#     encoded_game_dataset = pd.read_csv('encoded_game_dataset.csv')
#
#     # Define user's game preferences and play time as a dictionary
#
#
#     # Select only the games in the user's library from the encoded game dataset
#     print(encoded_game_dataset)
#     print(user_games)
#     user_games_encoding = encoded_game_dataset[encoded_game_dataset['app_id'].isin(user_games.keys())]
#
#     # Compute the weighted average encoding for the user's games based on play time
#     user_preferences = np.average(
#         user_games_encoding.drop('app_id', axis=1),
#         axis=0,
#         weights=[user_games.get(app_id, 0) for app_id in user_games_encoding['app_id']]
#     )
#
#     # Compute cosine similarity between the user preferences and all other games
#     all_games_features = encoded_game_dataset.drop('app_id', axis=1)
#     similarity_scores = cosine_similarity(user_preferences.reshape(1, -1), all_games_features)
#
#     # Get the indices of games sorted by similarity scores
#     similar_games_indices = similarity_scores.argsort()[0][::-1]
#
#     # Retrieve the app_ids of the similar games
#     similar_games_app_ids = encoded_game_dataset.loc[similar_games_indices, 'app_id']
#
#     # Filter out the user's games from the recommended games
#     recommended_games = similar_games_app_ids[~similar_games_app_ids.isin(user_games.keys())]
#
#
#     # Print the recommended games
#     print("Recommended games:")
#     print(recommended_games.head(5))  # Change the number to adjust the number of recommendations
#     return recommended_games.head(10)



# def content_based_recommendations(user_games):
#     print(user_games)
#     # Read the encoded game dataset from the CSV file
#     encoded_game_dataset = pd.read_csv('encoded_game_dataset.csv')
#
#     # Select only the games in the user's library from the encoded game dataset
#     user_games_encoding = encoded_game_dataset[encoded_game_dataset['app_id'].isin(user_games.keys())]
#
#     # Compute the weighted average encoding for the user's games based on play time
#     user_preferences = np.average(
#         user_games_encoding.drop('app_id', axis=1),
#         axis=0,
#         weights=[user_games.get(app_id, 0) for app_id in user_games_encoding['app_id']]
#     )
#
#     # Compute cosine similarity between the user preferences and all other games
#     all_games_features = encoded_game_dataset.drop('app_id', axis=1)
#     similarity_scores = cosine_similarity(user_preferences.reshape(1, -1), all_games_features)
#
#     # Get the indices of games sorted by similarity scores
#     similar_games_indices = similarity_scores.argsort()[0][::-1]
#
#     # Retrieve the app_ids of the similar games
#     similar_games_app_ids = encoded_game_dataset.loc[similar_games_indices, 'app_id']
#
#     # Filter out the user's games from the recommended games
#     recommended_games = similar_games_app_ids[~similar_games_app_ids.isin(user_games.keys())]
#
#     # Calculate the similarity scores for the recommended games
#     recommended_scores = similarity_scores[0, similar_games_indices][~similar_games_app_ids.isin(user_games.keys())]
#
#     # Create a DataFrame with the recommended games and similarity scores
#     recommended_games_df = pd.DataFrame({'app_id': recommended_games, 'similarity': recommended_scores})
#
#     # Sort the recommendations by similarity scores
#     sorted_recommendations = recommended_games_df.sort_values(by='similarity', ascending=False)
#
#     # Print the recommended games
#     print("Recommended games:")
#     print(sorted_recommendations.head(5))  # Change the number to adjust the number of recommendations
#     # Convert the DataFrame to a list of dictionaries
#     recommendations_list = sorted_recommendations.head(20).to_dict(orient='records')
#     # Return the sorted recommendations DataFrame
#     return recommendations_list

import logging

logging.basicConfig(filename='recommendation.log', level=logging.INFO)

def content_based_recommendations(user_games):
    # Read the encoded game dataset from the CSV file
    logging.info(f'user Games: {user_games}')
    encoded_game_dataset = pd.read_csv('encoded_game_dataset.csv')

    # Select only the games in the user's library from the encoded game dataset
    user_games_encoding = encoded_game_dataset[encoded_game_dataset['app_id'].isin(user_games.keys())]

    # If the user's games do not intersect with the encoded game dataset, return an empty list
    if user_games_encoding.empty:
        return []

    # Compute the weighted average encoding for the user's games based on play time
    user_preferences = np.average(
        user_games_encoding.drop('app_id', axis=1),
        axis=0,
        weights=[user_games.get(app_id, 0) for app_id in user_games_encoding['app_id']]
    )

    # Compute cosine similarity between the user preferences and all other games
    all_games_features = encoded_game_dataset.drop('app_id', axis=1)
    similarity_scores = cosine_similarity(user_preferences.reshape(1, -1), all_games_features)

    # Get the indices of games sorted by similarity scores
    similar_games_indices = similarity_scores.argsort()[0][::-1]

    # Retrieve the app_ids and similarity scores of the similar games
    similar_games_app_ids = encoded_game_dataset.loc[similar_games_indices, 'app_id']
    similar_games_scores = similarity_scores[0][similar_games_indices]

    # Filter out the user's games from the recommended games
    recommended_games = similar_games_app_ids[~similar_games_app_ids.isin(user_games.keys())]
    recommended_scores = similar_games_scores[~similar_games_app_ids.isin(user_games.keys())]

    # Create a list of recommended games with similarity scores
    recommended_games_list = [{'appid': app_id, 'similarity_score': score} for app_id, score in
                              zip(recommended_games, recommended_scores)]

    # Sort the recommended games by similarity score
    recommended_games_list.sort(key=lambda x: x['similarity_score'], reverse=True)

    return recommended_games_list

from sklearn.preprocessing import MinMaxScaler
from sklearn.metrics import mean_squared_error
from math import sqrt
from sklearn.preprocessing import RobustScaler

def normalize_playtime(df):
    # Create a new column for ratings
    df['rating'] = df['playtime_forever']

    # Apply RobustScaler to the playtime ratings
    scaler = RobustScaler()
    df['rating'] = scaler.fit_transform(df['rating'].values.reshape(-1, 1))

    # Rescale the ratings to the 0-10 range
    df['rating'] = (df['rating'] - df['rating'].min()) / (df['rating'].max() - df['rating'].min()) * 100
    # Log the playtime ratings for each user
    for steam_id in df['steam_id'].unique():
        user_df = df[df['steam_id'] == steam_id]
        playtimes = user_df['rating'].values.tolist()
        logging.info(f"User {steam_id} - Playtime Ratings: {playtimes}")

    return df



from sklearn.model_selection import train_test_split

from sklearn.preprocessing import RobustScaler

def calculate_rmse_for_user(user_df):
    # Read the encoded game dataset from the CSV file
    encoded_game_dataset = pd.read_csv('encoded_game_dataset.csv')

    # Filter out games that are not in the game dataset
    user_df = user_df[user_df['app_id'].isin(encoded_game_dataset['app_id'])]

    # Check if user_df has enough samples for train-test split
    if len(user_df) < 2:
        logging.info(f'Insufficient data to perform train-test split.')
        return None

    logging.info(f'User: {user_df.iloc[0]["steam_id"]}')

    # Split user data into train and test sets
    train_df, test_df = train_test_split(user_df, test_size=0.2, random_state=42)

    # Get the games that the user actually has in the test set
    user_test_games = set(test_df['app_id'])

    # Generate recommendations for the user using the train set
    user_games_train = {row['app_id']: row['playtime_forever'] for _, row in train_df.iterrows()}
    predicted = content_based_recommendations(user_games_train)

    # Filter out recommended games that the user doesn't have in the test set
    recommended_games = [game for game in predicted if game['appid'] in user_test_games]

    logging.info(f'predicted {predicted}')
    logging.info(f'recommended {recommended_games}')
    logging.info(recommended_games)

    # If there are no recommended games in the test set, return None
    if not recommended_games:
        logging.info(f'No recommendations for user {user_df.iloc[0]["steam_id"]} in the test set.')
        return None

    # Create a DataFrame with the scaled similarity scores
    scaled_scores = [game['similarity_score'] * 10 for game in recommended_games]
    predicted_ratings = pd.DataFrame({'appid': [game['appid'] for game in recommended_games],
                                      'predicted_rating': scaled_scores}).set_index('appid')

    # Merge the test set with the predicted ratings
    merged = test_df.merge(predicted_ratings, left_on='app_id', right_index=True)

    if merged.empty:
        return None
    else:
        # Calculate RMSE using the scaled similarity scores and the test set
        rmse = sqrt(mean_squared_error(merged['playtime_forever'], merged['predicted_rating']))
        logging.info(f'RMSE for user: {rmse} playtime: {merged["playtime_forever"]}.')
        return rmse


def calculate_average_rmse_for_all_users(filename):

    all_games_df = pd.read_csv(filename)
    all_games_df = normalize_playtime(all_games_df)

    all_rmse = []
    for steam_id in all_games_df['steam_id'].unique():
        user_games_df = all_games_df[all_games_df['steam_id'] == steam_id]
        user_rmse = calculate_rmse_for_user(user_games_df)
        if user_rmse is not None:  # Only append RMSE if it's not None
            all_rmse.append(user_rmse)

    return np.nanmean(all_rmse)  # Use np.nanmean to ignore NaN values


average_rmse = calculate_average_rmse_for_all_users('all_owned_games.csv')
print(f'The average RMSE is: {average_rmse}')

"""
from sklearn.neighbors import NearestNeighbors

def content_based_recommendations(user_games, k):
    # Read the encoded game dataset from the CSV file
    encoded_game_dataset = pd.read_csv('encoded_game_dataset.csv')

    # Select only the games in the user's library from the encoded game dataset
    user_games_encoding = encoded_game_dataset[encoded_game_dataset['app_id'].isin(user_games.keys())]

    # Compute the weighted average encoding for the user's games based on play time
    user_preferences = np.average(
        user_games_encoding.drop('app_id', axis=1),
        axis=0,
        weights=[user_games.get(app_id, 0) for app_id in user_games_encoding['app_id']]
    )

    # Filter out the user's games from all the games
    all_games = encoded_game_dataset[~encoded_game_dataset['app_id'].isin(user_games.keys())]

    # Compute k-Nearest Neighbors
    knn = NearestNeighbors(metric='cosine', algorithm='brute', n_neighbors=k, n_jobs=-1)
    knn.fit(all_games.drop('app_id', axis=1))
    distances, indices = knn.kneighbors(user_preferences.reshape(1, -1))

    # Get the indices of the k most similar games
    similar_games_indices = indices[0]

    # Retrieve the app_ids and similarity scores (1 - cosine distance) of the similar games
    similar_games_app_ids = all_games.iloc[similar_games_indices]['app_id']
    similar_games_scores = 1 - distances[0]

    # Create a list of recommended games with similarity scores
    recommended_games_list = [{'appid': int(app_id), 'similarity_score': float(score)} for app_id, score in
                              zip(similar_games_app_ids, similar_games_scores)]

    # Sort the recommended games by similarity score
    recommended_games_list.sort(key=lambda x: x['similarity_score'], reverse=True)

    return recommended_games_list
"""