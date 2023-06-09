import json
import requests
import time

# Read the JSON file containing game names and app IDs
with open('game_appids.json', 'r') as file:
    game_list = json.load(file)

# Create an empty list to store the filtered game details
filtered_games = []

# Set the number of requests to wait after
requests_to_wait = 100

# Set the waiting period in seconds
waiting_period = 30

# Iterate over each game in the list
for i, (game_name, app_id) in enumerate(game_list.items(), 1):
    # Construct the URL for the SteamSpy API endpoint
    url = f'http://steamspy.com/api.php?request=appdetails&appid={app_id}'

    # Retry mechanism
    retry_count = 0
    while retry_count < 3:
        try:
            # Send a GET request to the API endpoint
            response = requests.get(url, timeout=5)

            # Check if the API request was successful
            if response.status_code == 200:
                try:
                    # Parse the response JSON
                    data = response.json()

                    # Check if the API response contains the game data
                    if 'appid' in data:
                        game_data = data

                        # Extract the desired information from the game data
                        appid = game_data['appid']
                        name = game_data['name']
                        developer = game_data['developer']
                        publisher = game_data['publisher']
                        owners = game_data['owners']
                        average_forever = game_data['average_forever']
                        average_2weeks = game_data['average_2weeks']
                        median_forever = game_data['median_forever']
                        median_2weeks = game_data['median_2weeks']
                        ccu = game_data['ccu']
                        price = game_data['price']
                        initialprice = game_data['initialprice']
                        discount = game_data['discount']
                        languages = game_data['languages']
                        genre = game_data['genre']
                        tags = game_data['tags']

                        # Create a dictionary with the filtered game details
                        game_details = {
                            'appid': appid,
                            'name': name,
                            'developer': developer,
                            'publisher': publisher,
                            'owners': owners,
                            'average_forever': average_forever,
                            'average_2weeks': average_2weeks,
                            'median_forever': median_forever,
                            'median_2weeks': median_2weeks,
                            'ccu': ccu,
                            'price': price,
                            'initialprice': initialprice,
                            'discount': discount,
                            'languages': languages,
                            'genre': genre,
                            'tags': tags
                        }

                        # Append the game details to the filtered games list
                        filtered_games.append(game_details)

                        print(f"Processed game: {name} {appid}")
                        break

                except (KeyError, json.JSONDecodeError):
                    print(f"Error processing game: {game_name}. Retrying...")
                    retry_count += 1

            else:
                print(f"Error in API response for game: {game_name}. Retrying...")
                retry_count += 1

        except requests.RequestException as e:
            print(f"Error making API request for game: {game_name}. Retrying...")
            retry_count += 1
        time.sleep(0.1)
    if retry_count == 3:
        print(f"Error processing game: {game_name}. Maximum retries reached.")
        time.sleep(30)

    # Check if waiting period is needed
    if i % requests_to_wait == 0:
        print(f"Waiting for {waiting_period} seconds after {i} requests...")
        time.sleep(waiting_period)

# Save the filtered game details to a new JSON file
with open('filtered_games.json', 'w') as file:
    json.dump(filtered_games, file, indent=4)

print("Filtered game details saved to filtered_games.json")
