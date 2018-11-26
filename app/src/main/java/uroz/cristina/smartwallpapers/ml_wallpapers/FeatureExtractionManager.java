package uroz.cristina.smartwallpapers.ml_wallpapers;

import static uroz.cristina.smartwallpapers.ml_wallpapers.FeatureExtractionHelper.showWallpaperPreferenceFileContent;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import uroz.cristina.smartwallpapers.MainActivity;
import uroz.cristina.smartwallpapers.SharedPreferencesHelper;

/**
 */
public class FeatureExtractionManager implements EvaluateImageTaskListener {

  public static final String WALLPAPER_PREFERENCES_FILE = "WALLPAPER_PREFERENCES";
  public static final String LABEL_DETECTION = "LABEL_DETECTION";
  public static final String LANDMARK_DETECTION = "LANDMARK_DETECTION";
  public static final String SMART_WALLPAPERS_TAG = "SMARTWALLPAPERS";


  public static final float CONFIDENCE_THRESHOLD = 0.9f;
  public static final int MAXIMUM_LABELS_TO_EXTRACT = 10;

  private String labelResults = "";
  private String landmarkResults = "";

  private Context context;
  private Bitmap bitmapImage;

  public FeatureExtractionManager(Bitmap bitmapImage, Context context) {
    this.context = context;
    this.bitmapImage = bitmapImage;

    File preferencesFile = new File(context.getFilesDir() + "/" + WALLPAPER_PREFERENCES_FILE);

    try {
      //for the first image processed the preference file doesn't exist; read the documentation of this method for more info
      preferencesFile.createNewFile();
    } catch (IOException e) {
      Log.d(SMART_WALLPAPERS_TAG,
          "Failed to create the preferences file -> " + WALLPAPER_PREFERENCES_FILE);
      e.printStackTrace();
    }
  }

  public void processImage() {
    Feature feature = createFeature(LABEL_DETECTION);
    callCloudVision(feature);
  }

  private Feature createFeature(String feature_type) {
    Feature f = new Feature();
    f.setType(feature_type);
    f.setMaxResults(MAXIMUM_LABELS_TO_EXTRACT);

    return f;
  }

  private void callCloudVision(Feature feature) {
    ArrayList<Feature> features = new ArrayList<>();
    features.add(feature);

    final List<AnnotateImageRequest> annotateImageRequests = new ArrayList<>();

    AnnotateImageRequest imgReq = new AnnotateImageRequest();
    imgReq.setFeatures(features);
    imgReq.setImage(encodeToJPEG(bitmapImage));
    annotateImageRequests.add(imgReq);

    EvaluateImageTask evaluateImageTask = new EvaluateImageTask(this);
    evaluateImageTask.execute(annotateImageRequests);
  }

  /**
   * @param preferences -  the list of relevant labels for a picture ( the one for which the confidence percentage is > 90%);
   */
  private void writePreferencesToFile(HashMap<String, Integer> preferences) {
    String pathToFile = context.getFilesDir() + "/" + WALLPAPER_PREFERENCES_FILE;
    File preferenceFile = new File(pathToFile);

    boolean deletedSuccessfully = preferenceFile.delete();

    if (deletedSuccessfully) {
      try {
        FileOutputStream fileOutput = context
            .openFileOutput(WALLPAPER_PREFERENCES_FILE, Context.MODE_APPEND);

        Set<Entry<String, Integer>> entries = preferences.entrySet();
        String delimiterSymbol = ";";

        for (Entry<String, Integer> entry : entries) {
          StringBuilder line = new StringBuilder("");
          String label = entry.getKey();
          Integer score = entry.getValue();

          line.append(label);
          line.append(delimiterSymbol);
          line.append(score);
          line.append(delimiterSymbol);
          line.append("\n");

          String entryLine = line.toString();

          fileOutput.write(entryLine.getBytes());
        }

        fileOutput.close();
      } catch (FileNotFoundException e) {
        Log.d(SMART_WALLPAPERS_TAG,
            " wasn't able to create preferences file after it has just deleted it ");
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  @NonNull
  private static Image encodeToJPEG(Bitmap bitmapImage) {
    Image image = new Image();
    ByteArrayOutputStream byteOS = new ByteArrayOutputStream();

    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 90, byteOS);
    byte[] imageBytes = byteOS.toByteArray();

    image.encodeContent(imageBytes);
    return image;
  }

  @Override
  /**
   * Callback method to process the labels detected from an image. (will be called when the EvaluateImageTask finished extracting the labels)
   */
  public void onLabelsDetected(String results) {
    updateWallpaperPreferences(results, LABEL_DETECTION);
    labelResults = results;

    //make a request to analyze the photo in order to detect any landmarks
    Feature feature = createFeature(LANDMARK_DETECTION);
    callCloudVision(feature);

//    Log.i(SMART_WALLPAPERS_TAG, "Labels: " + results);
  }

  @Override
  /**
   *    Callback method to process the  detected landmark from an image. (will be called when the EvaluateImageTask finished looking if there is a landmark in the photo)
   */
  public void onLandmarkDetected(String results) {
    updateWallpaperPreferences(results, LANDMARK_DETECTION);
    landmarkResults = results;

    //showContent(LABEL_DETECTION, labelResults);

    //delete the photo because is no longer needed
    String processedPhotoPath = SharedPreferencesHelper
        .readString(context, MainActivity.NEXT_IMAGE_LOCATION);
    File imageFile = new File(processedPhotoPath);

    if (imageFile.exists()) {
      imageFile.delete();
      Log.i(SMART_WALLPAPERS_TAG, "onLandmarkDetected -> deleted photo " + processedPhotoPath  );
    }
//
//    Log.i(SMART_WALLPAPERS_TAG, "Landmarks: " + results);

  }

  private void updateWallpaperPreferences(String results, String feature_type) {

    if (results.isEmpty()) { //didn't detect anything in the picture
      return;
    }

    //add the labels to preference file
    String[] lines = results.split("\n");

    /**
     * element<k,v> -  element k has been extracted v time from pictures having a confidence score above CONFIDENCE_THRESHOLD
     */
    HashMap<String, Integer> preferencesHM = FeatureExtractionHelper
        .readWallpaperPreferences(context);
    Set<String> labelsSet = preferencesHM.keySet();
    String delimiterSymbol = ";";
    boolean preferencesModified = false;

    for (int i = 0; i < lines.length; i++) {
      String[] labelAndScore = lines[i].split(delimiterSymbol);
      String label = labelAndScore[0];
      float score = Float.valueOf(labelAndScore[1]);

      if ((score > CONFIDENCE_THRESHOLD) ||
          (feature_type.equals(LANDMARK_DETECTION))) {

        if (labelsSet.contains(label)) {
          Integer oldScore = preferencesHM.get(label);
          //update
          preferencesHM.put(label, oldScore + 1);
        } else {
          //new label, hasn't been met in any other processed images
          preferencesHM.put(label, new Integer(1));
        }

        preferencesModified = true;
      }
    }

    if (preferencesModified) {
      Log.i(SMART_WALLPAPERS_TAG, "wallpaper preferences were updated!");
      writePreferencesToFile(preferencesHM);
    }
  }

  /**
   * Method used for debuggin purposes
   */
  public void showContent(final String feature_type, String message) {
    Builder builder = new Builder(context);
    builder.setTitle(feature_type);
    builder.setMessage(message);

    builder.setPositiveButton("Ok", new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        dialogInterface.dismiss();

        if (feature_type.equals(LABEL_DETECTION)) {
          showContent(LANDMARK_DETECTION, landmarkResults);
        }

        if (feature_type.equals(LANDMARK_DETECTION)) {
          showWallpaperPreferenceFileContent(context);
        }
      }
    });

    AlertDialog dialog = builder.create();

    //make the dialog scrollable in case there is too much content
//    TextView textView = (TextView) dialog.findViewById(android.R.id.message);
//    textView.setScroller(new Scroller(context));
//    textView.setVerticalScrollBarEnabled(true);
//    textView.setMovementMethod(new ScrollingMovementMethod());

  }


}
