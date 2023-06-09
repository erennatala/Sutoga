import json
import requests

with open('game_data3.json') as f:
    reviews = json.load(f)
steam_ids = []
for review in reviews:
    friends = review['friends']
    steam_ids.append(review['user_id'])
    for friend in friends:
        steam_ids.append(friend['steamid'])


api_key = '4D3BE17D82F44DE7727A8287A7F0F869'


all_game_data = {}
i = 0
for steam_id in steam_ids:
    all_game_data[steam_id] = dict()


    try:
        # Retrieve list of owned games
        url = f'http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key={api_key}&steamid={steam_id}&format=json'
        response = requests.get(url)
        data = response.json()['response']
       # print(data)
        # Extract game information
        game_count = data['game_count']
        games = data['games']

        # Add game data to dictionary
        for game in games:
            app_id = game['appid']

            playtime_forever = game['playtime_forever']

            if playtime_forever == 0:
                continue
            all_game_data[steam_id][app_id] = {'id': app_id, 'playtime_forver': playtime_forever}
            """if app_id not in all_game_       data:
                all_game_data[app_id] = {'id': app_id, 'playtime_forever': playtime_forever}
            else:
                all_game_data[app_id]['playtime_forever'] += playtime_forever"""

        print(f'Successfully retrieved data for Steam ID {steam_id}')

    except Exception as e:
        print(f'Unable to retrieve data for Steam ID {steam_id}')
        print(e)

# Write all data to JSON file
with open('all_owned_games.json', 'w') as f:
    json.dump(all_game_data, f, indent=4)