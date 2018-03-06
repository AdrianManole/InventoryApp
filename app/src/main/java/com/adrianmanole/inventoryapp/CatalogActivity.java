package com.adrianmanole.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.adrianmanole.inventoryapp.data.InventoryContract.InventoryEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = CatalogActivity.class.getSimpleName();
    private static final int PRODUCT_LOADER = 1;
    final Context mContext = this;
    private ListView mListViewProducts;
    private ProductCursorAdapter mCursorAdapter;
    private View mEmptyStateView;

    private TextView mTextViewEmptyStateTitle;
    private TextView mTextViewEmptyStateSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        mTextViewEmptyStateTitle = (TextView) findViewById(R.id.text_empty_store_title);
        mTextViewEmptyStateSubtitle = (TextView) findViewById(R.id.text_empty_store_subtitle);

        // Setup FAB to open EditorActivity
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.floating_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the list data
        mListViewProducts = (ListView) findViewById(R.id.list_product);

        // Find and set empty view on the ListView, when the list it`s not populated
        mEmptyStateView = findViewById(R.id.empty_view);
        mListViewProducts.setEmptyView(mEmptyStateView);

        // Set the adapter to create a list of item for each row of data in Cursor
        mCursorAdapter = new ProductCursorAdapter(this);
        mListViewProducts.setAdapter(mCursorAdapter);

        // Set the item OnClickListener
        mListViewProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                // Create new intent to go to {@link DetailActivity}
                Intent intent = new Intent(CatalogActivity.this, DetailActivity.class);

                // Form the content URI that represents the specific list item that was clicked on
                Uri currentProductUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                // Set the URI on the data field intent
                intent.setData(currentProductUri);

                // Launch the {@link DetailActivity} to display the data for the current item
                startActivity(intent);
            }
        });

        // Start the loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    /**
     * Method loads the cursor with record fetched from inventory database
     *
     * @param i
     * @param bundle
     * @return cursor
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY
        };
        return new CursorLoader(this, InventoryEntry.CONTENT_URI, projection, null, null, null
        );
    }

    /**
     * Method executes when the cursor has finished loading data
     *
     * @param loader
     * @param cursor
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    /**
     * Method executes when data needs to be reset
     *
     * @param loader
     */

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    /**
     * Method to inflate menu
     *
     * @param menu
     * @return true/false
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all_products:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method displays a confirmation dialog for the user to confirm before deleting all entries
     */
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_confirmation_dialog);

        builder.setPositiveButton(R.string.dialog_action_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked "Delete all products" button, and confirmed the action
                deleteAllItems();
            }
        });
        builder.setNegativeButton(R.string.dialog_action_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // If user clicked the "Cancel" button, dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Method invoked when "Delete all products" menu item clicked
     */
    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        if (rowsDeleted > 0) {
            Toast.makeText(CatalogActivity.this, getString(R.string.confirmation_all_products_deleted),
                    Toast.LENGTH_SHORT).show();
        } else {
            Log.e(LOG_TAG, getString(R.string.error_deleting_all_entries));
        }
    }
}