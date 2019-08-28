package com.github.scaleimageandswipedissmiss

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.squareup.picasso.Picasso
import androidx.core.view.ViewCompat.getTransitionName
import android.app.ActivityOptions
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.transition.Fade
import android.view.Window
import androidx.core.view.ViewCompat
import kotlinx.android.synthetic.main.activity_second.*


class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        Picasso.get().load(R.drawable.panda).into(scaleImage)

        scaleImage.setBackgroundColor(Color.BLACK)
        scaleImage.setZoomEnabled(true)
        scaleImage.setDismissEnabled(true)

        scaleImage.setOnDismissRateChange {rate,isCanNowDismiss->
            scaleImage.setBackgroundColor(Color.argb(Math.round(255*(1f-rate)),0,0,0))
            if(isCanNowDismiss){
               finish()
            }
        }

        scaleImage.setOnZoomChange { currentScale, minScale, maxScale ->
            //your code
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
