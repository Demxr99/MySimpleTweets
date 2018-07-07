package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.restclienttemplate.models.User;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FollowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // list of all tweets
    private List<User> mUsers;

    TwitterClient client;
    Context context;

    // pass in Tweets array in the constructor
    public FollowAdapter(List<User> users){
        mUsers = users;
    }

    // create ViewHolder class for tweets with media
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // views to be displayed
        @BindView(R.id.ivProfileImage) ImageView ivProfileImage;
        @BindView(R.id.tvUserName) TextView tvUsername;
        @BindView(R.id.tvScreenName) TextView tvScreenName;
        @BindView(R.id.description) TextView tvDescription;

        public ViewHolder(View itemView) {
            super(itemView);
            // resolve view objects
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onClick(View v) {
            // gets position of item in ArrayList
            int position = getAdapterPosition();
            // check if position is valid
            if (position != RecyclerView.NO_POSITION) {
                // get the user at position
                User user = mUsers.get(position);
                // create intent to new activity
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra(User.class.getSimpleName(), Parcels.wrap(user));
                context.startActivity(intent);
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // inflate layout
        View userView = inflater.inflate(R.layout.item_user, parent, false);
        ViewHolder viewHolder = new ViewHolder(userView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // get data based on position
        User user = mUsers.get(position);

//        // populate views with information
//        holder.tvUsername.setText(user.name);
//        holder.tvScreenName.setText("@" + user.screenName);
//        holder.tvDescription.setText(user.description);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    // clear list of tweets
    public void clear() {
        mUsers.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<User> list) {
        mUsers.addAll(list);
        notifyDataSetChanged();
    }
}
