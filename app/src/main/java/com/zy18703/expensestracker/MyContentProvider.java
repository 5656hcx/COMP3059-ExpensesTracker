package com.zy18703.expensestracker;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;

public class MyContentProvider extends ContentProvider {

    private static final String TABLE_NAME = "expense";
    private DBHelper dbHelper = null;
    private static final UriMatcher uriMatcher;

    static {
        // strictly limited uriMatcher, will not accept URIs with default last uri segment
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MyContract.AUTHORITY, "expense/#", 1);
        uriMatcher.addURI(MyContract.AUTHORITY, "expense", 2);
    }

    @Override
    public boolean onCreate() {
        // initialize a DBHelper object for later use
        dbHelper = new DBHelper(getContext(), 1);
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String order) {
        // query a cursor points to all required rows from table expense
        // if uri contains id at its end, only query the certain row
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        switch (uriMatcher.match(uri)) {
            case 1:
                selection = MyContract._ID + "=?";
                selectionArgs = new String[] { uri.getLastPathSegment() };
            case 2:
                return db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, order);
            default:
                return null;
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        // insert a new row to table expense
        // if the last uri segment is does not match than assign a new uri to it
        // return uri in which the last segment is the id of inserted row or -1
        switch (uriMatcher.match(uri)) {
            case 1:
                uri = MyContract.URI_EXPENSE;
            case 2:
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                long rowId = db.insert(TABLE_NAME, null, values);
                db.close();
                Uri rowUri = ContentUris.withAppendedId(uri, rowId);
                Context context = getContext();
                if (context != null)
                    context.getContentResolver().notifyChange(rowUri, null);
                return rowUri;
            default:
                return ContentUris.withAppendedId(MyContract.URI_EXPENSE, -1);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // delete one or more rows from expense
        // return number of rows deleted
        int rowDeleted = 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case 1:
                selection = MyContract._ID + "=?";
                selectionArgs = new String[] { uri.getLastPathSegment() };
            case 2:
                rowDeleted = db.delete(TABLE_NAME, selection, selectionArgs);
                break;
        }
        db.close();
        return rowDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // update one or more rows from expense
        // return number of rows updated
        int rowUpdated = 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case 1:
                selection = MyContract._ID + "=?";
                selectionArgs = new String[] { uri.getLastPathSegment() };
            case 2:
                rowUpdated = db.update(TABLE_NAME, values, selection, selectionArgs);
        }
        db.close();
        return rowUpdated;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        // data types that provider can provide while receiving specific uri request
        if (uri.getLastPathSegment() == null)
            return MyContract.CONTENT_TYPE_MULTIPLE;
        else
            return MyContract.CONTENT_TYPE_SINGLE;
    }

    private static final class DBHelper extends SQLiteOpenHelper {

        private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        private static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
                " (_id INTEGER PRIMARY KEY AUTOINCREMENT, category TEXT NOT NULL, " +
                "amount INTEGER NOT NULL, date INTEGER NOT NULL);";

        DBHelper(Context context, int version) {
            super(context, "database", null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DROP_TABLE);
            onCreate(db);
        }
    }
}
