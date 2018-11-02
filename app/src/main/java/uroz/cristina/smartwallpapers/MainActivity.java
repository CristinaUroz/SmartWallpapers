package uroz.cristina.smartwallpapers;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.SyncStateContract;
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
import com.kc.unsplash.api.Order;
import com.kc.unsplash.models.Photo;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private ViewStub stubGrid;
    private ViewStub stubList;
    private ListView listView;
    private GridView gridView;
    private ListViewAdapter listViewAdapter;
    private GridViewAdapter gridViewAdapter;
    private ImageViewAdapter imageViewAdapter;
    private List<Quote> quoteList;
    private List<Image> imageList;
    private List<Category> categoryList;
    private int currentViewMode = 0;
    private Menu optionsMenu;
    private Parcelable listState;
    private Parcelable gridState;
    private static final int MY_PERMISSIONS_REQUEST_SET_WALLPAPER = 0;

    //unplash
    private final String CLIENT_ID = "611b6a517cafa69285e513282257c6516061c205974a8a39a641cdb408f4d4ca";
    private List<Photo> photoList = new ArrayList<>();
    int f = 1;
    int l = 10;
    Order order = Order.POPULAR;
    String imageDir = "Wallpaper";


    Unsplash unsplash = new Unsplash(CLIENT_ID);


    static final int VIEW_MODE_LISTVIEW = 0;
    static final int VIEW_MODE_GRIDVIEW = 1;
    static final int VIEW_MODE_IMAGEVIEW = 2;

    static final int icon_grid=android.R.drawable.ic_dialog_dialer;
    static final int icon_list=android.R.drawable.ic_menu_sort_by_size;
    static final int icon_revertgrid=android.R.drawable.ic_menu_revert;

    //Change view menu item id
    static final int CHANGE_VIEW_MODE = 2;

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

        //get list of product;
        getQuoteList();
        getCategoryList();

        //Get current view mode in share reference
        SharedPreferences sharedPreferences = getSharedPreferences("viewMode", MODE_PRIVATE);
        currentViewMode = sharedPreferences.getInt("currentViewMode", VIEW_MODE_GRIDVIEW); //Default

        listView.setOnItemClickListener(onItemClick);
        listView.setOnItemLongClickListener(onLongClick);
        gridView.setOnItemClickListener(onItemClick);
        gridView.setOnItemLongClickListener(onLongClick);
        switchView();
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
            gridViewAdapter = new GridViewAdapter(this, R.layout.grid_item, categoryList);
            gridView.setAdapter(gridViewAdapter);
            gridView.onRestoreInstanceState(gridState);
        }
        else {
            imageViewAdapter = new ImageViewAdapter(this, R.layout.grid_item, imageList);
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

    private List<Image> getImageList(String category_title) {

        unsplash.getPhotos(f, l, order, new Unsplash.OnPhotosLoadedListener() {
            @Override
            public void onComplete(List<Photo> photos) {
                photoList = photos;
            }

            @Override
            public void onError(String error) {
            }
        });


        imageList = new ArrayList<>();
        for (int i=0;  i<photoList.size(); i++) {
            Photo photo = photoList.get(i);
            imageList.add(new Image(photo.getUrls().getRegular(), photo.getId(), photo.getCreatedAt(), false, false));
            //imageList.add(new Image(R.drawable.wallpaper, "Image "+ Integer.toString(i), "Descriptor", false, false, false));
        }
        return imageList;
    }

    private List<Category> getCategoryList() {

        categoryList = new ArrayList<>();
        for (int i=0;  i<15; i++) {
            categoryList.add(new Category(android.R.drawable.ic_menu_gallery, " Category "+ Integer.toString(i), false, false));
            //categoryList.add(new Category(R.drawable.wallpaper, " Category "+ Integer.toString(i), false, false, false));
        }

        return categoryList;
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
                //getImageList(categoryList.get(position).getTitle());
                getSupportActionBar().setTitle(categoryList.get(position).getTitle());
                getImageList(categoryList.get(position).getTitle());
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
                createDialog(position);
            }
            return false;
        }
    };

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

        Picasso.get().load(imageList.get(position).getSrc()).into(imageView);
        Title.setText(imageList.get(position).getTitle());
        Autor.setText(imageList.get(position).getAutor());

        final Image image=imageList.get(position);


        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image.setLiked();
                imageList.remove(image);
                imageViewAdapter.notifyDataSetChanged();
                dialog.cancel();
                int new_position=position+1;
                if (new_position>=imageList.size()){ new_position=0;}
                createDialog(new_position);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image.setDeleted();
                imageList.remove(image);
                imageViewAdapter.notifyDataSetChanged();
                dialog.cancel();
                int new_position=position+1;
                if (new_position>=imageList.size()){ new_position=0;}
                createDialog(new_position);
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                int new_position=position-1;
                if (new_position<0){ new_position=imageList.size()-1;}
                createDialog(new_position);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                int new_position=position+1;
                if (new_position>=imageList.size()){ new_position=0;}
                createDialog(new_position);
            }
        });

        dialog.show();
    }

    AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {

            if(VIEW_MODE_LISTVIEW == currentViewMode){

                lastTouchTime = currentTouchTime;
                currentTouchTime = System.currentTimeMillis();

                if (currentTouchTime - lastTouchTime < 250) {
                    //Toast.makeText(getApplicationContext(), quoteList.get(position).getTitle() + " star", Toast.LENGTH_SHORT).show();
                    lastTouchTime = 0;
                    currentTouchTime = 0;

                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);

                    mBuilder.setMessage("Do you want to display this quote as wallpaper?");
                    mBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //setWallpaper("nothing");
                            Toast.makeText(getApplicationContext(), "This option is temporaly unviable", Toast.LENGTH_SHORT).show();
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

                if (currentTouchTime - lastTouchTime < 250) {
                    //Toast.makeText(getApplicationContext(), quoteList.get(position).getTitle() + " star", Toast.LENGTH_SHORT).show();
                    lastTouchTime = 0;
                    currentTouchTime = 0;

                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);

                    mBuilder.setMessage("Do you want to display a " +categoryList.get(position).getTitle()+" image as wallpaper?");
                    mBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //setWallpaper("nothing");
                            Toast.makeText(getApplicationContext(), "This option is temporaly unviable", Toast.LENGTH_SHORT).show();
                        }
                    });

                    categoryList.get(position).setLiked();
                    gridViewAdapter.notifyDataSetChanged();

                    AlertDialog dialog = mBuilder.create();
                    dialog.show();

                }

            }
            else {

                lastTouchTime = currentTouchTime;
                currentTouchTime = System.currentTimeMillis();

                if (currentTouchTime - lastTouchTime < 250) {
                    //Toast.makeText(getApplicationContext(), quoteList.get(position).getTitle() + " star", Toast.LENGTH_SHORT).show();
                    lastTouchTime = 0;
                    currentTouchTime = 0;

                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);

                    mBuilder.setMessage("Do you want to display this image as wallpaper?");
                    mBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            setWallpaper(imageList.get(position).getSrc());
                        }
                    });

                    imageList.get(position).setLiked();
                    imageViewAdapter.notifyDataSetChanged();

                    AlertDialog dialog = mBuilder.create();
                    dialog.show();

                }

            }

        }
    };

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

                mBuilder.setMessage("Double click to display a quote/image/category image as a wallpaper. " +
                        "\n\nLong click to enter in a category " +
                        "\n\nLong click to view an image " +
                        "\n\nClick on the top right icon to change view to quotes/categories " +
                        "\n\nClick on the top left icon to change wallpaper");

                AlertDialog dialog = mBuilder.create();
                dialog.show();
                break;
            case R.id.changeWallpaper:
                //setWallpaper("nothing");
                Toast.makeText(getApplicationContext(), "This option is temporaly unviable", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setWallpaper(String src) {
        Picasso.get().load(src).into(srcToWallpaper(this, src));
    }

    private Target srcToWallpaper(Context context, String src) {
        Log.d("picassoImageTarget", " picassoImageTarget");
        //ContextWrapper cw = new ContextWrapper(context);
        //final File directory = cw.getDir(imageDir, Context.MODE_PRIVATE); // path to /data/data/yourapp/app_imageDir
        writeString(context, "ACTUAL_IMAGE", src);

        return new Target() {

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                setWallpaper p = new setWallpaper(MainActivity.this,bitmap);
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

}


