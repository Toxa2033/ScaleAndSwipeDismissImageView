package com.github.scaleimageandswipedissmiss

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
            scaleImage.alpha = 1-rate
            if(isCanNowDismiss){
                finish()
            }
        }

        scaleImage.setOnZoomChange { currentScale, minScale, maxScale ->
            //your code
        }
    }
}
