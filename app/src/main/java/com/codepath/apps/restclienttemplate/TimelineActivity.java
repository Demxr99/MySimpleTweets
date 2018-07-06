package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.codepath.apps.restclienttemplate.models.Tweet;
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

public class TimelineActivity extends AppCompatActivity {
    // a numeric code to identify the edit activity
    public static final int EDIT_REQUEST_CODE = 20;
    private TwitterClient client;
    long maxID;

    TweetAdapter tweetAdapter;
    ArrayList<Tweet> tweets;
    @BindView(R.id.rvTweet) RecyclerView rvTweets;
    MenuItem miActionProgressItem;

    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        // resolve view objects
        ButterKnife.bind(this);

        // initialize max_id to be 0
        maxID = 0;

        client = TwitterApp.getRestClient(this);

        // initialise the array list
        tweets = new ArrayList<>();
        // construct the adapter
        tweetAdapter = new TweetAdapter(tweets);
        // setup progress bar

        // setup RecyclerView
        rvTweets.setAdapter(tweetAdapter);
        rvTweets.setLayoutManager(new LinearLayoutManager(this));

        rvTweets.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.miCompose:
                Intent i = new Intent(this, ComposeActivity.class);
                startActivityForResult(i, EDIT_REQUEST_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        // Extract the action-view from the menu item
        ProgressBar v =  (ProgressBar) MenuItemCompat.getActionView(miActionProgressItem);
        // Return to finish
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onResume(){
        super.onResume();
        fetchTimelineAsync(0);
    }

    private void addTimeline(){
        showProgressBar();
        client.getHomeTimeline(maxID, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("TwitterClient refresh here", response.toString());
                List<Tweet> new_tweets = new ArrayList<>();
                // iterate through JSONArray
                for (int i=0; i < response.length(); i++){
                    // convert each object to a Tweet model
                    Tweet tweet = null;
                    try {
                        tweet = Tweet.fromJSON(response.getJSONObject(i));
                        // add tweet model to list of tweets
                        new_tweets.add(tweet);
                        // notify the adapter of new item
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // set max_id to be oldest tweet
                maxID = new_tweets.get(new_tweets.size()-1).uid-1;
                tweetAdapter.addAll(new_tweets);
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
        hideProgressBar();
    }

    private void populateTimeline(){
        client.getHomeTimeline(maxID, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("TwitterClient refresh here", response.toString());
                // iterate through JSONArray
                for (int i=0; i < response.length(); i++){
                    // convert each object to a Tweet model
                    Tweet tweet = null;
                    try {
                        tweet = Tweet.fromJSON(response.getJSONObject(i));
                        // add tweet model to list of tweets
                        tweets.add(tweet);
                        // notify the adapter of new item
                        tweetAdapter.notifyItemInserted(tweets.size()-1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // set max_id to be oldest tweet
                maxID = tweets.get(tweets.size()-1).uid-1;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // check request code and result code first
        if (resultCode == RESULT_OK && requestCode == EDIT_REQUEST_CODE) {
            // unwrap Parcel containing tweet
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra(Tweet.class.getSimpleName()));
            // add new tweet to list of tweets
            tweets.add(0, tweet);
            // notify adapter item has been added
            tweetAdapter.notifyItemInserted(0);
            // scroll to top of list
            rvTweets.scrollToPosition(0);
        }
    }

    public void fetchTimelineAsync(int page) {
        // Send the network request to fetch the updated data
        // `client` here is an instance of Android Async HTTP
        // getHomeTimeline is an example endpoint.
        client.getHomeTimeline(0, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                // clear the adapter
                tweetAdapter.clear();
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
                // add new tweets back to the adapter
                tweetAdapter.addAll(tweets);
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

    public void showProgressBar() {
        // Show progress item
        miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        // Hide progress item
        miActionProgressItem.setVisible(false);
    }
}

