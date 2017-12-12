# Android ImagePicker

* Step 1: Initialize Request code
~~~
public static final int REQUEST_PICK_IMAGE = 101;
public static final int REQUEST_CAPTURE_IMAGE = 102;
public static final int REQUEST_CROP_IMAGE = 103;

private Uri selectedImage;
private String selectedFilePath;
~~~

* Step 2 : Call openImagePickerDialog(this); 
~~~
/**
 * Open image picker dialog
 */
 private void openImagePickerDialog(Context mContext) {
    ImagePickerDialog imagePickerDialog = new ImagePickerDialog();
    imagePickerDialog.setDialogListener(this);
    imagePickerDialog.show(getFragmentManager(), mContext.getClass().getSimpleName());
 }
~~~ 
 * Step 3 : implements ImagePickerDialog.OnImagePickerItemClick to Activity
 ~~~
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
 ~~~   
 
 ~~~
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
 ~~~
 
