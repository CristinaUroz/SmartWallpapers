package uroz.cristina.smartwallpapers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

public class DatabaseLikedQuotesHelper extends SQLiteOpenHelper {
    public static final String TAG = "DatabaseLQH";
    public static String TABLE_NAME = "liked_quotes";
    public static final String COL1 = "ID";
    public static final String COL2 = "quote_content";
    public static final String COL3 = "quote_author";
    public static final String COL4 = "quote_category";

    public DatabaseLikedQuotesHelper(Context context){
        super(context, TABLE_NAME, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "quote_content TEXT, quote_author TEXT, quote_category TEXT)";

        db.execSQL(createTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_NAME + "'");
        onCreate(db);
    }

    public boolean addData (Quote newQuote){
        long result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2,  newQuote.getQuotation());
        contentValues.put(COL3, newQuote.getAuthor());
        contentValues.put(COL4, newQuote.getCategory());


        result = db.insert(TABLE_NAME, null, contentValues);
        db.setTransactionSuccessful();
        db.endTransaction();

        if (result == - 1){
            return false;
        }else {
            return true;
        }

    }
    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;

    }

    public Cursor getRandomQuote(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY RANDOM() LIMIT 1";
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public Cursor getRandomQuoteFromCategory(String cat){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL4 + " = '" + cat + "' ORDER BY RANDOM() LIMIT 1";
        Cursor data = db.rawQuery(query, null);
        return data;
    }

}
