package ru.test.scaleimageandswipedissmiss

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Picasso.get().load(R.drawable.panda).into(scaleImage)

        scaleImage.setBackgroundColor(Color.BLACK)

        scaleImage.setOnDismissRateChange {rate,isCanNowDismiss->
            scaleImage.alpha = 1-rate
            if(isCanNowDismiss){
                finish()
            }
        }
    }
}
