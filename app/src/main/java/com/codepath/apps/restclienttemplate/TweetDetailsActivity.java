package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class TweetDetailsActivity extends AppCompatActivity {

    // instance fields
    Tweet tweet;
    Context context;
    TwitterClient client;
    TweetAdapter adapter;
    List<Tweet> mTweets;

    // views to be displayed
    public @BindView(R.id.ivProfileImage) ImageView ivProfileImage;
    public @BindView(R.id.ivReply) ImageView ivReply;
    public @BindView(R.id.ivFavorite) ImageView ivFavorite;
    public @BindView(R.id.ivRetweet) ImageView ivRetweet;
    public @BindView(R.id.ivMedia) ImageView ivMedia;
    public @BindView(R.id.tvUserName) TextView tvUsername;
    public @BindView(R.id.tvScreenName) TextView tvScreenName;
    public @BindView(R.id.tvBody) TextView tvBody;
    public @BindView(R.id.tvCreatedAt) TextView tvCreatedAt;
    public @BindView(R.id.tvFavCount) TextView tvFavCount;
    public @BindView(R.id.tvRetweetCount) TextView tvRetweetCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_details);
        // resolve view objects
        ButterKnife.bind(this);

        // deserialize tweet object
        tweet = Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));
        mTweets = Parcels.unwrap((getIntent().getParcelableExtra("list")));

        // initialize adapter
        adapter = new TweetAdapter(mTweets);

        // set view objects
        tvUsername.setText(tweet.user.name);
        tvScreenName.setText("@" + tweet.user.screenName);
        tvBody.setText(tweet.body);
        tvCreatedAt.setText(tweet.createdAt);
        tvRetweetCount.setText(Integer.toString(tweet.retweetCount));
        tvFavCount.setText(Integer.toString(tweet.favCount));

        GlideApp.with(this)
                .load(tweet.user.profileImageUrl)
                .placeholder(R.drawable.ic_vector_photo)
                .apply(RequestOptions.circleCropTransform())
                .into(ivProfileImage);

        // TODO -fix rounded corners
        GlideApp.with(this)
                .load(tweet.media)
                .placeholder(R.drawable.ic_vector_photo)
                .transform(new RoundedCornersTransformation(35, 0))
                .into((ivMedia));

        handleChanges();

    }

    public void handleChanges(){
        // check if tweet has been favorited
        if (tweet.favorited){
            ivFavorite.setImageResource(R.drawable.ic_vector_heart);
            tvFavCount.setText(Integer.toString(tweet.favCount));
        } else{
            ivFavorite.setImageResource(R.drawable.ic_vector_heart_stroke);
            tvFavCount.setText(Integer.toString(tweet.favCount));
        }

        // check if tweet has been retweeted
        if (tweet.retweeted){
            ivRetweet.setImageResource(R.drawable.ic_vector_retweet);
            tvRetweetCount.setText(Integer.toString(tweet.retweetCount));
        } else{
            ivRetweet.setImageResource(R.drawable.ic_vector_retweet_stroke);
            tvRetweetCount.setText(Integer.toString(tweet.retweetCount));
        }
    }

    @OnClick(R.id.ivReply)
    public void replyTweet(ImageView v){
        tweet = Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));
        context = ivReply.getContext();
        // create intent to new activity
        Intent intent = new Intent(context, ComposeActivity.class);
        intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
        context.startActivity(intent);
    }

    @OnClick(R.id.ivFavorite)
    public void favoriteTweet(ImageView v) {
        // initializes TwitterClient object
        client = new TwitterClient(ivFavorite.getContext());

        long id = tweet.uid;

        // check if tweet has been favorited
        if (tweet.favorited){
            client.unfavoriteTweet(id, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        Tweet tweet = Tweet.fromJSON(response);
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Toast.makeText(ivFavorite.getContext(), "Failed to unfavorite tweet", Toast.LENGTH_SHORT).show();
                }
            });
            tweet.favorited = false;
            tweet.favCount -= 1;
            adapter.notifyDataSetChanged();
        } else {
            client.favoriteTweet(id, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        Tweet tweet = Tweet.fromJSON(response);
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Toast.makeText(ivFavorite.getContext(), "Failed to favorite tweet", Toast.LENGTH_SHORT).show();
                }
            });
            tweet.favorited = true;
            tweet.favCount += 1;
            adapter.notifyDataSetChanged();
        }

        handleChanges();
    }

    @OnClick(R.id.ivRetweet)
    public void retweetTweet(ImageView v) {
        // initializes TwitterClient object
        client = new TwitterClient(ivRetweet.getContext());

        long id = tweet.uid;
        // check if tweet has been favorited
        if (tweet.retweeted){
            client.undoRetweet(id, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        Tweet tweet = Tweet.fromJSON(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Toast.makeText(ivRetweet.getContext(), "Failed to undo retweet tweet", Toast.LENGTH_SHORT).show();
                }
            });
            tweet.retweeted = false;
            tweet.retweetCount -= 1;
            adapter.notifyDataSetChanged();
        } else {
            client.retweet(id, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        Tweet tweet = Tweet.fromJSON(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Toast.makeText(ivRetweet.getContext(), "Failed to retweet tweet", Toast.LENGTH_SHORT).show();
                }
            });
            tweet.retweeted = true;
            tweet.retweetCount += 1;
            adapter.notifyDataSetChanged();
        }

        handleChanges();
    }
}
