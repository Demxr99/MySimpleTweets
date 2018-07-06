package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Selection;
import android.text.Spannable;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

public class ComposeActivity extends AppCompatActivity {

    public final static String TAG = "ComposeActivity";

    // initialize views
    @BindView(R.id.etCompose) TextView etCompose;
    @BindView(R.id.btnTweet) Button btnTweet;
    @BindView(R.id.ivCancel) ImageView ivCancel;

    Tweet inReplyTo;
    TwitterClient client;
    Boolean reply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        // resolve view objects
        ButterKnife.bind(this);

        // create new client
        client = new TwitterClient(this);

        // initialize reply to false
        reply = false;

        Intent intent = getIntent();
        inReplyTo = Parcels.unwrap(intent.getParcelableExtra(Tweet.class.getSimpleName()));

        // checks if the intent contains a screenname to reply to
        if (intent.getParcelableExtra(Tweet.class.getSimpleName()) != null) {
            reply = true;
            etCompose.setText("@" + inReplyTo.user.screenName);
            // positions cursor to end of screenname
            int position = etCompose.length();
            CharSequence editText = etCompose.getText();
            Selection.setSelection((Spannable) editText, position);
        }

    }

    // handle errors, log and report to user
    private void logError(String message, Throwable error, boolean alertUser){
        // log the error
        Log.e(TAG, message, error);
        // report to the user
        if (alertUser) {
            // show a long toast with the error message
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.btnTweet)
    public void tweet(Button btn){
        if (etCompose.length() <= 140) {
            if (reply) {
                client.sendTweetReply(etCompose.getText().toString(), inReplyTo.uid, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            Tweet tweet = Tweet.fromJSON(response);
                            Intent intent = new Intent(btnTweet.getContext(), TimelineActivity.class);
                            intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                            setResult(RESULT_OK, intent);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Toast.makeText(getApplicationContext(), "Failed to post tweet", Toast.LENGTH_SHORT).show();
                        logError("Failed to post tweet", throwable, true);
                    }
                });
            } else {
                client.sendTweet(etCompose.getText().toString(), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            Tweet tweet = Tweet.fromJSON(response);
                            Intent intent = new Intent(btnTweet.getContext(), TimelineActivity.class);
                            intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                            setResult(RESULT_OK, intent);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Toast.makeText(getApplicationContext(), "Failed to post tweet", Toast.LENGTH_SHORT).show();
                        logError("Failed to post tweet", throwable, true);
                    }
                });
            }
        }
    }

    @OnClick(R.id.ivCancel)
    public void cancel(ImageView v){
        Intent intent = new Intent(v.getContext(), TimelineActivity.class);
        startActivity(intent);
    }
}
