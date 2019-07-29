package com.github.scaleimageandswipedissmiss

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Picasso.get().load(R.drawable.panda).into(scaleImage)

        scaleImage.setBackgroundColor(Color.BLACK)
        scaleImage.setZoomEnabled(true)
        scaleImage.setDismissEnabled(true)

        scaleImage.setOnDismissRateChange {rate,isCanNowDismiss->
            scaleImage.setBackgroundColor(Color.argb(Math.round(255*(1f-rate)),0,0,0))
            if(isCanNowDismiss){
                Toast.makeText(this@MainActivity,"Success dismiss",Toast.LENGTH_LONG).show()
            }
        }

        scaleImage.setOnZoomChange { currentScale, minScale, maxScale ->
            //your code
        }
    }
}
