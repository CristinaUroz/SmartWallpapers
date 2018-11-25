package uroz.cristina.smartwallpapers.ml_wallpapers;


import static uroz.cristina.smartwallpapers.ml_wallpapers.FeatureExtractionManager.LABEL_DETECTION;
import static uroz.cristina.smartwallpapers.ml_wallpapers.FeatureExtractionManager.LANDMARK_DETECTION;

import android.os.AsyncTask;
import android.util.Log;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import java.io.IOException;
import java.util.List;

/**
 * Task to extract labels from an image.
 */
public class EvaluateImageTask extends AsyncTask<Object, Void, String> {

  private static final String CLOUD_VISION_API_KEY = "AIzaSyDzDdSKPfHlIwkqNFIBYvlcgyvcZGdM97E";
  /**
   * Flag that marks that image processing has failed
   */
  private final String FAILED = "failed";

  private EvaluateImageTaskListener listener;
  private String feature_type = "";

  public EvaluateImageTask(EvaluateImageTaskListener listener) {
    this.listener = listener;
  }

  @Override
  protected String doInBackground(Object[] objects) {
    try {
      List<AnnotateImageRequest> imageRequests = (List<AnnotateImageRequest>) objects[0];

      //Extract the feature for which the current request is processing the image (possible values: LABEL_DETECTION, LANDMARK_DETECTION);
      feature_type = imageRequests.get(0).getFeatures().get(0).getType();

      HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
      JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

      VisionRequestInitializer requestInitializer = new VisionRequestInitializer(
          CLOUD_VISION_API_KEY);

      Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
      builder.setVisionRequestInitializer(requestInitializer);

      Vision vision = builder.build();

      BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
      batchAnnotateImagesRequest.setRequests(imageRequests);

      Vision.Images.Annotate annotateRequest = vision.images()
          .annotate(batchAnnotateImagesRequest);
      annotateRequest.setDisableGZipContent(true);
      BatchAnnotateImagesResponse response = annotateRequest.execute();

      return formatResponse(response);

    } catch (GoogleJsonResponseException e) {
      Log.d(FeatureExtractionManager.SMART_WALLPAPERS_TAG, " failed to make Vision API request because: " + e.getContent());
    } catch (IOException e) {
      Log.d(FeatureExtractionManager.SMART_WALLPAPERS_TAG,
          " failed to make Vision API request because of other IOException " + e.getMessage());
    }

    return FAILED;
  }

  @Override
  protected void onPostExecute(String result) {
    super.onPostExecute(result);

    if (result.equals(FAILED)) {
      Log.d(FeatureExtractionManager.SMART_WALLPAPERS_TAG, " failed to process the response received from Vision API");
    } else {
      //everything when fine
      switch (feature_type) {
        case LANDMARK_DETECTION:
          listener.onLandmarkDetected(result);
          break;

        case LABEL_DETECTION:
          listener.onLabelsDetected(result);
          break;

        default:
          Log.d(FeatureExtractionManager.SMART_WALLPAPERS_TAG, " doesn't support feature extraction: " + feature_type);
      }
    }

  }

  private String formatResponse(BatchAnnotateImagesResponse response) {
    String result = "";
    AnnotateImageResponse annotateImageResponse = response.getResponses().get(0);
    List<EntityAnnotation> entityAnnotations;

    switch (feature_type) {
      case LANDMARK_DETECTION:
        entityAnnotations = annotateImageResponse.getLandmarkAnnotations();
        result = extractEntityAnnotations(entityAnnotations);
        break;

      case LABEL_DETECTION:
        entityAnnotations = annotateImageResponse.getLabelAnnotations();
        result = extractEntityAnnotations(entityAnnotations);
        break;
      default:
        result = FAILED;
    }
    return result;
  }

  /**
   * Each label will appear on a line delimited by the ';' (comma) symbol
   * The extracted string should look like:
   * nature; 0.95;
   * lake; 0.82;
   * sky; 0.78;
   */
  private String extractEntityAnnotations(List<EntityAnnotation> entityAnnotations) {
    StringBuilder result = new StringBuilder("");
    char delimiterSymbol = ';';

    if (entityAnnotations != null) {
      for (EntityAnnotation entityAnnotation : entityAnnotations) {
        result.append(entityAnnotation.getDescription());
        result.append(delimiterSymbol);
        result.append(entityAnnotation.getScore());
        result.append(delimiterSymbol);
        result.append("\n");
      }
    }

    return result.toString();
  }

}
