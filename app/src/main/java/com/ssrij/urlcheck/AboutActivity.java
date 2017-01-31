package com.ssrij.urlcheck;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0.0f);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showLicensesDialog(View v) {
        final Notices notices = new Notices();
        notices.addNotice(new Notice("Retrofit", "https://github.com/square/retrofit", "Square, Inc", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("Material Dialogs", "https://github.com/afollestad/material-dialogs", "Aidan Michael Follestad", new MITLicense()));
        notices.addNotice(new Notice("EasyPreferences", "https://github.com/Pixplicity/EasyPreferences", "Pixplicity", new ApacheSoftwareLicense20()));
        new LicensesDialog.Builder(this)
                .setTitle("Open source software licenses")
                .setNotices(notices)
                .setIncludeOwnLicense(false)
                .build()
                .show();
    }
}
