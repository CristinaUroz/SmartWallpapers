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

import com.kc.unsplash.models.Collection;

import java.util.List;

public class GridViewAdapter extends ArrayAdapter <Collection>{

    private Context context;

    public GridViewAdapter(@NonNull Context context, int resource, @NonNull List<Collection> objects) {
        super(context, resource, objects);
        this.context=context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View v = convertView;
        if (null == v) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.grid_item, null);
        }

        final Collection collection = getItem(position);

        ImageView img = (ImageView) v.findViewById(R.id.ImageViewG);
        final ImageView like = (ImageView) v.findViewById(R.id.likeViewG);
        ImageView delete = (ImageView) v.findViewById(R.id.deleteViewG);
        TextView txtTitle = (TextView) v.findViewById(R.id.txtTitleG);

        com.squareup.picasso.Picasso.get().load(collection.getCoverPhoto().getUrls().getRegular()).into(img);
        txtTitle.setText(collection.getTitle());

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(context instanceof MainActivity){
                    ((MainActivity)context).collectionLiked(collection);
                }
                remove(collection);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(context instanceof MainActivity){
                    ((MainActivity)context).collectionDeleted(collection);
                }
                remove(collection);
            }
        });

        return v;
    }
}
