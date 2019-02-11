package com.blackviking.hosh.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;

import com.blackviking.hosh.Interface.ItemClickListener;
import com.blackviking.hosh.R;

/**
 * Created by Scarecrow on 2/5/2019.
 */

public class UserProfileGalleryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener{

    private ItemClickListener itemClickListener;
    public ImageView galleryImage;

    public UserProfileGalleryViewHolder(View itemView) {
        super(itemView);

        galleryImage = (ImageView)itemView.findViewById(R.id.userProfileGalleryImage);

        itemView.setOnCreateContextMenuListener(this);
        itemView.setOnClickListener(this);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(), true);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        menu.setHeaderTitle("Options");
        menu.add(0, 0, getAdapterPosition(), "Remove");

    }
}
