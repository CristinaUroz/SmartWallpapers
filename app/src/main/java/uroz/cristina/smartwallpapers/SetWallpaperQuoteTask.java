package uroz.cristina.smartwallpapers;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.IOException;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;

public class SetWallpaperQuoteTask implements Runnable {

    private Context context;
    private Bitmap bitmap;
    private String quote;
    private String quote_autor;

    public SetWallpaperQuoteTask(Context context, Bitmap bitmap, String quote, String quote_autor) {
        this.context = context;
        this.bitmap = bitmap;
        this.quote = quote;
        this.quote_autor = quote_autor;
    }

    public void run() {

        WallpaperManager manager = WallpaperManager.getInstance(context);

        bitmap= bitmap.copy(Bitmap.Config.ARGB_8888, true); //Make bitmap mutable

        //Process to add the quote at the photo
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();

        paint.setColor(Color.WHITE); // Text Color
        paint.setShadowLayer(5,0,0,Color.GRAY);
        paint.setTextSize(30); // Text Size
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text Overlapping Pattern
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        canvas.drawText(quote, canvas.getWidth()/2, canvas.getHeight()/3, paint); //Write the quote

        paint.setTextSize(24); // Text Size
        paint.setShadowLayer(3,0,0,Color.GRAY);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text Overlapping Pattern
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(quote_autor, canvas.getWidth()/2, canvas.getHeight()/3*2, paint);//Write the autor

        //Set bitmap as a wallpaper
        try{
            manager.setBitmap(bitmap);
        } catch (IOException e) {
            Log.e("SetWallpaperQuoteTask","run: IOException");
        }

    }

}
