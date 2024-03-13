package com.example.plugindemo.installapk;

import android.content.Context;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class PackageManagerHelper {
    private static final String TAG = "PackageMgrCompat_v28_later";
    private Context mContext;
    private PackageManager mPackageManager;

    public PackageManagerHelper(Context context) {
        mContext = context;
        mPackageManager = context.getPackageManager();

    }


    public boolean deletePackage(String pkgName, int unInstallFlags) {
        final LocalIntentReceiver localReceiver = new LocalIntentReceiver();
        getPi().uninstall(pkgName,
                unInstallFlags | PackageManager.DELETE_ALL_USERS,
                localReceiver.getIntentSender());
        final Intent result = localReceiver.getResult();
        synchronized (localReceiver) {
            final int status = result.getIntExtra(PackageInstaller.EXTRA_STATUS,
                    PackageInstaller.STATUS_FAILURE);
            if (status != PackageInstaller.STATUS_SUCCESS) {
                Log.e(TAG, "UnInstallation should have succeeded, but got code "
                        + status);
                return false;
            } else {
                Log.e(TAG, "UnInstallation  have succeeded");
                return true;
            }
        }
    }

    public int installFromAssert(String name) {
        Log.w(TAG, "installPackage pkg: " + name);

        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);

        PackageInstaller.Session session = null;
        // 创建一个Session
        try {
            int sessionId = getPi().createSession(params);
            // 建立和PackageManager的socket通道，Android中的通信不仅仅有Binder还有很多其它的
            session = getPi().openSession(sessionId);
            addApkToInstallSession(name, session);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return PackageManager.INSTALL_FAILED_INVALID_APK;
        }

        final LocalIntentReceiver localReceiver = new LocalIntentReceiver();
        session.commit(localReceiver.getIntentSender());
        final Intent result = localReceiver.getResult();
        synchronized (localReceiver) {
            final int status = result.getIntExtra(PackageInstaller.EXTRA_STATUS,
                    PackageInstaller.STATUS_FAILURE);
            if (session != null) {
                session.close();
            }
            if (status != PackageInstaller.STATUS_SUCCESS) {
                Log.e(TAG, "Installation should have succeeded, but got code "
                        + status);
                return status;
            } else {
                Log.e(TAG, "Installation  have succeeded");
                return status;
            }
        }
    }
    public int installPackage(File apkFilePath) {
        Log.w(TAG, "installPackage pkg: " + apkFilePath);

        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);

        PackageInstaller.Session session = null;
        // 创建一个Session
        try {
            int sessionId = getPi().createSession(params);
            // 建立和PackageManager的socket通道，Android中的通信不仅仅有Binder还有很多其它的
            session = getPi().openSession(sessionId);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return PackageManager.INSTALL_FAILED_INVALID_APK;
        }
        addApkToInstallSession(apkFilePath, session);
        final LocalIntentReceiver localReceiver = new LocalIntentReceiver();
        session.commit(localReceiver.getIntentSender());
        final Intent result = localReceiver.getResult();
        synchronized (localReceiver) {
            final int status = result.getIntExtra(PackageInstaller.EXTRA_STATUS,
                    PackageInstaller.STATUS_FAILURE);
            if (session != null) {
                session.close();
            }
            if (status != PackageInstaller.STATUS_SUCCESS) {
                Log.e(TAG, "Installation should have succeeded, but got code "
                        + status);
                return status;
            } else {
                Log.e(TAG, "Installation  have succeeded");
                return status;
            }
        }

    }

    private void addApkToInstallSession(String assetName, PackageInstaller.Session session)
            throws IOException {
        // It's recommended to pass the file size to openWrite(). Otherwise installation may fail
        // if the disk is almost full.
        try (OutputStream packageInSession = session.openWrite("package", 0, -1);
             InputStream is = mContext.getAssets().open(assetName)) {
            byte[] buffer = new byte[16384];
            int n;
            while ((n = is.read(buffer)) >= 0) {
                packageInSession.write(buffer, 0, n);
            }
        }
    }

    private boolean addApkToInstallSession(File apkFilePath,
                                           PackageInstaller.Session session) {
        InputStream in = null;
        OutputStream out = null;
        boolean success = false;
        try {
            out = session.openWrite("base.apk", 0, apkFilePath.length());
            in = new FileInputStream(apkFilePath);
            int total = 0, c;
            byte[] buffer = new byte[1024 * 1024];
            while ((c = in.read(buffer)) != -1) {
                total += c;
                out.write(buffer, 0, c);
            }
            session.fsync(out);
            Log.d(TAG, "streamed " + total + " bytes");
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != session) {
                session.close();
            }
            try {
                if (null != out) {
                    out.close();
                }
                if (null != in) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    private static class LocalIntentReceiver {
        private final SynchronousQueue<Intent> mResult = new SynchronousQueue<>();

        private IIntentSender.Stub mLocalSender = new IIntentSender.Stub() {
            @Override
            public void send(int code, Intent intent, String resolvedType,
                             IBinder whitelistToken, IIntentReceiver finishedReceiver,
                             String requiredPermission, Bundle options) {
                try {
                    mResult.offer(intent, 5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        public IntentSender getIntentSender() {
            return new IntentSender((IIntentSender) mLocalSender);
        }

        public Intent getResult() {
            try {
                return mResult.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private PackageManager getPm() {
        return mContext.getPackageManager();
    }

    private PackageInstaller getPi() {
        return getPm().getPackageInstaller();
    }
}
