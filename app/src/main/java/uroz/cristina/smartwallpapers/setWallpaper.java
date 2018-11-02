package uroz.cristina.smartwallpapers;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;

public class setWallpaper implements Runnable {

    int image_src;
    private Context context;

    setWallpaper(Context context,int image_src) {
        this.image_src = image_src;
        this.context = context;
    }

    public void run() {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), image_src);
        WallpaperManager manager = WallpaperManager.getInstance(context);

        try{
            manager.setBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
