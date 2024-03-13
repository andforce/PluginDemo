package com.example.plugindemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.mylibrary.Test;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = (TextView) findViewById(R.id.textView);
        Log.d("MainActivity", "Test.getTestString(): " + Test.getTestString());
        Context context = getApplicationContext();
        Log.d("MainActivity", "" + context.canStartActivityForResult());
        textView.setText("" + context.canStartActivityForResult());

        PackageManager packageManager = context.getPackageManager();
        PackageInstaller installer = packageManager.getPackageInstaller();
    }
}