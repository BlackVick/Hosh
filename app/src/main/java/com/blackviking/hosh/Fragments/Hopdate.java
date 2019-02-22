package com.blackviking.hosh.Fragments;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.support.v4.content.FileProvider;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.hosh.BuildConfig;
import com.blackviking.hosh.Common.Common;
import com.blackviking.hosh.Common.Permissions;
import com.blackviking.hosh.Home;
import com.blackviking.hosh.Model.HopdateModel;
import com.blackviking.hosh.R;
import com.blackviking.hosh.Settings.Faq;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.theartofdev.edmodo.cropper.CropImage;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import dmax.dialog.SpotsDialog;
import id.zelory.compressor.Compressor;
import io.paperdb.Paper;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class Hopdate extends Fragment {

    private ImageView exitActivity, help, hopdateImage;
    private TextView activityName;
    private EditText hopdateText;
    private Button hopdateShare;
    private android.app.AlertDialog mDialog;
    private static final int VERIFY_PERMISSIONS_REQUEST = 757;
    private static final int CAMERA_REQUEST_CODE = 656;
    private static final int GALLERY_REQUEST_CODE = 665;
    private Uri imageUri;
    private String currentUid;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference hopdateRef, pushRef;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference imageRef, imageThumbRef;
    private String originalImageUrl, thumbDownloadUrl;
    private HopdateModel newHopdate;

    public Hopdate() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_hopdate, container, false);


        /*---   PAPER DB   ---*/
        Paper.init(getContext());


        /*---   FIREBASE   ---*/
        hopdateRef = db.getReference("Hopdate");
        imageRef = storage.getReference("HopdateImages");
        imageThumbRef = storage.getReference("HopdateImages");


        /*---   WIDGETS   ---*/
        exitActivity = (ImageView)v.findViewById(R.id.exitActivity);
        help = (ImageView)v.findViewById(R.id.helpIcon);
        hopdateImage = (ImageView)v.findViewById(R.id.hopdateImage);
        activityName = (TextView)v.findViewById(R.id.activityName);
        hopdateText = (EditText)v.findViewById(R.id.hopdateDetails);
        hopdateShare = (Button)v.findViewById(R.id.hopdateShare);


        /*---   EXIT   ---*/
        exitActivity.setImageResource(R.drawable.ic_action_close_app);
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });


        /*---   ACTIVITY NAME   ---*/
        activityName.setText("Hopdate Share");


        /*---   HELP   ---*/
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent faqIntent = new Intent(getContext(), Faq.class);
                startActivity(faqIntent);
            }
        });


        /*---   PERMISSIONS HANDLER   ---*/
        if (checkPermissionsArray(Permissions.PERMISSIONS)){


        } else {

            verifyPermissions(Permissions.PERMISSIONS);

        }


        /*---   HOPDATE IMAGE   ---*/
        hopdateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openChoiceDialog();

            }
        });


        /*---   SHARE HOPDATE   ---*/
        hopdateShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareHopdate();
            }
        });

        return v;
    }

    private void shareHopdate() {

        final String theHopdate = hopdateText.getText().toString();

        currentUid = mAuth.getCurrentUser().getUid();

        final long date = System.currentTimeMillis();
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yy HH:mm");
        final String dateString = sdf.format(date);

        if (imageUri != null || !theHopdate.isEmpty()){

            if (Common.isConnectedToInternet(getContext())){

                if (imageUri != null){

                    newHopdate = new HopdateModel("User", "", theHopdate, currentUid, originalImageUrl, thumbDownloadUrl, dateString, "Image");

                    pushRef = hopdateRef.push();
                    final String pushId = pushRef.getKey();
                    hopdateRef.child(pushId).setValue(newHopdate);

                    FirebaseMessaging.getInstance().subscribeToTopic(Common.FEED_NOTIFICATION_TOPIC+pushId);
                    Paper.book().write(Common.FEED_NOTIFICATION_TOPIC+pushId, "true");

                    Intent i = new Intent(getContext(), Home.class);
                    startActivity(i);
                    getActivity().finish();

                } else {

                    newHopdate = new HopdateModel("User", "", theHopdate, currentUid, "", "", dateString, "Text");

                    pushRef = hopdateRef.push();
                    final String pushId = pushRef.getKey();
                    hopdateRef.child(pushId).setValue(newHopdate);

                    FirebaseMessaging.getInstance().subscribeToTopic(Common.FEED_NOTIFICATION_TOPIC+pushId);
                    Paper.book().write(Common.FEED_NOTIFICATION_TOPIC+pushId, "true");

                    Intent i = new Intent(getContext(), Home.class);
                    startActivity(i);
                    getActivity().finish();

                }

            } else {

                Snackbar.make(getView(), "No Internet Access !", Snackbar.LENGTH_LONG).show();

            }

        } else {

            Snackbar.make(getView(), "Hopdate Has To Contain Valid Stuff . . .", Snackbar.LENGTH_LONG).show();

        }

    }

    private void openChoiceDialog() {

        final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
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

                if (Common.isConnectedToInternet(getContext())){

                    openCamera();

                }else {

                    Snackbar.make(getView(), "No Internet Access !", Snackbar.LENGTH_LONG).show();
                }
                alertDialog.dismiss();

            }
        });

        galleryPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
                alertDialog.dismiss();
            }
        });
        alertDialog.show();

    }

    private void openGallery() {

        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , GALLERY_REQUEST_CODE);

    }

    private void openCamera() {

        final long date = System.currentTimeMillis();
        final String dateShitFmt = String.valueOf(date);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file=getOutputMediaFile(1);
        imageUri = FileProvider.getUriForFile(
                getContext(),
                BuildConfig.APPLICATION_ID + ".provider",
                file);
        //imageUri = Uri.fromFile(file);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);

    }

    private void verifyPermissions(String[] permissions) {

        ActivityCompat.requestPermissions(
                getActivity(),
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

        int permissionRequest = ActivityCompat.checkSelfPermission(getContext(), permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED){

            return false;
        } else {

            return true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){

            Uri theUri = imageUri;
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(getContext(), Hopdate.this);

        }

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK){

            if (data.getData() != null) {
                imageUri = data.getData();

                CropImage.activity(imageUri)
                        .setAspectRatio(1,1)
                        .start(getContext(), Hopdate.this);
            }

        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mDialog = new SpotsDialog(getContext(), "Upload In Progress . . .");
                mDialog.show();

                Uri resultUri = result.getUri();
                String imgURI = resultUri.toString();
                setImage(imgURI, hopdateImage);

                final long date = System.currentTimeMillis();
                final String dateShitFmt = String.valueOf(date);

                File thumb_filepath = new File(resultUri.getPath());


                try {
                    Bitmap thumb_bitmap = new Compressor(getContext())
                            .setMaxWidth(300)
                            .setMaxHeight(300)
                            .setQuality(70)
                            .compressToBitmap(thumb_filepath);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    final byte[] thumb_byte = baos.toByteArray();

                    final StorageReference imageRef1 = imageRef.child("FullImages").child(dateShitFmt + ".jpg");

                    final StorageReference imageThumbRef1 = imageThumbRef.child("Thumbnails").child(dateShitFmt + ".jpg");

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
                                            Snackbar.make(getView(), "Upload Completed", Snackbar.LENGTH_LONG).show();

                                        } else {
                                            mDialog.dismiss();
                                            Snackbar.make(getView(), "Error Occurred While Uploading", Snackbar.LENGTH_LONG).show();
                                        }
                                    }
                                });

                            } else {

                                mDialog.dismiss();
                                Snackbar.make(getView(), "Error Uploading", Snackbar.LENGTH_LONG).show();

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

    private void setImage(String imgUrl, ImageView image){

        ImageLoader loader = ImageLoader.getInstance();

        loader.init(ImageLoaderConfiguration.createDefault(getContext()));

        loader.displayImage(imgUrl, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });

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
}
