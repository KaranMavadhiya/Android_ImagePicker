package com.imagepicker;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import library.imagepicker.GetFilePath;
import library.imagepicker.ImagePickerDialog;
import library.imagepicker.imageloader.ImageLoader;

public class PicImageActivity extends AppCompatActivity implements ImagePickerDialog.OnImagePickerItemClick {

    private String TAG = getClass().getSimpleName();
    private Context mContext;

    public static final int REQUEST_PICK_IMAGE = 101;
    public static final int REQUEST_CAPTURE_IMAGE = 102;
    public static final int REQUEST_CROP_IMAGE = 103;

    @BindView(R.id.main_imgPicture)
    ImageView imgPicture;
    @BindView(R.id.main_btnPicImage)
    Button btnPicImage;
    @BindView(R.id.main_btnCropImage)
    Button btnCropImage;

    private Uri selectedImage;
    private String selectedFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_image);
        initializeComponents();

    }

    public void initializeComponents() {
        mContext = PicImageActivity.this;
        ButterKnife.bind(this);

    }

    @OnClick({R.id.main_imgPicture, R.id.main_btnPicImage, R.id.main_btnCropImage})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.main_imgPicture:
                if (selectedFilePath != null && selectedFilePath.length() > 0) {
                    // Construct an Intent as normal
                    Intent intent = new Intent(this, PicImageDetailActivity.class);
                    intent.putExtra(PicImageDetailActivity.EXTRA_PARAM_ID, selectedFilePath);

                    // BEGIN_INCLUDE(start_activity)
                    /**
                     * Now create an {@link android.app.ActivityOptions} instance using the
                     * {@link ActivityOptionsCompat#makeSceneTransitionAnimation(Activity, Pair[])} factory
                     * method.
                     */
                    ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                            // Now we provide a list of Pair items which contain the view we can transitioning
                            // from, and the name of the view it is transitioning to, in the launched activity
                            new Pair<View, String>(view.findViewById(R.id.main_imgPicture), PicImageDetailActivity.VIEW_NAME_HEADER_IMAGE));
                    // Now we can start the Activity, providing the activity options as a bundle
                    ActivityCompat.startActivity(this, intent, activityOptions.toBundle());
                }
                break;
            case R.id.main_btnPicImage:
                openImagePickerDialog(this);
                break;
            case R.id.main_btnCropImage:
                if (selectedFilePath != null && selectedFilePath.length() > 0) {
                    try {
                        cropImage(selectedFilePath, getString(R.string.app_name), 1, 1, 200, 200);
                    } catch (IOException e) {
                        Toast.makeText(mContext, e.getMessage().toString().trim(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(mContext, "Please select image first.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    /**
     * Open image picker dialog
     */
    private void openImagePickerDialog(Context mContext) {
        ImagePickerDialog imagePickerDialog = new ImagePickerDialog();
        imagePickerDialog.setDialogListener(this);
        imagePickerDialog.show(getFragmentManager(), mContext.getClass().getSimpleName());
    }

    @Override
    public void onCameraClick() {
        ImagePickerDialog.captureImage(this, getString(R.string.app_name), REQUEST_CAPTURE_IMAGE);
    }

    @Override
    public void onGalleryClick() {
        ImagePickerDialog.pickImageFromGallery(this, REQUEST_PICK_IMAGE);
    }

    @Override
    public void onCancelClick() {

    }

    /**
     * Perform crop operation on selected image
     *
     * @param sourceFilePath
     * @param directoryName  getString(R.string.app_name)
     * @param aspectX        1
     * @param aspectY        1
     * @param outputX        200
     * @param outputY        200
     * @throws IOException
     */
    private void cropImage(String sourceFilePath, String directoryName, int aspectX, int aspectY, int outputX, int outputY) throws IOException {

        Intent cropIntent = new Intent("com.android.camera.action.CROP");

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + directoryName);
        if (!file.exists()) {
            file.mkdir();
        }

        File cropedFile = new File(file.getAbsolutePath() + File.separator + "CRP_" + System.currentTimeMillis() + ".jpg");
        cropedFile.createNewFile();

        Uri cropImageUri;
        Uri dataImageUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cropImageUri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", cropedFile);
            dataImageUri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", new File(sourceFilePath));
        } else {
            cropImageUri = Uri.fromFile(cropedFile);
            dataImageUri = Uri.fromFile(new File(sourceFilePath));
        }
        cropIntent.setDataAndType(dataImageUri, "image");
        cropIntent.putExtra("crop", "true");
        cropIntent.putExtra("aspectX", aspectX);
        cropIntent.putExtra("aspectY", aspectY);
        cropIntent.putExtra("outputX", outputX);
        cropIntent.putExtra("outputY", outputY);
        cropIntent.putExtra("return-data", false);
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, cropImageUri);

        try {
            startActivityForResult(cropIntent, REQUEST_CROP_IMAGE);
        } catch (ActivityNotFoundException ane) {
            Toast.makeText(mContext, "Sorry! No application found for crop.", Toast.LENGTH_LONG).show();
            ane.printStackTrace();
        }
    }

    /**
     * @param requestCode request code
     * @param resultCode  result code Activity.RESULT_OK/Activity.RESULT_CANCELED...
     * @param data        return data
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_IMAGE:
                    selectedImage = data.getData();
                    selectedFilePath = GetFilePath.getPath(mContext, selectedImage);
                    if (!TextUtils.isEmpty(selectedFilePath) && new File(selectedFilePath).exists()) {
                        Log.d(TAG, "REQUEST_PIC_FROM_GALLERY: " + selectedFilePath);
                        ImageLoader.loadImage(mContext, imgPicture, selectedImage, R.drawable.place_holder_square);
                    }
                    break;
                case REQUEST_CAPTURE_IMAGE:
                    selectedImage = ImagePickerDialog.getSelectedImageUri();
                    selectedFilePath = ImagePickerDialog.getSelectedFilePath();
                    if (!TextUtils.isEmpty(selectedFilePath) && new File(selectedFilePath).exists()) {
                        Log.d(TAG, "REQUEST_CAPTURE_IMAGE: " + selectedFilePath);
                        ImageLoader.loadImage(mContext, imgPicture, selectedImage, R.drawable.place_holder_square);
                    }
                    break;
                case REQUEST_CROP_IMAGE:
                    selectedImage = data.getData();
                    selectedFilePath = GetFilePath.getPath(mContext, selectedImage);
                    if (!TextUtils.isEmpty(selectedFilePath) && new File(selectedFilePath).exists()) {
                        Log.d(TAG, "REQUEST_CROP_PIC: " + selectedFilePath);
                        ImageLoader.loadImage(mContext, imgPicture, selectedImage, R.drawable.place_holder_square);
                    }
                    break;
            }
        }
    }
}
