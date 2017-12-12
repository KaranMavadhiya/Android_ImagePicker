package com.imagepicker;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import library.imagepicker.imageloader.ImageLoader;


public class PicImageDetailActivity extends AppCompatActivity {

    private String TAG = getClass().getSimpleName();
    private Context mContext;

    // Extra name for the ID parameter
    public static final String EXTRA_PARAM_ID = "image:_uri";
    // View name of the header image. Used for activity scene transitions
    public static final String VIEW_NAME_HEADER_IMAGE = "detail:image";

    @BindView(R.id.imageview)
    ImageView imageDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_image_detail);
        initializeComponents();

    }

    public void initializeComponents() {
        mContext = PicImageDetailActivity.this;
        ButterKnife.bind(this);

        // Retrieve the correct Item instance, using the ID provided in the Intent
        String imagePath = getIntent().getStringExtra(EXTRA_PARAM_ID);
        ViewCompat.setTransitionName(imageDetail, VIEW_NAME_HEADER_IMAGE);
        if (imagePath != null && imagePath.length() > 0) {
            Log.d(TAG, "imagePath = " + imagePath);
            Uri imageUri = Uri.fromFile(new File(imagePath));
            ImageLoader.loadImage(mContext, imageDetail, imageUri, R.drawable.place_holder_landscape);
        }

    }

    @OnClick(R.id.imageview)
    public void onViewClicked() {


    }
}
