package com.zy18703.expensestracker;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TrackerActivity extends AppCompatActivity {

    private ListView listView;

    private boolean searchIconify = true;
    private String searchQuery = null;

    private static final String ORDER_DEFAULT = MyContract._ID + " DESC";
    private String myOrder = null;
    private String mySelection = null;
    private String[] mySelectionArgs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        // init the listView with default order (_ID, descending)
        myOrder = ORDER_DEFAULT;
        listView = findViewById(R.id.listView);
        updateList();

        // setup the radio group, user can select order to view expenses
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_date:
                        myOrder = MyContract.DATE + ", " + ORDER_DEFAULT;
                        break;
                    case R.id.radio_category:
                        myOrder = MyContract.CATEGORY + ", " + ORDER_DEFAULT;
                        break;
                    case R.id.radio_amount:
                        myOrder = MyContract.AMOUNT + ", " + ORDER_DEFAULT;
                        break;
                }
                updateList();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // save current activity state
        outState.putString("order", myOrder);
        outState.putString("selection", mySelection);
        outState.putStringArray("selectionArgs", mySelectionArgs);
        outState.putBoolean("iconify", searchIconify);
        outState.putString("query", searchQuery);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // restore saved activity state
        myOrder = savedInstanceState.getString("order");
        mySelection = savedInstanceState.getString("selection");
        mySelectionArgs = savedInstanceState.getStringArray("selectionArgs");
        searchIconify = savedInstanceState.getBoolean("iconify");
        searchQuery = savedInstanceState.getString("query");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // select to go back to previous activity
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // primarily perform search function on toolbar
        getMenuInflater().inflate(R.menu.tracker_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);

        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            // when user enter the search view, save current opening state
            @Override
            public void onClick(View v) {
                searchIconify = false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            // when user close the search view, update opening state and clear selection
            @Override
            public boolean onClose() {
                searchIconify = true;
                searchQuery = null;
                mySelection = null;
                mySelectionArgs = null;
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // when user click search button, clear focus to display search result
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }
            // when user enter new keywords, query content provider and update listView
            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery = newText;
                mySelection = MyContract.CATEGORY + " LIKE ? OR " + MyContract.AMOUNT + " LIKE ?";
                mySelectionArgs = new String[] { newText + "%", newText + "%" };
                updateList();
                return true;
            }
        });

        searchView.setQueryHint(getString(R.string.hint_search));
        searchView.setIconified(searchIconify);
        searchView.setQuery(searchQuery, true);
        return true;
    }

    private void updateList() {
        // update listView according to different parameters
        Cursor cursor = getContentResolver().
                query(MyContract.URI_EXPENSE, null, mySelection, mySelectionArgs, myOrder);
        listView.setAdapter(new MyCursorAdapter(this, R.layout.expense_list_item, cursor,
                new String[] { MyContract.CATEGORY, MyContract.AMOUNT, MyContract.DATE },
                new int[] { R.id.list_item_category, R.id.list_item_amount, R.id.list_item_date }));
    }

    private class MyCursorAdapter extends SimpleCursorAdapter {
        // extend SimplerCursorAdapter to customize the way cursor data displayed in every view
        // also add a delete action to every list item
        private Calendar calendar;

        public MyCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to, 0);
            this.calendar = Calendar.getInstance();
        }

        @Override
        public void bindView(View view, final Context context, final Cursor cursor) {
            super.bindView(view, context, cursor);

            // milliseconds to formatted date text
            calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(MyContract.DATE)));
            TextView textView = view.findViewById(R.id.list_item_date);
            textView.setText(new SimpleDateFormat("EEE MMM dd yyyy").format(calendar.getTime()));

            // set up delete operation for every button
            // associate a unique id to button to delete certain row
            final long id = Long.parseLong(cursor.getString(cursor.getColumnIndex(MyContract._ID)));
            ImageButton imageButton = view.findViewById(R.id.imageButton);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentResolver cr = getContentResolver();
                    cr.delete(ContentUris.withAppendedId(MyContract.URI_EXPENSE, id), null, null);
                    updateList();
                }
            });
        }
    }
}
