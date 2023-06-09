import pandas as pd
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.model_selection import train_test_split
import logging
from joblib import Parallel, delayed

logging.basicConfig(filename='content_based_accuracy.log', level=logging.INFO,
                    format='%(asctime)s - %(levelname)s - %(message)s')

console_handler = logging.StreamHandler()
console_handler.setLevel(logging.INFO)
formatter = logging.Formatter('%(asctime)s - %(levelname)s - %(message)s')
console_handler.setFormatter(formatter)
logging.getLogger('').addHandler(console_handler)


def clean_data(user_db):
    logging.info('Cleaning datasets...')
    user_counts = user_db['user_id'].value_counts()

    valid_users = user_counts[user_counts > 1].index
    cleaned_data = user_db[user_db['user_id'].isin(valid_users)]
    return cleaned_data


def create_datasets(user_db, test_size=0.2, random_state=None):
    logging.info('Creating datasets...')
    unique_users = user_db['user_id'].unique()
    train_list = []
    test_list = []

    for user in unique_users:
        user_data = user_db[user_db['user_id'] == user]
        if len(user_data) > 1:  # Ensure user has more than 1 game
            train_temp, test_temp = train_test_split(user_data, test_size=test_size, random_state=random_state)
            train_list.append(train_temp)
            test_list.append(test_temp)

    train_data = pd.concat(train_list)
    test_data = pd.concat(test_list)
    logging.info('Created...')
    return train_data, test_data



logging.info('Reading all_owned_games.csv')
user_db = pd.read_csv("all_owned_games.csv")
user_db.columns = ['user_id', 'app_id', 'hours']
user_db['app_id'] = user_db['app_id'].astype(str)
print(user_db['app_id'])
percentage = 0.1

logging.info('Creating train and test datasets...')
train_data, test_data = create_datasets(user_db, test_size=0.2, random_state=None)

train_data.to_csv("training.csv", index=False)
test_data.to_csv("test.csv", index=False)



game_db = pd.read_csv("top_100_detailed.csv")
game_db = game_db[['appid', 'name', 'publishers', 'platforms', 'categories', 'genres']]
game_db.columns = ['app_id', 'name', 'publishers', 'platforms', 'categories', 'genres']
game_db['app_id'] = game_db['app_id'].astype(str)
print(game_db['app_id'])
train_db = pd.read_csv("training.csv")
test_db = pd.read_csv("test.csv")
# Remove non-existent games
games_in_train = set(train_db['app_id'].unique())
games_in_db = set(game_db['app_id'].unique())
missing_games = games_in_train - games_in_db
for game in missing_games:
    train_db = train_db[train_db['app_id'] != game]

unique_user_ids = test_db['user_id'].unique()# Remove non-existent games
games_in_train = set(train_db['app_id'].unique())
games_in_db = set(game_db['app_id'].unique())
missing_games = games_in_train - games_in_db
for game in missing_games:
    train_db = train_db[train_db['app_id'] != game]

unique_user_ids = test_db['user_id'].unique()


feature_cols = ['publishers', 'platforms', 'categories', 'genres']
feature_matrix = pd.get_dummies(game_db[feature_cols])
feature_matrix = feature_matrix.fillna(0)

game_similarity = cosine_similarity(feature_matrix)


def test_user(user_id, train_data, game_similarity, game_db, test_data):
    logging.info('Test User')
    user_games = train_data[train_data['user_id'] == user_id][['app_id', 'hours']]
    user_games['user_id'] = user_id
    user_games['hours'] = user_games['hours'] / user_games['hours'].sum()

    games = user_games['app_id'].tolist()
    logging.info(f"User games before pivot:\n{user_games}")
    user_games = pd.pivot_table(user_games, values='hours', index='app_id', columns='user_id')
    user_games = user_games.fillna(0)
    logging.info(f"User games after pivot:\n{user_games}")

    recommendations = []
    logging.info(f"User games:\n{user_games}")
    for game in games:
        game_index = game_db[game_db['app_id'] == game].index[0]


        weighted_game_similarity = game_similarity[game_index] * user_games.loc[game, user_id]
        logging.info(f"Weighted game similarity for game {game} (index {game_index}): {weighted_game_similarity}")
        similar_games = list(enumerate(weighted_game_similarity))
        similar_games = sorted(similar_games, key=lambda x: x[1], reverse=True)
        logging.info(f"Similar games: {similar_games}")

        for index, similarity in similar_games:
            if similarity > 0 and game_db.at[index, 'name'] not in recommendations:
                recommendations.append(game_db.at[index, 'app_id'])

    logging.info(f"Recommendations: {recommendations}")

    test_user_games = test_data[test_data['user_id'] == user_id]['app_id'].tolist()

    correct_predictions = len(set(recommendations).intersection(set(test_user_games)))
    return correct_predictions, len(recommendations), len(test_user_games)


logging.info('Test User')
correct_predictions = 0
total_predictions = 0
total_actual = 0

for user_id in unique_user_ids:
    if user_id in train_data['user_id'].unique():

        res = test_user(user_id, train_db, game_similarity, game_db, test_db)
        correct_predictions += res[0]
        total_predictions += res[1]
        total_actual += res[2]
    else:
        logging.info(f"User id {user_id} not found in training data. Skipping...")

precision = correct_predictions / total_predictions if total_predictions else 0
recall = correct_predictions / total_actual if total_actual else 0

f1_score = 2 * (precision * recall) / (precision + recall) if (precision + recall) else 0
train_user_ids = set(train_data['user_id'].unique())
test_user_ids = set(test_db['user_id'].unique())
logging.info(f"User ids in train but not in test: {train_user_ids - test_user_ids}")
logging.info(f"User ids in test but not in train: {test_user_ids - train_user_ids}")
logging.info(f"Data type of 'user_id' in train_data: {train_data['user_id'].dtype}")
logging.info(f"Data type of 'user_id' in loop: {type(user_id)}")
game_counts = train_data.groupby('user_id')['app_id'].nunique()
users_without_games = game_counts[game_counts == 0].index
logging.info(f"Users without games: {users_without_games}")
print(f"Precision: {precision * 100:.2f}%")
print(f"Recall: {recall * 100:.2f}%")
print(f"F1 Score: {f1_score * 100:.2f}%")
