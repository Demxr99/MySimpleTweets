package com.codepath.apps.restclienttemplate;

import android.arch.paging.DataSource;

import com.codepath.apps.restclienttemplate.models.Tweet;

public class TweetDataSourceFactory extends DataSource.Factory<Long, Tweet> {

    TwitterClient client;

    public TweetDataSourceFactory(TwitterClient client) {
        this.client = client;
    }

    @Override
    public DataSource<Long, Tweet> create() {
        TweetDataSource dataSource = new TweetDataSource(this.client);
        return dataSource;
    }
}
