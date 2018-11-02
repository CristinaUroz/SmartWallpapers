package uroz.cristina.smartwallpapers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageViewAdapter extends ArrayAdapter<Image> {

    static final int icon_like_off=android.R.drawable.presence_invisible;
    static final int icon_like_on=android.R.drawable.presence_online;
    static final int icon_favorite_off=android.R.drawable.star_big_on;
    static final int icon_favorite_on=android.R.drawable.star_big_off;

    public ImageViewAdapter(@NonNull Context context, int resource, @NonNull List<Image> objects) {
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
        final Image image = getItem(position);
        ImageView img = (ImageView) v.findViewById(R.id.ImageViewI);
        ImageView delete = (ImageView) v.findViewById(R.id.deleteViewI);
        final ImageView like = (ImageView) v.findViewById(R.id.likeViewI);

        //img.setImageResource(image.getImageId());
        Picasso.get().load(image.getSrc()).into(img);


        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image.setLiked();
                remove(image);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image.setDeleted();
                remove(image);
            }
        });

        return v;



    }

}
