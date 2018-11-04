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

    private Context context;

    public ImageViewAdapter(@NonNull Context context, int resource, @NonNull List<Photo> objects) {
        super(context, resource, objects);
        this.context=context;
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

        Picasso.get().load(photo.getUrls().getRegular()).into(img);

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(context instanceof MainActivity){
                    ((MainActivity)context).photoLiked(photo);
                }
                remove(photo);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(context instanceof MainActivity){
                    ((MainActivity)context).photoDeleted(photo);
                }
                remove(photo);
            }
        });

        return v;
    }
}
