package uroz.cristina.smartwallpapers;

import static java.lang.Thread.sleep;
import static uroz.cristina.smartwallpapers.PreferencesManager.BOOLEAN;
import static uroz.cristina.smartwallpapers.PreferencesManager.INTERVAL_MILLIS;
import static uroz.cristina.smartwallpapers.PreferencesManager.readString;
import static uroz.cristina.smartwallpapers.PreferencesManager.writeToPreferenceFile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.Image;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.kc.unsplash.Unsplash;
import com.kc.unsplash.Unsplash.OnPhotosLoadedListener;
import com.kc.unsplash.models.Collection;
import com.kc.unsplash.models.Photo;
import com.kc.unsplash.models.SearchResults;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import uroz.cristina.smartwallpapers.ml_wallpapers.WallpaperAlarm;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity {

  //TODO: CRISTINA Set screen orientation changeable

  //TODO: ALL Translate words in our language

  //Variables for the layout
  private ViewStub stubGrid;
  private ViewStub stubList;
  private ListView listView;
  private GridView gridView;
  private FloatingActionButton search_fb;
  private FloatingActionButton addQuote;
  private ListViewAdapter listViewAdapter;
  private GridViewAdapter gridViewAdapter;
  private ImageViewAdapter imageViewAdapter;
  private View quoteDisplayLayout, addNewQuote;
  private Parcelable listState;
  private Parcelable gridState;
  private int currentViewMode = 0;
  static final int VIEW_MODE_LISTVIEW = 0;
  static final int VIEW_MODE_GRIDVIEW = 1;
  static final int VIEW_MODE_IMAGEVIEW = 2;

  // Menu variables
  private Menu optionsMenu;
  static final int CHANGE_VIEW_MODE = 1;
  //Change view menu item id

  //Photos, collections and quotes variables
  private List<Quote> quoteList; //List of all the quotes that will be displayed
  private List<Quote> tmpQuoteList;
  private Map<String, List<Photo>> photoMap = new TreeMap<>(); //Map <collection id, list of photos displayed in this collection> | the class photo is an Unsplash model
  private List<Collection> collectionsList = new ArrayList<>(); //List of all the collections that will be displayed | the class collection is an Unsplash model
  private String actual_collection = ""; //To save the id of the collection that the user is looking at the moment
  private int actual_collection_position; //To save the position the collection that the user is looking at the moment in the list
  private boolean enable_auto = true; //If the user wants the wallpaper auto-refresh
  private ArrayList<String> quoteDisplayInfo = new ArrayList<>();

  //Variables to save what user likes

  private List<String> likedPhotos = new ArrayList<>(); //List with all the photo's source that the user have liked. This list will be saved in the internal storage after closing th app
  private List<String> likedQuotes = new ArrayList<>(); //List with all the quotes ("quote;author") that the user have liked. This list will be saved in the internal storage after closing th app
  private List<String> dislikedPhotos = new ArrayList<>();
  private List<String> dislikedQuotes = new ArrayList<>();

  private List<String> likedCollections = new ArrayList<>();
  private List<String> dislikedCollections = new ArrayList<>();


  public static final String FILENAME_PHOTOS = "liked_photos.txt";
  public static final String FILENAME_QUOTES = "liked_quotes.txt";
  private static final int MAX_BYTES = 8000;
  private final int page = 1;   //number of pages
  private final int perPage = 30; //Number of categories displyed, number of photos displayed in a category

  //variables for Database to store liked quotes
  DatabaseLikedQuotesHelper mDatabaseLQHelper;


  //Unplash variables
  private final String CLIENT_ID = "4254aee191dd7d4dec3ff36c75a61ffb50cdcd320d1c14942b1dec21f67159b9"; //Cristina's sesion Id
  private Unsplash unsplash = new Unsplash(CLIENT_ID);

  //Icons to change the view
  static final int icon_grid = android.R.drawable.ic_dialog_dialer;
  static final int icon_list = android.R.drawable.ic_menu_sort_by_size;
  static final int icon_revertgrid = android.R.drawable.ic_menu_revert;

  //Variables to save strings in the cache memory
  static final String ACTUAL_IMAGE = "ACTUAL_IMAGE"; //To save the last wallpaper image set from this app
  static final String ACTUAL_QUOTE = "ACTUAL_QUOTE"; //To save the last quote image set from this app
  public static final String NEXT_IMAGE_TAG = "NEXT_IMAGE";
  static final String ACTUAL_QUOTE_AUTHOR = "ACTUAL_QUOTE_AUTHOR"; //To save the last author of the quote image set from this app
  static final String PHOTO_INTERVAL = "PHOTO_INTERVAL"; //To save the refresh interval minutes
  static final String REFRESHING = "REFRESHING"; //To save if the user wants the wallpaper auto-refresh
  static final String FIRST_LAUNCH = "FIRST_LAUNCH"; // On the first time when the app is launched do some on time configurations
  public static final String SMART_WALLPAPERS_TAG = "SmartWallpapers";


  //Variables of the thread to do the wallpaper auto-refresh
  private List<Boolean> stopThreadList = new ArrayList<>();
  private int stopThreadPos = 0;

  //variables for quotes pop up windows
  TextView newQuoteContent, newQuoteAuthor, newQuoteCat;

  // global reference necessary for displaying wallpaper
  // after first click - for calling onBitmapLoaded
  // check: https://stackoverflow.com/questions/24180805/onbitmaploaded-of-target-object-not-called-on-first-load#answers
  Target target;

  private String TAG = "MainActivity";

  //Business Logic
  private WallpaperAlarm wallpaperAlarm;

  //__________________________

  //TODO Marius: Change app icon
  //TODO Marius: Create a widget to refresh the wallpaper

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    //Find layout viewstub items
    stubList = (ViewStub) findViewById(R.id.stub_list);
    stubGrid = (ViewStub) findViewById(R.id.stub_grid);

    //Inflate ViewStub before get view
    stubList.inflate();
    stubGrid.inflate();

    //Get current view mode in share reference
    SharedPreferences sharedPreferences = getSharedPreferences("viewMode", MODE_PRIVATE);
    currentViewMode = sharedPreferences.getInt("currentViewMode", VIEW_MODE_GRIDVIEW); //Default

    //Find layout items
    listView = findViewById(R.id.mylistview);
    gridView = findViewById(R.id.mygridview);
    search_fb = findViewById(R.id.search_floating_button);
    addQuote =  findViewById(R.id.add_quote);

    //Set listeners
    listView.setOnItemClickListener(onItemClick);
    listView.setOnItemLongClickListener(onLongClick);
    gridView.setOnItemClickListener(onItemClick);
    gridView.setOnItemLongClickListener(onLongClick);

    //Initialize database
      mDatabaseLQHelper = new DatabaseLikedQuotesHelper(MainActivity.this);

    //Get all the quotes and collections
    getQuoteList();
    getCollectionList();

    //Read the previous liked photos and quotes from the user
    // Note: There is no need to read them in memory because the service will get them from the output file. There is a need for only for
//      writing to those output files (where we have liked wallpapers/quotes);
    //readLikedPhotos();
    //readLikedQuotes();
    wallpaperAlarm = new WallpaperAlarm();

    //do the one time configurations needed after the app was installed
    if (readString(MainActivity.this, FIRST_LAUNCH) == null) {
      PreferencesManager
          .writeToPreferenceFile(this, PreferencesManager.STRING, MainActivity.FIRST_LAUNCH, "no");
      PreferencesManager.writeToPreferenceFile(this, BOOLEAN, MainActivity.REFRESHING, true);

    //  int intervalMillis = 30 * 1000; //30 seconds, debugging purposes

      int intervalMillis = 30 * 60 * 1000;
      PreferencesManager
          .writeToPreferenceFile(this, PreferencesManager.INT, INTERVAL_MILLIS,
              intervalMillis);

      //recurrent alarm
      wallpaperAlarm.setAlarm(this);
    }

    search_fb.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(MainActivity.this);
        View mViews = getLayoutInflater().inflate(R.layout.search_dialog, null);
        mbuilder.setView(mViews);
        final AlertDialog mDialogs = mbuilder.create();

        ImageButton search_btn = (ImageButton) mViews.findViewById(R.id.search_button);
        ImageButton search_btn_loc = (ImageButton) mViews.findViewById(R.id.search_button_loc);
        final EditText search_txt = (EditText) mViews.findViewById(R.id.editText_search);

        search_btn_loc.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            TrackGPS gps = new TrackGPS(MainActivity.this);
            if (gps.canGetLocation()) {
              try {
                Geocoder geo = new Geocoder(MainActivity.this, Locale.getDefault());
                List<Address> addresses = geo
                    .getFromLocation(gps.getLatitude(), gps.getLongitude(), 1);
                //List<Address> addresses = geo.getFromLocation(55.86515, -4.25763, 1);
                if (addresses.isEmpty()) {
                  Toast.makeText(MainActivity.this, "Waiting for Location", Toast.LENGTH_SHORT)
                      .show();
                } else {
                  if (addresses.size() > 0) {
                    Toast.makeText(MainActivity.this,
                        addresses.get(0).getAdminArea() + " (" + gps.getLatitude() + "," + gps
                            .getLongitude() + ")", Toast.LENGTH_SHORT).show();

                    getSupportActionBar().setTitle(addresses.get(0).getAdminArea());

                    currentViewMode = VIEW_MODE_IMAGEVIEW;

                    gridState = listView.onSaveInstanceState();

                    optionsMenu.getItem(CHANGE_VIEW_MODE).setIcon(icon_revertgrid);

                    SharedPreferences sharedPreferences = getSharedPreferences("ViewMode",
                        MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("currentViewMode", currentViewMode);
                    editor.commit();

                    searchPhotoList(addresses.get(0).getAdminArea());

                  }
                }
              } catch (Exception e) {
                Toast.makeText(MainActivity.this, "No Location Name Found", Toast.LENGTH_SHORT)
                    .show();
              }
              mDialogs.cancel();
            } else {
              gps.showAlert();
            }
          }
        });

        search_btn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            String search_text = search_txt.getText().toString();
            if (search_text.length() <= 0) {
              Toast.makeText(MainActivity.this, R.string.enter_word, Toast.LENGTH_SHORT).show();
            } else {

              getSupportActionBar().setTitle(search_text);

              currentViewMode = VIEW_MODE_IMAGEVIEW;

              gridState = listView.onSaveInstanceState();

              optionsMenu.getItem(CHANGE_VIEW_MODE).setIcon(icon_revertgrid);

              SharedPreferences sharedPreferences = getSharedPreferences("ViewMode", MODE_PRIVATE);
              SharedPreferences.Editor editor = sharedPreferences.edit();
              editor.putInt("currentViewMode", currentViewMode);
              editor.commit();

              searchPhotoList(search_text);
              mDialogs.cancel();
            }
          }
        });
        mDialogs.show();
      }
    });

    addQuote.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            addNewQuote = inflater.inflate(R.layout.add_quote, null);
            float density = MainActivity.this.getResources().getDisplayMetrics().density;
          Display display = getWindowManager().getDefaultDisplay();
          Point size = new Point();
          display.getSize(size);
            final PopupWindow pw = new PopupWindow(addNewQuote, (int) (size.x*0.95), (int)(size.y*0.8), true);

            addNewQuote.findViewById(R.id.save_qd).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Quote new_quote = getNewQuoteInfo();
                    quoteList.add(0, new_quote);
                    pw.dismiss();
                }
            });
          addNewQuote.findViewById(R.id.save_and_like_qd).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              Quote new_quote = getNewQuoteInfo();
              quoteLiked(new_quote, true);
              pw.dismiss();
            }
          });

          addNewQuote.findViewById(R.id.withdraw_add).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              pw.dismiss();
            }
          });


            pw.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            pw.showAtLocation(addNewQuote, Gravity.CENTER, 0, 0);
        }
    });

  }

  private Quote getNewQuoteInfo() {
      SharedPreferences quotesCat = getSharedPreferences("quotesCat", Context.MODE_PRIVATE);
      SharedPreferences.Editor edit = quotesCat.edit();

      newQuoteContent = addNewQuote.findViewById(R.id.quote_content);
      newQuoteAuthor = addNewQuote.findViewById(R.id.quote_author);
      newQuoteCat = addNewQuote.findViewById(R.id.quote_cat);
      String q_cont = newQuoteContent.getText().toString();
      String q_auth = newQuoteAuthor.getText().toString();
      String q_cat = newQuoteCat.getText().toString();

    if(!quotesCat.contains(q_cat)){
        edit.putInt(q_cat, 0);
    }

    edit.commit();
    return new Quote(q_cont, q_auth, q_cat);
  }

  @Override //Save the liked photos & quotes file
  protected void onStop() {
    super.onStop();

  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    //disabled these for the moment
//    writeLikedPhotos();
//    writeLikedQuotes();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    optionsMenu = menu;
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    //TODO: Put an option to allow the user write quotes

    switch (item.getItemId()) {
      case R.id.change_view: //To change the view and the menu icon
        if (VIEW_MODE_LISTVIEW == currentViewMode) {
          currentViewMode = VIEW_MODE_GRIDVIEW;
          item.setIcon(icon_list);
        } else if (VIEW_MODE_IMAGEVIEW == currentViewMode) {
          currentViewMode = VIEW_MODE_GRIDVIEW;
          item.setIcon(icon_list);
        } else {
          currentViewMode = VIEW_MODE_LISTVIEW;
          item.setIcon(icon_grid);
        }
        switchView();
        SharedPreferences sharedPreferences = getSharedPreferences("ViewMode", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("currentViewMode", currentViewMode);
        editor.commit();
        break;

      case R.id.info: //Creates a dialog with the information about how to use the app

        //TODO: Create an AlertDialog with a video player that shows the user how to use the app
        //TODO: Put voice-over depending of the lenguage of the mobilephone

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setMessage(R.string.app_info);
        AlertDialog dialog = mBuilder.create();
        dialog.show();
        break;

      case R.id.changeWallpaper: //Change the wallpaper with an image and quote liked by the users
        getA_Suggested_Image();

        break;

      case R.id.settings: //Creates a dialog to enable the auto-refresh and to choose the interval value in minutes
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.settings_dialog, null);
        builder.setView(mView);
        final AlertDialog mDialog = builder.create();

        final EditText photo_interval = (EditText) mView.findViewById(R.id.photo_interval);

        Button apply = (Button) mView.findViewById(R.id.apply_button);
        Button cancel = (Button) mView.findViewById(R.id.cancel_button);

        String photo_i = readString(this, PHOTO_INTERVAL);

        photo_interval.setText(photo_i);

        apply.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            float changeWallpaperTimerFloat  = 1f;
            changeWallpaperTimerFloat = Float.valueOf(photo_interval.getText().toString());
            int changeWallpaperTimer = (int) (changeWallpaperTimerFloat * 60);

            int oldWallpaperTimer = PreferencesManager
                .readInt(MainActivity.this, PreferencesManager.INTERVAL_MILLIS);

            //transform into millis the data read from the view
            changeWallpaperTimer *= 1000;

            int THIRTY_MINUTES = 30 * 60 * 1000;
            int THREE_HUNDRED_MINUTES = 300 * 60 * 1000;

            //FIXME: Alarm is not triggering, there might be a problem here

            //limit the interval changing between 30 minutes and 300 minutes
            if (changeWallpaperTimer != oldWallpaperTimer) {
              //NOTE: For debugging purposes you can comment the following constraint statements and write only once the INTERVAL_MILLIS to set wallpaper changing at a faster rate (e.g 30 seconds)
//              changeWallpaperTimer =
//                  (changeWallpaperTimer < THIRTY_MINUTES) ? THIRTY_MINUTES : changeWallpaperTimer;
//              changeWallpaperTimer =
//                  (changeWallpaperTimer > THREE_HUNDRED_MINUTES) ? THREE_HUNDRED_MINUTES
//                      : changeWallpaperTimer;

              PreferencesManager.writeToPreferenceFile(MainActivity.this, PreferencesManager.INT,
                  PreferencesManager.INTERVAL_MILLIS, changeWallpaperTimer);

              if (wallpaperAlarm.alarmExists(MainActivity.this)) {
                wallpaperAlarm.cancelAlarm(MainActivity.this);
              }

              wallpaperAlarm.setAlarm(MainActivity.this);
            }

            //Marius: idk if writing this string to the preference file is really necessary since we have the state of the UI element
            //    PreferencesManager.writeToPreferenceFile(MainActivity.this, PreferencesManager.BOOLEAN, MainActivity.REFRESHING, new Boolean(changeWallpaperAutomatically));

            mDialog.cancel();
          }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            mDialog.cancel();
          }
        });
        mDialog.show();
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  //_____________________________________

  //Change te view between collections and list. It also enable to go from image grid to collection grid
  private void switchView() {
    if (VIEW_MODE_LISTVIEW == currentViewMode) {
      getSupportActionBar().setTitle("Quotes");
      stubList.setVisibility(View.VISIBLE);
      stubGrid.setVisibility(View.GONE);
      listState = listView.onSaveInstanceState();
      addQuote.bringToFront();
    } else if (VIEW_MODE_IMAGEVIEW == currentViewMode) {
      getSupportActionBar().setTitle("Categories");
      stubList.setVisibility(View.VISIBLE);
      stubGrid.setVisibility(View.GONE);
    } else {
      getSupportActionBar().setTitle("Categories");
      stubList.setVisibility(View.GONE);
      stubGrid.setVisibility(View.VISIBLE);
      gridState = gridView.onSaveInstanceState();
    }
    setAdapters();
  }

  //Set adapters to view photos/collections/quotes
  private void setAdapters() {
    if (VIEW_MODE_LISTVIEW == currentViewMode) {
      listViewAdapter = new ListViewAdapter(this, R.layout.list_item, quoteList);
      listView.setAdapter(listViewAdapter);
      listView.onRestoreInstanceState(listState);
    } else if (VIEW_MODE_GRIDVIEW == currentViewMode) {
      gridViewAdapter = new GridViewAdapter(this, R.layout.grid_item, collectionsList);
      gridView.setAdapter(gridViewAdapter);
      gridView.onRestoreInstanceState(gridState);
    } else {
      imageViewAdapter = new ImageViewAdapter(this, R.layout.grid_item,
          photoMap.get(actual_collection));
      gridView.setAdapter(imageViewAdapter);
    }
  }


  //Create a dialog to see a photo
  private void createPhotoDialog(final int position) {

    AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
    View mView = getLayoutInflater().inflate(R.layout.image_dialog, null);
    mBuilder.setView(mView);
    final AlertDialog dialog = mBuilder.create();

    ImageView imageView = (ImageView) mView.findViewById(R.id.ImageViewD);
    final ImageView like = (ImageView) mView.findViewById(R.id.likeViewD);
    ImageView delete = (ImageView) mView.findViewById(R.id.deleteViewD);
    ImageView previous = (ImageView) mView.findViewById(R.id.previousViewD);
    ImageView next = (ImageView) mView.findViewById(R.id.nextViewD);
    TextView Title = (TextView) mView.findViewById(R.id.txtTitleD);
    TextView Author = (TextView) mView.findViewById(R.id.txtAuthorD);

    Picasso.get().load(photoMap.get(actual_collection).get(position).getUrls().getRegular())
        .into(imageView);
    Title.setText(photoMap.get(actual_collection).get(position).getUser().getName());
    Author.setText("");

    if (photoMap.get(actual_collection).get(position).getLocation() != null) {

      if (photoMap.get(actual_collection).get(position).getLocation().getCity() != null) {
        Author.setText(
            photoMap.get(actual_collection).get(position).getLocation().getCity() + ", " + photoMap
                .get(actual_collection).get(position).getLocation().getCountry());
      } else if (photoMap.get(actual_collection).get(position).getLocation().getCountry() != null) {
        Author.setText(photoMap.get(actual_collection).get(position).getLocation().getCountry());
      }
    }

    final Photo photo = photoMap.get(actual_collection).get(position);

    like.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        photoMap.get(actual_collection).remove(photo);
        boolean last = photoLiked(photo);
        if (last) {
          dialog.cancel();
        } else {
          imageViewAdapter.notifyDataSetChanged();
          dialog.cancel();
          int new_position = position + 1;
          if (new_position >= photoMap.get(actual_collection).size()) {
            new_position = 0;
          }
          createPhotoDialog(new_position);
        }
      }
    });

    delete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        photoMap.get(actual_collection).remove(photo);
        boolean last = dislikedPhoto(photo);
        if (last) {
          dialog.cancel();
        } else {
          imageViewAdapter.notifyDataSetChanged();
          dialog.cancel();
          int new_position = position + 1;
          if (new_position >= photoMap.get(actual_collection).size()) {
            new_position = 0;
          }
          createPhotoDialog(new_position);
        }
      }
    });

    previous.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        dialog.cancel();
        int new_position = position - 1;
        if (new_position < 0) {
          new_position = photoMap.get(actual_collection).size() - 1;
        }
        createPhotoDialog(new_position);
      }
    });

    next.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        dialog.cancel();
        int new_position = position + 1;
        if (new_position >= photoMap.get(actual_collection).size()) {
          new_position = 0;
        }
        createPhotoDialog(new_position);
      }
    });

    dialog.show();
  }

  //_______________________________________


  //To get all the quotes for the first time
  private void getQuoteList() {

    //used API: https://talaikis.com/api
    //every request returns 100 random quotes
    quoteList = new ArrayList<>();
    tmpQuoteList = new ArrayList<>();
    getQuotes(true);

  }

//  get 100 quotes from API
    private void getQuotes(final Boolean first) {
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        String url = "https://talaikis.com/api/quotes/";
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>()
                {
                    SharedPreferences quotesCat = getSharedPreferences("quotesCat", Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = quotesCat.edit();

                    @Override
                    public void onResponse(JSONArray response) {
                        for( int i=0; i < response.length(); ++i){
                            try {
                                String quote = (String) response.getJSONObject(i).get("quote");
                                String author = (String) response.getJSONObject(i).get("author");
                                String category = (String) response.getJSONObject(i).get("cat");
                                if (quote.length() < 100) {
                                    Quote tmp_quote = new Quote(quote, author, category);
                                    if (first) {
                                        quoteList.add(tmp_quote);
                                    }
                                    else{
                                        tmpQuoteList.add(tmp_quote);
                                    }
                                    if(!quotesCat.contains(category)){
                                        edit.putInt(category, 0);
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        edit.commit();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );
        queue.add(getRequest);


    }

    private void sortQuotes() {
        final SharedPreferences quotesCat = getSharedPreferences("quotesCat", Context.MODE_PRIVATE);

        Collections.sort(quoteList, new Comparator<Quote>() {
            @Override
            public int compare(Quote q1, Quote q2) {

                return Integer.compare(quotesCat.getInt(q2.getCategory(), 0) ,(quotesCat.getInt(q1.getCategory(), 0)));
            }
        });
    }

    //To get all the collections
  private void getCollectionList() {

    //TODO: MARIUS Show the collections depending on the user's preferences. If it is not possible, show new categories everytime that the user opens the app

    unsplash.getCollections(page, perPage, new Unsplash.OnCollectionsLoadedListener() {
      @Override
      public void onComplete(List<Collection> collections) {
        collectionsList = collections;
        switchView();
      }

      @Override
      public void onError(String error) {
      }
    });
  }

  //To get all the photos from a collection (actual_collection)
  private void getPhotoList() {

    //TODO: MARIUS Show the photos depending on the user's preferences and always new ones

    if (photoMap.containsKey(actual_collection)) {

      setAdapters();

    } else {
      unsplash.getCollectionPhotos(actual_collection, page, perPage,
          new Unsplash.OnPhotosLoadedListener() {

            @Override
            public void onComplete(List<Photo> list) {

              photoMap.put(actual_collection, list);

              setAdapters();
            }

            @Override
            public void onError(String error) {

            }
          });
    }
  }

  //To search photos
  private void searchPhotoList(String query) {

    //TODO: MARIUS Show the photos depending on the user's preferences and always new ones

    actual_collection = query;

    actual_collection_position = -1;

    if (photoMap.containsKey(actual_collection)) {

      setAdapters();

    } else {
      unsplash.searchPhotos(query, page, perPage, new Unsplash.OnSearchCompleteListener() {

        @Override
        public void onComplete(SearchResults results) {
          List<Photo> photos = results.getResults();
          photoMap.put(actual_collection, photos);

          setAdapters();
        }

        @Override
        public void onError(String error) {

        }
      });
    }
  }

  //____________________________________

  //TODO: When the user likes o deletes an item create a new one to not let the list/grid be empty
  //Be careful of losing the position where is the user on the list/grid

  //What happens when a photo is liked
  public boolean photoLiked(Photo photo) {

    String authorName = photo.getUser().getName();
    Toast.makeText(this, "Liked photo by: " + authorName, Toast.LENGTH_SHORT).show();

    String imgUrl = photo.getUrls().getRegular();
    likedPhotos.add(imgUrl); //Photo's source it's saved at the likes list
    writeImageUrlToLikedPhotosFile(imgUrl);

    PreferencesManager
        .writeToPreferenceFile(this, PreferencesManager.STRING, NEXT_IMAGE_TAG, imgUrl);
    writeLikedPhotos();

    //UI thing
    if (photoMap.get(actual_collection).size()
        == 1) { //If the photo was the last one of the collection the view will return to the collection grid
      photoMap.remove(actual_collection);
      if (actual_collection_position != -1) {
        collectionsList.remove(actual_collection_position);
      }
      currentViewMode = VIEW_MODE_GRIDVIEW;
      gridViewAdapter.notifyDataSetChanged();
      optionsMenu.getItem(CHANGE_VIEW_MODE).setIcon(icon_list);
      switchView();
      return true;
    }
    return false;
  }


  //What happens when a collection is liked
  public boolean collectionLiked(Collection collection) {

    //TODO: MARIUS What happens when the users likes this collection?

    //FIXME check wht this is
    String html = collection.getLinks().getHtml();
    String regularLink = collection.getCoverPhoto().getUrls().getRegular();
//  /  collectionsList.add(html);

    String col_id = Integer.toString(collection.getId());

    if (photoMap.containsKey(col_id)) { //If the collection images have been read again...
      for (int i = 0; i < photoMap.get(col_id).size(); i++) {
        String currentPhotoUrl = photoMap.get(col_id).get(i).getUrls()
            .getRegular();

        // Idk if we still need this collection since the automatic changing of the wallpaper is done based on what is written in photos file
        likedPhotos.add(currentPhotoUrl); //Put all the photo's source at the likes list
        writeImageUrlToLikedPhotosFile(currentPhotoUrl);

      }
      photoMap.remove(col_id);
    } else {
      unsplash.getCollectionPhotos(col_id, page, perPage,
          new OnPhotosLoadedListener() {//If not read them first
            @Override
            public void onComplete(List<Photo> list) {
              for (int i = 0; i < list.size(); i++) {
                String currentPhotoUrl = list.get(i).getUrls().getRegular();
                likedPhotos.add(currentPhotoUrl);
                writeImageUrlToLikedPhotosFile(currentPhotoUrl);
              }
            }

            @Override
            public void onError(String error) {

            }
          });
    }
    return false;
  }

  //What happens when a quote is liked
   //FIX: loading new quotes takes too much time
    // liked quotes are stored in the database
  public boolean quoteLiked(Quote quote, Boolean added) {
      SharedPreferences quotesCat = getSharedPreferences("quotesCat", Context.MODE_PRIVATE);
      SharedPreferences.Editor edit = quotesCat.edit();
      String q_cat = quote.getCategory();

//    likedQuotes.add(~String URL to where I can get it ~); ~Marius~

    likedQuotes.add(
        String.format("%s;%s;%s", quote.getQuotation(), quote.getAuthor(), quote.getCategory())); //Put the quote at likes list

    mDatabaseLQHelper.addData(quote); // put liked quote in the database

      //higher priority if quote was added by user
      if (added){
          if(!quotesCat.contains(q_cat)){
              edit.putInt(q_cat, 3);
          }
          else{
              int tmp_value = quotesCat.getInt(q_cat, 0);
              tmp_value += 2;
              edit.putInt(q_cat, tmp_value);
          }
      }
      else {
          if (!quotesCat.contains(q_cat)) {
              edit.putInt(q_cat, 0);
          } else {
              int tmp_value = quotesCat.getInt(q_cat, 0);
              tmp_value += 1;
              edit.putInt(q_cat, tmp_value);

          }
      }
      edit.commit();
    if(listViewAdapter.getCount() < 10){
      getQuotes(false);
      listViewAdapter.addAll(tmpQuoteList);
      listViewAdapter.notifyDataSetChanged();
      tmpQuoteList.clear();
    }

      return false;
  }


  //What happens when a photo is deleted
  public boolean dislikedPhoto(Photo photo) {

    dislikedPhotos.add(photo.getUrls().getRegular());

    String authorName = photo.getUser().getName();
    Toast.makeText(this, "Disliked photo by: " + authorName, Toast.LENGTH_SHORT).show();

    if (photoMap.get(actual_collection).size()
        == 1) {//If the photo was the last one of the collection the view will return to the collection grid
      photoMap.remove(actual_collection);
      if (actual_collection_position != -1) {
        collectionsList.remove(actual_collection_position);
      }
      currentViewMode = VIEW_MODE_GRIDVIEW;
      gridViewAdapter.notifyDataSetChanged();
      optionsMenu.getItem(CHANGE_VIEW_MODE).setIcon(icon_list);
      switchView();
      return true;
    }

    return false;
  }

  //What happens when a collection is deleted
  public boolean collectionDeleted(Collection collection) {

    //TODO: MARIUS What happens when the users deletes this collection?

    return false;
  }

  //What happens when a quote is deleted
  //FIX: loading new quotes takes too much time
  public boolean quoteDeleted(Quote quote) {
      SharedPreferences quotesCat = getSharedPreferences("quotesCat", Context.MODE_PRIVATE);
      SharedPreferences.Editor edit = quotesCat.edit();

      int tmp_value = quotesCat.getInt(quote.getCategory(), 0);
      tmp_value -= 1;
      edit.putInt(quote.getCategory(), tmp_value);
      if(listViewAdapter.getCount() < 10) {
          getQuotes(false);
          listViewAdapter.addAll(tmpQuoteList);
          listViewAdapter.notifyDataSetChanged();
          tmpQuoteList.clear();
      }
      edit.commit();
    return false;
  }

  //____________________________________

  //To get a random image and quote to set as a wallpaper
  public void getA_Suggested_Image() {

    //TODO: MARIUS get photo depending on user's preferences... (now only takes photos that have been marked as like)

//    if (likedPhotos.size() != 0) {
//      int pos = (int) Math.floor(Math.random() * (likedPhotos.size()));
//      //writeString(MainActivity.this, ACTUAL_IMAGE, likedPhotos.get(pos), );
//
//    }
//    if (likedQuotes.size() != 0) {
//      int pos = (int) Math.floor(Math.random() * (likedQuotes.size()));
//      String[] parts = likedQuotes.get(pos).split(";");
//      //     writeString(MainActivity.this, ACTUAL_QUOTE, parts[0], );
//      //   writeString(MainActivity.this, ACTUAL_QUOTE_AUTHOR, parts[1], );
//    }

      Quote suggestedQuote = getSuggestedQuote();

    setWallpaper();
  }
//for now returns random Quote form database
    private Quote getSuggestedQuote() {
      Quote suggestedQuote = new Quote("", "", "");
      Cursor cursor = mDatabaseLQHelper.getRandomQuote();
      while(cursor.moveToNext()) {
           suggestedQuote = new Quote(cursor.getString(1), cursor.getString(2), cursor.getString(3));

      }
//      TODO: get map of categories and sort it by value
//        boolean quote_found = false;
//        for (String cat : list_of_sorted_categories) {
//            try {
//                Cursor cursor =  mDatabaseLQHelper.getRandomQuoteFromCategory("cat");
//                while(cursor.moveToNext()) {
//                    Quote randomQuote = new Quote(cursor.getString(1),
//                            cursor.getString(2), cursor.getString(3));
//
//                }
//                quote_found = true;
//            } catch (SQLException e) {
//                // ?
//            }
//        }
//        if(!quote_found){
//            Cursor cursor = mDatabaseLQHelper.getRandomQuote();
//            while(cursor.moveToNext()) {
//                Quote randomQuote = new Quote(cursor.getString(1),
//                        cursor.getString(2), cursor.getString(3));
//
//            }
//        }

        return suggestedQuote;
    }

      //Set image as a wallpaper
  private void setWallpaper() {
    final String src = readString(this, ACTUAL_IMAGE);
    if (src != "" & src != null) {
      String quote = readString(this, ACTUAL_QUOTE);
      String quote_author = readString(this, ACTUAL_QUOTE_AUTHOR);

      if (quote == null) {
        quote = "";
        quote_author = "";
      }

      // Refactored this method by removing "context" and "src" parameters since they were obsolete (not used)
      // added resize and centerCrop to have wallpaper more or less in the size of the screen
      Display display = getWindowManager().getDefaultDisplay();
      Point size = new Point();
      display.getSize(size);
      target = srcToWallpaper(quote, quote_author);
      Picasso.get().load(src).resize(size.x, size.y).centerCrop(0).into(target);

    }
  }

  //To get a target with the download image and start the SetWallpaperQuoteTask (class) thread
  private Target srcToWallpaper(final String quote,
      final String quote_author) {

      target = new Target() {

      @Override
      public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
        SetWallpaperQuoteTask p = new SetWallpaperQuoteTask(MainActivity.this, bitmap, quote,
            quote_author, quoteDisplayInfo);

        new Thread(p).start();

        //Download image to internal memory
//        try{
//          FileOutputStream imgFos = openFileOutput("SmartWallpaper_Photo.png", Context.MODE_PRIVATE);
//          bitmap.compress(CompressFormat.PNG, 100, imgFos);
//          Log.i("Marius", "onBitmapLoaded: " + MainActivity.this.getFilesDir().getAbsolutePath());
//          // Toast.makeText(MainActivity.this, MainActivity.this.getFilesDir().getAbsolutePath(), Toast.LENGTH_SHORT).show();
//          listAllSavedFiles();
//
//          imgFos.close();
//        }catch (Exception e){
//          Log.e(TAG, "onBitmapLoaded: Failed to download image" );
//          Log.e(TAG, "onBitmapLoaded: "+ e.getStackTrace() );
//        }

      }

      @Override
      public void onBitmapFailed(Exception e, Drawable errorDrawable) {

      }

      @Override
      public void onPrepareLoad(Drawable placeHolderDrawable) {
        if (placeHolderDrawable != null) {
        }
      }
    };
      return target;
  }

  private void writeImageUrlToLikedPhotosFile(String imgUrl) {
    FileOutputStream fos;
    try {
      fos = openFileOutput(FILENAME_PHOTOS, Context.MODE_APPEND);
      String line = imgUrl + "\n";
      fos.write(line.getBytes());

      fos.close();
    } catch (FileNotFoundException e1) {
      Log.i(TAG, "WriteLikedPhoto: FileNotFoundException");
    } catch (IOException e) {
      Log.e(TAG, "WriteLikedPhoto: IOException");
    }
  }

  //Write a txt file in the internal memory with all the sources of liked photos
  private void writeLikedPhotos() {
    FileOutputStream fos;
    try {
      fos = openFileOutput(FILENAME_PHOTOS, Context.MODE_PRIVATE);
      for (int i = 0; i < likedPhotos.size(); i++) {
        String line = String.format("%s\n", likedPhotos.get(i));
        fos.write(line.getBytes());
      }
      fos.close();
    } catch (FileNotFoundException e1) {
      Log.i(TAG, "WriteLikedPhoto: FileNotFoundException");
    } catch (IOException e) {
      Log.e(TAG, "WriteLikedPhoto: IOException");
    }
  }

  //Read a txt file in the internal memory with all the sources of liked photos
  private void readLikedPhotos() {
    try {
      FileInputStream fis = openFileInput(FILENAME_PHOTOS);
      byte[] buffer = new byte[MAX_BYTES];
      int nread = fis.read(buffer);
      if (nread > 0) {
        String content = new String(buffer, 0, nread);
        String[] lines = content.split("\n");
        for (String line : lines) {
          likedPhotos.add(line);
        }
      }
      fis.close();
    } catch (FileNotFoundException e) {
      Log.i(TAG, "readLikedPhotos: FileNotFoundException");
    } catch (IOException e) {
      Log.e(TAG, "readLikedPhotos: IOException");
    }
  }

  //Write a txt file in the internal memory with all the liked quotes
  private void writeLikedQuotes() {
    FileOutputStream fos = null;
    try {
      fos = openFileOutput(FILENAME_QUOTES, Context.MODE_PRIVATE);
      for (int i = 0; i < likedQuotes.size(); i++) {
        String line = String.format("%s\n", likedQuotes.get(i));
        fos.write(line.getBytes());
      }
      fos.close();
    } catch (FileNotFoundException e1) {
      Log.i(TAG, "writeLikedQuotes: FileNotFoundException");
    } catch (IOException e) {
      Log.e(TAG, "writeLikedQuotes: IOException");
    }
  }

  //Read a txt file in the internal memory with all the liked quotes
  private void readLikedQuotes() {
    try {
      FileInputStream fis = openFileInput(FILENAME_QUOTES);
      byte[] buffer = new byte[MAX_BYTES];
      int nread = fis.read(buffer);
      if (nread > 0) {
        String content = new String(buffer, 0, nread);
        String[] lines = content.split("\n");
        for (String line : lines) {
          likedQuotes.add(line);
        }
      }
      fis.close();
    } catch (FileNotFoundException e) {
      Log.i(TAG, "readLikedQuotes: FileNotFoundException");
    } catch (IOException e) {
      Log.e(TAG, "readLikedQuotes: IOException");
    }
  }

  //_____________________________________

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {

    if (keyCode == KeyEvent.KEYCODE_BACK) {
      if (VIEW_MODE_IMAGEVIEW == currentViewMode) {
        currentViewMode = VIEW_MODE_GRIDVIEW;
        optionsMenu.getItem(CHANGE_VIEW_MODE).setIcon(icon_list);
        switchView();
        SharedPreferences sharedPreferences = getSharedPreferences("ViewMode", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("currentViewMode", currentViewMode);
        editor.commit();
      } else {
        finish();
      }

    }
    return true;
  }

  //On item long click listener
  AdapterView.OnItemLongClickListener onLongClick = new AdapterView.OnItemLongClickListener() {
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position,
        long l) {

      if (VIEW_MODE_LISTVIEW == currentViewMode) {

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);

        mBuilder.setMessage(
            R.string.display_quote); //Creates a dialog asking the user if he wants to display this as a wallpaper
        mBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            writeToPreferenceFile(MainActivity.this, PreferencesManager.STRING, ACTUAL_QUOTE,
                quoteList.get(position).getQuotation());
            writeToPreferenceFile(MainActivity.this, PreferencesManager.STRING, ACTUAL_QUOTE_AUTHOR,
                quoteList.get(position).getAuthor());

            collectQuoteDisplayInfo();
            setWallpaper();



          }
        });

        mBuilder.setNegativeButton("no", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            dialog.cancel();
          }
        });

        AlertDialog dialog = mBuilder.create();
        dialog.show();

      } else if (VIEW_MODE_GRIDVIEW == currentViewMode) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);

        mBuilder.setMessage(String.format(getString(R.string.display_collection),
            collectionsList.get(position).getTitle()));

        mBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            actual_collection = Integer.toString(collectionsList.get(position).getId());
            if (photoMap.containsKey(actual_collection)) {
              int pos = (int) Math.floor(Math.random() * (photoMap.get(actual_collection)
                  .size())); //Get a random position of an image in the collection
              PreferencesManager.writeToPreferenceFile(MainActivity.this,
                  PreferencesManager.STRING, ACTUAL_IMAGE,
                  photoMap.get(actual_collection).get(pos).getUrls().getRegular());

              setWallpaper();
            } else { //If we haven't read the photos of this collection before
              unsplash.getCollectionPhotos(actual_collection, page, perPage,
                  new Unsplash.OnPhotosLoadedListener() {

                    @Override
                    public void onComplete(List<Photo> list) {
                      int pos = (int) Math.floor(Math.random() * (list.size()));
                      photoMap.put(actual_collection, list);
                      PreferencesManager.writeToPreferenceFile(MainActivity.this,
                          PreferencesManager.STRING, ACTUAL_IMAGE,
                          list.get(pos).getUrls().getRegular());

                      setWallpaper();
                    }

                    @Override
                    public void onError(String error) {
                    }
                  });
            }
          }
        });

        mBuilder.setNegativeButton("No", new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
          }
        });

        AlertDialog dialog = mBuilder.create();
        dialog.show();

      } else {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);

        mBuilder.setMessage(R.string.display_photo);
        mBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            PreferencesManager.writeToPreferenceFile(MainActivity.this,
                PreferencesManager.STRING, ACTUAL_IMAGE,
                photoMap.get(actual_collection).get(position).getUrls().getRegular());
            setWallpaper();
          }
        });

        AlertDialog dialog = mBuilder.create();
        dialog.show();
      }
      return false;
    }
  };

    private void collectQuoteDisplayInfo() {
        LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        quoteDisplayLayout = inflater.inflate(R.layout.popup_quote_display, null);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final PopupWindow pw = new PopupWindow(quoteDisplayLayout,(int) (size.x*0.95), (int)(size.y*0.9), true);

        quoteDisplayLayout.findViewById(R.id.save_qd).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RadioGroup size, color, localization;
                RadioButton rb_size, rb_color, rb_loc;
                size = quoteDisplayLayout.findViewById(R.id.quote_size);
                color = quoteDisplayLayout.findViewById(R.id.quote_color);
                localization = quoteDisplayLayout.findViewById(R.id.quote_loc);
                int size_answer = size.getCheckedRadioButtonId();
                int color_answer = color.getCheckedRadioButtonId();
                int loc_answer = localization.getCheckedRadioButtonId();

                rb_size = quoteDisplayLayout.findViewById(size_answer);
                rb_color  = quoteDisplayLayout.findViewById(color_answer);
                rb_loc = quoteDisplayLayout.findViewById(loc_answer);
                if (quoteDisplayInfo.isEmpty()){
                    quoteDisplayInfo.add((String) rb_size.getText());
                    quoteDisplayInfo.add((String) rb_color.getText());
                    quoteDisplayInfo.add ((String) rb_loc.getText());
                } else {
                    quoteDisplayInfo.set(0, ((String) rb_size.getText()));
                    quoteDisplayInfo.set(1, ((String) rb_color.getText()));
                    quoteDisplayInfo.set(2, (String) rb_loc.getText());
                }
                pw.dismiss();
            }
        });

      quoteDisplayLayout.findViewById(R.id.withdraw_qd).setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            pw.dismiss();
          }
      });

        pw.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        pw.showAtLocation(quoteDisplayLayout, Gravity.CENTER, 0, 0);



    }

    //On item click listener
  AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {

      if (VIEW_MODE_GRIDVIEW == currentViewMode) {

        getSupportActionBar().setTitle(collectionsList.get(position).getTitle());

        currentViewMode = VIEW_MODE_IMAGEVIEW;

        gridState = listView.onSaveInstanceState();

        optionsMenu.getItem(CHANGE_VIEW_MODE).setIcon(icon_revertgrid);

        SharedPreferences sharedPreferences = getSharedPreferences("ViewMode", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("currentViewMode", currentViewMode);
        editor.commit();

        actual_collection = Integer.toString(collectionsList.get(position).getId());
        actual_collection_position = position;
        getPhotoList();

      } else if (VIEW_MODE_IMAGEVIEW == currentViewMode) {
        createPhotoDialog(position);
      }

    }

  };


  private void listAllSavedFiles() {
    File filesDir = MainActivity.this.getFilesDir();
    File[] files = filesDir.listFiles();

    for (File f :
        files) {
      Log.i(TAG,
          "listAllSavedFiles: " + f.getAbsolutePath() + ", Size:" + (f.length() / 1000) + " KB");
    }
  }

}


