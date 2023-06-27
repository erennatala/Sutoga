from flask import Flask, request, jsonify
from flask_cors import CORS

import mysql.connector

from app.recommendation_engine import hybrid_recommendations

app = Flask(__name__)
CORS(app)  # This will enable CORS for all routes

# Add your MySQL connection details here
mysql_host = "*********"
mysql_user = "admin"
mysql_password = "*******"
mysql_database = "*******"

def get_user_games(user_id):
    connection = mysql.connector.connect(
        host=mysql_host,
        user=mysql_user,
        password=mysql_password,
        database=mysql_database
    )
    cursor = connection.cursor()
    query = "SELECT game.appid, user_game.play_time FROM user_game INNER JOIN game ON user_game.game_id = game.id WHERE user_game.user_id = %s"
    params = (user_id,)
    cursor.execute(query, params)
    user_games = cursor.fetchall()
    cursor.close()
    connection.close()

    user_game_data = []
    for game in user_games:
        appid, play_time = game
        user_game_data.append({"user_id": user_id, "appid": appid, "play_time": play_time})

    return user_game_data


@app.route('/test', methods=['POST'])
def test():
    return "Success", 200

@app.route('/recommend', methods=['POST'])
def recommend():
    user_id = request.json['user_id']

    # Retrieve user's game data from MySQL
    user_games = get_user_games(user_id)

    # Extract game IDs from user's game data
    #user_game_ids = [game[0] for game in user_games]

    # Generate hybrid recommendations
    #recommendations = hybrid_recommendations(user_id, user_game_ids)
    recommendations = hybrid_recommendations(user_games)
    # Format recommendations into response format
    print(recommendations)
    response = {
        'recommendations': [
            {
                'appid': int(rec['appid']),
                'similarity_score': rec.get('similarity_score'),
                'estimated_rating': rec.get('estimated_rating')
            } for rec in recommendations
        ]
    }

    print(response)
    # Return recommendations as JSON response
    return response


if __name__ == '__main__':
    app.run()
