package library.imagepicker;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


public class ImagePickerDialog extends DialogFragment implements View.OnClickListener {

    /*
     * Max time interval to prevent double click
     */
    public static final long MAX_CLICK_INTERVAL = 1000;

    /*
     * Contains last clicked time
     */
    protected long lastClickedTime = 0;

    public static final int REQUEST_PICK_IMAGE = 1000;
    public static final int REQUEST_CAPTURE_IMAGE = 2000;

    private static final int REQUEST_PERMISSION_GALLERY = 1001;
    private static final int REQUEST_PERMISSION_CAMERA = 2001;

    private static final int REQUEST_PERMISSION_CAMERA_GALLERY = 100;

    private OnImagePickerItemClick onImagePickerItemClick;

    public void setDialogListener(OnImagePickerItemClick mOnImagePickerItemClick) {
        onImagePickerItemClick = mOnImagePickerItemClick;
    }

    @Override
    public void onStart() {
        super.onStart();

        Window window = getDialog().getWindow();
        window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM);

        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.dimAmount = 0.5f; // dim only a little bit

        window.setAttributes(params);
        window.setBackgroundDrawableResource(android.R.color.transparent);
    }

    public interface OnImagePickerItemClick {
        void onCameraClick();
        void onGalleryClick();
        void onCancelClick();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_NoActionBar);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(library.imagepicker.R.layout.dialog_image_picker, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeComponent(view);
    }

    /*
     * Initialize components
     *
     * @param view
     */
    private void initializeComponent(View view) {

        RelativeLayout rlParent = view.findViewById(library.imagepicker.R.id.dialog_image_picker_rl_parent);
        TextView tvGallery = view.findViewById(library.imagepicker.R.id.dialog_image_picker_tv_gallery);
        TextView tvCamera = view.findViewById(library.imagepicker.R.id.dialog_image_picker_tv_camera);
        TextView tvCancel = view.findViewById(library.imagepicker.R.id.dialog_image_picker_tv_cancel);
        LinearLayout llAnimateLayout = view.findViewById(library.imagepicker.R.id.dialog_image_picker_ll_dialog);

        Animation animation = AnimationUtils.loadAnimation(getActivity(), library.imagepicker.R.anim.bottom_up);
        llAnimateLayout.startAnimation(animation);
        llAnimateLayout.setVisibility(View.VISIBLE);

        rlParent.setOnClickListener(this);
        tvGallery.setOnClickListener(this);
        tvCamera.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - lastClickedTime < MAX_CLICK_INTERVAL) {
            return;
        }
        lastClickedTime = SystemClock.elapsedRealtime();

        String writeStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        String cameraAccess = Manifest.permission.CAMERA;

        int view = v.getId();
        if (view == library.imagepicker.R.id.dialog_image_picker_tv_gallery) {
            if (checkForPermission(getActivity(), writeStorage)) {
                openGallery();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{writeStorage}, REQUEST_PERMISSION_GALLERY);
                }
            }

        } else if (view == library.imagepicker.R.id.dialog_image_picker_tv_camera) {
            if (checkForPermission(getActivity(), cameraAccess) && checkForPermission(getActivity(), writeStorage)) {
                openCamera();
            } else {
                if (!checkForPermission(getActivity(), cameraAccess)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{cameraAccess}, REQUEST_PERMISSION_CAMERA);
                    }
                } else if (!checkForPermission(getActivity(), writeStorage)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{writeStorage}, REQUEST_PERMISSION_CAMERA_GALLERY);
                    }
                }
            }
        } else if (view == library.imagepicker.R.id.dialog_image_picker_tv_cancel) {
            cancel();
        } else if (view == library.imagepicker.R.id.dialog_image_picker_rl_parent) {
            cancel();
        }
    }

    /*
     * Called to check permission(In Android M and above versions only)
     *
     * @param permission, which we need to pass
     * @return true, if permission is granted else false
     */
    public static boolean checkForPermission(final Context context, final String permission) {
        int result = ContextCompat.checkSelfPermission(context, permission);
        //If permission is granted then it returns 0 as result
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    showMessage(getActivity(),getString(library.imagepicker.R.string.permission_required));
                }
                break;
            case REQUEST_PERMISSION_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String writeStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                    if (!checkForPermission(getActivity(), writeStorage)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{writeStorage}, REQUEST_PERMISSION_CAMERA_GALLERY);
                        }
                    } else {
                        openCamera();
                    }
                } else {
                    showMessage(getActivity(),getString(library.imagepicker.R.string.permission_required));
                }
                break;
            case REQUEST_PERMISSION_CAMERA_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    showMessage(getActivity(),getString(library.imagepicker.R.string.permission_required));
                }
                break;
        }
    }

    private void openCamera() {
        onImagePickerItemClick.onCameraClick();
        getDialog().dismiss();
    }

    private void openGallery() {
        onImagePickerItemClick.onGalleryClick();
        getDialog().dismiss();
    }

    private void cancel() {
        onImagePickerItemClick.onCancelClick();
        getDialog().dismiss();
    }

    /**
     * Pick image from an Activity
     *
     * @param activity Activity to receive result
     */
    public static void pickImageFromGallery(Activity activity) {
        pickImageFromGallery(activity, REQUEST_PICK_IMAGE);
    }

    /**
     * Pick image from a Fragment
     *
     * @param context  Context
     * @param fragment Fragment to receive result
     */
    public static void pickImageFromGallery(Context context, Fragment fragment) {
        pickImageFromGallery(context, fragment, REQUEST_PICK_IMAGE);
    }

    /**
     * Pick image from a support library Fragment
     *
     * @param context  Context
     * @param fragment Fragment to receive result
     */
    public static void pickImageFromGallery(Context context, android.support.v4.app.Fragment fragment) {
        pickImageFromGallery(context, fragment, REQUEST_PICK_IMAGE);
    }

    /**
     * Pick image from an Activity with a custom request code
     *
     * @param activity    Activity to receive result
     * @param requestCode requestCode for result
     */
    public static void pickImageFromGallery(Activity activity, int requestCode) {
        try {
            activity.startActivityForResult(getGalleryImagePicker(), requestCode);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            showMessage(activity,e.getMessage());
        }
    }

    /**
     * Pick image from a Fragment with a custom request code
     *
     * @param context     Context
     * @param fragment    Fragment to receive result
     * @param requestCode requestCode for result
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void pickImageFromGallery(Context context, Fragment fragment, int requestCode) {
        try {
            fragment.startActivityForResult(getGalleryImagePicker(), requestCode);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            showMessage(context,e.getMessage());
        }
    }

    /**
     * Pick image from a support library Fragment with a custom request code
     *
     * @param context     Context
     * @param fragment    Fragment to receive result
     * @param requestCode requestCode for result
     */
    public static void pickImageFromGallery(Context context, android.support.v4.app.Fragment fragment, int requestCode) {
        try {
            fragment.startActivityForResult(getGalleryImagePicker(), requestCode);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            showMessage(context,e.getMessage());
        }
    }

    private static Intent getGalleryImagePicker() {
        return new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).setType("image/*");
    }

    /**
     * Capture image from an Activity
     *
     * @param activity Activity to receive result
     */
    public static void captureImage(Activity activity,String directoryName) {
        captureImage(activity,directoryName, REQUEST_CAPTURE_IMAGE);
    }

    /**
     * Capture image from a Fragment
     *
     * @param context  Context
     * @param fragment Fragment to receive result
     */
    public static void captureImage(Context context, Fragment fragment,String directoryName) {
        captureImage(context, fragment,directoryName, REQUEST_CAPTURE_IMAGE);
    }

    /**
     * Capture image from a support library Fragment
     *
     * @param context  Context
     * @param fragment Fragment to receive result
     */
    public static void captureImage(Context context, android.support.v4.app.Fragment fragment,String directoryName) {
        captureImage(context, fragment,directoryName, REQUEST_CAPTURE_IMAGE);
    }

    /**
     * Capture image from an Activity with a custom request code
     *
     * @param activity    Activity to receive result
     * @param directoryName   Name of the directory in which image will store
     * @param requestCode requestCode for result
     */
    public static void captureImage(Activity activity,String directoryName, int requestCode) {
        try {
            activity.startActivityForResult(getCameraImagePicker(activity,directoryName), requestCode);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            showMessage(activity,e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            showMessage(activity,e.getMessage());
        }
    }

    /**
     * Capture image from a Fragment with a custom request code
     *
     * @param context     Context
     * @param fragment    Fragment to receive result
     * @param requestCode requestCode for result
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void captureImage(Context context, Fragment fragment, String directoryName, int requestCode) {
        try {
            fragment.startActivityForResult(getCameraImagePicker(context, directoryName), requestCode);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            showMessage(context, e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            showMessage(context, e.getMessage());
        }
    }

    /**
     * Capture image from a support library Fragment with a custom request code
     *
     * @param context     Context
     * @param fragment    Fragment to receive result
     * @param requestCode requestCode for result
     */
    public static void captureImage(Context context, android.support.v4.app.Fragment fragment, String directoryName, int requestCode) {
        try {
            fragment.startActivityForResult(getCameraImagePicker(context, directoryName), requestCode);
        }catch (ActivityNotFoundException e) {
            e.printStackTrace();
            showMessage(context, e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            showMessage(context, e.getMessage());
        }
    }

    private static Uri selectedImageUri;
    private static String selectedFilePath;

    private static Intent getCameraImagePicker(Context context, String directoryName) throws IOException {

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + directoryName);
        if (!file.exists()) {
            file.mkdir();
        }

        File cameraFile = new File(file.getAbsolutePath() + File.separator + "IMG_" + System.currentTimeMillis() + ".jpg");
        cameraFile.createNewFile();
        selectedFilePath = cameraFile.getAbsolutePath();

        Intent intentPicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            selectedImageUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", cameraFile);
        } else {
            selectedImageUri = Uri.fromFile(cameraFile);
        }
        intentPicture.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri);
        return intentPicture;
    }


    public static Uri getSelectedImageUri() {
        return selectedImageUri;
    }


    public static String getSelectedFilePath() {
        return selectedFilePath;
    }

    private static void showMessage(Context context, String message) {
        Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
