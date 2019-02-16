package com.blackviking.hosh.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.hosh.Interface.ItemClickListener;
import com.blackviking.hosh.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Scarecrow on 6/18/2018.
 */

public class MessagesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public CircleImageView friendsPicture;
    public ImageView pictureMsg;
    public TextView friendUsername, theLastMessage;
    private ItemClickListener itemClickListener;


    public MessagesViewHolder(View itemView) {
        super(itemView);

        friendsPicture = (CircleImageView)itemView.findViewById(R.id.msgListImage);
        friendUsername = (TextView)itemView.findViewById(R.id.msgListUserName);
        theLastMessage = (TextView)itemView.findViewById(R.id.msgListMessage);
        pictureMsg = (ImageView)itemView.findViewById(R.id.msgListPictureMessage);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }
}
