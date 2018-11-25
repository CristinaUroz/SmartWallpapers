package uroz.cristina.smartwallpapers.ml_wallpapers;

import static uroz.cristina.smartwallpapers.ml_wallpapers.FeatureExtractionManager.SMART_WALLPAPERS_TAG;
import static uroz.cristina.smartwallpapers.ml_wallpapers.FeatureExtractionManager.WALLPAPER_PREFERENCES_FILE;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

/**
 */

public class FeatureExtractionHelper {

  public static void showWallpaperPreferenceFileContent(Context context) {
    HashMap<String, Integer> preferencesHM = readWallpaperPreferences(context);
    LinkedList<Entry<String, Integer>> sortedList = sortHM(preferencesHM);



    Builder builder = new Builder(context);
    builder.setTitle("Your Preferences");

    if (sortedList.isEmpty()){
      builder.setMessage("No preferences atm");
      builder.create().show();
      return;
    }

    StringBuilder messageSB = new StringBuilder("");

//    for (Entry<String, Integer> p : preferencesHM.entrySet()) {
//      messageSB.append(p.getKey() + " " + p.getValue() + "\n");
//    }

    for (Entry<String, Integer> entry : sortedList) {
      messageSB.append(entry.getKey()).append(" ").append(entry.getValue()).append("\n");
    }

    builder.setMessage(messageSB.toString());

    builder.setPositiveButton("Ok", new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        dialogInterface.dismiss();
      }
    });

    builder.create().show();


  }

  /**
   * Reads the mappings of labels their score from the preference file
   */
  public static HashMap<String, Integer> readWallpaperPreferences(Context context) {
    HashMap<String, Integer> preferencesHM = new HashMap<>();
    String pathToFile = context.getFilesDir() + "/" + WALLPAPER_PREFERENCES_FILE;
    File preferencesFile = new File(pathToFile);

    //preference file is created first time when a photo is processed;
    //this is a safeguard for the case when no photo was processed and the user wants to see the preference file
    if (!preferencesFile.exists()) {
      return preferencesHM;
    }

    try {
      BufferedReader reader = new BufferedReader(
          new FileReader(pathToFile));

      String line;
      String delimiterSymbol = ";";

      while ((line = reader.readLine()) != null) {
        String[] values = line.split(delimiterSymbol);
        String label = values[0];
        Integer count = Integer.valueOf(values[1]);

        preferencesHM.put(label, count);
      }

      reader.close();
    } catch (FileNotFoundException e) {
      Log.d(SMART_WALLPAPERS_TAG,
          " failed to open preferences file : " + WALLPAPER_PREFERENCES_FILE);
      e.printStackTrace();
    } catch (IOException e) {
      Log.d(SMART_WALLPAPERS_TAG,
          " failed to read from preferences file : " + WALLPAPER_PREFERENCES_FILE);
      e.printStackTrace();
    }

    return preferencesHM;
  }

  /**
   * Sort the Hash Map in descending order of the values
   * @param inputHM
   * @return
   */
  public static LinkedList<Entry<String, Integer>> sortHM(HashMap<String, Integer> inputHM) {
    LinkedList<Entry<String, Integer>> list = new LinkedList<>(inputHM.entrySet());

    Collections.sort(list, new Comparator<Entry<String, Integer>>() {
      @Override
      public int compare(Entry<String, Integer> t1, Entry<String, Integer> t2) {
        //descending
        return t2.getValue() - t1.getValue();
      }
    });


    return list;
  }
}
