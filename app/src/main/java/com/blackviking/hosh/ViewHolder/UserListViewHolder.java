package com.blackviking.hosh.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.blackviking.hosh.Interface.ItemClickListener;
import com.blackviking.hosh.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Scarecrow on 2/18/2019.
 */

public class UserListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ItemClickListener itemClickListener;
    public CircleImageView userImage;
    public TextView name, status, onlineStat;


    public UserListViewHolder(View itemView) {
        super(itemView);

        userImage = (CircleImageView) itemView.findViewById(R.id.userListImage);
        name = (TextView)itemView.findViewById(R.id.userListName);
        status = (TextView)itemView.findViewById(R.id.userListStatus);
        onlineStat = (TextView)itemView.findViewById(R.id.userListOnlineStat);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(), false);
    }
}
