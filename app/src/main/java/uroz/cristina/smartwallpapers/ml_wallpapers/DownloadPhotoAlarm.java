package uroz.cristina.smartwallpapers.ml_wallpapers;

import static uroz.cristina.smartwallpapers.MainActivity.SMART_WALLPAPERS_TAG;
import static uroz.cristina.smartwallpapers.ml_wallpapers.WallpaperAlarm.readPhotosFile;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import java.util.List;
import uroz.cristina.smartwallpapers.SharedPreferencesHelper;

/**
 * Created by Marius: marius.lucian.olariu@gmail.com
 */
public class DownloadPhotoAlarm extends BroadcastReceiver{

  /**
   * Do not delete this; service requirment
   */
  public DownloadPhotoAlarm() {
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, SMART_WALLPAPERS_TAG);

    //Make sure that the service is executed atomically
    wl.acquire();

    if (WallpaperAlarm.wifiOn(context)){
      List<String> photosUrls = readPhotosFile(context);

      if (photosUrls.size() != 0){
        String imgToDownloadUrl = photosUrls.get (0);

        DownloadPhotoTask downloadPhotoTask = new DownloadPhotoTask();
        downloadPhotoTask.execute(imgToDownloadUrl, context);

      }
    }

    wl.release();
  }


  public boolean alarmExists(Context context){
    Intent alarmIntent = new Intent(context, DownloadPhotoAlarm.class);

    return PendingIntent.getBroadcast(context, 0,
        alarmIntent,
        PendingIntent.FLAG_NO_CREATE) != null;
  }


  public void cancelAlarm(Context context){
    Intent intent = new Intent(context, DownloadPhotoAlarm.class);
    PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    alarmManager.cancel(sender);
  }
}

