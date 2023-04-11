import logging
from tqdm.contrib.concurrent import thread_map
import numpy as np
from tqdm.auto import tqdm
from sklearn.model_selection import train_test_split
import concurrent.futures
import pandas as pd
from sklearn.metrics.pairwise import cosine_similarity
#logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logging.basicConfig(filename='output.log', filemode='a', level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

user_db = pd.read_csv("steam.csv")
game_db = pd.read_csv("content-based.csv")
game_db_filtered = game_db[game_db['name'].isin(user_db['game_name'].unique())]
game_db.drop_duplicates()    

# Get a 10% random sample of the user_db
user_db_sample = user_db.sample(frac=0.05, random_state=11)
# Calculate total playtime for each user
total_playtime = user_db_sample.groupby('user_id')['hours'].sum()

# Get the user_ids of users with total playtime >= 50 hours
valid_users = total_playtime[total_playtime >= 50].index
logging.info(len(valid_users))
# Filter user_db_sample to only include valid_users
user_db_sample = user_db_sample[user_db_sample['user_id'].isin(valid_users)]
MINIMUM_GAMES_PLAYED = 10

# Filter out users with insufficient play history
user_counts = user_db_sample.groupby('user_id').size()
sufficient_play_history_users = user_counts[user_counts >= MINIMUM_GAMES_PLAYED].index
filtered_user_db_sample = user_db_sample[user_db_sample['user_id'].isin(sufficient_play_history_users)]

# Split the filtered sample user database into training and testing datasets
train_user_db, test_user_db = train_test_split(filtered_user_db_sample, test_size=0.5, random_state=None)
# Split the sample user database into training and testing datasets

#train_user_db, test_user_db = train_test_split(user_db_sample, test_size=0.2, random_state=42)
def get_recommendations(user_id, user_db, game_db):
    #game_db = game_db[game_db['name'].isin(user_db['game_name'].unique())]

    game_db = game_db_filtered.copy()

    game_db.reset_index(drop=True, inplace=True)
    user_games = user_db[(user_db['user_id'] == user_id) & (user_db['play'] == 1)][['game_name', 'hours']]
    user_games['user_id'] = user_id

    #user_games['hours'] = user_games['hours'] / user_games['hours'].sum()

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

    """" Print sparsity
    non_zero_count = np.count_nonzero(feature_matrix)
    total_elements = feature_matrix.shape[0] * feature_matrix.shape[1]
    sparsity = non_zero_count / total_elements
    print(f"Sparsity: {sparsity}")"""
    # Multiply each column with its weight factor

    # Calculate cosine similarity between all games
    game_similarity = cosine_similarity(feature_matrix)

    # Weight the similarity scores by hours played
    """for i, game in enumerate(user_games.index):
        hours_played = user_games.loc[game, user_id]
        if hours_played != 0:
            game_similarity[i] *= hours_played ** 5"""

    recommendations = []
    rec_sim_dict = {}
    for game in games:
        game_index = games.index(game)
        similar_games = list(enumerate(game_similarity[game_index]))
        similar_games = sorted(similar_games, key=lambda x: x[1], reverse=True)
        for index, similarity in similar_games:
            if similarity > 0.3 and game_db.at[index, 'name'] not in recommendations:
                recommendations.append(game_db.at[index, 'name'])
                rec_sim_dict[game_db.at[index, 'name']] = similarity
    return recommendations, rec_sim_dict
def calculate_accuracy(user_db, game_db, recommendations, rec_sim_dict):
    true_positives = 0
    total_predictions = len(recommendations)

    if total_predictions == 0:
        return 0  # Return a default value for accuracy when there are no recommendations

    for game in recommendations:
        if user_db[(user_db['user_id'] == user_id) & (user_db['game_name'] == game) & (user_db['play'] == 1)].shape[0] > 0:
            true_positives += 1

    accuracy = true_positives / total_predictions
    return accuracy

accuracy_scores = []

logging.info("Starting recommendation calculations...")

for user_id in test_user_db['user_id'].unique():
    logging.info(f"Processing recommendations for user {user_id}")
    recommendations, rec_sim_dict = get_recommendations(user_id, test_user_db, game_db)
    logging.info(f"Recommendations for user {user_id}: {recommendations}")
    accuracy = calculate_accuracy(test_user_db, game_db, recommendations, rec_sim_dict)
    logging.info(f"Accuracy for user {user_id}: {accuracy:.4f}")
    accuracy_scores.append(accuracy)

average_accuracy = sum(accuracy_scores) / len(accuracy_scores)
logging.info(f"Average accuracy: {average_accuracy:.4f}")

def calculate_tp_fp_fn(user_id, user_db, game_db, recommendations):
    true_positives = 0
    false_positives = 0
    false_negatives = 0

    for game in recommendations:
        if user_db[(user_db['user_id'] == user_id) & (user_db['game_name'] == game) & (user_db['play'] == 1)].shape[0] > 0:
            true_positives += 1
        else:
            false_positives += 1

    for _, row in user_db[(user_db['user_id'] == user_id) & (user_db['play'] == 1)].iterrows():
        game = row['game_name']
        if game not in recommendations:
            false_negatives += 1

    return true_positives, false_positives, false_negatives

def calculate_precision_recall_f1(user_id, user_db, game_db, recommendations):
    if len(recommendations) == 0:
        return 0, 0, 0  # Return default values for precision, recall, and F1-score when there are no recommendations

    tp, fp, fn = calculate_tp_fp_fn(user_id, user_db, game_db, recommendations)

    precision = tp / (tp + fp) if tp + fp > 0 else 0
    recall = tp / (tp + fn) if tp + fn > 0 else 0
    f1_score = 2 * (precision * recall) / (precision + recall) if precision + recall > 0 else 0

    return precision, recall, f1_score

"""def process_user_metrics(user_id):
    logging.info(f"Processing metrics for user {user_id}")
    recommendations, _ = get_recommendations(user_id, test_user_db, game_db)
    precision, recall, f1_score = calculate_precision_recall_f1(user_id, test_user_db, game_db, recommendations)
    logging.info(f"Precision, recall, F1-score for user {user_id}: {precision:.4f}, {recall:.4f}, {f1_score:.4f}")
    return precision, recall, f1_score"""
def process_user_metrics(user_id):
    logging.info(f"Processing metrics for user {user_id}")
    recommendations, _ = get_recommendations(user_id, test_user_db, game_db_filtered)
    precision, recall, f1_score = calculate_precision_recall_f1(user_id, test_user_db, game_db_filtered, recommendations)
    logging.info(f"Precision, recall, F1-score for user {user_id}: {precision:.4f}, {recall:.4f}, {f1_score:.4f}")
    return precision, recall, f1_score


batch_size = 100
all_metrics = []

for i in range(0, len(test_user_db['user_id'].unique()), batch_size):
    user_ids_batch = test_user_db['user_id'].unique()[i:i + batch_size]



    metrics_batch = list(thread_map(process_user_metrics, user_ids_batch,
                                    total=len(user_ids_batch), desc="Processing user metrics",
                                    max_workers=10))

    all_metrics.extend(metrics_batch)

logging.info("Finished calculating precision, recall, and F1-score")

average_precision = sum([p for p, _, _ in all_metrics]) / len(all_metrics)
average_recall = sum([r for _, r, _ in all_metrics]) / len(all_metrics)
average_f1_score = sum([f for _, _, f in all_metrics]) / len(all_metrics)
""""with concurrent.futures.ThreadPoolExecutor() as executor:
    metrics = list(tqdm(executor.map(process_user_metrics, test_user_db['user_id'].unique()),
                        total=len(test_user_db['user_id'].unique()), desc="Processing user metrics"))
    logging.info("Finished calculating precision, recall, and F1-score")

average_precision = sum([p for p, _, _ in metrics]) / len(metrics)
average_recall = sum([r for _, r, _ in metrics]) / len(metrics)
average_f1_score = sum([f for _, _, f in metrics]) / len(metrics)"""

"""def process_user(user_id):
    logging.info(f"Processing accuracy for user {user_id}")
    recommendations, rec_sim_dict = get_recommendations(user_id, test_user_db, game_db)
    accuracy = calculate_accuracy(test_user_db, game_db, recommendations, rec_sim_dict)
    return accuracy"""

def process_user(user_id):
    logging.info(f"Processing accuracy for user {user_id}")
    recommendations, rec_sim_dict = get_recommendations(user_id, test_user_db, game_db_filtered)
    accuracy = calculate_accuracy(test_user_db, game_db_filtered, recommendations, rec_sim_dict)
    return accuracy


batch_size = 100
all_accuracy_scores = []

for i in range(0, len(test_user_db['user_id'].unique()), batch_size):
    user_ids_batch = test_user_db['user_id'].unique()[i:i + batch_size]

    from tqdm.contrib.concurrent import thread_map



    accuracy_scores_batch = list(thread_map(process_user, user_ids_batch,
                                            total=len(user_ids_batch), desc="Processing user accuracy",
                                            max_workers=10))

    all_accuracy_scores.extend(accuracy_scores_batch)

logging.info("Finished calculating accuracy")

average_accuracy = sum(all_accuracy_scores) / len(all_accuracy_scores)

"""with concurrent.futures.ThreadPoolExecutor() as executor:
    accuracy_scores = list(tqdm(executor.map(process_user, test_user_db['user_id'].unique()),
                                total=len(test_user_db['user_id'].unique()), desc="Processing user accuracy")"")"
    logging.info("Finished calculating accuracy")"""

print(f"Average accuracy: {average_accuracy:.4f}")
print(f"Average precision: {average_precision:.4f}")
print(f"Average recall: {average_recall:.4f}")
print(f"Average F1-score: {average_f1_score:.4f}")

logging.info(f"Average accuracy: {average_accuracy:.4f}")
logging.info(f"Average precision: {average_precision:.4f}")
logging.info(f"Average recall: {average_recall:.4f}")
logging.info(f"Average F1-score: {average_f1_score:.4f}")

