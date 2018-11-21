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
    }

    public void run() {

        WallpaperManager manager = WallpaperManager.getInstance(context);

        bitmap= bitmap.copy(Bitmap.Config.ARGB_8888, true); //Make bitmap mutable

        //Process to add the quote at the photo
        Canvas canvas = new Canvas(bitmap);
        String size, color, loc;
        Paint paint = new Paint();
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
        }
        else if (color.equals("green")){
            paint.setColor(Color.GREEN); // Text Color
        }
        else{
            paint.setColor(Color.WHITE); // Text Color
        }
        StringBuffer finalQuote;

        if (size.equals("large")){
            paint.setTextSize(65); // Text Size
            finalQuote = prepareQuoteToDisplay(quote, 0);
        }
        else if (size.equals("medium")){
            paint.setTextSize(50); // Text Size
            finalQuote = prepareQuoteToDisplay(quote, 1);
        }
        else {
            paint.setTextSize(30); // Text Size
            finalQuote = prepareQuoteToDisplay(quote, 2);
        }
        paint.setShadowLayer(5,0,0,Color.GRAY);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text Overlapping Pattern
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawBitmap(bitmap, 0, 0, paint);


        int x = canvas.getWidth()/2, y = canvas.getHeight()/3;
        for (String line: String.valueOf(finalQuote).split("\n")) {
            canvas.drawText(line, x, y, paint);
            y += paint.descent() - paint.ascent();
        }

        paint.setTextSize(24); // Text Size
        paint.setShadowLayer(3,0,0,Color.GRAY);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text Overlapping Pattern
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(quote_author, canvas.getWidth()/2, canvas.getHeight()/3*2, paint);//Write the author

        //Set bitmap as a wallpaper
        try{
            manager.setBitmap(bitmap);
        } catch (IOException e) {
            Log.e("SetWallpaperQuoteTask","run: IOException");
        }

    }
    // temporary solution
    private StringBuffer prepareQuoteToDisplay(String quote, int text_size) {

        StringBuffer finalQuote = new StringBuffer();
        String[] splited = quote.split("\\s+");
        for (int i=0; i<splited.length; ++i){
            if(i != 0 && i % (text_size + 4) == 0){
                finalQuote.append("\n");
            }
            finalQuote.append(splited[i] + " ");

    }

        return finalQuote;
    }

}
