package library.imagepicker.imageloader;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.io.File;

public class ImageLoader {


    public static void loadImage(Context context, ImageView imageView, Uri imageUri, int placeHolderRes) {
        Glide.with(context)
                .using(new GlideContentProviderLoader(context))
                .load(imageUri)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(placeHolderRes)
                .into(imageView);

    }

    public static void loadImage(Context context, ImageView imageView, String imageUrl, int placeHolderRes) {
        Glide.with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(placeHolderRes)
                .into(imageView);

    }

    public static void loadImage(Context context, ImageView imageView, int resource, int placeHolderRes) {
        Glide.with(context)
                .load(resource)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(placeHolderRes)
                .into(imageView);
    }

    public static void loadRoundedImage(final Context context, final ImageView imageView, String imageUrl, int placeHolderRes) {
        Glide.with(context)
                .load(imageUrl)
                .asBitmap()
                .placeholder(placeHolderRes).centerCrop().into(new BitmapImageViewTarget(imageView) {
            @Override
            protected void setResource(Bitmap resource) {
                final RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                imageView.setImageDrawable(circularBitmapDrawable);
            }
        });
    }

    public static void loadImage(Context context, ImageView imageView, File file, int placeHolderRes) {
        Glide.with(context)
                .load(file)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(placeHolderRes)
                .into(imageView);
    }
}
