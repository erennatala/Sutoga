import requests
import json

# base_url = "https://steamspy.com/api.php?request=all&page={}"
#
# all_games = []
#
# # Iterate over all pages
# for page in range(1, 64):
#     url = base_url.format(page)
#     response = requests.get(url)
#     data = response.json()
#     games_on_page = data.values()
#     all_games.extend(games_on_page)
#
# # Save the data to a JSON file
# with open("games.json", "w") as json_file:
#     json.dump(all_games, json_file)
#
# print("Data saved to 'games.json'")

# with open("games.json", "r") as json_file:
#     data = json.load(json_file)
#
# # Get the number of games
# num_games = len(data)
#
# # Print the number of games
# print("Number of games:", num_games)
#
# import json

# Read the JSON file
# with open("games.json", "r") as json_file:
#     data = json.load(json_file)
#
# # Sort the games by "average_forever"
# sorted_games = sorted(data, key=lambda x: x["average_forever"], reverse=True)
#
# # Get the top 1000 games
# top_games = sorted_games[:1000]
#
# # Save the top games to a JSON file
# with open("top_games.json", "w") as json_file:
#     json.dump(top_games, json_file)
#
# print("Top 1000 games saved to 'top_games.json'")
#
# import requests
# import json
#
# # Steam Web API endpoint for getting the list of all apps
# url = "https://api.steampowered.com/ISteamApps/GetAppList/v2/"
#
# # Send the request to the Steam Web API
# response = requests.get(url)
# data = response.json()
#
# # Check if the request was successful
# if response.status_code == 200:
#     all_games = data["applist"]["apps"]
#
#     # Filter and process the game list
#     filtered_games = []
#     for game in all_games:
#         name = game["name"]
#
#         # Exclude games with empty names and non-English names
#         if name and name.isascii():
#             filtered_games.append(game)
#
#     # Sort the games alphabetically by name
#     sorted_games = sorted(filtered_games, key=lambda x: x["name"])
#
#     # Save the filtered games to a JSON file
#     with open("filtered_games.json", "w") as json_file:
#         json.dump(sorted_games, json_file)
#
#     print("Filtered games saved to 'filtered_games.json'")
#
#     # Print the number of games in the resulting JSON
#     num_games = len(sorted_games)
#     print("Number of games:", num_games)
# else:
#     print("Failed to retrieve game list. Error:", data["response"]["error"])
import json
import requests

# Steam API endpoint UR
import json
import requests


import json
import requests
import re

# Steam API endpoint URL
url = 'http://api.steampowered.com/ISteamApps/GetAppList/v0002/'

# Replace 'YOUR_STEAM_API_KEY' with your actual Steam API key
steam_api_key = '4D3BE17D82F44DE7727A8287A7F0F869'
# Set the parameters for the API request
params = {
    'key': steam_api_key,
    'format': 'json'
}

# Send a GET request to the API endpoint
response = requests.get(url, params=params)

# Parse the response JSON
data = response.json()

# Extract the list of games
games = data['applist']['apps']

# Filter out games with empty or non-English names
filtered_games = [game for game in games if game['name'] and re.match(r'^[\x00-\x7F]+$', game['name'])]

# Save the filtered games to a JSON file
with open('steam_games.json', 'w') as file:
    json.dump(filtered_games, file, indent=4)

print("Games saved to steam_games.json")

import re

game_names = []

# Open the file
with open("game_5k_info.txt", "r") as file:
    # Read each line
    for line in file:
        # Extract the game name using regular expressions
        match = re.search(r"\d+\.\s+(.*?)\s+\d", line)
        if match:
            game_name = match.group(1)
            game_names.append(game_name)
            print(game_name)

import json


# Read the game data from the "games.json" file
with open("steam_games.json", "r") as json_file:
    data = json.load(json_file)

# Create a dictionary to store the game names and their corresponding appIDs
game_appids = {}

# Iterate over the game data
for game in data:
    name = game["name"]
    appid = game["appid"]

    # Check if the game name is in the `game_names` list
    if name in game_names:
        game_appids[name] = appid

# Print the game names and their corresponding appIDs
for name, appid in game_appids.items():
    print("Game:", name)
    print("AppID:", appid)
    print("*************")
print(len(game_appids.items()))
# Save the game_appids dictionary to a JSON file
with open("game_appids.json", "w") as json_file:
    json.dump(game_appids, json_file)

print("Game names and appIDs saved to 'game_appids.json'")

