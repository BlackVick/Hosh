package com.blackviking.hosh;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.blackviking.hosh.Common.Common;
import com.blackviking.hosh.Common.GetTimeAgo;
import com.blackviking.hosh.Common.Permissions;
import com.blackviking.hosh.ImageViewers.MessageImageView;
import com.blackviking.hosh.Interface.ItemClickListener;
import com.blackviking.hosh.Model.DataMessage;
import com.blackviking.hosh.Model.EmojiModel;
import com.blackviking.hosh.Model.MessageModel;
import com.blackviking.hosh.Model.MessageSessionModel;
import com.blackviking.hosh.Model.MyResponse;
import com.blackviking.hosh.Notification.APIService;
import com.blackviking.hosh.ViewHolder.EmojiViewHolder;
import com.blackviking.hosh.ViewHolder.MessagingViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import id.zelory.compressor.Compressor;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Messaging extends AppCompatActivity {

    private RelativeLayout rootLayout;
    private ImageView exitActivity, attachement, sendMessageBtn, sendVoiceNote, addSmiley;
    private TextView userName;
    private CircleImageView userImage;
    private EditText chatBox;
    private RecyclerView messageRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, messageRef, friendMessageRef, messageListRef, friendMessageListRef, messageSessionRef, messageSessionFriendRef, sessionPushIdRef;
    private String currentUid, friendId, friendUserName;
    private FirebaseRecyclerAdapter<MessageModel, MessagingViewHolder> adapter;
    private static final int GALLERY_REQUEST_CODE = 686;
    private static final int CAMERA_REQUEST_CODE = 456;
    private static final int VERIFY_PERMISSIONS_REQUEST = 17;
    private Uri imageUri;
    private String originalImageUrl, thumbDownloadUrl;
    private android.app.AlertDialog mDialog;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference imageRef, imageThumbRef;
    private String messageSessionId = "";
    private String sessionId = "";
    private APIService mService;
    private BroadcastReceiver mMessageReceiver = null;

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

        setContentView(R.layout.activity_messaging);


        /*---   LOCAL   ---*/
        Paper.init(this);
        Paper.book().write(Common.APP_STATE, "Foreground");


        /*---   FCM   ---*/
        mService = Common.getFCMService();


        /*---   INTENT   ---*/
        friendId = getIntent().getStringExtra("UserId");
        friendUserName = getIntent().getStringExtra("UserName");


        /*---   WIDGETS   ---*/
        rootLayout = (RelativeLayout)findViewById(R.id.messagingRootLayout);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        userName = (TextView)findViewById(R.id.msgUserNameView);
        userImage = (CircleImageView)findViewById(R.id.msgUserImageView);
        sendMessageBtn = (ImageView) findViewById(R.id.sendMessage);
        sendVoiceNote = (ImageView) findViewById(R.id.sendVoiceNote);
        addSmiley = (ImageView) findViewById(R.id.addSmiley);
        attachement = (ImageView) findViewById(R.id.addAttachment);
        chatBox = (EditText)findViewById(R.id.messageEditText);
        messageRecycler = (RecyclerView)findViewById(R.id.messagingRecycler);


        /*---   IN APP NOTIFICATION   ---*/
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("Message");

                Snackbar snackbar = Snackbar
                        .make(rootLayout, message, Snackbar.LENGTH_LONG);
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.WHITE);
                sbView.setBackgroundColor(ContextCompat.getColor(Messaging.this, R.color.colorPrimaryDark));
                snackbar.setDuration(2500);
                snackbar.show();
            }
        };



        /*---   FIREBASE   ---*/
        currentUid = mAuth.getCurrentUser().getUid();


        userRef = db.getReference("Users");

        /*---   ONLINE STATE   ---*/
        userRef.child(currentUid).child("onlineState").setValue("Online");


        /*---   MESSAGE REF   ---*/
        messageRef = db.getReference("Users").child(currentUid).child("Messages").child(friendId);
        friendMessageRef = db.getReference("Users").child(friendId).child("Messages").child(currentUid);


        /*---   MESSAGE LIST REF   ---*/
        messageListRef = db.getReference("Users").child(currentUid).child("MessageList");
        friendMessageListRef = db.getReference("Users").child(friendId).child("MessageList");


        /*---   SESSION REF   ---*/
        messageSessionRef = db.getReference("Users").child(currentUid).child("MessageSessions");
        messageSessionFriendRef = db.getReference("Users").child(friendId).child("MessageSessions");


        /*---   STORAGE REF   ---*/
        imageRef = storage.getReference("ChatImages");
        imageThumbRef = storage.getReference("ChatImages");


        /*---   FRIEND INFO   ---*/
        userName.setText("@"+friendUserName);
        userRef.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String friendImageLink = dataSnapshot.child("profilePictureThumb").getValue().toString();

                if (!friendImageLink.equals("")) {

                    Picasso.with(getBaseContext())
                            .load(friendImageLink)
                            .placeholder(R.drawable.ic_loading_animation)
                            .into(userImage);

                } else {

                    userImage.setImageResource(R.drawable.empty_profile);

                }

                /*---   VIEW PROFILE   ---*/
                userImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent userIntent = new Intent(Messaging.this, OtherUserProfile.class);
                        userIntent.putExtra("UserId", friendId);
                        startActivity(userIntent);
                    }
                });

                userName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent userIntent = new Intent(Messaging.this, OtherUserProfile.class);
                        userIntent.putExtra("UserId", friendId);
                        startActivity(userIntent);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /*---   EXIT ACTIVITY   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageListRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(friendId).exists()){

                            messageListRef.child(friendId).child("read").setValue("true");
                            finish();

                        } else {

                            finish();

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });


        /*---   SEND MESSAGE   ---*/
        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(currentUid);
            }
        });


        /*---   ATTACHMENTS   ---*/
        attachement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAttachmentDialog();
            }
        });


        /*---   CREATE MESSAGE SESSION KEY   ---*/
        messageSessionRef.orderByChild("friendId").equalTo(friendId).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        sessionPushIdRef = messageSessionRef.push();

                        final String sessionPushId = sessionPushIdRef.getKey();

                        if (!dataSnapshot.exists()) {

                            MessageSessionModel newMessageSession = new MessageSessionModel(currentUid, friendId);
                            final MessageSessionModel newMessageSessionFriend = new MessageSessionModel(friendId, currentUid);

                            messageSessionRef.child(sessionPushId).setValue(newMessageSession)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            messageSessionFriendRef.child(sessionPushId).setValue(newMessageSessionFriend)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            messageSessionId = sessionPushId;
                                                            System.out.println("Done");
                                                        }
                                                    });

                                        }
                                    });

                        } else {

                            for (DataSnapshot child : dataSnapshot.getChildren()) {

                                sessionId = child.getKey();
                                messageSessionId = sessionId;

                            }

                        }

                        messageSessionRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );


        /*---   PERMISSIONS HANDLER   ---*/
        if (checkPermissionsArray(Permissions.PERMISSIONS)){


        } else {

            verifyPermissions(Permissions.PERMISSIONS);

        }

        /*---   TEXT WATCHER   ---*/
        addSmiley.setVisibility(View.VISIBLE);
        chatBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {



            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String stg = s.toString();

                if (stg.length() > 0){

                    sendMessageBtn.setVisibility(View.VISIBLE);
                    addSmiley.setVisibility(View.GONE);

                } else {

                    sendMessageBtn.setVisibility(View.GONE);
                    addSmiley.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        /*---    SEND EMOTICON   ---*/
        addSmiley.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEmoticonDialog();
            }
        });

        loadMessages();

    }

    private void openEmoticonDialog() {

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.emoji_dialog,null);

        final RecyclerView emojiRecycler;
        final LinearLayoutManager emojiLayoutManager;
        final FirebaseRecyclerAdapter<EmojiModel, EmojiViewHolder> emojiAdapter;


        emojiRecycler = (RecyclerView)viewOptions.findViewById(R.id.emojiRecycler);

        /*---   LOADING EMOJI   ---*/
        emojiRecycler.setHasFixedSize(true);
        emojiLayoutManager = new GridLayoutManager(this, 4);
        emojiRecycler.setLayoutManager(emojiLayoutManager);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.EmojiShitAnimation;

        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        //layoutParams.x = 100; // left margin
        //layoutParams.y = 100; // bottom margin
        alertDialog.getWindow().setAttributes(layoutParams);

        DatabaseReference emojiRef = db.getReference("Emoticons");

        emojiAdapter = new FirebaseRecyclerAdapter<EmojiModel, EmojiViewHolder>(
                EmojiModel.class,
                R.layout.emoji_item,
                EmojiViewHolder.class,
                emojiRef
        ) {
            @Override
            protected void populateViewHolder(final EmojiViewHolder viewHolder, final EmojiModel model, int position) {

                Picasso.with(getBaseContext())
                        .load(model.getLink())
                        .placeholder(R.drawable.ic_loading_animation)
                        .into(viewHolder.emojiView);

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        /*---   PUSH   ---*/
                        final DatabaseReference pushIdRef = messageRef.push();
                        final String pushId = pushIdRef.getKey();


                                            /*---   MODEL FOR CHAT LIST   ---*/
                        final Map<String, Object> messageListMap = new HashMap<>();
                        messageListMap.put("id", pushId);
                        messageListMap.put("message", model.getLink());
                        messageListMap.put("messageThumb", model.getLink());
                        messageListMap.put("timeStamp", ServerValue.TIMESTAMP);
                        messageListMap.put("read", "true");
                        messageListMap.put("type", "Image");
                        messageListMap.put("from", currentUid);
                        messageListMap.put("sessionId", messageSessionId);


                                            /*---   FRIEND MODEL FOR CHAT LIST   ---*/
                        final Map<String, Object> messageListMapFriend = new HashMap<>();
                        messageListMapFriend.put("id", pushId);
                        messageListMapFriend.put("message", model.getLink());
                        messageListMapFriend.put("messageThumb", model.getLink());
                        messageListMapFriend.put("timeStamp", ServerValue.TIMESTAMP);
                        messageListMapFriend.put("read", "false");
                        messageListMapFriend.put("type", "Image");
                        messageListMapFriend.put("from", currentUid);
                        messageListMapFriend.put("sessionId", messageSessionId);


                                            /*---   MODEL FOR MESSAGES   ---*/
                                            /*---   MODEL FOR MESSAGE   ---*/
                        final Map<String, Object> messageMap = new HashMap<>();
                        messageMap.put("id", pushId);
                        messageMap.put("message", model.getLink());
                        messageMap.put("messageThumb", model.getLink());
                        messageMap.put("timeStamp", ServerValue.TIMESTAMP);
                        messageMap.put("read", "true");
                        messageMap.put("type", "Image");
                        messageMap.put("from", currentUid);
                        messageMap.put("sessionId", messageSessionId);



                                            /*---   FRIEND MODEL FOR MESSAGE   ---*/
                        final Map<String, Object> messageMapFriend = new HashMap<>();
                        messageMapFriend.put("id", pushId);
                        messageMapFriend.put("message", model.getLink());
                        messageMapFriend.put("messageThumb", model.getLink());
                        messageMapFriend.put("timeStamp", ServerValue.TIMESTAMP);
                        messageMapFriend.put("read", "false");
                        messageMapFriend.put("type", "Image");
                        messageMapFriend.put("from", currentUid);
                        messageMapFriend.put("sessionId", messageSessionId);


                        messageRef.child(pushId).setValue(messageMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                alertDialog.dismiss();
                                chatBox.setText("");
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                friendMessageRef.child(pushId).setValue(messageMapFriend);
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                messageListRef.child(friendId).updateChildren(messageListMap).addOnSuccessListener(
                                        new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                friendMessageListRef.child(currentUid).updateChildren(messageListMapFriend);
                                                sendEmojiNotification();
                                            }
                                        }
                                );

                            }
                        });

                    }
                });

            }
        };
        emojiRecycler.setAdapter(emojiAdapter);
        emojiAdapter.notifyDataSetChanged();

        alertDialog.show();

    }

    private void loadMessages() {

        /*---   LOADING MESSAGES   ---*/
        messageRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        messageRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<MessageModel, MessagingViewHolder>(
                MessageModel.class,
                R.layout.single_message_item,
                MessagingViewHolder.class,
                messageRef
        ) {
            @Override
            protected void populateViewHolder(final MessagingViewHolder viewHolder, final MessageModel model, int position) {

                /*---   GET TIME AGO ALGORITHM   ---*/
                GetTimeAgo getTimeAgo = new GetTimeAgo();
                long lastTime = model.getTimeStamp();
                String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());



                if (model.getFrom().equals(currentUid)){

                    if (model.getType().equals("Text")){

                        viewHolder.yourMsgLayout.setVisibility(View.VISIBLE);
                        viewHolder.otherMsgLayout.setVisibility(View.GONE);
                        viewHolder.yourMsgImage.setVisibility(View.GONE);
                        viewHolder.myText.setVisibility(View.VISIBLE);
                        viewHolder.myText.setText(model.getMessage());


                    } else if (model.getType().equals("Image")){

                        viewHolder.yourMsgLayout.setVisibility(View.VISIBLE);
                        viewHolder.otherMsgLayout.setVisibility(View.GONE);
                        viewHolder.yourMsgImage.setVisibility(View.VISIBLE);
                        viewHolder.myText.setVisibility(View.GONE);

                        if (!model.getMessageThumb().equals("")){

                            Picasso.with(getBaseContext())
                                    .load(model.getMessageThumb())
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.ic_loading_animation)
                                    .into(viewHolder.yourMsgImage, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(getBaseContext())
                                                    .load(model.getMessageThumb())
                                                    .placeholder(R.drawable.ic_loading_animation)
                                                    .into(viewHolder.yourMsgImage);
                                        }
                                    });

                            viewHolder.yourMsgImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent viewImage = new Intent(Messaging.this, MessageImageView.class);
                                    viewImage.putExtra("ThumbUrl", model.getMessageThumb());
                                    viewImage.putExtra("ImageUrl", model.getMessage());
                                    startActivity(viewImage);
                                }
                            });

                        }

                    }

                    viewHolder.myTextTimeStamp.setVisibility(View.VISIBLE);
                    viewHolder.myTextTimeStamp.setText(lastSeenTime);


                } else if (model.getFrom().equals(friendId)){

                    if (model.getType().equals("Text")){

                        viewHolder.yourMsgLayout.setVisibility(View.GONE);
                        viewHolder.otherMsgLayout.setVisibility(View.VISIBLE);
                        viewHolder.otherMsgImage.setVisibility(View.GONE);
                        viewHolder.otherText.setVisibility(View.VISIBLE);
                        viewHolder.otherText.setText(model.getMessage());

                        if (model.getRead().equals("false")){

                            messageRef.child(adapter.getRef(position).getKey()).child("read").setValue("true");
                            messageListRef.child(friendId).child("read").setValue("true");

                        }


                    } else if (model.getType().equals("Image")){

                        viewHolder.yourMsgLayout.setVisibility(View.GONE);
                        viewHolder.otherMsgLayout.setVisibility(View.VISIBLE);
                        viewHolder.otherMsgImage.setVisibility(View.VISIBLE);
                        viewHolder.otherText.setVisibility(View.GONE);

                        if (!model.getMessageThumb().equals("")){

                            Picasso.with(getBaseContext())
                                    .load(model.getMessageThumb())
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.ic_loading_animation)
                                    .into(viewHolder.otherMsgImage, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(getBaseContext())
                                                    .load(model.getMessageThumb())
                                                    .placeholder(R.drawable.ic_loading_animation)
                                                    .into(viewHolder.otherMsgImage);
                                        }
                                    });

                            viewHolder.otherMsgImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent viewImage = new Intent(Messaging.this, MessageImageView.class);
                                    viewImage.putExtra("ThumbUrl", model.getMessageThumb());
                                    viewImage.putExtra("ImageUrl", model.getMessage());
                                    startActivity(viewImage);
                                }
                            });

                        }

                    }

                    viewHolder.otherTextTimeStamp.setVisibility(View.VISIBLE);
                    viewHolder.otherTextTimeStamp.setText(lastSeenTime);
                    viewHolder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                        @Override
                        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                        }
                    });

                }

            }
        };
        messageRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = adapter.getItemCount();
                int lastVisiblePosition =
                        layoutManager.findLastCompletelyVisibleItemPosition();
                /* If the recycler view is initially being loaded or the
                   user is at the bottom of the list, scroll to the bottom
                   of the list to show the newly added message.*/
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    messageRecycler.scrollToPosition(positionStart);

                }
                layoutManager.smoothScrollToPosition(messageRecycler, null, adapter.getItemCount());
            }
        });

    }

    private void openAttachmentDialog() {

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.image_source_choice,null);

        final ImageView cameraPick = (ImageView) viewOptions.findViewById(R.id.cameraPick);
        final ImageView galleryPick = (ImageView) viewOptions.findViewById(R.id.galleryPick);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        //layoutParams.x = 100; // left margin
        layoutParams.y = 100; // bottom margin
        alertDialog.getWindow().setAttributes(layoutParams);



        cameraPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(Messaging.this)){

                    openCamera();

                }else {

                    Snackbar.make(rootLayout, "No Internet Access !", Snackbar.LENGTH_LONG).show();
                }
                alertDialog.dismiss();

            }
        });

        galleryPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(Messaging.this)){

                    openGallery();

                }else {

                    Snackbar.make(rootLayout, "No Internet Access !", Snackbar.LENGTH_LONG).show();
                }
                alertDialog.dismiss();
            }
        });
        alertDialog.show();

    }

    private void openCamera() {

        final long date = System.currentTimeMillis();
        final String dateShitFmt = String.valueOf(date);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        /*File photo = new File(Environment.getExternalStorageDirectory(), "Hosh/Images/"+dateShitFmt+".jpg");

        imageUri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", photo);*/

        File file=getOutputMediaFile(1);
        imageUri = FileProvider.getUriForFile(
                Messaging.this,
                BuildConfig.APPLICATION_ID + ".provider",
                file);
        //imageUri = Uri.fromFile(file);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);

    }

    private void openGallery() {

        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , GALLERY_REQUEST_CODE);

    }

    private void sendMessage(final String currentUid) {

        final String message = chatBox.getText().toString().trim();

        if (!TextUtils.isEmpty(message)){

            if (Common.isConnectedToInternet(getBaseContext())){

                /*---   PUSH   ---*/
                final DatabaseReference pushIdRef = messageRef.push();
                final String pushId = pushIdRef.getKey();


                        /*---   MODEL FOR CHAT LIST   ---*/
                final Map<String, Object> messageListMap = new HashMap<>();
                messageListMap.put("id", pushId);
                messageListMap.put("message", message);
                messageListMap.put("messageThumb", "");
                messageListMap.put("timeStamp", ServerValue.TIMESTAMP);
                messageListMap.put("read", "true");
                messageListMap.put("type", "Text");
                messageListMap.put("from", currentUid);
                messageListMap.put("sessionId", messageSessionId);


                        /*---   FRIEND MODEL FOR CHAT LIST   ---*/
                final Map<String, Object> messageListMapFriend = new HashMap<>();
                messageListMapFriend.put("id", pushId);
                messageListMapFriend.put("message", message);
                messageListMapFriend.put("messageThumb", "");
                messageListMapFriend.put("timeStamp", ServerValue.TIMESTAMP);
                messageListMapFriend.put("read", "false");
                messageListMapFriend.put("type", "Text");
                messageListMapFriend.put("from", currentUid);
                messageListMapFriend.put("sessionId", messageSessionId);


                        /*---   MODEL FOR MESSAGES   ---*/
                        /*---   MODEL FOR MESSAGE   ---*/
                final Map<String, Object> messageMap = new HashMap<>();
                messageMap.put("id", pushId);
                messageMap.put("message", message);
                messageMap.put("messageThumb", "");
                messageMap.put("timeStamp", ServerValue.TIMESTAMP);
                messageMap.put("read", "true");
                messageMap.put("type", "Text");
                messageMap.put("from", currentUid);
                messageMap.put("sessionId", messageSessionId);



                        /*---   FRIEND MODEL FOR MESSAGE   ---*/
                final Map<String, Object> messageMapFriend = new HashMap<>();
                messageMapFriend.put("id", pushId);
                messageMapFriend.put("message", message);
                messageMapFriend.put("messageThumb", "");
                messageMapFriend.put("timeStamp", ServerValue.TIMESTAMP);
                messageMapFriend.put("read", "false");
                messageMapFriend.put("type", "Text");
                messageMapFriend.put("from", currentUid);
                messageMapFriend.put("sessionId", messageSessionId);



                messageRef.child(pushId).setValue(messageMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        chatBox.setText("");
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        friendMessageRef.child(pushId).setValue(messageMapFriend);
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        messageListRef.child(friendId).updateChildren(messageListMap).addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        friendMessageListRef.child(currentUid).updateChildren(messageListMapFriend);
                                        sendNotification(message);
                                    }
                                }
                        );

                    }
                });

            } else {

                Snackbar.make(rootLayout, "No Internet Access !", Snackbar.LENGTH_LONG).show();

            }

        }

    }

    private void sendNotification(final String message) {

        userRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String userName = dataSnapshot.child("userName").getValue().toString();
                String userImage = dataSnapshot.child("profilePictureThumb").getValue().toString();

                Map<String, String> dataSend = new HashMap<>();
                dataSend.put("title", "New Message");
                dataSend.put("message", "@"+userName+" : "+message);
                dataSend.put("user_id", currentUid);
                dataSend.put("user_name", userName);
                dataSend.put("user_image", userImage);
                dataSend.put("my_name", friendUserName);
                dataSend.put("my_id", currentUid);
                DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(friendId).toString(), dataSend);

                mService.sendNotification(dataMessage)
                        .enqueue(new retrofit2.Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {
                                Snackbar.make(rootLayout, "Error Sending Notification !", Snackbar.LENGTH_LONG).show();
                            }
                        });

                userRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendPictureNotification() {

        userRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String userName = dataSnapshot.child("userName").getValue().toString();
                String userImage = dataSnapshot.child("profilePictureThumb").getValue().toString();

                Map<String, String> dataSend = new HashMap<>();
                dataSend.put("title", "New Message");
                dataSend.put("message", "@"+userName+" Just Sent You An Image");
                dataSend.put("user_id", currentUid);
                dataSend.put("user_name", userName);
                dataSend.put("user_image", userImage);
                dataSend.put("my_name", friendUserName);
                dataSend.put("my_id", currentUid);
                DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(friendId).toString(), dataSend);

                mService.sendNotification(dataMessage)
                        .enqueue(new retrofit2.Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {
                                Snackbar.make(rootLayout, "Error Sending Notification !", Snackbar.LENGTH_LONG).show();
                            }
                        });

                userRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendEmojiNotification() {

        userRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String userName = dataSnapshot.child("userName").getValue().toString();
                String userImage = dataSnapshot.child("profilePictureThumb").getValue().toString();

                Map<String, String> dataSend = new HashMap<>();
                dataSend.put("title", "New Message");
                dataSend.put("message", "@"+userName+" Just Sent You An Emoji");
                dataSend.put("user_id", currentUid);
                dataSend.put("user_name", userName);
                dataSend.put("user_image", userImage);
                dataSend.put("my_name", friendUserName);
                dataSend.put("my_id", currentUid);
                DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(friendId).toString(), dataSend);

                mService.sendNotification(dataMessage)
                        .enqueue(new retrofit2.Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {
                                Snackbar.make(rootLayout, "Error Sending Notification !", Snackbar.LENGTH_LONG).show();
                            }
                        });

                userRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void verifyPermissions(String[] permissions) {

        ActivityCompat.requestPermissions(
                Messaging.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
    }

    private boolean checkPermissionsArray(String[] permissions) {

        for (int i = 0; i < permissions.length; i++){

            String check = permissions[i];
            if (!checkPermissions(check)){
                return false;
            }

        }
        return true;
    }

    private boolean checkPermissions(String permission) {

        int permissionRequest = ActivityCompat.checkSelfPermission(Messaging.this, permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED){

            return false;
        } else {

            return true;
        }
    }

    private  File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Hosh");

        /**Create the storage directory if it does not exist*/
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }

        /**Create a media file name*/
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == 1){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".png");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){

            //Uri theUri = imageUri;
            CropImage.activity(imageUri)
                    .start(this);

        }

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK){

            if (data.getData() != null) {
                imageUri = data.getData();

                CropImage.activity(imageUri)
                        .start(this);
            }

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mDialog = new SpotsDialog(Messaging.this, "Upload In Progress . . .");
                mDialog.show();

                Uri resultUri = result.getUri();
                String imgURI = resultUri.toString();
                currentUid = mAuth.getCurrentUser().getUid();

                final long date = System.currentTimeMillis();
                final String dateShitFmt = String.valueOf(date);

                File thumb_filepath = new File(resultUri.getPath());

                try {
                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxWidth(250)
                            .setMaxHeight(250)
                            .setQuality(60)
                            .compressToBitmap(thumb_filepath);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                    final byte[] thumb_byte = baos.toByteArray();

                    final StorageReference imageRef1 = imageRef.child("FullImages").child(currentUid + dateShitFmt + ".jpg");

                    final StorageReference imageThumbRef1 = imageThumbRef.child("Thumbnails").child(currentUid + dateShitFmt + ".jpg");

                    imageRef1.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {

                                originalImageUrl = Objects.requireNonNull(task.getResult().getDownloadUrl()).toString();
                                UploadTask uploadTask = imageThumbRef1.putBytes(thumb_byte);

                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                        thumbDownloadUrl = Objects.requireNonNull(thumb_task.getResult().getDownloadUrl()).toString();

                                        if (thumb_task.isSuccessful()){


                                            mDialog.dismiss();

                                            /*---   PUSH   ---*/
                                            final DatabaseReference pushIdRef = messageRef.push();
                                            final String pushId = pushIdRef.getKey();


                                            /*---   MODEL FOR CHAT LIST   ---*/
                                            final Map<String, Object> messageListMap = new HashMap<>();
                                            messageListMap.put("id", pushId);
                                            messageListMap.put("message", originalImageUrl);
                                            messageListMap.put("messageThumb", thumbDownloadUrl);
                                            messageListMap.put("timeStamp", ServerValue.TIMESTAMP);
                                            messageListMap.put("read", "true");
                                            messageListMap.put("type", "Image");
                                            messageListMap.put("from", currentUid);
                                            messageListMap.put("sessionId", messageSessionId);


                                            /*---   FRIEND MODEL FOR CHAT LIST   ---*/
                                            final Map<String, Object> messageListMapFriend = new HashMap<>();
                                            messageListMapFriend.put("id", pushId);
                                            messageListMapFriend.put("message", originalImageUrl);
                                            messageListMapFriend.put("messageThumb", thumbDownloadUrl);
                                            messageListMapFriend.put("timeStamp", ServerValue.TIMESTAMP);
                                            messageListMapFriend.put("read", "false");
                                            messageListMapFriend.put("type", "Image");
                                            messageListMapFriend.put("from", currentUid);
                                            messageListMapFriend.put("sessionId", messageSessionId);


                                            /*---   MODEL FOR MESSAGES   ---*/
                                            /*---   MODEL FOR MESSAGE   ---*/
                                            final Map<String, Object> messageMap = new HashMap<>();
                                            messageMap.put("id", pushId);
                                            messageMap.put("message", originalImageUrl);
                                            messageMap.put("messageThumb", thumbDownloadUrl);
                                            messageMap.put("timeStamp", ServerValue.TIMESTAMP);
                                            messageMap.put("read", "true");
                                            messageMap.put("type", "Image");
                                            messageMap.put("from", currentUid);
                                            messageMap.put("sessionId", messageSessionId);



                                            /*---   FRIEND MODEL FOR MESSAGE   ---*/
                                            final Map<String, Object> messageMapFriend = new HashMap<>();
                                            messageMapFriend.put("id", pushId);
                                            messageMapFriend.put("message", originalImageUrl);
                                            messageMapFriend.put("messageThumb", thumbDownloadUrl);
                                            messageMapFriend.put("timeStamp", ServerValue.TIMESTAMP);
                                            messageMapFriend.put("read", "false");
                                            messageMapFriend.put("type", "Image");
                                            messageMapFriend.put("from", currentUid);
                                            messageMapFriend.put("sessionId", messageSessionId);


                                            messageRef.child(pushId).setValue(messageMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    chatBox.setText("");
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    friendMessageRef.child(pushId).setValue(messageMapFriend);
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    messageListRef.child(friendId).updateChildren(messageListMap).addOnSuccessListener(
                                                            new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    friendMessageListRef.child(currentUid).updateChildren(messageListMapFriend);
                                                                    sendPictureNotification();
                                                                }
                                                            }
                                                    );

                                                }
                                            });

                                        } else {
                                            mDialog.dismiss();
                                            Snackbar.make(rootLayout, "Error Occurred While Uploading", Snackbar.LENGTH_LONG).show();
                                        }
                                    }
                                });



                            } else {

                                mDialog.dismiss();
                                Snackbar.make(rootLayout, "Error Uploading", Snackbar.LENGTH_LONG).show();

                            }
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }




            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        String chatMessageId = adapter.getRef(item.getOrder()).getKey();

        if (item.getTitle().equals(Common.DELETE_SINGLE)){
            deleteCategory(chatMessageId);
        } else if (item.getTitle().equals(Common.DELETE_BOTH)){

            deleteBothCategory(chatMessageId);
        }

        return super.onContextItemSelected(item);
    }

    private void deleteBothCategory(final String chatMessageId) {

        messageRef.child(chatMessageId).removeValue().addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        friendMessageRef.child(chatMessageId).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        final Map<String, Object> messageListMap = new HashMap<>();
                                        messageListMap.put("message", "Retracted a message");

                                        final Map<String, Object> messageFriendListMap = new HashMap<>();
                                        messageFriendListMap.put("message", "A message was retracted");

                                        friendMessageListRef.child(currentUid).updateChildren(messageFriendListMap);
                                        messageListRef.child(friendId).updateChildren(messageListMap);
                                    }
                                });

                    }
                }
        );




    }

    private void deleteCategory(final String chatMessageId) {

        messageRef.child(chatMessageId).removeValue().addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        final Map<String, Object> messageListMap = new HashMap<>();
                        messageListMap.put("message", "Deleted a message");

                        messageListRef.child(friendId).updateChildren(messageListMap);

                    }
                }
        );

    }

    @Override
    public void onBackPressed() {
        messageListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(friendId).exists()){

                    messageListRef.child(friendId).child("read").setValue("true");
                    finish();

                } else {

                    finish();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        /*---   ONLINE STATE   ---*/
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        userRef.child(currentUid).child("onlineState").setValue("Offline");
        Paper.book().write(Common.APP_STATE, "Background");
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*---   ONLINE STATE   ---*/
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("NOTIFICATION_BROADCAST"));
        userRef.child(currentUid).child("onlineState").setValue("Online");
        Paper.book().write(Common.APP_STATE, "Foreground");
    }
}
