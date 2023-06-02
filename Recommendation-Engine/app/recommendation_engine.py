from app.collaborative_filtering import collaborative_filtering_recommendations
from app.content_based import content_based_recommendations

def convert_user_games_to_dict(user_games):
    user_games_dict = {}
    for game in user_games:
        app_id = game['appid']
        play_time = game['play_time']
        user_games_dict[app_id] = play_time
    return user_games_dict
"""
def hybrid_recommendations(user_games):
    user_games_dict = convert_user_games_to_dict(user_games)
    content_based_recs = content_based_recommendations(user_games_dict)

    # Generate collaborative filtering recommendations
    #collaborative_recs = collaborative_filtering_recommendations(user_games)

    # Combine the recommendations or apply your hybrid logic
    #combined_recs = combine_recommendations(content_based_recs, collaborative_recs)
    combined_recs = []
    return content_based_recs
"""
"""
def hybrid_recommendations(user_games):
    user_games_dict = convert_user_games_to_dict(user_games)

    # Generate content-based recommendations
    content_based_recs = content_based_recommendations(user_games_dict)
    print(content_based_recs)
    print("*****")
    # Generate collaborative filtering recommendations
    collaborative_recs = collaborative_filtering_recommendations(user_games_dict)
    print(collaborative_recs)
    # Filter content-based recommendations based on similarity scores
    filtered_content_based_recs = []
    similarity_threshold = 0.7

    for rec in content_based_recs:
        if rec['similarity_score'] >= similarity_threshold:
            filtered_content_based_recs.append(rec)
        else:
            break

    # Filter collaborative filtering recommendations based on estimated ratings
    filtered_collaborative_recs = []
    rating_threshold = 0.7

    for rec in collaborative_recs:
        if rec['estimated_rating'] >= rating_threshold:
            filtered_collaborative_recs.append(rec)
        else:
            break

    # Combine the filtered recommendations
    combined_recs = filtered_content_based_recs + filtered_collaborative_recs

    return combined_recs

"""
def hybrid_recommendations(user_games):
    user_games_dict = convert_user_games_to_dict(user_games)
    # Generate content-based recommendations
    content_based_recs = content_based_recommendations(user_games_dict)

    # Generate collaborative filtering recommendations
    collaborative_recs = collaborative_filtering_recommendations(user_games_dict)

    # Filter content-based recommendations based on similarity scores
    filtered_content_based_recs = []
    similarity_threshold = 0.7

    for rec in content_based_recs:
        similarity_score = rec.get('similarity_score')  # Use get() with a default value
        if similarity_score is not None and similarity_score >= similarity_threshold:
            filtered_content_based_recs.append(rec)

    # Filter collaborative filtering recommendations based on estimated ratings
    filtered_collaborative_recs = []
    rating_threshold = 0.03

    for rec in collaborative_recs:
        estimated_rating = rec.get('estimated_rating')  # Use get() with a default value
        if estimated_rating is not None and estimated_rating >= rating_threshold:
            filtered_collaborative_recs.append(rec)

    # Combine the filtered recommendations
    combined_recs = filtered_content_based_recs + filtered_collaborative_recs

    return combined_recs


