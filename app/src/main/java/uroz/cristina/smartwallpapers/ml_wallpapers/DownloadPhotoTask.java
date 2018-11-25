package uroz.cristina.smartwallpapers.ml_wallpapers;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import uroz.cristina.smartwallpapers.MainActivity;
import uroz.cristina.smartwallpapers.SharedPreferencesHelper;

/**
 * Created by Marius: marius.lucian.olariu@gmail.com
 */
public class DownloadPhotoTask extends AsyncTask<Object, Void, String> {

  public static final String SMART_WALLPAPERS_TAG = "SmartWallpapers";
  private final int quality = 100;
  private Context context;
  private String locationOfImage = "";

  @Override
  protected String doInBackground(Object... params) {
    final String urlOfImage = (String) params[0];
    context = (Context) params[1];

    // Download image to internal memory
//    Picasso.get().load(urlOfImage).into(new Target() {
//      @Override
//      public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
//        try {

      Bitmap bitmap = getBitmapFromURL(urlOfImage);
      if (bitmap != null) {       //downloading the photo might fail sometime
//        FileOutputStream imgFos = null;
//        imgFos = context.openFileOutput(MainActivity.NEXT_PHOTO_NAME,
//            Context.MODE_PRIVATE);
//        bitmap.compress(CompressFormat.PNG, quality, imgFos);
       // Log.i(SMART_WALLPAPERS_TAG, " successfully downloaded -> " + locationOfImage);
      }

    //Log.i("Marius", "onBitmapLoaded: " + context.getFilesDir().getAbsolutePath());
    // Toast.makeText(MainActivity.this, MainActivity.this.getFilesDir().getAbsolutePath(), Toast.LENGTH_SHORT).show();
    //listAllSavedFiles();

//          imgFos.close();
//        } catch (Exception e) {
//          Log.e(SMART_WALLPAPERS_TAG, "onBitmapLoaded - Failed to download image -> " + urlOfImage);
//          Log.e(SMART_WALLPAPERS_TAG, "onBitmapLoaded -> " + e.getStackTrace());
//        }
//      }
//
//      @Override
//      public void onBitmapFailed(Exception e, Drawable errorDrawable) {
//
//      }
//
//      @Override
//      public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//      }
//    });

    return urlOfImage;
  }

  @Override
  protected void onPostExecute(String urlOfImage) {
//    Log.i(SMART_WALLPAPERS_TAG, " downloaded image ->" + urlOfImage);
    SharedPreferencesHelper.writeToPreferenceFile(context, SharedPreferencesHelper.STRING, MainActivity.NEXT_IMAGE_LOCATION, locationOfImage );
  }

  public Bitmap getBitmapFromURL(String src) {
    try {
      java.net.URL url = new java.net.URL(src);
      HttpURLConnection connection = (HttpURLConnection) url
          .openConnection();
      connection.setDoInput(true);
      connection.connect();
      InputStream input = connection.getInputStream();

      Bitmap myBitmap = BitmapFactory.decodeStream(input);

      // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
      Uri tempUri = getImageUri(context, myBitmap);

      // CALL THIS METHOD TO GET THE ACTUAL PATH
      File finalFile = new File(getRealPathFromURI(tempUri));

      locationOfImage = finalFile.getAbsolutePath();
      Log.i(SMART_WALLPAPERS_TAG, "Downloaded photo path: " + locationOfImage);

      input.close();
      return myBitmap;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public Uri getImageUri(Context inContext, Bitmap inImage) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    inImage.compress(CompressFormat.PNG, quality, bytes);
    String path = Images.Media
        .insertImage(inContext.getContentResolver(), inImage, MainActivity.NEXT_PHOTO_NAME, "SW photo");
    return Uri.parse(path);
  }

  public String getRealPathFromURI(Uri uri) {
    Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
    cursor.moveToFirst();
    int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
    return cursor.getString(idx);
  }
}
