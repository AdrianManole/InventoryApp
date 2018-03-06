package com.adrianmanole.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Inventory app
 */

public class InventoryContract {

    /**
     * ContentProvider name
     */
    public static final String CONTENT_AUTHORITY = "com.adrianmanole.inventoryapp";
    /**
     * ContentProvider Base Uri
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Path appended to base URI for possible URI`s
     */
    public static final String PATH_INVENTORY = "inventory";

    /**
     * Empty constructor.
     */

    private InventoryContract() {
    }

    /**
     * Inner class that defines constant values for the inventory database table.
     * Each entry in the table represents a single product
     */
    public static final class InventoryEntry implements BaseColumns {

        /**
         * The content URI to access the inventory data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * Name of database table for products
         */
        public final static String TABLE_NAME = "inventory";

        /**
         * Unique ID number for a product (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of product
         * <p>
         * TYPE: TEXT
         */
        public final static String COLUMN_NAME = "name";

        /**
         * Price of product
         * <p>
         * TYPE: REAL
         */
        public final static String COLUMN_PRICE = "price";

        /**
         * Quantity in stock of a product
         * <p>
         * TYPE: INTEGER
         */
        public static final String COLUMN_QUANTITY = "quantity";

        /**
         * Image of the product
         * <p>
         * TYPE: TEXT
         */
        public static final String COLUMN_IMAGE = "image";

        /**
         * Supplier name
         * <p>
         * TYPE: TEXT
         */
        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";

        /**
         * Supplier e-mail address
         * <p>
         * TYPE: TEXT
         */
        public static final String COLUMN_SUPPLIER_EMAIL = "supplier_email";

        /**
         * Supplier phone number
         * <p>
         * TYPE: TEXT
         */
        public static final String COLUMN_SUPPLIER_PHONE = "supplier_phone";

    }
}