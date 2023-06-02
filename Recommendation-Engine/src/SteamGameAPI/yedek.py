import requests
import time
import json

api_key = '4D3BE17D82F44DE7727A8287A7F0F869'
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

# Load cached data
try:
    with open(cache_file, 'r') as f:
        cached_data = json.load(f)
except FileNotFoundError:
    cached_data = {}

# Make requests and measure actual time
start_time = time.time()
for i, app in enumerate(app_list_data):
    app_id = app['appid']

    # Check if data is already cached
    if str(app_id) in cached_data:
        app_data = cached_data[str(app_id)]
    else:
        app_data_url = f'http://store.steampowered.com/api/appdetails/?appids={app_id}&include_appinfo=1'
        app_data = requests.get(app_data_url).json()
        cached_data[str(app_id)] = app_data
        time.sleep(delay_seconds)

    # Cache data periodically
    current_time = time.time()
    if current_time - last_cache_time > cache_time_seconds:
        with open(cache_file, 'w') as f:
            json.dump(cached_data, f)
        last_cache_time = current_time

    # Check if we have reached the total number of requests
    if i >= 1000:  # Make 1000 requests for this test run
        break

# Save remaining cached data
with open(cache_file, 'w') as f:
    json.dump(cached_data, f)

end_time = time.time()

# Calculate actual time
elapsed_time = end_time - start_time

# Print results
print(f"Number of requests: {i}")
print(f"Actual time: {elapsed_time:.2f} seconds")