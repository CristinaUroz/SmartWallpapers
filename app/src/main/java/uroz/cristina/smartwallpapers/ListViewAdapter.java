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

    private Context context;

    public ListViewAdapter(@NonNull Context context, int resource, @NonNull List<Quote> objects) {
        super(context, resource, objects);
        this.context=context;
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
        ImageView delete= (ImageView) v.findViewById(R.id.deleteViewL);
        TextView txtTitle= (TextView) v.findViewById(R.id.txtTitleL);
        TextView txtAuthor = (TextView) v.findViewById(R.id.txtAuthorL);

        txtTitle.setText(quote.getQuotation());
        txtAuthor.setText(quote.getAuthor());

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(context instanceof MainActivity){
                    ((MainActivity)context).quoteLiked(quote, false);
                }
                remove(quote);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(context instanceof MainActivity){
                    ((MainActivity)context).quoteDeleted(quote);
                }
                remove(quote);
            }
        });

        return v;
    }


}
