package uroz.cristina.smartwallpapers;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import com.squareup.picasso.Target;

public class setWallpaper implements Runnable {


    private Context context;
    private  Bitmap bitmap;


    setWallpaper(Context context, Bitmap bitmap) {
        this.context = context;
        this.bitmap = bitmap;
    }

    public void run() {

        //Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), image_src);
        WallpaperManager manager = WallpaperManager.getInstance(context);

        try{
            manager.setBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
