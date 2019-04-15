package com.zy18703.expensestracker;

import android.net.Uri;

public final class MyContract {

    // Data related to use of content provider
    public static final String AUTHORITY = "com.zy18703.expensestracker.provider";
    public static final Uri URI_EXPENSE = Uri.parse("content://" + AUTHORITY + "/expense");

    // Database column names
    public static final String _ID = "_id";
    public static final String CATEGORY = "category";
    public static final String AMOUNT = "amount";
    public static final String DATE = "date";

    // Provider return types
    public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/vnd." + AUTHORITY + ".expense";
    public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/vnd." + AUTHORITY + ".expense";
}
