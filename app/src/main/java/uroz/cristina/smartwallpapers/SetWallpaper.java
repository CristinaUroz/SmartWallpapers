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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;

public class SetWallpaper implements Runnable {


    private Context context;
    private  Bitmap bitmap;
    private String quote;
    private String quote_autor;


    SetWallpaper(Context context, Bitmap bitmap, String quote, String quote_autor) {

        this.context = context;
        this.bitmap = bitmap;
        this.quote = quote;
        this.quote_autor = quote_autor;
    }

    public void run() {

        //Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), image_src);
        WallpaperManager manager = WallpaperManager.getInstance(context);

        bitmap= bitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE); // Text Color
        paint.setShadowLayer(5,0,0,Color.GRAY);
        paint.setTextSize(30); // Text Size
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text Overlapping Pattern
        paint.setTextAlign(Paint.Align.CENTER);
        // some more settings...
        canvas.drawBitmap(bitmap, 0, 0, paint);
        canvas.drawText(quote, canvas.getWidth()/2, canvas.getHeight()/3, paint);

        paint.setTextSize(24); // Text Size
        paint.setShadowLayer(3,0,0,Color.GRAY);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text Overlapping Pattern
        paint.setTextAlign(Paint.Align.LEFT);

        canvas.drawText(quote_autor, canvas.getWidth()/2, canvas.getHeight()/3*2, paint);

        try{
            manager.setBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
