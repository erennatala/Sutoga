import json
import csv

# Read the JSON file
with open('/Users/eren/PycharmProjects/Sutoga/Recommendation-Engine/src/content_based/top_100_detailed.json') as f:
    games_data = json.load(f)

# Prepare the CSV file
fieldnames = ['appid', 'name', 'type', 'steam_appid', 'is_free', 'short_description', 'header_image',
              'publishers', 'platforms', 'categories', 'genres', 'screenshots']
csv_filename = 'top_100_detailed.csv'

# Write the data to the CSV file
with open(csv_filename, 'w', newline='', encoding='utf-8') as csvfile:
    writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
    writer.writeheader()
    for game_id, game_data in games_data.items():
        writer.writerow(game_data)
