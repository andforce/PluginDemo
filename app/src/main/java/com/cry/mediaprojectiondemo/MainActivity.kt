package com.cry.mediaprojectiondemo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.plugindemo.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wel)

        findViewById<Button>(R.id.btn_1).setOnClickListener { _ ->
            val intent = Intent(this@MainActivity, MediaProjectionSocketActivity::class.java)
            startActivity(intent)
        }
//        findViewById<Button>(R.id.btn_2).setOnClickListener{
//            _->
//            val intent = Intent(this@WelComeActivity, RecordActivity::class.java)
//            startActivity(intent)
//        }

    }


}
