package com.ssrij.urlcheck;

import android.content.ComponentName;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ScanActivity extends AppCompatActivity {

    private String urlToCheck;
    private Retrofit retrofit;
    private ScanURLAPI myAPI;
    private MaterialDialog progressDialog;
    private Intent browserIntent;
    private ComponentName browserCompName;
    private String virusTotalUrl = "";
    private boolean blockWithoutDiag = false;
    private boolean ignoreNotInDbWarning = false;
    private Set<String> whitelistArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        urlToCheck = getIntent().getDataString();
        browserIntent = new Intent();

        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

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

        whitelistArray = Prefs.getOrderedStringSet("whitelistArray", Collections.<String>emptySet());

        for (ResolveInfo info : list) {
            if (info.activityInfo.applicationInfo.loadLabel(packageManager).toString().equals(Prefs.getString("browserName", "Chrome"))) {
                browserCompName = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
                browserIntent.setComponent(browserCompName);
            }
        }

        for (String url : whitelistArray) {
            if (urlToCheck.contains(url)) {
                launchURLInBrowser();
            }
        }

        if (Prefs.getBoolean("suppressWarnings", false)) {
            launchURLInBrowser();
        }

        if (Prefs.getBoolean("blockWithoutDialog", false)) {
            blockWithoutDiag = true;
        }

        if (Prefs.getBoolean("blockWithDialog", false)) {
            blockWithoutDiag = false;
        }

        if (Prefs.getBoolean("ignoreNotInDbWarn", false)) {
            ignoreNotInDbWarning = true;
        }

        showProgressDialog();
        getScanIDAndExecuteScan();

    }

    public void launchURLInBrowser() {
        browserIntent.setAction(Intent.ACTION_VIEW);
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        browserIntent.setComponent(browserCompName);
        browserIntent.setData(Uri.parse(urlToCheck));
        startActivity(browserIntent);
        finish();
    }

    public void getScanIDAndExecuteScan() {

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(ScanURLAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        myAPI = retrofit.create(ScanURLAPI.class);

        Call<String> scanID = myAPI.getScanResultID(new ScanRequestBody(urlToCheck));
        scanID.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Log.i("TAG", response.body());
                    performScan(response.body());
                } else {
                    hideProgressDialog();
                    showScanUnavailableDialog();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                t.printStackTrace();
                Log.i("TAG", "Failed to get scan ID");
                hideProgressDialog();
                showScanUnavailableDialog();
            }
        });
    }

    public void performScan(String id) {
        Call<Object> scanResults = myAPI.getScanResults(id);
        scanResults.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    hideProgressDialog();
                    if (response.body() instanceof ArrayList) {
                        ArrayList<?> ls = ((ArrayList<?>) response.body());
                        Log.i("TAG", response.body().toString());
                        if ((Double) ls.get(2) > 0.0) {
                            virusTotalUrl = (String) ls.get(1);
                            if (!blockWithoutDiag) {
                                showWarningDialog();
                            } else {
                                Toast.makeText(ScanActivity.this, getString(R.string.no_dialog_warning_toast), Toast.LENGTH_SHORT).show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                }, Toast.LENGTH_SHORT);
                            }
                        } else {
                            launchURLInBrowser();
                        }
                    } else if (response.body() instanceof String) {
                        if (!ignoreNotInDbWarning) {
                            showURLBeingAnalyzedDialog();
                        } else {
                            launchURLInBrowser();
                        }
                    } else {
                        launchURLInBrowser();
                    }
                } else {
                    hideProgressDialog();
                    showScanUnavailableDialog();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                t.printStackTrace();
                Log.i("TAG", "Failed to get scan result");
                hideProgressDialog();
                showScanUnavailableDialog();
            }
        });
    }

    public void showProgressDialog() {
        progressDialog = new MaterialDialog.Builder(this)
                .title(getString(R.string.progress_dialog_title))
                .content(getString(R.string.progress_dialog_text))
                .progress(true, 0)
                .autoDismiss(false)
                .cancelable(false)
                .build();

        progressDialog.show();
    }

    public void hideProgressDialog() {
        progressDialog.dismiss();
    }

    public void showScanUnavailableDialog() {
        MaterialDialog suDialog = new MaterialDialog.Builder(this)
                .title(getString(R.string.scan_unavail_dialog_title))
                .content(getString(R.string.scan_unavail_dialog_text))
                .positiveText(getString(R.string.scan_unavail_dialog_positive_button))
                .negativeText(getString(R.string.scan_unavail_dialog_negative_button))
                .cancelable(false)
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        launchURLInBrowser();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .build();
        suDialog.show();
    }

    public void showURLBeingAnalyzedDialog() {
        MaterialDialog urlAnalyzedDialog = new MaterialDialog.Builder(this)
                .title(getString(R.string.url_not_in_db_dialog_title))
                .content(getString(R.string.url_not_in_db_dialog_text))
                .positiveText(getString(R.string.url_not_in_db_dialog_positive_button))
                .negativeText(getString(R.string.url_not_in_db_dialog_negative_button))
                .neutralText(R.string.url_not_in_db_neutral_text)
                .autoDismiss(false)
                .cancelable(false)
                .backgroundColor(Color.parseColor("#FDD835"))
                .positiveColor(Color.WHITE)
                .negativeColor(Color.WHITE)
                .neutralColor(Color.WHITE)
                .contentColor(Color.WHITE)
                .titleColor(Color.WHITE)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        launchURLInBrowser();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Prefs.putBoolean("ignoreNotInDbWarn", true);
                        dialog.dismiss();
                        launchURLInBrowser();
                    }
                })
                .build();
        urlAnalyzedDialog.show();
    }

    public void showWarningDialog() {
        MaterialDialog wDialog = new MaterialDialog.Builder(this)
                .title(getString(R.string.danger_dialog_title))
                .content(getString(R.string.danger_dialog_text))
                .positiveText(getString(R.string.danger_dialog_positive_text))
                .cancelable(false)
                .autoDismiss(false)
                .negativeText(getString(R.string.danger_dialog_negative_button))
                .neutralText(getString(R.string.danger_dialog_neutral_button))
                .backgroundColor(Color.parseColor("#f44336"))
                .positiveColor(Color.WHITE)
                .negativeColor(Color.WHITE)
                .neutralColor(Color.WHITE)
                .contentColor(Color.WHITE)
                .titleColor(Color.WHITE)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        browserIntent.setAction(Intent.ACTION_VIEW);
                        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        browserIntent.setComponent(browserCompName);
                        browserIntent.setData(Uri.parse(virusTotalUrl));
                        startActivity(browserIntent);
                        finish();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        launchURLInBrowser();
                    }
                })
                .build();
        wDialog.show();
    }
}

