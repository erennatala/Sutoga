from surprise import SVD
from surprise import Dataset
from surprise import Reader
from surprise.model_selection import cross_validate
import pandas as pd
import numpy as np
"""
# Load your dataset
df = pd.read_csv(
    '/Users/eren/PycharmProjects/Sutoga/Recommendation-Engine/src/collaborative_filtering/all_owned_games.csv')

# Normalize play_time using Min-Max scaling
df['playtime_forever'] = (df['playtime_forever'] - df['playtime_forever'].min()) / (
            df['playtime_forever'].max() - df['playtime_forever'].min())

# Define the format
reader = Reader(rating_scale=(0, 1))

# Load data from the DataFrame
data = Dataset.load_from_df(df[['steam_id', 'app_id', 'playtime_forever']], reader)

# Use the SVD algorithm
algo = SVD()

# Run 5-fold cross-validation and print results
cross_validate(algo, data, measures=['RMSE', 'MAE'], cv=5, verbose=True)
""""""
def get_top_n_recommendations(user_id, n=10):
    # Get a list of all game ids
    all_game_ids = df['app_id'].unique()

    # Get a list of games that the user has already played
    played_game_ids = df.loc[df['steam_id'] == user_id, 'app_id']

    # Remove the games that the user has already played from the list of all games
    game_ids_to_pred = np.setdiff1d(all_game_ids, played_game_ids)

    # Predict the ratings that the user would give to the games they haven't played yet
    predictions = [algo.predict(user_id, game_id) for game_id in game_ids_to_pred]

    # Sort the predictions by estimated rating
    predictions.sort(key=lambda x: x.est, reverse=True)

    # Get the top n predictions
    top_n_preds = predictions[:n]

    # Print the recommended games
    for pred in top_n_preds:
        print('Recommended game id: ', pred.iid, ', estimated rating: ', pred.est)
        """
"""def get_top_n_recommendations(user_id, n=10):
    # Get a list of all game ids
    all_game_ids = df['app_id'].unique()

    # Get a list of games that the user has already played
    played_game_ids = df.loc[df['steam_id'] == user_id, 'app_id']

    # Remove the games that the user has already played from the list of all games
    game_ids_to_pred = np.setdiff1d(all_game_ids, played_game_ids)

    # Predict the ratings that the user would give to the games they haven't played yet
    predictions = [algo.predict(user_id, game_id) for game_id in game_ids_to_pred]

    # Sort the predictions by estimated rating
    predictions.sort(key=lambda x: x.est, reverse=True)

    # Get the top n predictions that the user doesn't already own
    top_n_preds = [pred for pred in predictions if pred.iid not in played_game_ids][:n]

    # Print the recommended games
    for pred in top_n_preds:
        print('Recommended game id: ', pred.iid, ', estimated rating: ', pred.est)"""

# def get_top_n_recommendations(user_id, n=10):
#     # Get a list of games that the user has already played
#     played_games = df.loc[df['steam_id'] == user_id]
#
#     # Get a list of all game IDs
#     all_game_ids = df['app_id'].unique()
#
#     # Remove the games that the user has already played from the list of all games
#     game_ids_to_pred = np.setdiff1d(all_game_ids, played_games['app_id'])
#
#     # Predict the ratings that the user would give to the games they haven't played yet
#     predictions = [algo.predict(user_id, game_id) for game_id in game_ids_to_pred]
#
#     # Sort the predictions by estimated rating
#     predictions.sort(key=lambda x: x.est, reverse=True)
#
#     # Get the top n predictions
#     top_n_preds = predictions[:n]
#
#     # Print the recommended games
#     for pred in top_n_preds:
#         print('Recommended game id:', pred.iid, ', estimated rating:', pred.est)

import pandas as pd
from surprise import Reader, Dataset, SVD
from surprise.model_selection import cross_validate


import pandas as pd
from surprise import Reader, Dataset, SVD
from surprise.model_selection import cross_validate

def collaborative_filtering_recommendations(user_games, n=10):
    # Load your dataset
    df = pd.read_csv('/Users/eren/PycharmProjects/Sutoga/Recommendation-Engine/src/collaborative_filtering/all_owned_games.csv')

    # Normalize play_time using Min-Max scaling
    df['playtime_forever'] = (df['playtime_forever'] - df['playtime_forever'].min()) / (
            df['playtime_forever'].max() - df['playtime_forever'].min())

    # Define the format
    reader = Reader(rating_scale=(0, 1))

    # Load data from the DataFrame
    data = Dataset.load_from_df(df[['steam_id', 'app_id', 'playtime_forever']], reader)

    # Use the SVD algorithm
    algo = SVD()
    cross_validate(algo, data, measures=['RMSE', 'MAE'], cv=5, verbose=True)
    # Train the algorithm on the dataset
    trainset = data.build_full_trainset()
    algo.fit(trainset)

    # Get a list of all game IDs
    all_game_ids = df['app_id'].unique()

    # Remove the games that the user has already played from the list of all games
    game_ids_to_pred = np.setdiff1d(all_game_ids, list(user_games.keys()))

    # Predict the ratings that the user would give to the games they haven't played yet
    predictions = [algo.predict(0, game_id) for game_id in game_ids_to_pred]

    # Sort the predictions by estimated rating
    predictions.sort(key=lambda x: x.est, reverse=True)

    # Get the top n predictions
    top_n_preds = predictions[:n]

    # Normalize the estimated ratings to a 0 to 1 range
    min_rating = min(pred.est for pred in top_n_preds)
    max_rating = max(pred.est for pred in top_n_preds)
    normalized_ratings = [(pred.est - min_rating) / (max_rating - min_rating) for pred in top_n_preds]

    # Create a list of recommended games with normalized ratings
    recommended_games = [{'appid': pred.iid, 'estimated_rating': normalized_rating} for pred, normalized_rating in
                         zip(top_n_preds, normalized_ratings)]

    return recommended_games




input = {836620: 0, 1145360: 239, 737800: 0, 355840: 11, 231430: 155, 1149460: 1406, 448000: 224, 230410: 1796,
         629760: 1314, 10: 0, 261640: 182, 63500: 0,
         582660: 0, 365590: 1483, 20: 0, 252950: 15607, 225300: 0, 302110: 0, 226840: 182, 30: 0, 40990: 8,
         1045520: 127, 255520: 0, 911400: 0, 550: 180,
         239140: 319, 892970: 2147, 40: 1, 578080: 12641, 608800: 0, 292910: 202, 225320: 0, 391720: 154, 224300: 0,
         18480: 0, 391220: 0, 50: 0, 289840: 0,
         504370: 89, 696370: 272, 239160: 0, 60: 0, 552500: 0, 463930: 215, 261180: 157, 473670: 214, 268870: 136,
         544330: 0, 440900: 1194, 266310: 0, 755790: 622,
         70: 0, 640590: 0, 252490: 3547, 397900: 134, 242760: 4775, 80: 0, 7760: 0, 1313860: 3224, 204880: 275,
         562270: 154, 670290: 0, 35420: 0, 242780: 190, 439910: 248,
         812140: 7959, 239200: 0, 342630: 0, 100: 0, 402530: 145, 43110: 0, 648800: 156, 620: 43, 456810: 91,
         285800: 175, 337000: 21, 308330: 0, 560230: 0, 457330: 0,
         674940: 76, 400510: 239, 996470: 0, 616560: 0, 359550: 15512, 50300: 0, 205950: 0, 394360: 365, 638070: 184,
         130: 0, 301190: 332, 432770: 0, 1593500: 1765,
         63110: 0, 397440: 0, 1283220: 0, 314510: 0, 319630: 0, 318600: 103, 302730: 0, 211600: 0, 301200: 274,
         905370: 473, 234650: 309, 224920: 0, 291480: 26, 1172620: 4777,
         540840: 0, 375460: 263, 1030830: 3, 222880: 181, 40100: 0, 611500: 0, 667820: 0, 233130: 154, 506540: 31,
         224940: 0, 328880: 0, 1174180: 18873, 35000: 0, 323260: 0,
         292030: 5625, 586930: 0, 209080: 220, 433850: 638, 339130: 144, 224960: 0, 526540: 223, 291010: 156,
         1085660: 865, 1190110: 44, 110800: 923, 323280: 1, 461010: 260,
         6870: 0, 312530: 61, 224980: 0, 371420: 475, 730: 14029, 370910: 180, 220: 172, 255710: 340, 945360: 1278,
         6880: 0, 252130: 0, 1250: 152, 284390: 144, 361190: 285,
         344800: 0, 225000: 0, 344810: 0, 240: 0, 238320: 531, 422130: 0, 207610: 162, 422140: 0, 532210: 194,
         1254120: 4, 6910: 0, 35070: 0, 225020: 0, 427270: 49, 96000: 180,
         306950: 174, 845070: 0, 304390: 714, 815370: 91, 225540: 565, 6920: 0, 508170: 297, 442120: 0, 714010: 66,
         699160: 1, 39190: 0, 280: 0, 474910: 60, 398620: 0,
         322330: 898, 211740: 0, 381210: 1908, 39200: 0, 880940: 285, 1063730: 5208, 310560: 191, 739630: 1388,
         304930: 148, 34600: 0, 1284410: 2, 427820: 0, 67370: 0,
         300: 182, 236850: 4170, 863550: 6, 35130: 0, 243000: 0, 55100: 0, 431930: 0, 386360: 0, 44350: 209,
         1080110: 789, 320: 0, 8000: 0, 336710: 207, 490820: 249,
         6980: 0, 657230: 0, 356160: 0, 438080: 253, 211780: 0, 499520: 146, 340300: 247, 264520: 144, 243020: 0,
         450390: 0, 210770: 193, 294230: 251, 438100: 2,
         340310: 249, 353110: 0, 340: 0, 15700: 131, 1089350: 13250, 7000: 0, 533330: 258, 348510: 0, 278360: 182,
         431960: 697, 336730: 173, 28000: 0, 7010: 0, 236390: 44, 340320: 310, 360: 0, 421740: 0, 760160: 80,
         55150: 180, 286570: 141, 677220: 0, 49520: 1032, 398710: 60, 265590: 182, 264560: 174, 289650: 786,
         1128810: 41, 380: 0, 308600: 181, 510840: 117, 264580: 0, 334210: 131, 65930: 155, 335240: 625, 424840: 0,
         365450: 131, 400: 0, 8080: 0, 439700: 0, 334230: 44, 447890: 315, 203160: 1, 289690: 0, 868270: 14,
         1249200: 36, 420: 0, 8100: 0, 20900: 0, 395170: 181,
         491950: 182, 261550: 3063, 205230: 0, 1097150: 377, 601510: 0, 1184160: 23, 12210: 1980, 304050: 172,
         976310: 1249, 275390: 0, 1091500: 4379, 289260: 0}
#get_top_n_recommendations(input)
