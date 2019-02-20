package com.blackviking.hosh.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.blackviking.hosh.Interface.ItemClickListener;
import com.blackviking.hosh.R;

/**
 * Created by Scarecrow on 2/20/2019.
 */

public class EmojiViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ItemClickListener itemClickListener;
    public ImageView emojiView;

    public EmojiViewHolder(View itemView) {
        super(itemView);

        emojiView = (ImageView)itemView.findViewById(R.id.emojiView);

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
