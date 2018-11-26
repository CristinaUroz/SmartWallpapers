package uroz.cristina.smartwallpapers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.kc.unsplash.models.Photo;
import com.squareup.picasso.Picasso;

import java.util.List;

import static uroz.cristina.smartwallpapers.SharedPreferencesHelper.STRING;
import static uroz.cristina.smartwallpapers.SharedPreferencesHelper.writeToPreferenceFile;

public class ImageViewAdapter extends ArrayAdapter<Photo> {

  private Context context;
  static final String ACTUAL_IMAGE = "ACTUAL_IMAGE"; //To save the last wallpaper image set from this app

  public ImageViewAdapter(@NonNull Context context, int resource, @NonNull List<Photo> objects) {
    super(context, resource, objects);
    this.context = context;
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

    View v = convertView;
    if (null == v) {
      LayoutInflater inflater = (LayoutInflater) getContext()
          .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      v = inflater.inflate(R.layout.image_item, null);
    }

    final Photo photo = getItem(position);

    ImageView img = (ImageView) v.findViewById(R.id.ImageViewI);
    ImageView delete = (ImageView) v.findViewById(R.id.deleteViewI);
    ImageView set = (ImageView) v.findViewById(R.id.setViewI);
    final ImageView like = (ImageView) v.findViewById(R.id.likeViewI);

    Picasso.get().load(photo.getUrls().getRegular()).into(img);

    like.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (context instanceof MainActivity) {
          ((MainActivity) context).photoLiked(photo);
        }
        remove(photo);
      }
    });

    delete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (context instanceof MainActivity) {
          ((MainActivity) context).dislikedPhoto(photo);
        }
        remove(photo);
      }
    });

    set.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(((MainActivity) context));

        mBuilder.setMessage(R.string.display_photo);
        mBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            writeToPreferenceFile(((MainActivity) context),
                STRING, ACTUAL_IMAGE,
                photo.getUrls().getRegular());
            ((MainActivity) context).setWallpaper();
          }
        });

        mBuilder.setNegativeButton("No", new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
          }
        });

        AlertDialog dialog = mBuilder.create();
        dialog.show();
      }
    });

    return v;
  }
}
