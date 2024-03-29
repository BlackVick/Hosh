package com.blackviking.hosh;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.hosh.ImageViewers.MyGalleryImageView;
import com.blackviking.hosh.ImageViewers.OtherProfileImageView;
import com.blackviking.hosh.Interface.ItemClickListener;
import com.blackviking.hosh.Model.ImageModel;
import com.blackviking.hosh.Settings.Faq;
import com.blackviking.hosh.ViewHolder.GalleryViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rohitarya.picasso.facedetection.transformation.FaceCenterCrop;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class UserImageGallery extends AppCompatActivity {

    private ImageView exitActivity, help;
    private TextView activityName;
    private RelativeLayout rootLayout;
    private RecyclerView galleryRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<ImageModel, GalleryViewHolder> adapter;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference galleryRef;
    private String currentUserId;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*---   FONT MANAGEMENT   ---*/
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Thin.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_user_image_gallery);


        /*---   INTENT DATA   ---*/
        currentUserId = getIntent().getStringExtra("UserId");


        /*---   FIREBASE   ---*/
        galleryRef = db.getReference("Users").child(currentUserId).child("Gallery");


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        help = (ImageView)findViewById(R.id.helpIcon);
        rootLayout = (RelativeLayout)findViewById(R.id.userImageGalleryRootLayout);
        galleryRecycler = (RecyclerView)findViewById(R.id.userGalleryRecycler);


        /*---   ACTIVITY NAME   ---*/
        activityName.setText("Image Gallery");


        /*---   EXIT ACTIVITY   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        /*---   HELP   ---*/
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(UserImageGallery.this, Faq.class);
                startActivity(helpIntent);
            }
        });

        loadGallery();
    }

    private void loadGallery() {

        /*---   RECYCLER CONTROLLER   ---*/
        galleryRecycler.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(this, 5);
        galleryRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<ImageModel, GalleryViewHolder>(
                ImageModel.class,
                R.layout.gallery_item,
                GalleryViewHolder.class,
                galleryRef
        ) {
            @Override
            protected void populateViewHolder(final GalleryViewHolder viewHolder, final ImageModel model, int position) {

                if (!model.getImageThumbUrl().equals("")){

                    Picasso.with(getBaseContext())
                            .load(model.getImageThumbUrl())
                            .placeholder(R.drawable.ic_loading_animation)
                            .into(viewHolder.galleryImage);

                    viewHolder.setItemClickListener(new ItemClickListener() {
                        @Override
                        public void onClick(View view, int position, boolean isLongClick) {
                            Intent i = new Intent(UserImageGallery.this, OtherProfileImageView.class);
                            i.putExtra("UserId", currentUserId);
                            i.putExtra("ImageUrl", model.getImageUrl());
                            i.putExtra("ImageThumbUrl", model.getImageThumbUrl());
                            startActivity(i);
                        }
                    });



                }

            }
        };
        galleryRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
