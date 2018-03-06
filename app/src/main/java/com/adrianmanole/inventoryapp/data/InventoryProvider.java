package com.adrianmanole.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.adrianmanole.inventoryapp.R;
import com.adrianmanole.inventoryapp.data.InventoryContract.InventoryEntry;


/**
 * {@link android.content.ContentProvider} for Inventory app
 */

public class InventoryProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the inventory table
     */
    private static final int INVENTORY = 100;

    /**
     * URI matcher code for the content URI for a single product in the pets table
     */
    private static final int PRODUCT_ID = 101;
    /**
     * Tags used for input validation
     */
    private static final String TAG_NAME = "name";
    private static final String TAG_PRICE = "price";
    private static final String TAG_QUANTITY = "quantity";
    private static final String TAG_SUPPLIER = "supplier";
    private static final String TAG_EMAIL = "email";
    /**
     * UriMatcher object to match a content URI to a corresponding code
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // Content URI of the form "content://package-name/inventory"
        // This URI is used to provide access to multiple table rows.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);

        // Content URI of the form "content://package-name/inventory/#", where # is the ID value
        // This is used to provide access to a single row of the table
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#",
                PRODUCT_ID);
    }

    /**
     * Database Helper Object
     */
    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    /**
     * Perform query for the given URI and load the cursor with results fetched from table.
     * The returned result can have multiple rows or a single row, depending on given URI.
     *
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return cursor
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase dbReadable = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Check if the uri matches to a specific URI CODE
        int match = sUriMatcher.match(uri);

        switch (match) {
            case INVENTORY:
                cursor = dbReadable.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = dbReadable.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException(getContext().getString(R.string.exception_unknown_uri, uri));
        }

        // Set notification URI on Cursor so it knows when to update in the event the data in cursor changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case INVENTORY:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException(getContext().getString(R.string.exception_unknown_uri, uri));
        }

    }

    /**
     * This method inserts records in the table
     *
     * @param uri
     * @param contentValues
     * @return URI
     */

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        long id;
        String columnsToValidate = TAG_NAME + "|" + TAG_PRICE + "|" + TAG_QUANTITY
                + "|" + TAG_SUPPLIER + "|" + TAG_EMAIL;

        boolean isValidInput = validateInput(contentValues, columnsToValidate);

        if (isValidInput) {
            SQLiteDatabase dbWritable = mDbHelper.getWritableDatabase();
            id = dbWritable.insert(InventoryEntry.TABLE_NAME, null, contentValues);
        } else {
            id = -1;
        }

        // Notify all listeners that the data has changed
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID of the newly inserted row appended at the end
        return ContentUris.withAppendedId(uri, id);

    }


    /**
     * This method deletes records from the table
     *
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase dbWritable = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case INVENTORY:
                // Delete all rows that match the selection and selection args
                rowsDeleted = dbWritable.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = dbWritable.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.exception_unknown_uri, uri));
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * This method updates products
     *
     * @param uri
     * @param contentValues
     * @param selection
     * @param selectionArgs
     */

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case INVENTORY:
                return updateInventory(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateInventory(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.exception_unknown_uri, uri));
        }
    }

    private int updateInventory(Uri uri, ContentValues values, String selection,
                                String[] selectionArgs) {

        String columnsToValidate = null;
        StringBuilder stringBuilder = new StringBuilder();
        final String SEPARATOR = "|";
        int rowsUpdated = 0;

        // If there are no values to update, then don't try to update the database

        if (values.size() == 0) {
            return 0;
        } else {
            if (values.containsKey(InventoryEntry.COLUMN_NAME)) {
                stringBuilder.append(TAG_NAME);
            } else if (values.containsKey(InventoryEntry.COLUMN_PRICE)) {
                stringBuilder.append(SEPARATOR).append(TAG_PRICE);
            } else if (values.containsKey(InventoryEntry.COLUMN_QUANTITY)) {
                stringBuilder.append(SEPARATOR).append(TAG_QUANTITY);
            } else if (values.containsKey(InventoryEntry.COLUMN_SUPPLIER_NAME)) {
                stringBuilder.append(SEPARATOR).append(TAG_SUPPLIER);
            } else if (values.containsKey(InventoryEntry.COLUMN_SUPPLIER_EMAIL)) {
                stringBuilder.append(SEPARATOR).append(TAG_EMAIL);
            }
        }

        columnsToValidate = stringBuilder.toString();
        boolean isValidInput = validateInput(values, columnsToValidate);

        if (isValidInput) {
            SQLiteDatabase dbWritable = mDbHelper.getWritableDatabase();

            // Perform the update on the database and get the number of rows affected
            rowsUpdated = dbWritable.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

            // If 1 or more rows were updated, then notify all listeners that the data at the
            // given URI has changed
            if (rowsUpdated != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }

        }

        // Return the number of rows updated
        return rowsUpdated;

    }

    public boolean validateInput(ContentValues values, String columns) {

        String[] columnArgs = columns.split("|");
        String productName = null;
        Double productPrice = null;
        Integer productQuantity = null;
        String supplier = null;
        String supplierEmail = null;

        for (String columnArg : columnArgs) {

            switch (columnArg) {
                case TAG_NAME:
                    // Check if the product name is not null
                    productName = values.getAsString(InventoryEntry.COLUMN_NAME);
                    if (productName == null || productName.trim().length() == 0) {
                        throw new IllegalArgumentException(getContext().getString(R.string.error_product_name_invalid));
                    }
                    break;
                case TAG_PRICE:
                    // Check if the product price entered is not null
                    productPrice = values.getAsDouble(InventoryEntry.COLUMN_PRICE);
                    if (productPrice == null || productPrice < 0) {
                        throw new IllegalArgumentException(getContext().getString(R.string.error_product_price_invalid));
                    }
                    break;
                case TAG_QUANTITY:
                    productQuantity = values.getAsInteger(InventoryEntry.COLUMN_QUANTITY);
                    if (productQuantity == null || productQuantity < 0) {
                        throw new IllegalArgumentException(getContext().getString(R.string.error_quantity_invalid));
                    }
                    break;
                case TAG_SUPPLIER:
                    supplier = values.getAsString(InventoryEntry.COLUMN_SUPPLIER_NAME);
                    if (supplier == null || supplier.trim().length() == 0) {
                        throw new IllegalArgumentException(getContext().getString(R.string.error_supplier_name_invalid));
                    }
                    break;
                case TAG_EMAIL:
                    supplierEmail = values.getAsString(InventoryEntry.COLUMN_SUPPLIER_EMAIL);
                    if (supplierEmail == null || supplierEmail.trim().length() == 0) {
                        throw new IllegalArgumentException(getContext().getString(R.string.error_supplier_email_invalid));
                    }
                    break;
            }
        }

        return true;
    }
}