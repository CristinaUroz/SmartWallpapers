package uroz.cristina.smartwallpapers.ml_wallpapers;

import android.util.Log;
import com.kc.unsplash.Unsplash;
import com.kc.unsplash.models.Photo;
import com.kc.unsplash.models.SearchResults;
import java.util.List;
import uroz.cristina.smartwallpapers.MainActivity;

/**
 */
public class UnsplashSearchListener implements Unsplash.OnSearchCompleteListener {
  private PhotoSearchListener listener;

  public UnsplashSearchListener(PhotoSearchListener listener) {
    this.listener = listener;
  }

  @Override
  public void onComplete(SearchResults results) {
    List<Photo> photos = results.getResults();
    int size = photos.size();

    int randomIndex = (int) Math.floor(size * Math.random());
    Photo nextWallpaper = photos.get(randomIndex);

    String photoUrl = nextWallpaper.getUrls().getRegular();

    Log.i(MainActivity.SMART_WALLPAPERS_TAG,
        "Method UnsplashSearchListener-onComplete(), the suggested photo is: " + photoUrl);

    listener.onPhotoSearchComplete(photoUrl);

  }

  @Override
  public void onError(String error) {
    Log.i(MainActivity.SMART_WALLPAPERS_TAG,
        "Failed to search for top label, onError(): ");
  }
}
