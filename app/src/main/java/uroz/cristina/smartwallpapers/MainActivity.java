package uroz.cristina.smartwallpapers;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kc.unsplash.Unsplash;
import com.kc.unsplash.models.Photo;
import com.kc.unsplash.models.Collection;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class MainActivity extends AppCompatActivity {

    private ViewStub stubGrid;
    private ViewStub stubList;
    private ListView listView;
    private GridView gridView;
    private ListViewAdapter listViewAdapter;
    private GridViewAdapter gridViewAdapter;
    private ImageViewAdapter imageViewAdapter;
    private List<Quote> quoteList;
    private Map<String,List<Photo>> photoMap = new TreeMap<>();
    private List<Collection> collectionsList = new ArrayList<>();
    private String actual_collection;
    private int currentViewMode = 0;
    private Menu optionsMenu;
    private Parcelable listState;
    private Parcelable gridState;
    private static final int MY_PERMISSIONS_REQUEST_SET_WALLPAPER = 0;

    //unplash
    private final String CLIENT_ID = "4254aee191dd7d4dec3ff36c75a61ffb50cdcd320d1c14942b1dec21f67159b9"; //Cristina's sesion Id

    private int page = 1;
    private int perPage = 10;

    private Unsplash unsplash = new Unsplash(CLIENT_ID);

    static final int VIEW_MODE_LISTVIEW = 0;
    static final int VIEW_MODE_GRIDVIEW = 1;
    static final int VIEW_MODE_IMAGEVIEW = 2;

    static final int icon_grid=android.R.drawable.ic_dialog_dialer;
    static final int icon_list=android.R.drawable.ic_menu_sort_by_size;
    static final int icon_revertgrid=android.R.drawable.ic_menu_revert;

    //Change view menu item id
    static final int CHANGE_VIEW_MODE = 2;

    static final String ACTUAL_IMAGE=  "ACTUAL_IMAGE";
    static final String ACTUAL_QUOTE = "ACTUAL_QUOTE";
    static final String ACTUAL_QUOTE_AUTOR = "ACTUAL_QUOTE_AUTOR";

    private long lastTouchTime = 0;
    private long currentTouchTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request for permissions. Is it necessary?
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SET_WALLPAPER) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SET_WALLPAPER)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SET_WALLPAPER}, MY_PERMISSIONS_REQUEST_SET_WALLPAPER);
            }
        }

        stubList = (ViewStub) findViewById(R.id.stub_list);
        stubGrid = (ViewStub) findViewById(R.id.stub_grid);

        //Inflate ViewStub before get view

        stubList.inflate();
        stubGrid.inflate();

        listView = findViewById(R.id.mylistview);
        gridView = findViewById(R.id.mygridview);

        //Get current view mode in share reference
        SharedPreferences sharedPreferences = getSharedPreferences("viewMode", MODE_PRIVATE);
        currentViewMode = sharedPreferences.getInt("currentViewMode", VIEW_MODE_GRIDVIEW); //Default

        listView.setOnItemClickListener(onItemClick);
        listView.setOnItemLongClickListener(onLongClick);
        gridView.setOnItemClickListener(onItemClick);
        gridView.setOnItemLongClickListener(onLongClick);

        getQuoteList();
        getCollectionList();
    }

    private void switchView() {

        if(VIEW_MODE_LISTVIEW == currentViewMode){
            getSupportActionBar().setTitle("Quotes");
            stubList.setVisibility(View.VISIBLE);
            stubGrid.setVisibility(View.GONE);
            listState = listView.onSaveInstanceState();
        }
        else if(VIEW_MODE_IMAGEVIEW == currentViewMode){
            getSupportActionBar().setTitle("Categories");
            stubList.setVisibility(View.VISIBLE);
            stubGrid.setVisibility(View.GONE);
        }
        else {
            getSupportActionBar().setTitle("Categories");
            stubList.setVisibility(View.GONE);
            stubGrid.setVisibility(View.VISIBLE);
            gridState = gridView.onSaveInstanceState();
        }
        setAdapters();
    }

    private void setAdapters() {
        if(VIEW_MODE_LISTVIEW == currentViewMode){
            listViewAdapter = new ListViewAdapter(this, R.layout.list_item, quoteList);
            listView.setAdapter(listViewAdapter);
            listView.onRestoreInstanceState(listState);
        }
        else if (VIEW_MODE_GRIDVIEW == currentViewMode)
       {
            gridViewAdapter = new GridViewAdapter(this, R.layout.grid_item, collectionsList);
            gridView.setAdapter(gridViewAdapter);
            gridView.onRestoreInstanceState(gridState);
        }
        else {
            imageViewAdapter = new ImageViewAdapter(this, R.layout.grid_item, photoMap.get(actual_collection));
            gridView.setAdapter(imageViewAdapter);
        }
    }

    private List<Quote> getQuoteList() {

        quoteList = new ArrayList<>();
        for (int i=0;  i<15; i++) {
            quoteList.add(new Quote(" Title "+ Integer.toString(i), "Descriptor", false, false));
        }

        return quoteList;
    }

    private void getPhotoList() {

        if (photoMap.containsKey(actual_collection)){

            currentViewMode = VIEW_MODE_IMAGEVIEW;

            gridState = listView.onSaveInstanceState();

            optionsMenu.getItem(CHANGE_VIEW_MODE).setIcon(icon_revertgrid);

            SharedPreferences sharedPreferences = getSharedPreferences("ViewMode", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("currentViewMode",currentViewMode);
            editor.commit();

            setAdapters();
        }

        else {
            unsplash.getCollectionPhotos(actual_collection, page, perPage, new Unsplash.OnPhotosLoadedListener() {

                @Override
                public void onComplete(List<Photo> list) {

                    photoMap.put(actual_collection, list);
                    currentViewMode = VIEW_MODE_IMAGEVIEW;

                    gridState = listView.onSaveInstanceState();

                    optionsMenu.getItem(CHANGE_VIEW_MODE).setIcon(icon_revertgrid);

                    SharedPreferences sharedPreferences = getSharedPreferences("ViewMode", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("currentViewMode", currentViewMode);
                    editor.commit();

                    setAdapters();
                }

                @Override
                public void onError(String error) {

                }
            });
        }
    }

    private void getCollectionList() {

        unsplash.getCollections(page,perPage,new Unsplash.OnCollectionsLoadedListener(){
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        optionsMenu=menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.change_view:
                if (VIEW_MODE_LISTVIEW == currentViewMode){
                    currentViewMode = VIEW_MODE_GRIDVIEW;
                    item.setIcon(icon_list);
                }
                else if (VIEW_MODE_IMAGEVIEW == currentViewMode){
                    currentViewMode = VIEW_MODE_GRIDVIEW;
                    item.setIcon(icon_list);
                }
                else {
                    currentViewMode = VIEW_MODE_LISTVIEW;
                    item.setIcon(icon_grid);
                }

                switchView();

                SharedPreferences sharedPreferences = getSharedPreferences("ViewMode", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("currentViewMode",currentViewMode);
                editor.commit();
                break;
            case R.id.info:
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);

                mBuilder.setMessage("Double click to display a quote/photo/collection photo as a wallpaper. " +
                        "\n\nLong click to enter in a collection " +
                        "\n\nLong click to view an photo " +
                        "\n\nClick on the top right icon to change view to quotes/categories " +
                        "\n\nClick on the top left icon to change wallpaper");

                AlertDialog dialog = mBuilder.create();
                dialog.show();
                break;
            case R.id.changeWallpaper:
                //setWallpaper();
                Toast.makeText(getApplicationContext(), "This option is temporaly unviable", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setWallpaper() {
        String src = readString(this, ACTUAL_IMAGE);
        String quote = readString(this, ACTUAL_QUOTE);
        String quote_autor = readString(this, ACTUAL_QUOTE_AUTOR);
        Picasso.get().load(src).into(srcToWallpaper(this, src,quote,quote_autor));
    }

    private Target srcToWallpaper(Context context, String src, final String quote, final String quote_autor) {
        Log.d("picassoImageTarget", " picassoImageTarget");
        return new Target() {

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                setWallpaper p = new setWallpaper(MainActivity.this,bitmap, quote, quote_autor);
                new Thread(p).start();
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (placeHolderDrawable != null) {}
            }
        };
    }

     //????????????????????????????
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SET_WALLPAPER: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("Cristina", "Set wallpaper permission accepted");
                } else {
                    Log.i("Cristina", "Set wallpaper permission denied");
                }
                return;
            }
        }
    }

    public static void writeString(Context context, final String KEY, String property) {
        SharedPreferences.Editor editor = context.getSharedPreferences("WALLPAPER", context.MODE_PRIVATE).edit();
        editor.putString(KEY, property);
        editor.commit();
    }

    public static String readString(Context context, final String KEY) {
        return context.getSharedPreferences("WALLPAPER", context.MODE_PRIVATE).getString(KEY, null);
    }

    private void createDialog(final int position){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.image_dialog,null);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();

        ImageView imageView =  (ImageView) mView.findViewById(R.id.ImageViewD);
        final ImageView like =  (ImageView) mView.findViewById(R.id.likeViewD);
        ImageView delete =  (ImageView) mView.findViewById(R.id.deleteViewD);
        ImageView previous =  (ImageView) mView.findViewById(R.id.previousViewD);
        ImageView next =  (ImageView) mView.findViewById(R.id.nextViewD);
        TextView Title =  (TextView) mView.findViewById(R.id.txtTitleD);
        TextView Autor =  (TextView) mView.findViewById(R.id.txtAutorD);

        Picasso.get().load(photoMap.get(actual_collection).get(position).getUrls().getRegular()).into(imageView);
        Title.setText(photoMap.get(actual_collection).get(position).getUser().getName());
        Autor.setText("");

        final Photo photo=photoMap.get(actual_collection).get(position);


        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoMap.get(actual_collection).remove(photo);
                imageViewAdapter.notifyDataSetChanged();
                dialog.cancel();
                int new_position=position+1;
                if (new_position>=photoMap.get(actual_collection).size()){ new_position=0;}
                createDialog(new_position);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoMap.get(actual_collection).remove(photo);
                imageViewAdapter.notifyDataSetChanged();
                dialog.cancel();
                int new_position=position+1;
                if (new_position>=photoMap.get(actual_collection).size()){ new_position=0;}
                createDialog(new_position);
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                int new_position=position-1;
                if (new_position<0){ new_position=photoMap.get(actual_collection).size()-1;}
                createDialog(new_position);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                int new_position=position+1;
                if (new_position>=photoMap.get(actual_collection).size()){ new_position=0;}
                createDialog(new_position);
            }
        });

        dialog.show();
    }

    AdapterView.OnItemLongClickListener onLongClick = new AdapterView.OnItemLongClickListener()
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l)
        {

            if(VIEW_MODE_LISTVIEW == currentViewMode){

            }
            else if (VIEW_MODE_GRIDVIEW == currentViewMode)
            {
                getSupportActionBar().setTitle(collectionsList.get(position).getTitle());
                actual_collection=Integer.toString(collectionsList.get(position).getId());
                getPhotoList();
            }
            else {
                createDialog(position);
            }
            return false;
        }
    };

    AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {

            if(VIEW_MODE_LISTVIEW == currentViewMode){
                lastTouchTime = currentTouchTime;
                currentTouchTime = System.currentTimeMillis();

                if (currentTouchTime - lastTouchTime < 500) {

                    lastTouchTime = 0;
                    currentTouchTime = 0;

                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);

                    mBuilder.setMessage("Do you want to display this quote as wallpaper?");
                    mBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            writeString(MainActivity.this, ACTUAL_QUOTE,quoteList.get(position).getTitle());
                            writeString(MainActivity.this, ACTUAL_QUOTE_AUTOR,quoteList.get(position).getAutor());
                            setWallpaper();
                        }
                    });

                    quoteList.get(position).setLiked();
                    listViewAdapter.notifyDataSetChanged();

                    AlertDialog dialog = mBuilder.create();
                    dialog.show();

                }
            }
            else if (VIEW_MODE_GRIDVIEW == currentViewMode){


                lastTouchTime = currentTouchTime;
                currentTouchTime = System.currentTimeMillis();

                if (currentTouchTime - lastTouchTime < 500) {
                    lastTouchTime = 0;
                    currentTouchTime = 0;

                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);

                    mBuilder.setMessage("Do you want to display a " +collectionsList.get(position).getTitle()+" photo as wallpaper?");
                    mBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            actual_collection= Integer.toString(collectionsList.get(position).getId());
                            if (photoMap.containsKey(actual_collection)){
                                int pos = (int) Math.floor(Math.random() * (photoMap.get(actual_collection).size()));
                                writeString(MainActivity.this, ACTUAL_IMAGE,photoMap.get(actual_collection).get(pos).getUrls().getRegular());
                                setWallpaper();
                            }

                            else {
                                unsplash.getCollectionPhotos(actual_collection, page, perPage, new Unsplash.OnPhotosLoadedListener() {

                                    @Override
                                    public void onComplete(List<Photo> list) {
                                        int pos = (int) Math.floor(Math.random() * (list.size()));
                                        photoMap.put(actual_collection,list);
                                        writeString(MainActivity.this, ACTUAL_IMAGE,list.get(pos).getUrls().getRegular());
                                        setWallpaper();
                                    }

                                    @Override
                                    public void onError(String error) {

                                    }
                                });

                            }
                        }
                    });

                    AlertDialog dialog = mBuilder.create();
                    dialog.show();
                }
            }
            else {

                lastTouchTime = currentTouchTime;
                currentTouchTime = System.currentTimeMillis();

                if (currentTouchTime - lastTouchTime < 500) {
                    //Toast.makeText(getApplicationContext(), quoteList.get(position).getTitle() + " star", Toast.LENGTH_SHORT).show();
                    lastTouchTime = 0;
                    currentTouchTime = 0;

                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);

                    mBuilder.setMessage("Do you want to display this photo as wallpaper?");
                    mBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            writeString(MainActivity.this, ACTUAL_IMAGE, photoMap.get(actual_collection).get(position).getUrls().getRegular());
                            setWallpaper();
                        }
                    });

                    AlertDialog dialog = mBuilder.create();
                    dialog.show();

                }

            }

        }
    };

}


