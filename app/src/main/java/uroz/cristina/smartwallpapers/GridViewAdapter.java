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

public class GridViewAdapter extends ArrayAdapter <Category>{
    public GridViewAdapter(@NonNull Context context, int resource, @NonNull List<Category> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (null == v) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.grid_item, null);
        }
        final Category category = getItem(position);
        ImageView img = (ImageView) v.findViewById(R.id.ImageViewG);
        final ImageView like = (ImageView) v.findViewById(R.id.likeViewG);
        final ImageView favorite = (ImageView) v.findViewById(R.id.favoriteViewG);
        ImageView delete = (ImageView) v.findViewById(R.id.deleteViewG);
        TextView txtTitle = (TextView) v.findViewById(R.id.txtTitleG);

        img.setImageResource(category.getImageId());
        txtTitle.setText(category.getTitle());

        setResources(category, like, favorite);

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                category.toggleLiked();
                setResources(category, like, favorite);
            }
        });

        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                category.toggleFavorite();
                setResources(category, like, favorite);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                category.setDeleted(true);
                remove(category);
            }
        });

        return v;
    }

    private void setResources(Category category, ImageView like, ImageView favorite){
            if (category.isLiked()){
            like.setImageResource(android.R.drawable.presence_online);
        }
        else {like.setImageResource(android.R.drawable.presence_invisible);}

        if (category.isFavorite()){
            favorite.setImageResource(android.R.drawable.star_big_on);
        }
        else {favorite.setImageResource(android.R.drawable.star_big_off);}
    }
}
