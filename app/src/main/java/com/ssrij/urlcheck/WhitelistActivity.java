package com.ssrij.urlcheck;

import android.content.ContextWrapper;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.LinkedHashSet;
import java.util.Set;

public class WhitelistActivity extends AppCompatActivity {

    private RecyclerView listView;
    private Set<String> whitelistArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whitelist);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0.0f);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        whitelistArray = Prefs.getOrderedStringSet("whitelistArray", new LinkedHashSet<String>());

        listView = (RecyclerView) findViewById(R.id.listView1);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        listView.setLayoutManager(manager);
        listView.setHasFixedSize(true);
        listView.setAdapter(new RecyclerAdapter(whitelistArray.toArray(new String[whitelistArray.size()])));
    }

    public void showAddToWhitelistDialog() {
        new MaterialDialog.Builder(this)
                .title(getString(R.string.add_to_whitelist_dialog_title))
                .content(getString(R.string.add_to_whitelist_dialog_text))
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("google.com", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (!whitelistArray.contains(input.toString().trim())) {
                            whitelistArray.add(input.toString().trim());
                            listView.getAdapter().notifyDataSetChanged();
                            Toast.makeText(WhitelistActivity.this, getString(R.string.add_to_whitelist_dialog_toast_success), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(WhitelistActivity.this, getString(R.string.add_to_whitelist_dialog_toast_failure), Toast.LENGTH_SHORT).show();
                        }
                        Prefs.putOrderedStringSet("whitelistArray", whitelistArray);
                        dialog.dismiss();
                    }
                }).show();
    }

    public void showRemoveFromWhiteListDialog() {
        new MaterialDialog.Builder(this)
                .title(getString(R.string.remove_from_whitelist_dialog_title))
                .content(getString(R.string.remove_from_whitelist_dialog_text))
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("google.com", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (whitelistArray.contains(input.toString().trim())) {
                            whitelistArray.remove(input.toString().trim());
                            listView.getAdapter().notifyDataSetChanged();
                            Toast.makeText(WhitelistActivity.this, getString(R.string.remove_from_whitelist_dialog_toast_success), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(WhitelistActivity.this, getString(R.string.remove_from_whitelist_dialog_toast_failure), Toast.LENGTH_SHORT).show();
                        }
                        Prefs.putOrderedStringSet("whitelistArray", whitelistArray);
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.whitelist_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.add_to_whitelist:
                showAddToWhitelistDialog();
                break;
            case R.id.remove_from_whitelist:
                showRemoveFromWhiteListDialog();
                break;
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
