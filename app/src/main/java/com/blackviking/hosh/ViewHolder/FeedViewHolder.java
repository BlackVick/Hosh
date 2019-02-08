package com.blackviking.hosh.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.hosh.Interface.ItemClickListener;
import com.blackviking.hosh.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Scarecrow on 2/6/2019.
 */

public class FeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ItemClickListener itemClickListener;
    public ImageView postImage, likeBtn, commentBtn, options;
    public CircleImageView posterImage;
    public TextView posterName, postText, likeCount, commentCount, postTime;

    public FeedViewHolder(View itemView) {
        super(itemView);

        posterImage = (CircleImageView)itemView.findViewById(R.id.posterImage);
        postImage = (ImageView)itemView.findViewById(R.id.postImage);
        likeBtn = (ImageView)itemView.findViewById(R.id.likeBtn);
        commentBtn = (ImageView)itemView.findViewById(R.id.commentBtn);
        options = (ImageView)itemView.findViewById(R.id.options);
        posterName = (TextView)itemView.findViewById(R.id.posterName);
        postText = (TextView)itemView.findViewById(R.id.postText);
        likeCount = (TextView)itemView.findViewById(R.id.likeCount);
        commentCount = (TextView)itemView.findViewById(R.id.commentCount);
        postTime = (TextView)itemView.findViewById(R.id.postTime);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(), false);
    }
}
