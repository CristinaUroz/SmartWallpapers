package uroz.cristina.smartwallpapers;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;

public class SetWallpaperQuoteTask implements Runnable {

    private Context context;
    private Bitmap bitmap;
    private String quote;
    private String quote_author;
    private ArrayList<String> quote_display_info;

    public SetWallpaperQuoteTask(Context context, Bitmap bitmap, String quote, String quote_author, ArrayList<String> quote_display_info) {
        this.context = context;
        this.bitmap = bitmap;
        this.quote = quote;
        this.quote_author = quote_author;
        this.quote_display_info = quote_display_info;
        Log.i("konstruktor set wallpap", String.valueOf(this.quote_display_info));
    }

    public void run() {

        WallpaperManager manager = WallpaperManager.getInstance(context);

        bitmap= bitmap.copy(Bitmap.Config.ARGB_8888, true); //Make bitmap mutable

        //Process to add the quote at the photo
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        float width = canvas.getWidth();
        float height = canvas.getHeight();
        String size, color, loc;
        StringBuffer finalQuote;
        float textSize = 0;

        if (!quote_display_info.isEmpty()) {
            size = quote_display_info.get(0);
            color = quote_display_info.get(1);
            loc = quote_display_info.get(2);
        }
        else {
            size = "medium";
            color = "white";
            loc = "middle";
        }

        if (color.equals( "black")){
            paint.setColor(Color.BLACK); // Text Color
            paint.setShadowLayer(10,0,0,Color.WHITE);
        }
        else if (color.equals("green")){
            paint.setColor(Color.GREEN); // Text Color
            paint.setShadowLayer(10,0,0,Color.BLACK);
        }
        else{
            paint.setColor(Color.WHITE); // Text Color
            paint.setShadowLayer(10,0,0,Color.BLACK);
        }


        if (size.equals("large")){
            textSize = height * 0.05f;

        }
        else if (size.equals("medium")){
            textSize = height * 0.037f;

        }
        else {
            textSize = height * 0.025f;
        }


        paint.setTextSize(textSize);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text Overlapping Pattern
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawBitmap(bitmap, 0, 0, paint);

        finalQuote = prepareQuoteToDisplay(quote, paint, width);
        String[] finalQuoteArray = String.valueOf(finalQuote).split("\n");
        int linesToMove = finalQuoteArray.length/2;


        float x = (float) (width * 0.5);
        float y = (float) (height * 0.5) - linesToMove * textSize;
        if (loc.equals( "top")){
            y = (float) (height * 0.2);
        }
        else if (loc.equals("bottom")){
            y = (float) (height * 0.65) - (linesToMove + 1) * textSize;
        }

        for (String line: finalQuoteArray) {
            canvas.drawText(line, x, y, paint);
            y += paint.descent() - paint.ascent();
        }

        // writing author
        paint.setTextSize(textSize * 0.5f); // Text Size
        paint.setShadowLayer(3,0,0,Color.GRAY);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text Overlapping Pattern
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(quote_author, width/4, (float) y, paint);//Write the author

        //Set bitmap as a wallpaper
        try{
            manager.setBitmap(bitmap);
        } catch (IOException e) {
            //Note When logging information please use:
            //  -> the name of the app as tag, MainActivity.SMART_WALLPAPERS_TAG
            //  -> the method where the error/exception occurred
            // -> the error message (e.g. e.getMessage() ) STor a meaningful text wrote by you
            // ~Marius~
            Log.e(MainActivity.SMART_WALLPAPERS_TAG,"Failed to change wallpaper, SetWallpaperQuoteTask run(): " + e.getMessage());

        }

    }

    private StringBuffer prepareQuoteToDisplay(String quote, Paint paint, float width) {

        StringBuffer finalQuote = new StringBuffer();
        StringBuffer tmpQuote = new StringBuffer();
        String[] splited = quote.split("\\s+");

        for (int i=0; i<splited.length; ++i){
                if (paint.measureText(tmpQuote.toString() + splited[i] + " ") >= width * 0.7f) {
                    tmpQuote.append("\n");
                    finalQuote.append(tmpQuote);
                    tmpQuote.setLength(0);
                    --i;
                }
                else{
                    if(tmpQuote.length() != 0)
                        tmpQuote.append(" ");
                    tmpQuote.append(splited[i]);
                }

    }
        if (tmpQuote.length() != 0){
            finalQuote.append(tmpQuote);
        }

        return finalQuote;
    }

}
