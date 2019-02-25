package com.blackviking.hosh;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.hosh.Common.Common;
import com.blackviking.hosh.Common.Permissions;
import com.blackviking.hosh.Fragments.Hopdate;
import com.blackviking.hosh.ImageViewers.MyGalleryImageView;
import com.blackviking.hosh.Interface.ItemClickListener;
import com.blackviking.hosh.Model.ImageModel;
import com.blackviking.hosh.Settings.Faq;
import com.blackviking.hosh.ViewHolder.GalleryViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rohitarya.picasso.facedetection.transformation.FaceCenterCrop;
import com.rohitarya.picasso.facedetection.transformation.core.PicassoFaceDetector;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import dmax.dialog.SpotsDialog;
import id.zelory.compressor.Compressor;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ImageGallery extends AppCompatActivity {

    private ImageView exitActivity, help;
    private TextView activityName;
    private RelativeLayout rootLayout;
    private RecyclerView galleryRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<ImageModel, GalleryViewHolder> adapter;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference galleryRef;
    private FloatingActionButton addToGallery;
    private android.app.AlertDialog mDialog;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference imageRef, imageThumbRef;
    private static final int GALLERY_REQUEST_CODE = 686;
    private static final int CAMERA_REQUEST_CODE = 456;
    private static final int VERIFY_PERMISSIONS_REQUEST = 17;
    private Uri imageUri;
    private String originalImageUrl, thumbDownloadUrl, currentUid;


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

        setContentView(R.layout.activity_image_gallery);


        /*---   IMAGE FACE DETECTION   ---*/
        PicassoFaceDetector.initialize(this);


        /*---   FIREBASE   ---*/
        currentUid = mAuth.getCurrentUser().getUid();

        galleryRef = db.getReference("Users").child(currentUid).child("Gallery");
        imageRef = storage.getReference("Users").child(currentUid).child("ImageGallery");
        imageThumbRef = storage.getReference("Users").child(currentUid).child("ImageGallery");

        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        help = (ImageView)findViewById(R.id.helpIcon);
        rootLayout = (RelativeLayout)findViewById(R.id.imageGalleryRootLayout);
        galleryRecycler = (RecyclerView)findViewById(R.id.galleryRecycler);
        addToGallery = (FloatingActionButton)findViewById(R.id.addImageToGallery);


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
                Intent helpIntent = new Intent(ImageGallery.this, Faq.class);
                startActivity(helpIntent);
            }
        });


        /*---   ADD TO GALLERY   ---*/
        addToGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChoiceDialog();
            }
        });


        /*---   PERMISSIONS HANDLER   ---*/
        if (checkPermissionsArray(Permissions.PERMISSIONS)){


        } else {

            verifyPermissions(Permissions.PERMISSIONS);

        }


        loadGallery();

    }

    private void openChoiceDialog() {

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

                if (Common.isConnectedToInternet(ImageGallery.this)){

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
                if (Common.isConnectedToInternet(ImageGallery.this)){

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
                ImageGallery.this,
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
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_loading_animation)
                            .into(viewHolder.galleryImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getBaseContext())
                                            .load(model.getImageThumbUrl())
                                            .placeholder(R.drawable.ic_loading_animation)
                                            .into(viewHolder.galleryImage);
                                }
                            });

                    viewHolder.setItemClickListener(new ItemClickListener() {
                        @Override
                        public void onClick(View view, int position, boolean isLongClick) {
                            Intent i = new Intent(ImageGallery.this, MyGalleryImageView.class);
                            i.putExtra("CurrentImage", adapter.getRef(position).getKey());
                            startActivity(i);
                        }
                    });

                    

                }

            }
        };
        galleryRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private void verifyPermissions(String[] permissions) {

        ActivityCompat.requestPermissions(
                ImageGallery.this,
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

        int permissionRequest = ActivityCompat.checkSelfPermission(ImageGallery.this, permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED){

            return false;
        } else {

            return true;
        }
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

                mDialog = new SpotsDialog(ImageGallery.this, "Upload In Progress . . .");
                mDialog.setCancelable(false);
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();

                Uri resultUri = result.getUri();
                String imgURI = resultUri.toString();
                currentUid = mAuth.getCurrentUser().getUid();

                final long date = System.currentTimeMillis();
                final String dateShitFmt = String.valueOf(date);

                File thumb_filepath = new File(resultUri.getPath());

                try {
                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxWidth(400)
                            .setMaxHeight(400)
                            .setQuality(65)
                            .compressToBitmap(thumb_filepath);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 65, baos);
                    final byte[] thumb_byte = baos.toByteArray();

                    final StorageReference imageRef1 = imageRef.child("FullImages").child(currentUid+dateShitFmt + ".jpg");

                    final StorageReference imageThumbRef1 = imageThumbRef.child("Thumbnails").child(currentUid +dateShitFmt+ ".jpg");

                    final UploadTask originalUpload = imageRef1.putFile(resultUri);

                    mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            imageUri = null;
                            originalUpload.cancel();
                        }
                    });

                    originalUpload.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {

                                originalImageUrl = task.getResult().getDownloadUrl().toString();
                                final UploadTask uploadTask = imageThumbRef1.putBytes(thumb_byte);

                                mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        imageUri = null;
                                        uploadTask.cancel();
                                    }
                                });

                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                        thumbDownloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                        if (thumb_task.isSuccessful()){

                                            mDialog.dismiss();
                                            ImageModel newImage = new ImageModel(originalImageUrl, thumbDownloadUrl, "Unsecured");
                                            galleryRef.push().setValue(newImage);

                                        } else {
                                            mDialog.dismiss();
                                        }
                                    }
                                });



                            } else {

                                mDialog.dismiss();

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
    public void onBackPressed() {
        finish();
    }
}
