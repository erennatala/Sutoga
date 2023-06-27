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


def content_based_recommendations(user_games):
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


