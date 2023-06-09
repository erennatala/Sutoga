import pandas as pd
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.model_selection import train_test_split
import logging
from joblib import Parallel, delayed
logging.basicConfig(filename='content_based_accuracy.log', level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
# Add a StreamHandler to log to the console
console_handler = logging.StreamHandler()
console_handler.setLevel(logging.INFO)
formatter = logging.Formatter('%(asctime)s - %(levelname)s - %(message)s')
console_handler.setFormatter(formatter)
logging.getLogger('').addHandler(console_handler)
# Set up logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s [%(levelname)s] - %(message)s')
def clean_data(user_db):
    logging.info('Cleaning datasets...')
    user_counts = user_db['user_id'].value_counts()
    valid_users = user_counts[user_counts > 1].index
    cleaned_data = user_db[user_db['user_id'].isin(valid_users)]
    return cleaned_data
def create_datasets(user_db, test_size=0.2, random_state=None, percentage=1):
    logging.info('Creating datasets...')
    assert 0 < percentage <= 1, "Percentage should be between 0 and 1"
    user_db = user_db.sample(frac=percentage, random_state=random_state)
    cleaned_data = clean_data(user_db)
    #train_data, test_data = train_test_split(cleaned_data, test_size=test_size, random_state=random_state, stratify=cleaned_data['user_id'])
    train_data, test_data = train_test_split(cleaned_data, test_size=test_size, random_state=random_state)
    logging.info('Creaed...')
    return train_data, test_data



# Read the user data from the CSV file
logging.info('Reading steam.csv')
user_db = pd.read_csv("steam.csv")

# Set the percentage of the dataset to use
percentage = 0.1

# Create the training and testing datasets using the specified percentage
logging.info('Creating train and test datasets...')
train_data, test_data = create_datasets(user_db, test_size=0.2, random_state=None, percentage=percentage)

# Save the training and testing datasets as CSV files
train_data.to_csv("training.csv", index=False)
test_data.to_csv("test.csv", index=False)


def calculate_accuracy(recommendations, test_user_games):
    #logging.info('calculating accuracy')
    correct_predictions = 0
    total_recommendations = len(recommendations)

    for game in test_user_games:
        if game in recommendations:
            correct_predictions += 1

    return correct_predictions / total_recommendations if total_recommendations > 0 else 0



# Load the user and game databases
logging.info('Load datasets...')
user_db = pd.read_csv("steam.csv")
game_db = pd.read_csv("content-based.csv")
game_db.drop_duplicates()
train_db = pd.read_csv("training.csv")
test_db = pd.read_csv("test.csv")

logging.info('Creating datasets...Get the unique user_ids from the test dataset')
unique_user_ids = test_db['user_id'].unique()

# Initialize variables to calculate overall accuracy
total_accuracy = 0
num_users = len(unique_user_ids)

# Iterate over all unique user_ids
for user_id in unique_user_ids:
    #logging.info('iterate through users(uniqueId''s)')
    # Get the games that a specific user has played
    user_games = train_db[(train_db['user_id'] == user_id) & (train_db['play'] == 1)][['game_name', 'hours']]
    user_games['user_id'] = user_id
    #ogging.info('Calculate Mean of hours played')
    user_games['hours'] = user_games['hours'] / user_games['hours'].sum()

    games = user_games['game_name'].tolist()

    # Create a pivot table of the hours that each user has spent on each game
    user_games = pd.pivot_table(user_games, values='hours', index='game_name', columns='user_id')

    # Fill in any missing values with 0
    #logging.info('Filling iwth zeros')
    user_games = user_games.fillna(0)

    # Create a feature matrix with columns developer, publisher, popular_tags, game_details and genre
    #logging.info('Creating feature matrix.')
    feature_cols = ['developer', 'publisher', 'popular_tags', 'game_details', 'genre']
    feature_matrix = game_db[feature_cols]
    #logging.info('userid')

    # Add the hours column to the feature matrix
    feature_matrix = pd.concat([feature_matrix, user_games], axis=1)
    feature_matrix = pd.get_dummies(feature_matrix, columns=feature_cols)
    feature_matrix = feature_matrix.fillna(0)
    # Calculate cosine similarity between all games
    #logging.info('Calculate Sim')
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
    #logging.warning('sorted recommends')
    sorted_dict = dict(sorted(rec_sim_dict.items(), key=lambda item: item[1], reverse=True)[:10])

    # Get the test games that the user has played
    test_user_games = test_db[(test_db['user_id'] == user_id) ]['game_name'].tolist()

    # Calculate the accuracy for the current user
    user_accuracy = calculate_accuracy(recommendations, test_user_games)

    # Update the total accuracy

    total_accuracy += user_accuracy
def test_user(user_id, train_data, game_db, test_data):

    logging.info('Test User')
    user_games = train_data[train_data['user_id'] == user_id][['game_name', 'hours']]
    user_games['user_id'] = user_id
    user_games['hours'] = user_games['hours'] / user_games['hours'].sum()

    games = user_games['game_name'].tolist()
    user_games = pd.pivot_table(user_games, values='hours', index='game_name', columns='user_id')
    user_games = user_games.fillna(0)

    feature_cols = ['developer', 'publisher', 'popular_tags', 'game_details', 'genre']
    feature_matrix = game_db[feature_cols]

    feature_matrix = pd.concat([feature_matrix, user_games], axis=1)
    feature_matrix = pd.get_dummies(feature_matrix, columns=feature_cols)
    feature_matrix = feature_matrix.fillna(0)

    game_similarity = cosine_similarity(feature_matrix)

    for i, game in enumerate(user_games.index):
        hours_played = user_games.loc[game, user_id]
        game_similarity[i] *= hours_played

    recommendations = []
    for game in games:
        game_index = games.index(game)
        similar_games = list(enumerate(game_similarity[game_index]))
        similar_games = sorted(similar_games, key=lambda x: x[1], reverse=True)
        for index, similarity in similar_games:
            if similarity > 0 and game_db.at[index, 'name'] not in recommendations:
                if len(recommendations) < 10:
                    recommendations.append(game_db.at[index, 'name'])
                else:
                    break

    test_user_games = test_data[(test_data['user_id'] == user_id) & ((test_data['play'] == 1) | (test_data['play'] == 0))][
        'game_name'].tolist()

    #test_user_games = test_data[test_data['user_id'] == user_id]['game_name'].tolist()
    correctly_predicted = sum([1 for game in recommendations if game in test_user_games])
    total_predicted = len(recommendations)
    total_actual = len(test_user_games)

    #logging.warning(f"User {user_id}: {correctly_predicted}/{total_predicted} (Actual: {total_actual})")
    return correctly_predicted, total_predicted, total_actual

# Test the algorithm using the test_data and calculate the accuracy
correct_predictions = 0
total_predictions = 0
total_actual = 0

results = Parallel(n_jobs=-1)(delayed(test_user)(user_id, train_data, game_db, test_data) for user_id in unique_user_ids)

for res in results:
    correct_predictions += res[0]
    total_predictions += res[1]
    total_actual += res[2]

precision = correct_predictions / total_predictions
recall = correct_predictions / total_actual
f1_score = 2 * (precision * recall) / (precision + recall)

print(f"Precision: {precision * 100:.2f}%")
print(f"Recall: {recall * 100:.2f}%")
print(f"F1 Score: {f1_score * 100:.2f}%")
# Calculate the overall accuracy
overall_accuracy = total_accuracy / num_users

print(f"Overall Accuracy: {overall_accuracy * 100:.2f}%")
