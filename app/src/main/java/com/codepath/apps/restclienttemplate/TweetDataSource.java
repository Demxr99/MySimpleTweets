package com.codepath.apps.restclienttemplate;

import android.arch.paging.ItemKeyedDataSource;
import android.support.annotation.NonNull;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class TweetDataSource extends ItemKeyedDataSource<Long, Tweet> {

    // Pass whatever dependencies are needed to make the network call
    TwitterClient mClient;

    // Define the type of data that will be emitted by this datasource
    public TweetDataSource(TwitterClient client) {
        mClient = client;
    }

    // First type of ItemKeyedDataSource should match return type of getKey()
    @NonNull
    @Override
    public Long getKey(@NonNull Tweet item) {
        // item.getPostId() is a Long type
        return item.getUid();
    }

    public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull final LoadInitialCallback<Tweet> callback) {
        // Fetch data synchronously (second parameter is set to true)
        // load an initial data set so the paged list is not empty.
        // See https://issuetracker.google.com/u/2/issues/110843692?pli=1
        JsonHttpResponseHandler jsonHttpResponseHandler = createTweetHandler(callback, true);

        // No max_id should be passed on initial load
        mClient.getHomeTimeline(0, jsonHttpResponseHandler);
    }

    // Called repeatedly when more data needs to be set
    @Override
    public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Tweet> callback) {
        // This network call can be asynchronous (second parameter is set to false)
        JsonHttpResponseHandler jsonHttpResponseHandler = createTweetHandler(callback, false);

        // params.key & requestedLoadSize should be used
        // params.key will be the lowest Twitter post ID retrieved and should be used for the max_id= parameter in Twitter API.
        // max_id = params.key - 1
        mClient.getHomeTimeline(params.key - 1, jsonHttpResponseHandler);
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Tweet> callback) {

    }


    public JsonHttpResponseHandler createTweetHandler(final LoadCallback<Tweet> callback, boolean isAsync) {

        JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                ArrayList<Tweet> tweets = new ArrayList<Tweet>();
                // iterate through JSONArray
                for (int i = 0; i < response.length(); i++) {
                    // convert each object to a Tweet model
                    try {
                        Tweet tweet = Tweet.fromJSON(response.getJSONObject(i));
                        // add tweet model to list of tweets
                        tweets.add(tweet);
                        // notify the adapter of new item
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // send back to PagedList handler
                callback.onResult(tweets);
            }
        };

        if (isAsync) {
            // Fetch data synchronously
            // For AsyncHttpClient, this workaround forces the callback to be run synchronously

            handler.setUseSynchronousMode(true);
            handler.setUsePoolThread(true);
        }
        return handler;
    }
}