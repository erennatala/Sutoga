import requests
import json

api_key = '4D3BE17D82F44DE7727A8287A7F0F869'
user_steam_id = '76561199086961828'

url = f'http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key={api_key}&steamid={user_steam_id}&include_appinfo=1&include_played_free_games=1&format=json'

response = requests.get(url)
response_dict = json.loads(response.text)

games_list = response_dict['response']['games']

for game in games_list:
    print(game['name'], game['appid'], game['playtime_forever'])