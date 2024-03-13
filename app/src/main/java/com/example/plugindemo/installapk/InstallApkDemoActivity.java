package com.example.plugindemo.installapk;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.example.plugindemo.R;


public class InstallApkDemoActivity extends Activity {
    private static final String TAG = "InstallApkDemoActivity";

    private PackageManagerHelper mPackageManagerHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.install_apk_session_api);

        mPackageManagerHelper = new PackageManagerHelper(InstallApkDemoActivity.this);
        mPackageManagerHelper.registerListener(new PackageManagerHelper.Listener() {
            @Override
            public void onActionFinished(int actionType, boolean success) {
                Log.d(TAG, "onActionFinished: " + actionType + " " + success);
            }
        });

        findViewById(R.id.install_sync).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mPackageManagerHelper.installFromAssert("notes.apk");
                    }
                });
                t.start();
            }
        });

        findViewById(R.id.uninstall_sync).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mPackageManagerHelper.deletePackage("com.smartisan.notes");
                    }
                });
                t.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPackageManagerHelper.unregisterListener();
    }
}


