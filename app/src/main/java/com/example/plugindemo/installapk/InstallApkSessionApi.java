package com.example.plugindemo.installapk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.example.plugindemo.R;


public class InstallApkSessionApi extends Activity {
    private static final String PACKAGE_INSTALLED_ACTION =
            "com.xxx.install";
    private static final String PACKAGE_UNINSTALLED_ACTION =
            "com.xxx.uninstall";
    private static final String TAG = "install";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.install_apk_session_api);

        findViewById(R.id.install_sync).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PackageMgrCompat compat = new PackageMgrCompat(InstallApkSessionApi.this);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int result = compat.installFromAssert("notes.apk");
                        Log.e(TAG, "installFromAssert result: " + result);
                    }
                });
                t.start();
            }
        });

        // Watch for button clicks.
        Button button = (Button) findViewById(R.id.install);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PackageInstaller.Session session = null;
                try {
                    //获取PackageInstaller对象
                    PackageInstaller packageInstaller = getPackageManager().getPackageInstaller();
                    PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                            PackageInstaller.SessionParams.MODE_FULL_INSTALL);

                    //创建一个Session
                    int sessionId = packageInstaller.createSession(params);
                    //建立和PackageManager的socket通道，Android中的通信不仅仅有Binder还有很多其它的
                    session = packageInstaller.openSession(sessionId);

                    //将App的内容通过session传输
                    addApkToInstallSession("notes.apk", session);

                    // Create an install status receiver.
                    Context context = InstallApkSessionApi.this;
                    Intent intent = new Intent(context, InstallApkSessionApi.class);
                    intent.setAction(PACKAGE_INSTALLED_ACTION);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
                    IntentSender statusReceiver = pendingIntent.getIntentSender();

                    // Commit the session (this will start the installation workflow).
                    //开启安装
                    session.commit(statusReceiver);
                } catch (IOException e) {
                    throw new RuntimeException("Couldn't install package", e);
                } catch (RuntimeException e) {
                    if (session != null) {
                        session.abandon();
                    }
                    throw e;
                }
            }
        });


        Button uninstall = (Button)findViewById(R.id.uninstall);
        uninstall.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                uninstall("com.smartisan.notes");
            }
        });
    }

    private void addApkToInstallSession(String assetName, PackageInstaller.Session session)
            throws IOException {
        // It's recommended to pass the file size to openWrite(). Otherwise installation may fail
        // if the disk is almost full.
        try (OutputStream packageInSession = session.openWrite("package", 0, -1);
             InputStream is = getAssets().open(assetName)) {
            byte[] buffer = new byte[16384];
            int n;
            while ((n = is.read(buffer)) >= 0) {
                packageInSession.write(buffer, 0, n);
            }
        }
    }

    // Note: this Activity must run in singleTop launchMode for it to be able to receive the intent
    // in onNewIntent().
    //此处一定要运行单例模式或者singleTop模式，否则会一直创建该Activity
    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        Log.e(TAG, intent.toString());
        if (PACKAGE_INSTALLED_ACTION.equals(intent.getAction())) {
            Log.e(TAG, intent.getAction());
            int status = extras.getInt(PackageInstaller.EXTRA_STATUS);
            String message = extras.getString(PackageInstaller.EXTRA_STATUS_MESSAGE);

            switch (status) {
                case PackageInstaller.STATUS_PENDING_USER_ACTION:
                    // This test app isn't privileged, so the user has to confirm the install.
                    Intent confirmIntent = (Intent) extras.get(Intent.EXTRA_INTENT);
                    startActivity(confirmIntent);
                    break;

                case PackageInstaller.STATUS_SUCCESS:
                    Toast.makeText(this, "Install succeeded!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG,"Install succeeded!");
                    break;

                case PackageInstaller.STATUS_FAILURE:
                case PackageInstaller.STATUS_FAILURE_ABORTED:
                case PackageInstaller.STATUS_FAILURE_BLOCKED:
                case PackageInstaller.STATUS_FAILURE_CONFLICT:
                case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
                case PackageInstaller.STATUS_FAILURE_INVALID:
                case PackageInstaller.STATUS_FAILURE_STORAGE:
                    Toast.makeText(this, "Install failed! " + status + ", " + message,
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG,"Install failed! " + status + ", " + message);
                    break;
                default:
                    Toast.makeText(this, "Unrecognized status received from installer: " + status,
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG,"Unrecognized status received from installer: " + status);
            }
        }
        else if(PACKAGE_UNINSTALLED_ACTION.equals(intent.getAction())){
            Log.e(TAG, intent.getAction());
            int status = extras.getInt(PackageInstaller.EXTRA_STATUS);
            String message = extras.getString(PackageInstaller.EXTRA_STATUS_MESSAGE);

            switch (status) {
                case PackageInstaller.STATUS_PENDING_USER_ACTION:
                    // This test app isn't privileged, so the user has to confirm the install.
                    Intent confirmIntent = (Intent) extras.get(Intent.EXTRA_INTENT);
                    startActivity(confirmIntent);
                    break;

                case PackageInstaller.STATUS_SUCCESS:
                    Toast.makeText(this, "Uninstall succeeded!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG,"Uninstall succeeded!");
                    break;

                case PackageInstaller.STATUS_FAILURE:
                case PackageInstaller.STATUS_FAILURE_ABORTED:
                case PackageInstaller.STATUS_FAILURE_BLOCKED:
                case PackageInstaller.STATUS_FAILURE_CONFLICT:
                case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
                case PackageInstaller.STATUS_FAILURE_INVALID:
                case PackageInstaller.STATUS_FAILURE_STORAGE:
                    Toast.makeText(this, "Install failed! " + status + ", " + message,
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG,"Uninstall failed! " + status + ", " + message);
                    break;
                default:
                    Toast.makeText(this, "Unrecognized status received from installer: " + status,
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG,"Unrecognized status received from installer: " + status);
            }
        }
    }



    /**
     * 根据包名卸载应用
     *
     * @param packageName
     */
    public void uninstall(String packageName) {
        Intent broadcastIntent = new Intent(this, InstallApkSessionApi.class);
        broadcastIntent.setAction(PACKAGE_UNINSTALLED_ACTION);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PackageInstaller packageInstaller = getPackageManager().getPackageInstaller();
        packageInstaller.uninstall(packageName, pendingIntent.getIntentSender());

    }

}


