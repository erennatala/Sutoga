import json

import requests

# Replace <YOUR_API_KEY> with your actual Steam API key
api_key = '<4D3BE17D82F44DE7727A8287A7F0F869>'

def getAppInfo(api_key,app_id):
    # Make a request to the GetAppDetails API method
    url = f'https://store.steampowered.com/api/appdetails?appids={app_id}&key={api_key}'
    response = requests.get(url)
    #https://api.steampowered.com/IStoreService/GetMostPopularTags/v1/?key=4D3BE17D82F44DE7727A8287A7F0F869 all must popular tags
    # Parse the JSON response
    data = response.json()
    print(data)
    app_details = data[str(app_id)]['data']

    # Print some basic information about the app
    print(f"Name: {app_details['name']}")
    print(f"Description: {app_details['short_description']}")
    print(f"Release date: {app_details['release_date']['date']}")
    print(f"Supported languages: {', '.join(app_details['supported_languages'])}")
def getAllGames(api_key):
    # Make a request to the GetAppList API method
    url = f'http://api.steampowered.com/ISteamApps/GetAppList/v2?key={api_key}'

    response = requests.get(url)

    # Parse the JSON response
    data = response.json()
    print(data)
    app_list = data['applist']['apps']


    # Extract the app IDs and store them in a list
    app_info = [{'id': app['appid'], 'name': app['name']} for app in app_list]

    # Write the app IDs to a JSON file
    with open('app_id-name.json', 'w') as f:
        json.dump(app_info, f)

    # Print a message indicating that the app IDs were written to the file
    print(f"Wrote {len(app_info)} app IDs to app_ids.json")
def steamAppsPaging(api_key):
    # Initialize a list to store the app details
    app_details = []

    # Set the number of apps to retrieve per page
    apps_per_page = 5

    # Loop through each page of apps and retrieve their data
    for page in range(1, 2):
        # Make a request to the GetAppList API method for the current page
        url = f'http://api.steampowered.com/ISteamApps/GetAppList/v2?key={api_key}&page={page}&pagesize={apps_per_page}'
        response = requests.get(url)

        # Parse the JSON response and extract the app IDs
        data = response.json()
        app_list = data['applist']['apps']
        app_ids = [app['appid'] for app in app_list]

        # Loop through each app ID and retrieve its data
        for app_id in app_ids:
            app_url = f'http://store.steampowered.com/api/appdetails/?appids={app_id}&filters=genres,developers,publishers,categories,tags,metacritic'
            app_response = requests.get(app_url)
            app_data = app_response.json()
            if app_data and app_data.get('success'):
                app_details.append(app_data['data'])


        # Print a message indicating how many pages of apps have been processed
        print(f"Processed page {page}")

    # Write the app data to a JSON file with proper formatting
    with open('steam_apps.json', 'w') as f:
        json.dump(app_details, f, indent=4)

    # Print a message indicating that the app data was written to the file
    print(f"Wrote {len(app_details)} apps to steam_apps.json")
getAllGames(api_key)