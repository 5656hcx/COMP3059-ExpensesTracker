package com.zy18703.expensestracker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private final static String KEY_TIME_VALUE = "time_value";
    private final static String KEY_TIME_TEXT = "time_text";
    private TextView textView_date;
    private Toast toast;
    private static long timeStamp = -1;

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize objects for later use
        // keep only one instance of Toast
        textView_date = findViewById(R.id.text_date);
        toast = Toast.makeText(this, null, Toast.LENGTH_SHORT);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // save activity state
        outState.putLong(KEY_TIME_VALUE, timeStamp);
        outState.putString(KEY_TIME_TEXT, textView_date.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // restore activity state
        timeStamp = savedInstanceState.getLong(KEY_TIME_VALUE);
        textView_date.setText(savedInstanceState.getString(KEY_TIME_TEXT));
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // will be called when user has made a selection of date
        // save date as milliseconds from epoch
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, 0, 0, 0);
        timeStamp = calendar.getTimeInMillis();
        textView_date.setText(new SimpleDateFormat("EEE MMM dd yyyy").format(calendar.getTime()));
    }

    public void addRecord(View view) {
        // add a new expense record to database through content provider
        // also perform a series of data check
        EditText editText = findViewById(R.id.edit_category);
        String category = editText.getText().toString().trim();
        if (category.isEmpty())
            toast.setText(R.string.toast_empty_category);
        else {
            editText = findViewById(R.id.edit_amount);
            String amount = editText.getText().toString();
            if (amount.isEmpty())
                toast.setText(R.string.toast_empty_amount);
            else if (!checkAmount(amount))
                toast.setText(R.string.toast_invalid_amount);
            else if (timeStamp == -1)
                toast.setText(R.string.toast_empty_date);
            else {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MyContract.CATEGORY, category);
                contentValues.put(MyContract.AMOUNT, amount);
                contentValues.put(MyContract.DATE, timeStamp);
                Uri result = getContentResolver().insert(MyContract.URI_EXPENSE, contentValues);
                if (result == null || result.getLastPathSegment() == null ||
                        result.getLastPathSegment().equals("-1"))
                    toast.setText(R.string.toast_fail);
                else {
                    toast.setText(R.string.toast_ok);
                    startActivity(new Intent(this, TrackerActivity.class));
                }
            }
        }
        toast.show();
    }

    public void viewRecord(View view) {
        // go to another activity in which user can view saved records
        Intent intent = new Intent(this, TrackerActivity.class);
        startActivity(intent);
    }

    public void showDatePickerDialog(View view) {
        // display a date picker dialog
        DialogFragment dialogFragment = new DatePickerFragment();
        dialogFragment.show(getSupportFragmentManager(), "DatePicker");
    }

    private boolean checkAmount(String amount) {
        // check if the amount is invalid
        // valid amount is a 32bit integer value
        try {
            long value = Long.parseLong(amount);
            if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE)
                return false;
        } catch (NumberFormatException e) { return false; }
        return true;
    }

    public static class DatePickerFragment extends DialogFragment {
        // initialize and return a DatePickerDialog
        @Override
        public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar calendar = Calendar.getInstance();
            if (timeStamp != -1)
                calendar.setTimeInMillis(timeStamp);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            MainActivity activity = (MainActivity) getActivity();
            assert activity != null;
            return new DatePickerDialog(activity, activity, year, month, day);
        }
    }
}
