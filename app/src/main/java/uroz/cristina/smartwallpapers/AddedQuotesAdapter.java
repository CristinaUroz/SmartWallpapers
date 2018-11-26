package uroz.cristina.smartwallpapers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static uroz.cristina.smartwallpapers.SharedPreferencesHelper.STRING;
import static uroz.cristina.smartwallpapers.SharedPreferencesHelper.writeToPreferenceFile;

public class AddedQuotesAdapter extends ArrayAdapter<Quote> {

    private Context context;
    static final String ACTUAL_QUOTE = "ACTUAL_QUOTE";
    static final String ACTUAL_QUOTE_AUTHOR = "ACTUAL_QUOTE_AUTHOR";

    public AddedQuotesAdapter(@NonNull Context context, int resource, @NonNull List<Quote> objects) {
        super(context, resource, objects);
        this.context=context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View v=convertView;
        if(null == v){
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.added_quote_item, null);
        }

        final Quote quote= getItem(position);


        ImageView set= (ImageView) v.findViewById(R.id.set_added_quote);
        TextView txtQuote = (TextView) v.findViewById(R.id.liked_quote_content);
        TextView txtAuthor = (TextView) v.findViewById(R.id.liked_quote_author);

        txtQuote.setText(quote.getQuotation());
        txtAuthor.setText(quote.getAuthor());


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
