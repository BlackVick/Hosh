package com.blackviking.hosh.Fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.hosh.Common.Common;
import com.blackviking.hosh.Interface.ItemClickListener;
import com.blackviking.hosh.Model.HopdateModel;
import com.blackviking.hosh.Model.UserModel;
import com.blackviking.hosh.OtherUserProfile;
import com.blackviking.hosh.R;
import com.blackviking.hosh.Settings.Faq;
import com.blackviking.hosh.ViewHolder.FeedViewHolder;
import com.blackviking.hosh.ViewHolder.HookupViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rohitarya.picasso.facedetection.transformation.FaceCenterCrop;
import com.rohitarya.picasso.facedetection.transformation.core.PicassoFaceDetector;
import com.squareup.picasso.Picasso;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class HookUp extends Fragment {

    private ImageView exitActivity, help;
    private TextView activityName;
    private RecyclerView hookupRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<UserModel, HookupViewHolder> adapter;
    private String currentUid, sexToHaunt = "";
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef;
    private android.app.AlertDialog mDialog;

    public HookUp() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_hook_up, container, false);


        /*---   PAPER DB   ---*/
        Paper.init(getContext());


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();

        userRef = db.getReference("Users");


        /*---   WIDGETS   ---*/
        exitActivity = (ImageView)v.findViewById(R.id.exitActivity);
        help = (ImageView)v.findViewById(R.id.helpIcon);
        activityName = (TextView)v.findViewById(R.id.activityName);
        hookupRecycler = (RecyclerView)v.findViewById(R.id.hookupRecycler);


        /*---   EXIT   ---*/
        exitActivity.setImageResource(R.drawable.ic_action_close_app);
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });


        /*---   ACTIVITY NAME   ---*/
        activityName.setText("Hookup");


        /*---   DIALOG   ---*/
        mDialog = new SpotsDialog(getContext(), "Searching . . .");
        mDialog.show();


        /*---   HELP   ---*/
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent faqIntent = new Intent(getContext(), Faq.class);
                startActivity(faqIntent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });


        /*---   RECYCLER CONTROLLER   ---*/
        hookupRecycler.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(getContext(), 5);
        hookupRecycler.setLayoutManager(layoutManager);


        if (Common.isConnectedToInternet(getContext())) {

            /*---   INTERESTED IN   ---*/
            userRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String myInterest = dataSnapshot.child("lookingFor").getValue().toString();
                    String mySex = dataSnapshot.child("sex").getValue().toString();

                    if (myInterest.equalsIgnoreCase("Women")){

                        sexToHaunt = "Men";

                        /*---   LOAD   ---*/
                        userRef.orderByChild("lookingFor").equalTo(sexToHaunt).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){

                                    mDialog.dismiss();
                                    loadHookups(currentUid);

                                } else {

                                    mDialog.dismiss();
                                    openNoMatchFound();

                                }


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    } else if (myInterest.equalsIgnoreCase("Men")){

                        sexToHaunt = "Women";

                        /*---   LOAD   ---*/
                        userRef.orderByChild("lookingFor").equalTo(sexToHaunt).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){

                                    mDialog.dismiss();
                                    loadHookups(currentUid);

                                } else {

                                    mDialog.dismiss();
                                    openNoMatchFound();

                                }


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    } else if (myInterest.equalsIgnoreCase("Groupie")){

                        sexToHaunt = "Groupie";

                        /*---   LOAD   ---*/
                        userRef.orderByChild("lookingFor").equalTo(sexToHaunt).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){

                                    mDialog.dismiss();
                                    loadHookups(currentUid);

                                } else {

                                    mDialog.dismiss();
                                    openNoMatchFound();

                                }


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }

                    userRef.removeEventListener(this);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else {

            mDialog.dismiss();
            showNoInternetDialog();

        }

        return v;
    }

    private void showNoInternetDialog() {

        final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.no_internet_access_dialog,null);

        final Button okay = (Button) viewOptions.findViewById(R.id.okayButton);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.dismiss();

            }
        });
        alertDialog.show();

    }

    private void loadHookups(final String currentUid) {

        adapter = new FirebaseRecyclerAdapter<UserModel, HookupViewHolder>(
                UserModel.class,
                R.layout.hookup_item,
                HookupViewHolder.class,
                userRef.orderByChild("lookingFor").equalTo(sexToHaunt)
        ) {
            @Override
            protected void populateViewHolder(final HookupViewHolder viewHolder, final UserModel model, int position) {

                final String ids = adapter.getRef(position).getKey();

                if (!ids.equals(currentUid)) {

                    viewHolder.theLayout.setVisibility(View.VISIBLE);

                    if (!model.getProfilePictureThumb().equals("")) {

                        Picasso.with(getContext())
                                .load(model.getProfilePictureThumb())
                                .placeholder(R.drawable.ic_loading_animation)
                                .into(viewHolder.hookUpImage);

                    } else {

                        viewHolder.hookUpImage.setImageResource(R.drawable.empty_profile);

                    }


                    /*---    ONLINE STATUS   ---*/
                    if (model.getOnlineState().equalsIgnoreCase("Online")) {

                        viewHolder.onlineIndicator.setVisibility(View.VISIBLE);

                    } else {

                        viewHolder.onlineIndicator.setVisibility(View.GONE);

                    }

                    viewHolder.setItemClickListener(new ItemClickListener() {
                        @Override
                        public void onClick(View view, int position, boolean isLongClick) {
                            Intent posterProfile = new Intent(getContext(), OtherUserProfile.class);
                            posterProfile.putExtra("UserId", ids);
                            startActivity(posterProfile);
                            getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                    });

                } else {

                    viewHolder.theLayout.setVisibility(View.GONE);

                }

            }
        };
        hookupRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private void openNoMatchFound() {

        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(getContext())
                .setTitle("No Match !")
                .setIcon(R.drawable.ic_action_account)
                .setMessage("There are no more matches in your area")
                .setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                })
                .create();

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.show();

    }

}
