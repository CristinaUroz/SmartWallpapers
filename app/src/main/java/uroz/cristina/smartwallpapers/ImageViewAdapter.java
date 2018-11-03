package uroz.cristina.smartwallpapers;

import android.content.Context;
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

public class ImageViewAdapter extends ArrayAdapter<Photo> {

    public ImageViewAdapter(@NonNull Context context, int resource, @NonNull List<Photo> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (null == v) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.image_item, null);
        }
        final Photo photo = getItem(position);
        ImageView img = (ImageView) v.findViewById(R.id.ImageViewI);
        ImageView delete = (ImageView) v.findViewById(R.id.deleteViewI);
        final ImageView like = (ImageView) v.findViewById(R.id.likeViewI);

        //img.setImageResource(image.getImageId());
        Picasso.get().load(photo.getUrls().getRegular()).into(img);


        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //image.setLiked();
                remove(photo);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //image.setDeleted();
                remove(photo);
            }
        });

        return v;



    }

}
