import pandas as pd
from sklearn.preprocessing import MultiLabelBinarizer


""""# Read the game dataset from a CSV file
game_dataset = pd.read_csv('/Users/eren/PycharmProjects/Sutoga/Recommendation-Engine/data/top_100_detailed.csv')
# Extract the app_id, categories, genres, and publishers from the game dataset
app_id = game_dataset['appid']
categories = game_dataset['categories']
genres = game_dataset['genres']
publishers = game_dataset['publishers']

# Process categories, genres, and publishers to extract the IDs
categories_ids = categories.apply(lambda x: [category['id'] for category in eval(x)])
genres_ids = genres.apply(lambda x: [genre['id'] for genre in eval(x)])
publishers_ids = publishers.apply(lambda x: [x])

# Perform one-hot encoding on category IDs
category_encoder = MultiLabelBinarizer()
category_encoding = category_encoder.fit_transform(categories_ids)

# Perform one-hot encoding on genre IDs
genre_encoder = MultiLabelBinarizer()
genre_encoding = genre_encoder.fit_transform(genres_ids)

# Perform one-hot encoding on publisher IDs
publisher_encoder = MultiLabelBinarizer()
publisher_encoding = publisher_encoder.fit_transform(publishers_ids)

# Create a new DataFrame with app_id, encoded features
encoded_df = pd.DataFrame({
    'app_id': app_id,
    'categories': category_encoding.tolist(),
    'genres': genre_encoding.tolist(),
    'publishers': publisher_encoding.tolist()
})

# Write the encoded features to a new CSV file
encoded_df.to_csv('encoded_game_dataset.csv', index=False)"""


def encode_game_features():
    # Read the game dataset from a CSV file
    game_dataset = pd.read_csv('Recommendation-Engine/data/top_100_detailed.csv')

    # Process categories, genres, and publishers to extract the IDs
    categories_ids = game_dataset['categories'].apply(lambda x: [category['id'] for category in eval(x)])
    genres_ids = game_dataset['genres'].apply(lambda x: [genre['id'] for genre in eval(x)])
    publishers_ids = game_dataset['publishers'].apply(lambda x: [publisher for publisher in eval(x)])

    # Perform one-hot encoding on category IDs, genre IDs, and publisher IDs
    mlb = MultiLabelBinarizer()
    category_encoding = pd.DataFrame(mlb.fit_transform(categories_ids), columns=mlb.classes_, index=game_dataset.index)
    genre_encoding = pd.DataFrame(mlb.fit_transform(genres_ids), columns=mlb.classes_, index=game_dataset.index)
    publisher_encoding = pd.DataFrame(mlb.fit_transform(publishers_ids), columns=mlb.classes_, index=game_dataset.index)

    # Concatenate the one-hot encoded features
    encoded_features = pd.concat([category_encoding, genre_encoding, publisher_encoding], axis=1)

    # Insert app_id column to the encoded features DataFrame
    encoded_features.insert(0, 'app_id', game_dataset['appid'])

    # Save the DataFrame
    encoded_features.to_csv('encoded_game_dataset_deneme.csv', index=False)

encode_game_features()

