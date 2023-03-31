import pandas as pd
from sklearn.metrics.pairwise import cosine_similarity

# Load the user and game databases
user_db = pd.read_csv("steam.csv")
game_db = pd.read_csv("content-based.csv")
game_db.drop_duplicates()
# Get the games that a specific user has played
#user_id = 151603712
user_id = 33865373
#user_id = 270356700
user_games = user_db[(user_db['user_id'] == user_id) & (user_db['play'] == 1)][['game_name', 'hours']]
user_games['user_id'] = user_id



user_games['hours'] = user_games['hours'] / user_games['hours'].sum()

games = user_games['game_name'].tolist()

# Create a pivot table of the hours that each user has spent on each game
user_games = pd.pivot_table(user_games, values='hours', index='game_name', columns='user_id')

# Fill in any missing values with 0
user_games = user_games.fillna(0)

# Create a feature matrix with columns developer, publisher, popular_tags, game_details and genre
feature_cols = ['developer', 'publisher', 'popular_tags', 'game_details', 'genre']
feature_matrix = game_db[feature_cols]

# Add the hours column to the feature matrix
feature_matrix = pd.concat([feature_matrix, user_games], axis=1)
feature_matrix = pd.get_dummies(feature_matrix, columns=feature_cols)
feature_matrix = feature_matrix.fillna(0)
# Calculate cosine similarity between all games
game_similarity = cosine_similarity(feature_matrix)

# Weight the similarity scores by hours played
for i, game in enumerate(user_games.index):
    hours_played = user_games.loc[game, user_id]
    game_similarity[i] *= hours_played


recommendations = []
rec_sim_dict = {}
for game in games:
    game_index = games.index(game)
    similar_games = list(enumerate(game_similarity[game_index]))
    similar_games = sorted(similar_games, key=lambda x: x[1], reverse=True)
    for index, similarity in similar_games:
        if similarity > 0 and game_db.at[index, 'name'] not in recommendations:
            recommendations.append(game_db.at[index, 'name'])
            rec_sim_dict[game_db.at[index, 'name']] = similarity

print(len(recommendations))

sorted_dict = dict(sorted(rec_sim_dict.items(), key=lambda item: item[1], reverse=True))


