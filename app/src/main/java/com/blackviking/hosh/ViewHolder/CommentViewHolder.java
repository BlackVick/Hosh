package com.blackviking.hosh.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.blackviking.hosh.R;

/**
 * Created by Scarecrow on 3/14/2018.
 */

public class CommentViewHolder extends RecyclerView.ViewHolder {

    public TextView username, time, comment;


    public CommentViewHolder(View itemView) {
        super(itemView);

        username= (TextView)itemView.findViewById(R.id.commenter);
        time = (TextView)itemView.findViewById(R.id.commentTime);
        comment = (TextView)itemView.findViewById(R.id.comments);

    }
}
