import requests
import time
import json

api_key = ''
app_list_url = f'http://api.steampowered.com/ISteamApps/GetAppList/v2?key={api_key}'
app_list_data = requests.get(app_list_url).json()['applist']['apps']

total_apps = len(app_list_data)
requests_per_day = 8640
requests_per_hour = 4320
running_time_seconds = 5 * 60
delay_seconds = 0.1
cache_time_seconds = 5 * 60  # Cache data every 5 minutes
last_cache_time = time.time()
cache_file = 'steam_app_data_cache.json'
try:
    with open(cache_file, 'r') as f:
        cached_data = json.load(f)
except FileNotFoundError:
    cached_data = {}
cached_game_ids = set(cached_data.keys())


existing = len(cached_data)
print(f'Existing games = {existing}')
# Make requests and measure actual time
start_time = time.time()
for i, app in enumerate(app_list_data):
    app_id = app['appid']
    print(type(app_id))- print(cached_game_ids)
    print(cached_data)
    # Check if data is already cached
    if str(app_id) not in cached_game_ids:
        print(app_id)
        app_data_url = f'http://store.steampowered.com/api/appdetails/?appids={app_id}&include_appinfo=1'
        app_data = requests.get(app_data_url).json()
        existing += 1
        cached_data[str(app_id)] = app_data
        cached_game_ids.add(app_id)
        time.sleep(delay_seconds)

    # Cache data periodically
    current_time = time.time()
    if current_time - last_cache_time > cache_time_seconds:
        with open(cache_file, 'w') as f:
            json.dump(cached_data, f)
        last_cache_time = current_time

    # Check if we have reached the total number of requests
    if i >= 100:  # Make 1000 requests for this test run
        break

# Save remaining cached data
with open(cache_file, 'w') as f:
    json.dump(cached_data, f,indent=4)
print(f'After execution number of games = {existing}')
end_time = time.time()

# Calculate actual time
elapsed_time = end_time - start_time

# Print results
print(f"Number of requests: {i}")
print(f"Actual time: {elapsed_time:.2f} seconds")