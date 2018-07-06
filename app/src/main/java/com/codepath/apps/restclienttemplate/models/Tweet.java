package com.codepath.apps.restclienttemplate.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel
public class Tweet {

    // instance variables
    public String body;
    public long uid;
    public String createdAt;
    public User user;
    public String media;
    public boolean favorited;
    public boolean retweeted;
    public int favCount;
    public int retweetCount;

    // empty constructor for Parceler
    public Tweet(){}

    // deserialize the JSONObject
    public static Tweet fromJSON(JSONObject jsonObject) throws JSONException{
        Tweet tweet = new Tweet();

        // extract data from JSON
        tweet.body = jsonObject.getString("text");
        tweet.uid = jsonObject.getLong("id");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJSON(jsonObject.getJSONObject("user"));
        tweet.favorited = jsonObject.getBoolean("favorited");
        tweet.favCount = jsonObject.getInt("favorite_count");
        tweet.retweeted = jsonObject.getBoolean("retweeted");
        tweet.retweetCount = jsonObject.getInt("retweet_count");

        if (jsonObject.getJSONObject("entities").has("media")){
            JSONObject entities = jsonObject.getJSONObject("entities");
            JSONArray media = entities.getJSONArray("media");
            tweet.media = media.getJSONObject(0).getString("media_url");
        }
        return tweet;
    }

    public String getBody() {
        return body;
    }

    public long getUid() {
        return uid;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public String getMedia() { return media; }

    public boolean isFavorited() { return favorited; }
}
