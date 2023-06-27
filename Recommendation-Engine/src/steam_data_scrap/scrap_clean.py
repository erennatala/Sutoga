import json
import requests

with open('game_data3.json') as f:
    reviews = json.load(f)

api_key = ''
all_game_data = {}

for review in reviews:
    friends = review['friends']
    steam_id = review['user_id']
    steam_id = ""
    steam_ids = [steam_id] + [friend['steamid'] for friend in friends]

    for steam_id in steam_ids:
        try:
            # Retrieve list of owned games
            steam_id2=""
            url = f'http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key={api_key}&steamid={steam_id2}&format=json'
            response = requests.get(url)
            data = response.json()['response']
            print(data)

            # Check if the user has any owned games
            game_count = data['game_count']
            if game_count == 0:
                continue

            # Extract game information and add it to dictionary
            games = data['games']
            all_game_data[steam_id] = {}
            for game in games:
                app_id = game['appid']
                playtime_forever = game['playtime_forever']
                if playtime_forever > 0:
                    all_game_data[steam_id][app_id] = {'id': app_id, 'playtime_forver': playtime_forever}

            print(f'Successfully retrieved data for Steam ID {steam_id}')
        except Exception as e:
            print(f'Unable to retrieve data for Steam ID {steam_id}')
            print(e)

# Write all data to JSON file
with open('all_owned_games_clean2.json', 'w') as f:
    json.dump(all_game_data, f, indent=4)
