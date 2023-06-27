import csv
import json
import requests
from textblob import TextBlob
import re
import bs4

api_key = ''

with open('top_100_games.json') as f:
    data = json.load(f)

top_game_appids = []
for rank in data['response']['ranks'][:100]:
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
    response_json = steam_app_response.json()
    if str(appid) in response_json and 'success' in response_json[str(appid)] and response_json[str(appid)]['success']:
        app_data = response_json[str(appid)]['data']
        try:
            game = {
                'appid': appid,
                'name': app_data['name'],
                'type': app_data['type'],
                'steam_appid': app_data['steam_appid'],
                'is_free': app_data['is_free'],
                'short_description': app_data['short_description'],
                'header_image': app_data['header_image'],
                'publishers': app_data['publishers'],
                'platforms': app_data['platforms'],
                'categories': app_data['categories'],
                'genres': app_data['genres'],
                'screenshots': app_data['screenshots']
            }
        except KeyError:
            print(f"KeyError for appid {appid}")
            continue
        games[appid] = game
    else:
        print(f"Request unsuccessful for appid {appid}")

with open('top_100_detailed.json', 'w') as f:
    json.dump(games, f)
