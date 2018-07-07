package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class FollowersActivity extends AppCompatActivity {

    // a numeric code to identify the edit activity
    public static final int EDIT_REQUEST_CODE = 20;
    private TwitterClient client;
    long maxID;

    FollowAdapter followAdapter;
    ArrayList<User> users;
    @BindView(R.id.rvUser) RecyclerView rvUser;

    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        // resolve view objects
        ButterKnife.bind(this);

        // initialize max_id to be 0
        maxID = 0;

        client = TwitterApp.getRestClient(this);

        // initialise the array list
        users = new ArrayList<>();
        // construct the adapter
        followAdapter = new FollowAdapter(users);
        // setup progress bar

        // setup RecyclerView
        rvUser.setAdapter(followAdapter);
        rvUser.setLayoutManager(new LinearLayoutManager(this));

        rvUser.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1)) {
                    addTimeline();
                }
            }
        });

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchTimelineAsync(0);
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        populateTimeline();
    }

    private void addTimeline(){
        client.getFollowers(maxID, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("TwitterClient refresh here", response.toString());
                List<User> new_tweets = new ArrayList<>();
                // iterate through JSONArray
                for (int i=0; i < response.length(); i++){
                    // convert each object to a Tweet model
                    User tweet = null;
                    try {
                        tweet = User.fromJSON(response.getJSONObject(i));
                        // add tweet model to list of tweets
                        new_tweets.add(tweet);
                        // notify the adapter of new item
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // set max_id to be oldest tweet
                maxID = new_tweets.get(new_tweets.size()-1).uid-1;
                followAdapter.addAll(new_tweets);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("TwitterClient", response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TwitterClient", responseString);
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();            }
        });
    }

    private void populateTimeline(){
        client.getHomeTimeline(maxID, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("TwitterClient refresh here", response.toString());
                // iterate through JSONArray
                for (int i=0; i < response.length(); i++){
                    // convert each object to a Tweet model
                    User tweet = null;
                    try {
                        tweet = User.fromJSON(response.getJSONObject(i));
                        // add tweet model to list of tweets
                        users.add(tweet);
                        // notify the adapter of new item
                        followAdapter.notifyItemInserted(users.size()-1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // set max_id to be oldest tweet
                maxID = users.get(users.size()-1).uid-1;
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("TwitterClient", response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TwitterClient", responseString);
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();            }
        });
    }

    public void fetchTimelineAsync(int page) {
        // Send the network request to fetch the updated data
        // `client` here is an instance of Android Async HTTP
        // getHomeTimeline is an example endpoint.
        client.getHomeTimeline(0, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                // clear the adapter
                followAdapter.clear();
                // iterate through JSONArray
                for (int i = 0; i < response.length(); i++) {
                    // convert each object to a Tweet model
                    try {
                        User tweet = User.fromJSON(response.getJSONObject(i));
                        // add tweet model to list of tweets
                        users.add(tweet);
                        // notify the adapter of new item
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // add new tweets back to the adapter
                followAdapter.addAll(users);
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TwitterClient", responseString);
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }
        });
    }
}
