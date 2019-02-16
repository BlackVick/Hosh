package com.blackviking.hosh.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blackviking.hosh.Common.Common;
import com.blackviking.hosh.R;

/**
 * Created by Scarecrow on 1/19/2019.
 */

public class MessagingViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{

    public LinearLayout yourMsgLayout, otherMsgLayout;
    public ImageView yourMsgImage, otherMsgImage;
    public TextView myText, myTextTimeStamp, otherText, otherTextTimeStamp;

    public MessagingViewHolder(View itemView) {
        super(itemView);

        yourMsgLayout = (LinearLayout)itemView.findViewById(R.id.yourMessageTextLayout);
        otherMsgLayout = (LinearLayout)itemView.findViewById(R.id.otherMessageTextLayout);
        yourMsgImage = (ImageView)itemView.findViewById(R.id.yourMessageImage);
        otherMsgImage = (ImageView)itemView.findViewById(R.id.otherMessageImage);
        myText = (TextView)itemView.findViewById(R.id.yourMessageText);
        myTextTimeStamp = (TextView)itemView.findViewById(R.id.yourMessageTimeStamp);
        otherText = (TextView)itemView.findViewById(R.id.otherMessageText);
        otherTextTimeStamp = (TextView)itemView.findViewById(R.id.otherMessageTimeStamp);


        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Options");
        menu.add(0, 0, getAdapterPosition(), Common.DELETE_SINGLE);
        menu.add(0,1,getAdapterPosition(), Common.DELETE_BOTH);
    }
}
