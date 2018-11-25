package uroz.cristina.smartwallpapers;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Set;

public class SharedPreferencesHelper {

  //constants used for writing/reading to preferences
  public static final int STRING = 0;
  public static final int INT = 1;
  public static final int BOOLEAN = 2;
  public static final int ADD_PHOTO_FAVORITES = 3;


  private static final String PREFERENCE_FILE_NAME = "WALLPAPER";
  public static final String INTERVAL_MILLIS = "INTERVAL_MILLIS";
  public static final String DOWNLOAD_INTERVAL_MILLIS = "DOWNLOAD_INTERVAL_MILLIS";


  /**
   * Write a string in the preference file
   */
  public static void writeToPreferenceFile(Context context, int data_type, final String key,
      Object property) {
    SharedPreferences.Editor editor = context
        .getSharedPreferences(PREFERENCE_FILE_NAME, context.MODE_PRIVATE).edit();

    switch (data_type) {
      case STRING:
        editor.putString(key, (String) property);
        break;

      case INT:
        editor.putInt(key, (Integer) property);
        break;

      case BOOLEAN:
        editor.putBoolean(key, (Boolean) property);
        break;

    }

    editor.commit();
  }

  public static String readString(Context context, final String key) {
    return context.getSharedPreferences(PREFERENCE_FILE_NAME, context.MODE_PRIVATE)
        .getString(key, null);
  }

  public static Integer readInt(Context context, String key) {
    SharedPreferences sp = context
        .getSharedPreferences(PREFERENCE_FILE_NAME, context.MODE_PRIVATE);
    int value = sp
        .getInt(key, Integer.MIN_VALUE);

    return new Integer(value);
  }

  /**
   *
   * @return - the set of urls of the photos liked by the user
   */
//  public static Set<String> getSetFavoritePhotos(Context context, String key){
//    return context.getSharedPreferences(PREFERENCE_FILE_NAME, context.MODE_PRIVATE).getStringSet()
//  }

}
