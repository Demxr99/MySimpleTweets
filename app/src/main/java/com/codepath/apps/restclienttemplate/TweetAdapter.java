package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class TweetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    // list of all tweets
    private List<Tweet> mTweets;

    TwitterClient client;
    Context context;

    // pass in Tweets array in the constructor
    public TweetAdapter(List<Tweet> tweets){
        mTweets = tweets;
    }

    // for each row, inflate the layout into ViewHolder
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // check if tweet has media
        if (viewType == 1){
            // inflate layout with media
            View tweetView = inflater.inflate(R.layout.item_tweet, parent, false);
            ViewHolder1 viewHolder = new ViewHolder1(tweetView);
            return viewHolder;
        } else {
            // inflate layout without media
            View tweetView = inflater.inflate(R.layout.item_tweet_plain, parent, false);
            ViewHolder2 viewHolder = new ViewHolder2(tweetView);
            return viewHolder;
        }
    }

    // bind values based on position of element
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // get data based on position
        Tweet tweet = mTweets.get(position);

        // check if tweet has media
        if (holder.getItemViewType() == 1){
            ViewHolder1 vh1 = (ViewHolder1) holder;
            // populate views with data
            vh1.tvUsername.setText(tweet.user.name);
            vh1.tvScreenName.setText("@" + tweet.user.screenName);
            vh1.tvBody.setText(tweet.body);
            // TODO -fix format of time to match Twitter
            vh1.tvCreatedAt.setText(getRelativeTimeAgo(tweet.createdAt));
            vh1.tvFavCount.setText(Integer.toString(tweet.favCount));
            vh1.tvRetweetCount.setText(Integer.toString(tweet.retweetCount));


            // check if tweet has been favorited
            if (tweet.favorited){
                vh1.ivFavorite.setImageResource(R.drawable.ic_vector_heart);
            } else{
                vh1.ivFavorite.setImageResource(R.drawable.ic_vector_heart_stroke);
            }

            // check if tweet has been retweeted
            if (tweet.retweeted){
                vh1.ivRetweet.setImageResource(R.drawable.ic_vector_retweet);
            } else{
                vh1.ivRetweet.setImageResource(R.drawable.ic_vector_retweet_stroke);
            }

            // set profile image
            GlideApp.with(context)
                    .load(tweet.user.profileImageUrl)
                    .placeholder(R.drawable.ic_vector_photo)
                    .apply(RequestOptions.circleCropTransform())
                    .into(vh1.ivProfileImage);

            // set media image
            // TODO -fix rounded corners
            GlideApp.with(context)
                    .load(tweet.media)
                    .placeholder(R.drawable.ic_vector_photo)
                    .transform(new RoundedCornersTransformation(35, 0))
                    .into((vh1.ivMedia));

        } else {
            ViewHolder2 vh2 = (ViewHolder2) holder;
            // populate views with data
            vh2.tvUsername.setText(tweet.user.name);
            vh2.tvScreenName.setText("@" + tweet.user.screenName);
            vh2.tvBody.setText(tweet.body);
            // TODO -fix format of time to match Twitter
            vh2.tvCreatedAt.setText(getRelativeTimeAgo(tweet.createdAt));
            vh2.tvFavCount.setText(Integer.toString(tweet.favCount));
            vh2.tvRetweetCount.setText(Integer.toString(tweet.retweetCount));

            // check if tweet has been favorited
            if (tweet.favorited){
                vh2.ivFavorite.setImageResource(R.drawable.ic_vector_heart);
            } else{
                vh2.ivFavorite.setImageResource(R.drawable.ic_vector_heart_stroke);
            }

            // check if tweet has been retweeted
            if (tweet.retweeted){
                vh2.ivRetweet.setImageResource(R.drawable.ic_vector_retweet);
            } else{
                vh2.ivRetweet.setImageResource(R.drawable.ic_vector_retweet_stroke);
            }

            GlideApp.with(context)
                    .load(tweet.user.profileImageUrl)
                    .placeholder(R.drawable.ic_vector_photo)
                    .apply(RequestOptions.circleCropTransform())
                    .into(vh2.ivProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return mTweets.size();
    }

    @Override
    public int getItemViewType(int position) {
        // checks if tweet has media to be displayed
        if (mTweets.get(position).media == null) {
            return 0;
        } else {
            return 1;
        }
    }

    // create ViewHolder class for tweets with media
    public class ViewHolder1 extends RecyclerView.ViewHolder implements View.OnClickListener {

        // views to be displayed
        public @BindView(R.id.ivProfileImage) ImageView ivProfileImage;
        public @BindView(R.id.ivMedia) ImageView ivMedia;
        public @BindView(R.id.ivReply) ImageView ivReply;
        public @BindView(R.id.ivFavorite) ImageView ivFavorite;
        public @BindView(R.id.ivRetweet) ImageView ivRetweet;
        public @BindView(R.id.tvUserName) TextView tvUsername;
        public @BindView(R.id.tvScreenName) TextView tvScreenName;
        public @BindView(R.id.tvBody) TextView tvBody;
        public @BindView(R.id.tvCreatedAt) TextView tvCreatedAt;
        public @BindView(R.id.tvFavCount) TextView tvFavCount;
        public @BindView(R.id.tvRetweetCount) TextView tvRetweetCount;

        public ViewHolder1(View itemView) {
            super(itemView);
            // resolve view objects
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.ivProfileImage)
        public void viewProfile(ImageView v){
            // gets position of item in ArrayList
            int position = getAdapterPosition();
            // check if position is valid
            if (position != RecyclerView.NO_POSITION) {
                // get the tweet at position
                Tweet tweet = mTweets.get(position);
                // get user that tweeted
                User user = tweet.user;
                // create intent to new activity
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra(User.class.getSimpleName(), Parcels.wrap(user));
                context.startActivity(intent);
            }
        }

        @OnClick(R.id.ivReply)
        public void replyTweet (ImageView v){
            // gets position of item in ArrayList
            int position = getAdapterPosition();
            // check if position is valid
            if (position != RecyclerView.NO_POSITION) {
                // get the tweet at position
                Tweet tweet = mTweets.get(position);
                // create intent to new activity
                Intent intent = new Intent(context, ComposeActivity.class);
                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                context.startActivity(intent);
            }
        }

        @OnClick(R.id.ivFavorite)
        public void favoriteTweet (ImageView v){
            // gets position of item in ArrayList
            final int position = getAdapterPosition();
            // initializes TwitterClient object
            client = new TwitterClient(ivFavorite.getContext());

            // check if position is valid
            if (position != RecyclerView.NO_POSITION) {
                // get the tweet at position
                Tweet tweet = mTweets.get(position);
                long id = tweet.uid;
                // check if tweet has been favorited
                if (tweet.favorited) {
                    client.unfavoriteTweet(id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                Tweet tweet = Tweet.fromJSON(response);
                                mTweets.set(position, tweet);
                                notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Toast.makeText(ivFavorite.getContext(), "Failed to unfavorite tweet", Toast.LENGTH_SHORT).show();
                            logError("Failed to unfavorite tweet", throwable, true);
                        }
                    });
                } else {
                    client.favoriteTweet(id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                Tweet tweet = Tweet.fromJSON(response);
                                mTweets.set(position, tweet);
                                notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Toast.makeText(ivFavorite.getContext(), "Failed to favorite tweet", Toast.LENGTH_SHORT).show();
                            logError("Failed to favorite tweet", throwable, true);
                        }
                    });
                }
            }
        }

        @OnClick(R.id.ivRetweet)
        public void retweetTweet (ImageView v){
            // gets position of item in ArrayList
            final int position = getAdapterPosition();
            // initializes TwitterClient object
            client = new TwitterClient(ivRetweet.getContext());

            // check if position is valid
            if (position != RecyclerView.NO_POSITION) {
                // get the tweet at position
                Tweet tweet = mTweets.get(position);
                long id = tweet.uid;
                // check if tweet has been favorited
                if (tweet.retweeted) {
                    client.undoRetweet(id, new JsonHttpResponseHandler() {
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
                            logError("Failed to undo retweet tweet", throwable, true);
                        }
                    });
                    tweet.retweeted = false;
                    tweet.retweetCount -= 1;
                    notifyDataSetChanged();
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
                            logError("Failed to retweet tweet", throwable, true);
                        }
                    });
                    tweet.retweeted = true;
                    tweet.retweetCount += 1;
                    notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onClick(View v) {
            // gets position of item in ArrayList
            int position = getAdapterPosition();
            // check if position is valid
            if (position != RecyclerView.NO_POSITION) {
                // get the tweet at position
                Tweet tweet = mTweets.get(position);
                // create intent to new activity
                Intent intent = new Intent(context, TweetDetailsActivity.class);
                // serialize tweet with parceler
                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                context.startActivity(intent);
            }
        }
    }

    // create ViewHolder class for tweets without media
    public class ViewHolder2 extends RecyclerView.ViewHolder implements View.OnClickListener {

        // views to be displayed
        public @BindView(R.id.ivProfileImage) ImageView ivProfileImage;
        public @BindView(R.id.ivReply) ImageView ivReply;
        public @BindView(R.id.ivFavorite) ImageView ivFavorite;
        public @BindView(R.id.ivRetweet) ImageView ivRetweet;
        public @BindView(R.id.tvUserName) TextView tvUsername;
        public @BindView(R.id.tvScreenName) TextView tvScreenName;
        public @BindView(R.id.tvBody) TextView tvBody;
        public @BindView(R.id.tvCreatedAt) TextView tvCreatedAt;
        public @BindView(R.id.tvFavCount) TextView tvFavCount;
        public @BindView(R.id.tvRetweetCount) TextView tvRetweetCount;

        public ViewHolder2(View itemView){
            super(itemView);
            // resolve view objects
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @OnClick(R.id.ivProfileImage)
        public void viewProfile(ImageView v){
            // gets position of item in ArrayList
            int position = getAdapterPosition();
            // check if position is valid
            if (position != RecyclerView.NO_POSITION) {
                // get the tweet at position
                Tweet tweet = mTweets.get(position);
                // get user that tweeted
                User user = tweet.user;
                // create intent to new activity
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra(User.class.getSimpleName(), Parcels.wrap(user));
                context.startActivity(intent);
            }
        }

        @OnClick(R.id.ivReply)
        public void replyTweet(ImageView v){
            // gets position of item in ArrayList
            int position = getAdapterPosition();
            // check if position is valid
            if (position != RecyclerView.NO_POSITION) {
                // get the tweet at position
                Tweet tweet = mTweets.get(position);
                // create intent to new activity
                Intent intent = new Intent(context, ComposeActivity.class);
                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                context.startActivity(intent);
            }
        }

        @OnClick(R.id.ivFavorite)
        public void favoriteTweet(ImageView v){
            // gets position of item in ArrayList
            final int position = getAdapterPosition();
            // initializes TwitterClient object
            client = new TwitterClient(ivFavorite.getContext());

            // check if position is valid
            if (position != RecyclerView.NO_POSITION) {
                // get the tweet at position
                Tweet tweet = mTweets.get(position);
                long id = tweet.uid;
                // check if tweet has been favorited
                if (tweet.favorited){
                    client.unfavoriteTweet(id, new JsonHttpResponseHandler(){
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                Tweet tweet = Tweet.fromJSON(response);
                                mTweets.set(position, tweet);
                                notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Toast.makeText(ivFavorite.getContext(), "Failed to unfavorite tweet", Toast.LENGTH_SHORT).show();
                            logError("Failed to unfavorite tweet", throwable, true);
                        }
                    });
                } else {
                    client.favoriteTweet(id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                Tweet tweet = Tweet.fromJSON(response);
                                mTweets.set(position, tweet);
                                notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Toast.makeText(ivFavorite.getContext(), "Failed to favorite tweet", Toast.LENGTH_SHORT).show();
                            logError("Failed to favorite tweet", throwable, true);
                        }
                    });
                }
            }
        }

        @OnClick(R.id.ivRetweet)
        public void retweetTweet(ImageView v){
            // gets position of item in ArrayList
            final int position = getAdapterPosition();
            // initializes TwitterClient object
            client = new TwitterClient(ivRetweet.getContext());

            // check if position is valid
            if (position != RecyclerView.NO_POSITION) {
                // get the tweet at position
                Tweet tweet = mTweets.get(position);
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
                            logError("Failed to undo retweet tweet", throwable, true);
                        }
                    });
                    tweet.retweeted = false;
                    tweet.retweetCount -= 1;
                    notifyDataSetChanged();
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
                            logError("Failed to retweet tweet", throwable, true);
                        }
                    });
                    tweet.retweeted = true;
                    tweet.retweetCount += 1;
                    notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onClick(View v) {
            // gets position of item in ArrayList
            int position = getAdapterPosition();
            // check if position is valid
            if (position != RecyclerView.NO_POSITION) {
                // get the tweet at position
                Tweet tweet = mTweets.get(position);
                // create intent to new activity
                Intent intent = new Intent(context, TweetDetailsPlainActivity.class);
                // serialize tweet with parceler
                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                context.startActivity(intent);
            }
        }
    }

    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }

    // clear list of tweets
    public void clear() {
        mTweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Tweet> list) {
        mTweets.addAll(list);
        notifyDataSetChanged();
    }

    // handle errors, log and report to user
    private void logError(String message, Throwable error, boolean alertUser){
        // log the error
        Log.e("Favorite Tweet", message, error);
        // report to the user
        if (alertUser) {
            // show a long toast with the error message
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }
}
