package uroz.cristina.smartwallpapers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
        final ImageView favorite = (ImageView) v.findViewById(R.id.favoriteViewI);

        img.setImageResource(image.getImageId());

        setResources(image, like, favorite);

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image.toggleLiked();
                setResources(image, like, favorite);
            }
        });

        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image.toggleFavorite();
                setResources(image, like, favorite);
            }
        });



        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image.setDeleted(true);
                remove(image);
            }
        });

        return v;



    }

    private void setResources(Image image, ImageView like, ImageView favorite){
        if (image.isLiked()){
            like.setImageResource(icon_like_on);
        }
        else {like.setImageResource(icon_like_off);}

        if (image.isFavorite()){
            favorite.setImageResource(icon_favorite_off);
        }
        else {favorite.setImageResource(icon_favorite_on);}
    }

}
