package uroz.cristina.smartwallpapers.ml_wallpapers;

import static uroz.cristina.smartwallpapers.MainActivity.FILENAME_PHOTOS;
import static uroz.cristina.smartwallpapers.MainActivity.SMART_WALLPAPERS_TAG;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import uroz.cristina.smartwallpapers.MainActivity;
import uroz.cristina.smartwallpapers.PreferencesManager;
import uroz.cristina.smartwallpapers.R;
import uroz.cristina.smartwallpapers.SetWallpaperQuoteTask;

public class WallpaperAlarm extends BroadcastReceiver {

  /**
   * Do not delete this constructor, needed in order to be able to have an receiver
   */
  public WallpaperAlarm() {
  }


  @Override
  public void onReceive(final Context context, Intent intent) {
    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, SMART_WALLPAPERS_TAG);

    //Make sure that the service is executed atomic
    wl.acquire();

    List<String> photosUrls = readPhotosFile(context);


    //FIXME: The wallpaper won't be changed if the alarm goes off and the user doesn't have internet on through wifi.
    //As improvement we could cache 10 pictures on the phone to have them for these type of situations
    if (wifiOn(context)) {

      //display photo from favorites or display one based on user's preferences
      if (photosUrls.size() != 0){

        String imgUrl = photosUrls.get(0);

        //remove the photo that is going to be displayed on the background
        photosUrls.remove(0);
        writePhotosFile(context, photosUrls);

        changeWallpaper(context, imgUrl);

        createAndShowNotification(context, imgUrl);
      }else{
        //TODO display a photo based on user's preferences
      }
    }

    //schedule the alarm to go off again, since in setAlarm we use setExact(); The only way to have a reapeating exact time alarm in API 23 >
    // See for more info: https://google-developer-training.gitbooks.io/android-developer-fundamentals-course-concepts/content/en/Unit%203/82c_scheduling_alarms_md.html
    this.setAlarm(context);

    //Release the lock
    wl.release();

  }

  private void createAndShowNotification(Context context, String imgUrl) {
    NotificationCompat.Builder mBuilder;

    StringBuilder msgStr = new StringBuilder();

    Format formatter = new SimpleDateFormat("hh:mm:ss a");
    msgStr.append(formatter.format(new Date()));

    createNotificationChannel(context);

    mBuilder = new NotificationCompat.Builder(context, "1");

    String title = "Changed wall at:" + msgStr.toString();
    mBuilder.setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(title)
        .setContentText("Source:" + imgUrl)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

    // notificationId is a unique int for each notification that you must define
    notificationManager.notify(1, mBuilder.build());
  }

  private void changeWallpaper(final Context context, String imgUrl) {

    Picasso.get().load(imgUrl).into(new Target() {
      @Override
      public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
        ArrayList<String> quoteDisplayDetails = new ArrayList<>();
        SetWallpaperQuoteTask p = new SetWallpaperQuoteTask(context, bitmap, "",
            "", quoteDisplayDetails);
        new Thread(p).start();
      }

      @Override
      public void onBitmapFailed(Exception e, Drawable errorDrawable) {

      }

      @Override
      public void onPrepareLoad(Drawable placeHolderDrawable) {

      }
    });
  }

  public List<String> readPhotosFile(Context context){
    ArrayList<String> photosUrls = new ArrayList<>();

    try {
      BufferedReader reader = new BufferedReader(
          new FileReader(context.getFilesDir() + "/" + FILENAME_PHOTOS));

      String line;

      while ((line =reader.readLine()) != null) {
        photosUrls.add(line);
      }

      reader.close();
    } catch (FileNotFoundException e) {
      Log.e(SMART_WALLPAPERS_TAG, "onReceive: Failed to open \"liked_photos.txt\" file");
      e.printStackTrace();
    } catch (IOException e) {
      Log.e(SMART_WALLPAPERS_TAG, "onReceive: Couldn't read line from liked_photos.txt");
      e.printStackTrace();
    }

    return photosUrls;
  }

  private void writePhotosFile(Context context, List<String> photosUrls) {
    try {

      File photosFile = new File(context.getFilesDir() + "/" + FILENAME_PHOTOS);

      if (photosFile.delete()){
        Log.i(SMART_WALLPAPERS_TAG, "writePhotosFile: Deleted the liked_photos.txt");

        FileOutputStream newPhotosFile = context
            .openFileOutput(MainActivity.FILENAME_PHOTOS, Context.MODE_APPEND);

        for (String photosUrl : photosUrls) {
          String url = photosUrl + "\n";
          newPhotosFile.write(url.getBytes());
        }

        newPhotosFile.close();
      }else{
        Log.i(SMART_WALLPAPERS_TAG, "writePhotosFile: Failed to delete liked_photos.txt");
      }

    } catch (FileNotFoundException e) {
      Log.e(SMART_WALLPAPERS_TAG, "writePhotosFile: Failed to open \"liked_photos.txt\" file for deletion");
      e.printStackTrace();
    } catch (IOException e) {
      Log.e(SMART_WALLPAPERS_TAG, "writePhotosFile: Failed to write in \"liked_photos.txt\" ");
      e.printStackTrace();
    }

  }


  public void setAlarm(Context context) {
    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent intent = new Intent(context, WallpaperAlarm.class);
    PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);

    final int ALARM_TYPE = AlarmManager.RTC_WAKEUP;

    int intervalMillis = PreferencesManager.readInt(context, PreferencesManager.INTERVAL_MILLIS);

    long triggeringTimeMillis = System.currentTimeMillis() + intervalMillis;

   // SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss a");
    //String format1 = format.format(new Date(triggeringTimeMillis));

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
      am.setExactAndAllowWhileIdle(ALARM_TYPE, triggeringTimeMillis, pi);

    else
      am.setExact(ALARM_TYPE, triggeringTimeMillis, pi);

  }

  private void createNotificationChannel(Context context) {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = "Marius's channel";
      String description = "Show Notification";
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel channel = new NotificationChannel("1", name, importance);
      channel.setDescription(description);
      // Register the channel with the system; you can't change the importance
      // or other notification behaviors after this
      NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }
  }

  public void cancelAlarm(Context context) {
    Intent intent = new Intent(context, WallpaperAlarm.class);
    PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    alarmManager.cancel(sender);
  }


  public boolean alarmExists(Context context) {
    /**
     * The intent has to be the same as the one that was used to create the alarm (if there was created one)
     */
    Intent alarmIntent = new Intent(context, WallpaperAlarm.class);

    return PendingIntent.getBroadcast(context, 0,
        alarmIntent,
        PendingIntent.FLAG_NO_CREATE) != null;
  }

  public boolean wifiOn(Context context){
    ConnectivityManager cm =
        (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

    return activeNetwork != null &&
        activeNetwork.isConnected() &&
        (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI);
  }

}
