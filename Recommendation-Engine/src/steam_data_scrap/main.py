import csv
import json
import requests
from textblob import TextBlob
import re
import bs4

api_key = ''

with open('./data/top_100_games.json') as f:
    data = json.load(f)

top_game_appids = []
for rank in data['response']['ranks'][:10]:
    top_game_appids.append(rank['appid'])

# Set headers to emulate a browser request
headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3'}

# Fetch the game data from the Steam API
games = {}
num_of_request = 0
for appid in top_game_appids:
    steam_app_response = requests.get(f'https://store.steampowered.com/api/appdetails?appids={appid}', headers=headers)
    num_of_request += 1

    steam_app_data = steam_app_response.json()[str(appid)]['data']
    game = {
        'appid': appid,
        'name': steam_app_data['name']
    }
    games[appid] = game

# Create a list to store the review and friend data
data_list = []
num_reviews_per_page = 10
num_pages_to_retrieve = 10

# Loop through the 10 most popular games
for app_id, game_data in list(games.items())[:10]:
    name = game_data['name']


    for i in range(num_pages_to_retrieve):
        start_offset = i * num_reviews_per_page
        url = f'https://store.steampowered.com/appreviews/{app_id}?json=1&filter=recent&num_per_page={num_reviews_per_page}&start_offset={start_offset}'
        response = requests.get(url)
        reviews = response.json()['reviews']

# Loop through the first 10 reviews for the game
        for review in reviews:
            review_text = review['review']
            user_id = review['author']['steamid']

            is_public = review['author'].get('profilestate', 0)
            length = len(review_text)

            try:
                url = f'http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key={api_key}&steamids={user_id}'
                response = requests.get(url)
                num_of_request += 1

                data = response.json()

                user_data = data['response']['players'][0]

                # Group reviews by sentiment
                blob = TextBlob(review_text)
                polarity = blob.sentiment.polarity

                # Get the user's friend list
                try:
                    url = f'http://api.steampowered.com/ISteamUser/GetFriendList/v0001/?key={api_key}&steamid={user_id}&relationship=friend'
                    response = requests.get(url)
                    num_of_request += 1

                    data = response.json()
                    friends = data['friendslist']['friends']
                except:
                    print("Cant get friend data")
                    friends = []

                # Add the review and friend data to the list
                data = {
                    'game': name,
                    'review_text': review_text,
                    'user_id': user_id,
                    'is_public': is_public,
                    'polarity': polarity,
                    'length': length,
                    'friends': friends
                }
                data_list.append(data)
            except:
                print("Cant get data")
                pass

print(num_of_request)
# Write the review and friend data to a CSV file
with open('game_data2.csv', 'w', newline='', encoding='utf-8') as csvfile:
    fieldnames = ['game', 'review_text', 'user_id', 'is_public', 'polarity', 'length', 'friend_id']
    writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
    writer.writeheader()

    for data in data_list:
        game = data['game']
        review_text = data['review_text']
        user_id = data['user_id']
        is_public = data['is_public']
        polarity = data['polarity']
        length = data['length']
        friends = data['friends']

        for friend in friends:
            friend_id = friend['steamid']

            # Write the data to the CSV file
            writer.writerow({
                'game': game,
                'review_text': review_text,
                'user_id': user_id,
                'is_public': is_public,
                'polarity': polarity,
                'length': length,
                'friend_id': friend_id
            })

# Write the review and friend data to a JSON file
with open('game_data3.json', 'w', encoding='utf-8') as jsonfile:
    json.dump(data_list, jsonfile, ensure_ascii=False, indent=4)
