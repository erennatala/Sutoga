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
    # Construct the URL for the Steam API endpoint
    url = f'http://store.steampowered.com/api/appdetails/?appids={app_id}&include_appinfo=1'

    # Retry mechanism
    retry_count = 0
    while retry_count < 3:
        try:
            # Send a GET request to the API endpoint
            response = requests.get(url,timeout=5)

            # Check if the API request was successful
            if response.status_code == 200:
                try:
                    # Parse the response JSON
                    data = response.json()

                    # Check if the API response contains the game data
                    if str(app_id) in data and 'data' in data[str(app_id)]:
                        game_data = data[str(app_id)]['data']

                        # Extract the desired information from the game data
                        appid = app_id
                        name = game_data['name']
                        genres = game_data.get('genres', [])
                        categories = game_data.get('categories', [])
                        publishers = game_data.get('publishers', [])

                        # Create a dictionary with the filtered game details
                        game_details = {
                            'appid': appid,
                            'name': name,
                            'genres': genres,
                            'categories': categories,
                            'publishers': publishers
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
        time.sleep(0.15)
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
