import requests
import json

api_key = ''
user_steam_id = ''

url = f'http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key={api_key}&steamid={user_steam_id}&include_appinfo=1&include_played_free_games=1&format=json'

response = requests.get(url)
response_dict = json.loads(response.text)

games_list = response_dict['response']['games']

for game in games_list:
    print(game['name'], game['appid'], game['playtime_forever'])