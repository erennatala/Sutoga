import csv
import json

# Read the JSON file
with open('all_owned_games_clean.json') as f:
    all_owned_games = json.load(f)

# Extract the required data
all_game_data = []
for steam_id, games in all_owned_games.items():
    for game_id, playtime_data in games.items():

        playtime_forever = playtime_data['playtime_forver']
        all_game_data.append({'steam_id': steam_id, 'app_id': game_id, 'playtime_forever': playtime_forever})


# Write the data to a CSV file
fieldnames = ['steam_id', 'app_id', 'playtime_forever']
with open('all_owned_games.csv', 'w', newline='') as f:
    writer = csv.DictWriter(f, fieldnames=fieldnames)
    writer.writeheader()
    writer.writerows(all_game_data)
