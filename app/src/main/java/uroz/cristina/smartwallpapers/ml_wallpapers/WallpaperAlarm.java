package uroz.cristina.smartwallpapers.ml_wallpapers;

import static uroz.cristina.smartwallpapers.MainActivity.FILENAME_PHOTOS;
import static uroz.cristina.smartwallpapers.MainActivity.SMART_WALLPAPERS_TAG;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.PowerManager;
import android.provider.ContactsContract.Contacts.Photo;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Action;
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
import uroz.cristina.smartwallpapers.SharedPreferencesHelper;
import uroz.cristina.smartwallpapers.R;
import uroz.cristina.smartwallpapers.SetWallpaperQuoteTask;

public class WallpaperAlarm extends BroadcastReceiver implements PhotoSearchListener{
  public Context context;

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
    this.context = context;


    //TODO: As improvement we could cache 10 pictures on the phone to have them for these type of situations
    if (wifiOn(context)) {

      List<String> photosUrls = readPhotosFile(context);

      //display photo from favorites or display one based on user's preferences
      if (photosUrls.size() != 0){

        String imgUrl = photosUrls.get(0);

        //remove the photo that is going to be displayed on the background
        photosUrls.remove(0);
        
        extractLabelPhoto(context);
        
        writePhotosFile(context, photosUrls);

        changeWallpaper(context, imgUrl);

        createAndShowNotification(context, imgUrl);
      }else{
        //display photo based on user's preferences
        MainActivity.changeAutomaticallyWallpaperQuote(context, this);
      }
    }

    //schedule the alarm to go off again, since in setDownloadAlarm we use setExact(); The only way to have a reapeating exact time alarm in API 23 >
    // See for more info: https://google-developer-training.gitbooks.io/android-developer-fundamentals-course-concepts/content/en/Unit%203/82c_scheduling_alarms_md.html
    this.setWallpaperChangingAlarm(context);
    this.setDownloadAlarm(context);

    //Release the lock
    wl.release();

  }

  /**
   * Analyze the photo using Google Vision API in order to get the photo labels
   */
  private void extractLabelPhoto(Context context) {
    String imageAbsolutePath = "";
    imageAbsolutePath = SharedPreferencesHelper.readString(context, MainActivity.NEXT_IMAGE_LOCATION);
    File imageFile = null;
    if ((imageAbsolutePath!= null) && (!imageAbsolutePath.isEmpty())) {
      imageFile = new File(imageAbsolutePath);
    }else{
      return;
    }

    //if the file wasn't downloaded then there is no sense to do any processing
    if (imageFile.exists()) {
      Options bitmapOptions = new Options();
      Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bitmapOptions);

      FeatureExtractionManager fem = new FeatureExtractionManager(bitmap,
          context);

      Log.i(SMART_WALLPAPERS_TAG, "started to process image ->" + imageFile.getAbsolutePath());

      fem.processImage();
    }
  }

  private void createAndShowNotification(Context context, String imgUrl) {
    NotificationCompat.Builder mBuilder;

    StringBuilder msgStr = new StringBuilder();

    Format formatter = new SimpleDateFormat("hh:mm:ss a");
    msgStr.append(formatter.format(new Date()));

    createNotificationChannel(context);

    mBuilder = new NotificationCompat.Builder(context, "1");

    //launch Main Activity when tapping the notification
    Intent intent = new Intent(context, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);


    String title = "Changed wall at:" + msgStr.toString();
    mBuilder.setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(title)
        .setContentText("Source:" + imgUrl)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(false)
        .setContentIntent(pendingIntent);

    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);


    // notificationId is a unique int for each notification that you must define
    notificationManager.notify(1, mBuilder.build());
  }

  private void changeWallpaper(final Context context, String imgUrl) {

    //TODO this might failsometimes, try to change it using the downloaded photo
    Picasso.get().load(imgUrl).into(new Target() {
      @Override
      public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
        ArrayList<String> quoteDisplayDetails = new ArrayList<>();
        SetWallpaperQuoteTask p = new SetWallpaperQuoteTask(context, bitmap, "",
            "", quoteDisplayDetails);

        Log.i(SMART_WALLPAPERS_TAG, "Thread to change wallpaper was started ");
        new Thread(p).start();
      }

      @Override
      public void onBitmapFailed(Exception e, Drawable errorDrawable) {

      }

      @Override
      public void onPrepareLoad(Drawable placeHolderDrawable) {

      }
    });

//    String imageAbsolutePath = "";
//    imageAbsolutePath = SharedPreferencesHelper.readString(context, MainActivity.NEXT_IMAGE_LOCATION);
//    File imageFile = null;
//    if (!imageAbsolutePath.isEmpty()) {
//      imageFile = new File(imageAbsolutePath);
//    }else {
//      return;
//    }
//
//    Bitmap nextImage = BitmapFactory.decodeFile(imageAbsolutePath);
//
//    if (nextImage != null) {
//
//      //FIXME to be linked with Kasia's part, atm it won't put any quote -- will just change wallpaper
//      SetWallpaperQuoteTask task = new SetWallpaperQuoteTask(context, nextImage,
//          "", "", new ArrayList<String>());
//      new Thread(task).start();
//    }

  }

  public static List<String> readPhotosFile(Context context){
    ArrayList<String> photosUrls = new ArrayList<>();
    String pathToLikedPhotosFile = context.getFilesDir() + "/" + FILENAME_PHOTOS;

    File likedPhotosFile = new File(pathToLikedPhotosFile);

    //safeguard
    if (!likedPhotosFile.exists()){
      try {
        likedPhotosFile.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    try {
      BufferedReader reader = new BufferedReader(
          new FileReader(pathToLikedPhotosFile));

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
//        Log.i(SMART_WALLPAPERS_TAG, "writePhotosFile: Deleted the liked_photos.txt");

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


  public void setWallpaperChangingAlarm(Context context) {
    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent intent = new Intent(context, WallpaperAlarm.class);
    PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);

    final int ALARM_TYPE = AlarmManager.RTC_WAKEUP;

    int intervalMillis = SharedPreferencesHelper.readInt(context, SharedPreferencesHelper.INTERVAL_MILLIS);

    long triggeringTimeMillis = System.currentTimeMillis() + intervalMillis;

   // SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss a");
    //String format1 = format.format(new Date(triggeringTimeMillis));

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
      am.setExactAndAllowWhileIdle(ALARM_TYPE, triggeringTimeMillis, pi);

    else
      am.setExact(ALARM_TYPE, triggeringTimeMillis, pi);

  }

  public void setDownloadAlarm(Context context) {
    int downloadingTimeOffset = SharedPreferencesHelper.readInt(context, SharedPreferencesHelper.DOWNLOAD_INTERVAL_MILLIS);

    if (downloadingTimeOffset == 0) {
      Log.i(SMART_WALLPAPERS_TAG, "The timer for wallpaper change is too short");
      return;
    }

    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent intent = new Intent(context, DownloadPhotoAlarm.class);
    PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);

    final int ALARM_TYPE = AlarmManager.RTC_WAKEUP;


    long triggeringTimeMillis = System.currentTimeMillis() + downloadingTimeOffset;

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

  public static boolean wifiOn(Context context){
    ConnectivityManager cm =
        (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

    return activeNetwork != null &&
        activeNetwork.isConnected() &&
        (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI);
  }

  @Override
  public void onPhotoSearchComplete(String urlNextImage) {
    if (WallpaperAlarm.wifiOn(context)) {
      Picasso.get().load(urlNextImage).into(new Target() {
        @Override
        public void onBitmapLoaded(final Bitmap bitmap, LoadedFrom from) {

          //FIXME add the quote part
          //IN DEBUG MODE IT CHANGES THE WALLPAPER;
          SetWallpaperQuoteTask task = new SetWallpaperQuoteTask(context,
              bitmap, "", "", new ArrayList<String>());

          Log.i(SMART_WALLPAPERS_TAG, "Direct changing of wallpaper based on preferences from alarm, thread started, onBitmapLoaded() ");
          // new Thread(task).start();

          //FIXME fails to change wallpaper
          new Thread(task).start();

        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
      });
    }
  }
}
