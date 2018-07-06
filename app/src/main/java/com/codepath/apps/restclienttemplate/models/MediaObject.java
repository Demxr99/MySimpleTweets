package com.codepath.apps.restclienttemplate.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcel;

@Parcel
public class MediaObject {

    // instance variables
    public String url;
    public String mediaType;

    // deserialize JSONObject
    public static MediaObject fromJSON(JSONArray json) throws JSONException {
        MediaObject media = new MediaObject();

        // extract data from JSONObject
        media.url = json.getJSONObject(3).getString("media_url");
//        media.mediaType = json.getString("type");

        return media;
    }
}
