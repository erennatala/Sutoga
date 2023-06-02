import requests

def get_top_games_by_playtime():
    # Set the URL for retrieving the top games by playtime from SteamSpy
    steamspy_url = 'https://steamspy.com/api.php?request=top1000in2weeks'

    try:
        # Send a GET request to SteamSpy API to get the top games by playtime
        response = requests.get(steamspy_url)

        # Check if the request was successful (status code 200)
        if response.status_code == 200:
            # Parse the JSON response from SteamSpy
            steamspy_data = response.json()

            # Extract the app IDs of the top games
            app_ids = [int(app_id) for app_id in steamspy_data.keys()]

            # Set the base URL for retrieving game details from the Steam API
            steam_api_url = 'https://store.steampowered.com/api/appdetails'

            # Construct the comma-separated app IDs parameter for the Steam API URL
            app_ids_param = ','.join(str(app_id) for app_id in app_ids)

            # Set the parameters for publishers, categories, and genres
            parameters = {
                'appids': app_ids_param,
                'filters': 'publishers,categories,genres'
            }

            # Send a GET request to the Steam API to get game details
            response = requests.get(steam_api_url, params=parameters)

            # Check if the request was successful (status code 200)
            if response.status_code == 200:
                # Parse the JSON response from the Steam API
                steam_api_data = response.json()

                # Iterate over the app IDs and retrieve the desired game details
                for app_id in app_ids:
                    if str(app_id) in steam_api_data:
                        game_data = steam_api_data[str(app_id)]['data']
                        publishers = game_data.get('publishers', [])
                        categories = game_data.get('categories', [])
                        genres = game_data.get('genres', [])
                        user_score = steamspy_data[str(app_id)].get('userscore', None)

                        # Print the game details and user score
                        print("Game ID:", app_id)
                        print("Publishers:", publishers)
                        print("Categories:", categories)
                        print("Genres:", genres)
                        print("User Score:", user_score)
                        print()

            else:
                print("Steam API request failed with status code:", response.status_code)

        else:
            print("SteamSpy API request failed with status code:", response.status_code)

    except requests.exceptions.RequestException as e:
        print("An error occurred:", str(e))

# Call the function to get the top games by playtime and their details
get_top_games_by_playtime()
