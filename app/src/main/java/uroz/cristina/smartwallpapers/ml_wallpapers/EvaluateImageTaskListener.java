package uroz.cristina.smartwallpapers.ml_wallpapers;

/**
 * Created by Marius: marius.lucian.olariu@gmail.com
 */
public interface EvaluateImageTaskListener {

   void onLabelsDetected(String results);
   void onLandmarkDetected(String results);

}
