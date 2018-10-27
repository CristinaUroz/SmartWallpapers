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

public class ListViewAdapter extends ArrayAdapter<Quote> {

    static final int icon_like_off=android.R.drawable.presence_invisible;
    static final int icon_like_on=android.R.drawable.presence_online;
    static final int icon_favorite_off=android.R.drawable.star_big_on;
    static final int icon_favorite_on=android.R.drawable.star_big_off;

    public ListViewAdapter(@NonNull Context context, int resource, @NonNull List<Quote> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v=convertView;
        if(null == v){
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.list_item, null);
        }
        final Quote quote= getItem(position);
        final ImageView like= (ImageView) v.findViewById(R.id.likeViewL);
        final ImageView favorite= (ImageView) v.findViewById(R.id.favoriteViewL);
        ImageView delete= (ImageView) v.findViewById(R.id.deleteViewL);
        TextView txtTitle= (TextView) v.findViewById(R.id.txtTitleL);
        TextView txtAutor = (TextView) v.findViewById(R.id.txtAutorL);

        txtTitle.setText(quote.getTitle());
        txtAutor.setText(quote.getAutor());

        setResources(quote, like, favorite);

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quote.toggleLiked();
                setResources(quote, like, favorite);
            }
        });

        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quote.toggleFavorite();
                setResources(quote, like, favorite);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quote.setDeleted(true);
                remove(quote);
            }
        });

        return v;
    }

    private void setResources(Quote quote, ImageView like, ImageView favorite){
        if (quote.isLiked()){
            like.setImageResource(icon_like_on);
        }
        else {like.setImageResource(icon_like_off);}

        if (quote.isFavorite()){
            favorite.setImageResource(icon_favorite_off);
        }
        else {favorite.setImageResource(icon_favorite_on);}
    }
}
