package com.codepath.apps.restclienttemplate.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel
public class User {

    // instance variables
    public String name;
    public long uid;
    public String screenName;
    public String profileImageUrl;

    // default no-arg constructor
    public User(){}

    // deserialize JSONobject
    public static User fromJSON(JSONObject json) throws JSONException{
        User user = new User();

        // extract data from JSONObject
        user.name = json.getString("name");
        user.uid = json.getLong("id");
        user.screenName = json.getString("screen_name");
        user.profileImageUrl = json.getString("profile_image_url");

        return user;
    }
}
