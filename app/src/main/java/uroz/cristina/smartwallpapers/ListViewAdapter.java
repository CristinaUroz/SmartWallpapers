package uroz.cristina.smartwallpapers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static uroz.cristina.smartwallpapers.SharedPreferencesHelper.STRING;
import static uroz.cristina.smartwallpapers.SharedPreferencesHelper.writeToPreferenceFile;

public class ListViewAdapter extends ArrayAdapter<Quote> {

    private Context context;
    static final String ACTUAL_QUOTE = "ACTUAL_QUOTE"; //To save the last quote image set from this app
    static final String ACTUAL_QUOTE_AUTHOR = "ACTUAL_QUOTE_AUTHOR"; //To save the last author of the quote image set from this app

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
        ImageView set= (ImageView) v.findViewById(R.id.setViewL);
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

        set.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   AlertDialog.Builder mBuilder = new AlertDialog.Builder(((MainActivity)context));

                   mBuilder.setMessage(
                           R.string.display_quote); //Creates a dialog asking the user if he wants to display this as a wallpaper
                   mBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialogInterface, int i) {
                           writeToPreferenceFile(((MainActivity)context), STRING, ACTUAL_QUOTE,
                                   quote.getQuotation());
                           writeToPreferenceFile(((MainActivity)context), STRING, ACTUAL_QUOTE_AUTHOR,
                                   quote.getAuthor());
                           ((MainActivity)context).collectQuoteDisplayInfo();
                       }
                   });

                   mBuilder.setNegativeButton("no", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           dialog.cancel();
                       }
                   });

                   AlertDialog dialog = mBuilder.create();
                   dialog.show();
               }
           }
        );

        return v;
    }


}
