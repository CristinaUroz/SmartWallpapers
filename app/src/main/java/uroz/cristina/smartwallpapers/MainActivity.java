package uroz.cristina.smartwallpapers;

import android.Manifest;
import android.app.AlertDialog;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.DialogInterface;
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
import android.widget.TextView;
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

    static final int icon_like_off=android.R.drawable.presence_invisible;
    static final int icon_like_on=android.R.drawable.presence_online;
    static final int icon_favorite_off=android.R.drawable.star_big_on;
    static final int icon_favorite_on=android.R.drawable.star_big_off;
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
            quoteList.add(new Quote(" Title "+ Integer.toString(i), "Descriptor", false, false,false));
        }

        return quoteList;
    }

    private List<Image> getImageList(String category_title) {
        imageList = new ArrayList<>();
        for (int i=0;  i<15; i++) {
            imageList.add(new Image(android.R.drawable.ic_menu_gallery, "Image "+ Integer.toString(i), "Descriptor", false, false, false));
            //imageList.add(new Image(R.drawable.wallpaper, "Image "+ Integer.toString(i), "Descriptor", false, false, false));
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

    AdapterView.OnItemLongClickListener onLongClick = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {

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
        final ImageView favorite =  (ImageView) mView.findViewById(R.id.favoriteViewD);
        ImageView delete =  (ImageView) mView.findViewById(R.id.deleteViewD);
        ImageView previous =  (ImageView) mView.findViewById(R.id.previousViewD);
        ImageView next =  (ImageView) mView.findViewById(R.id.nextViewD);
        TextView Title =  (TextView) mView.findViewById(R.id.txtTitleD);
        TextView Autor =  (TextView) mView.findViewById(R.id.txtAutorD);

        imageView.setImageResource(imageList.get(position).getImageId());
        Title.setText(imageList.get(position).getTitle());
        Autor.setText(imageList.get(position).getAutor());

        final Image image=imageList.get(position);

        if (image.isLiked()){
            like.setImageResource(icon_like_on);
        }
        else {like.setImageResource(icon_like_off);}

        if (image.isFavorite()){
            favorite.setImageResource(icon_favorite_off);
        }
        else {favorite.setImageResource(icon_favorite_on);}

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image.toggleLiked();
                imageViewAdapter.notifyDataSetChanged();
                if (image.isLiked()){
                    like.setImageResource(icon_like_on);
                }
                else {like.setImageResource(icon_like_off);}
            }
        });

        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image.toggleFavorite();
                imageViewAdapter.notifyDataSetChanged();
                if (image.isFavorite()){
                    favorite.setImageResource(icon_favorite_off);
                }
                else {favorite.setImageResource(icon_favorite_on);}
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image.setDeleted(true);
                imageList.remove(image);
                imageViewAdapter.notifyDataSetChanged();
                dialog.cancel();
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
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

            if(VIEW_MODE_LISTVIEW == currentViewMode){
                quoteList.get(position).toggleLiked();
                listViewAdapter.notifyDataSetChanged();

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
                            setWallpaper();
                        }
                    });

                    AlertDialog dialog = mBuilder.create();
                    dialog.show();

                    if (!quoteList.get(position).isLiked()){
                        quoteList.get(position).toggleLiked();
                        listViewAdapter.notifyDataSetChanged();
                    }
                }
            }
            else if (VIEW_MODE_GRIDVIEW == currentViewMode){
                categoryList.get(position).toggleLiked();
                gridViewAdapter.notifyDataSetChanged();

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
                            setWallpaper();
                        }
                    });

                    AlertDialog dialog = mBuilder.create();
                    dialog.show();

                    if (!categoryList.get(position).isLiked()){
                        categoryList.get(position).toggleLiked();
                        gridViewAdapter.notifyDataSetChanged();
                    }
                }

            }
            else {
                imageList.get(position).toggleLiked();
                imageViewAdapter.notifyDataSetChanged();

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
                            setWallpaper();
                        }
                    });

                    AlertDialog dialog = mBuilder.create();
                    dialog.show();

                    if (!imageList.get(position).isLiked()){
                        imageList.get(position).toggleLiked();
                        imageViewAdapter.notifyDataSetChanged();
                    }
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
                setWallpaper();
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


