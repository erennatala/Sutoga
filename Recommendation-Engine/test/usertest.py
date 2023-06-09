import numpy as np
import pandas as pd
from sklearn.preprocessing import RobustScaler

# Load the dataset
df = pd.read_csv('all_owned_games.csv')

# Create a new column for scaled playtime values
df['scaled_playtime'] = np.nan

# Iterate over each unique user_id or steam_id
for steam_id in df['steam_id'].unique():
    # Filter the dataframe for the current user_id or steam_id
    user_df = df[df['steam_id'] == steam_id]

    # Scale the playtime values for the current user
    scaler = RobustScaler()  # Instantiate the RobustScaler
    playtime_values = user_df['playtime_forever'].values  # Get the playtime values for the current user
    converted_values = [max(x, 20) for x in playtime_values]  # Convert values lower than 20 to 20
    scaled_values = scaler.fit_transform(np.array(converted_values).reshape(-1, 1)).flatten()  # Scale the values

    # Assign the scaled values back to the original dataframe
    df.loc[df['steam_id'] == steam_id, 'scaled_playtime'] = scaled_values

# Save the scaled playtime values to a CSV file
df[['steam_id', 'scaled_playtime']].to_csv('scaled_playtime.csv', index=False)
