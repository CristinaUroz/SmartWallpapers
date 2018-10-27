package uroz.cristina.smartwallpapers;

import android.Manifest;
import android.app.AlertDialog;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

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

    static final int VIEW_MODE_LISTVIEW = 0;
    static final int VIEW_MODE_GRIDVIEW = 1;
    static final int VIEW_MODE_IMAGEVIEW = 2;

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
        currentViewMode = sharedPreferences.getInt("currentViewMode", VIEW_MODE_LISTVIEW); //Default

        listView.setOnItemClickListener(onItemClick);
        listView.setOnItemLongClickListener(onLongClick);
        gridView.setOnItemClickListener(onItemClick);
        gridView.setOnItemLongClickListener(onLongClick);
        switchView();
    }

    private void switchView() {

        if(VIEW_MODE_LISTVIEW == currentViewMode | VIEW_MODE_IMAGEVIEW == currentViewMode){
            stubList.setVisibility(View.VISIBLE);
            stubGrid.setVisibility(View.GONE);
            listState = listView.onSaveInstanceState();
        }
        else if(VIEW_MODE_IMAGEVIEW == currentViewMode){
            stubList.setVisibility(View.VISIBLE);
            stubGrid.setVisibility(View.GONE);
        }
        else {
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
            quoteList.add(new Quote(" Title "+ Integer.toString(i), "Descriptor", false, false,false));
        }

        return quoteList;
    }

    //private List<Image> getImageList(String category_title) {
    private List<Image> getImageList() {

        imageList = new ArrayList<>();
        for (int i=0;  i<15; i++) {
            imageList.add(new Image(android.R.drawable.ic_menu_gallery, "Image "+ Integer.toString(i), "Descriptor", false, false, false));
        }
        return imageList;
    }

    private List<Category> getCategoryList() {

        categoryList = new ArrayList<>();
        for (int i=0;  i<15; i++) {
            categoryList.add(new Category(android.R.drawable.ic_menu_gallery, " Category "+ Integer.toString(i), false,false, false));
            //categoryList.add(new Category(R.drawable.wallpaper, " Category "+ Integer.toString(i), false, false, false));
        }

        return categoryList;
    }

    AdapterView.OnItemLongClickListener onLongClick = new AdapterView.OnItemLongClickListener(){
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {

            if(VIEW_MODE_LISTVIEW == currentViewMode){

            }
            else if (VIEW_MODE_GRIDVIEW == currentViewMode)
            {
                //getImageList(categoryList.get(position).getTitle());
                Log.i("Cristina", "Long Click category");
                getImageList();
                currentViewMode = VIEW_MODE_IMAGEVIEW;

                gridState = listView.onSaveInstanceState();

                optionsMenu.getItem(0).setIcon(android.R.drawable.ic_menu_revert);

                SharedPreferences sharedPreferences = getSharedPreferences("ViewMode", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("currentViewMode",currentViewMode);
                editor.commit();

                setAdapters();

            }
            else {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);

                View mView = getLayoutInflater().inflate(R.layout.image_dialog,null);

                ImageView imageView5 =  (ImageView) mView.findViewById(R.id.image_id);
                int imageId = imageList.get(position).getImageId();
                imageView5.setImageResource(imageId);

                mBuilder.setView(mView);
                AlertDialog dialog = mBuilder.create();
                dialog.show();

            }
            return false;
        }
    };

    AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

            if(VIEW_MODE_LISTVIEW == currentViewMode){
                quoteList.get(position).toggleLiked();
                listViewAdapter.notifyDataSetChanged();

            }
            else if (VIEW_MODE_GRIDVIEW == currentViewMode){
                categoryList.get(position).toggleLiked();
                gridViewAdapter.notifyDataSetChanged();
            }
            else {
                imageList.get(position).toggleLiked();
                imageViewAdapter.notifyDataSetChanged();
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
                    item.setIcon(android.R.drawable.ic_menu_sort_by_size);
                }
                else if (VIEW_MODE_IMAGEVIEW == currentViewMode){
                    currentViewMode = VIEW_MODE_GRIDVIEW;
                    item.setIcon(android.R.drawable.ic_menu_sort_by_size);
                }
                else {
                    currentViewMode = VIEW_MODE_LISTVIEW;
                    item.setIcon(android.R.drawable.ic_dialog_dialer);
                }

                switchView();

                SharedPreferences sharedPreferences = getSharedPreferences("ViewMode", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("currentViewMode",currentViewMode);
                editor.commit();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setWallpaper(){

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wallpaper);
        WallpaperManager manager = WallpaperManager.getInstance(getApplicationContext());

        try{
            manager.setBitmap(bitmap);
            Toast.makeText(this, "Wallpaper set!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

}


