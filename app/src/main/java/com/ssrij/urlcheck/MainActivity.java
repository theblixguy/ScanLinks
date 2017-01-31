package com.ssrij.urlcheck;

import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.AppCompatSpinner;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppCompatRadioButton radioButton1;
    private AppCompatRadioButton radioButton2;
    private AppCompatRadioButton radioButton3;
    private AppCompatCheckBox checkBox1;
    private AppCompatSpinner spinner;
    private AppCompatButton button;
    private List<String> spinnerArray;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0.0f);
        }

        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        if (Prefs.getBoolean("firstRun", true)) {
            showFirstRunDialog();
        }

        if (!Prefs.getBoolean("firstRun", true)) {
            if (!checkIfDefault()) {
                showNotDefaultBrowserDialog();
            }
        }

        spinner = (AppCompatSpinner) findViewById(R.id.spinner1);
        radioButton1 = (AppCompatRadioButton) findViewById(R.id.radioButton1);
        radioButton2 = (AppCompatRadioButton) findViewById(R.id.radioButton2);
        radioButton3 = (AppCompatRadioButton) findViewById(R.id.radioButton3);
        checkBox1 = (AppCompatCheckBox)findViewById(R.id.checkBox1);
        button = (AppCompatButton)findViewById(R.id.button2);
        spinnerArray =  new ArrayList<>();

        loadPreferences();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePreferences();
            }
        });


    }

    public void showFirstRunDialog() {
        MaterialDialog frDialog = new MaterialDialog.Builder(this)
                .title(getString(R.string.welcome_app_dialog_title))
                .content(getString(R.string.welcome_app_text))
                .positiveText(getString(R.string.welcome_app_dialog_positive_text))
                .cancelable(false)
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        Prefs.putBoolean("firstRun", false);
                    }
                })
                .build();
        frDialog.show();
    }

    public void showNotDefaultBrowserDialog() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            MaterialDialog notDefDialog = new MaterialDialog.Builder(this)
                    .title(getString(R.string.browser_not_default_dialog_title))
                    .content(getString(R.string.browser_not_default_dialog_text))
                    .positiveText(getString(R.string.browser_not_default_dialog_positive_button))
                    .cancelable(false)
                    .autoDismiss(false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .build();
            notDefDialog.show();
        } else {
            MaterialDialog notDefDialog = new MaterialDialog.Builder(this)
                    .title(getString(R.string.browser_not_default_dialog_title))
                    .content(getString(R.string.browser_not_default_dialog_text))
                    .positiveText(getString(R.string.browser_not_default_dialog_positive_button))
                    .negativeText(getString(R.string.browser_not_default_dialog_negative_text))
                    .cancelable(false)
                    .autoDismiss(false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            startActivity(new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS));
                        }
                    })
                    .build();
            notDefDialog.show();
        }
    }

    public void loadPreferences() {
        radioButton1.setChecked(Prefs.getBoolean("blockWithDialog", true));
        radioButton2.setChecked(Prefs.getBoolean("blockWithoutDialog", false));
        radioButton3.setChecked(Prefs.getBoolean("suppressWarnings", false));
        checkBox1.setChecked(Prefs.getBoolean("ignoreNotInDbWarn", false));
        String bwName = Prefs.getString("browserName", "Chrome");
        loadInstalledBrowsers();
        if (!bwName.equals("")) {
            spinner.setSelection(adapter.getPosition(bwName));
        }
    }

    public void savePreferences() {
        Prefs.putBoolean("blockWithDialog", radioButton1.isChecked());
        Prefs.putBoolean("blockWithoutDialog", radioButton2.isChecked());
        Prefs.putBoolean("suppressWarnings", radioButton3.isChecked());
        Prefs.putBoolean("ignoreNotInDbWarn", checkBox1.isChecked());
        Prefs.putString("browserName", spinner.getSelectedItem().toString());
    }

    public void loadInstalledBrowsers() {
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://www.google.com"));
        List<ResolveInfo> list;

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            list = packageManager.queryIntentActivities(intent,
                    PackageManager.GET_META_DATA);
        } else {
            list = packageManager.queryIntentActivities(intent,
                    PackageManager.MATCH_ALL);
        }

        for (ResolveInfo info : list) {
            spinnerArray.add(info.activityInfo.applicationInfo.loadLabel(packageManager).toString());
        }

        adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, spinnerArray);

        adapter.remove("ScanLinks");

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public boolean checkIfDefault() {
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse("http://www.google.com"));
        final ResolveInfo defaultResolution = this.getPackageManager().resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (defaultResolution != null) {
            final ActivityInfo activity = defaultResolution.activityInfo;
            if (!activity.name.equals("com.android.internal.app.ResolverActivity")) {
                if (activity.applicationInfo.packageName.equals("com.ssrij.urlcheck")) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            menu.getItem(1).setVisible(false);
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about_app:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.show_default_apps:
                startActivity(new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS));
                break;
            case R.id.show_setup_instructions:
                showFirstRunDialog();
                break;
            case R.id.show_whitelist:
                startActivity(new Intent(this, WhitelistActivity.class));
                break;
        }
        return true;
    }


}
