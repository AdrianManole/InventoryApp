package com.adrianmanole.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.adrianmanole.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * {@link ProductCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */

public class ProductCursorAdapter extends CursorAdapter {

    private static String LOG_TAG = ProductCursorAdapter.class.getSimpleName();

    private static Context mContext;

    /**
     * Constructs a new {@link ProductCursorAdapter}
     *
     * @param context
     */

    public ProductCursorAdapter(Context context) {
        super(context, null, 0);
        mContext = context;
    }

    public static class ProductViewHolder {

        TextView textViewProductName;
        TextView textViewPrice;
        TextView textViewQuantity;
        ImageButton saleButton;

        // Find views within ListView
        public ProductViewHolder(View itemView) {
            textViewProductName = (TextView) itemView.findViewById(R.id.text_product_name);
            textViewPrice = (TextView) itemView.findViewById(R.id.text_product_price);
            textViewQuantity = (TextView) itemView.findViewById(R.id.text_product_stock);
            saleButton = (ImageButton) itemView.findViewById(R.id.button_sale);
        }
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_item, parent, false);
        ProductViewHolder holder = new ProductViewHolder(view);
        view.setTag(holder);

        return view;
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the product name
     * TextView in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        ProductViewHolder holder = (ProductViewHolder) view.getTag();

        // Find the columns of product attributes that we're interested in and set data
        final int productIdColumnIndex = cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID));
        String productNameColumnIndex = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_NAME));
        Double priceColumnIndex = cursor.getDouble(cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE));
        final int quantityColumnIndex = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY));

        holder.textViewProductName.setText(productNameColumnIndex);
        holder.textViewPrice.setText(mContext.getString(R.string.display_product_price, priceColumnIndex));
        holder.textViewQuantity.setText(mContext.getString(R.string.display_product_quantity, quantityColumnIndex));

        holder.saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri productUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, productIdColumnIndex);
                decreaseQuantity(context, productUri, quantityColumnIndex);
            }
        });
    }

    private void decreaseQuantity(Context context, Uri productUri, int currentQuantity) {

        // Decrease stock quantity by 1
        int newQuantity = (currentQuantity >= 1) ? currentQuantity - 1 : 0;

        // Update table with new stock quantity of the product
        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryEntry.COLUMN_QUANTITY, newQuantity);

        int rowsUpdated = context.getContentResolver().update(productUri, contentValues, null, null);

        // Display error LOG_TAG, if product stock quantity doesn`t update
        if (!(rowsUpdated > 0)) {
            Log.e(LOG_TAG, context.getString(R.string.error_quantity_update));
        }
    }
}