[![](https://jitpack.io/v/Toxa2033/ScaleAndSwipeDismissImageView.svg)](https://jitpack.io/#Toxa2033/ScaleAndSwipeDismissImageView)

# ScaleAndSwipeDismissImageView

This android library provide zoom and swipe(fling) dismiss like facebook

![](https://github.com/Toxa2033/ScaleAndSwipeDismissImageView/blob/master/sample2.gif?raw=true)


## Features
- Zoom image by gestures and double tap
- Smooth scroll
- Dismiss by swipe 

## Usage 

There is a [sample](https://github.com/Toxa2033/ScaleAndSwipeDismissImageView/blob/master/app/src/main/java/com/github/scaleimageandswipedissmiss/MainActivity.kt)

```
<com.github.toxa2033.ScaleImageView
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:scaleType="fitCenter"
            app:zoom_enabled="true"
            app:dismiss_enabled="true"
            android:id="@+id/scaleImage"/>
```

### XMLAttributes
| name | type | default |
| --- | --- | --- |
| zoom_enabled | boolean | true |
| dismiss_enabled | boolean | true |

## In code

You can:
 - Set enabled zoom and dismiss in code 
```
scaleImage.setDismissEnabled(false)
scaleImage.setZoomEnabled(false)
```

 - Listen dismiss rate. Dismiss rate form 0 to 1. When rate == 1 and touch was released isCanNowDismiss = true and you can close image
 ```
scaleImage.setOnDismissRateChange {rate,isCanNowDismiss->
            scaleImage.setBackgroundColor(Color.argb(Math.round(255*(1f-rate)),0,0,0))
            if(isCanNowDismiss){
                Toast.makeText(this@MainActivity,"Success dismiss",Toast.LENGTH_LONG).show()
            }
        }
```
 
 - Listen zoom change 
  ```
scaleImage.setOnZoomChange { currentScale, minScale, maxScale -> 
            //your code 
        }
```


## Download

```build.gradle
allprojects {
	repositories {
        maven { url "https://jitpack.io" }
    }
}
```

```app/build.gradle
dependencies {
    implementation 'com.github.Toxa2033:ScaleAndSwipeDismissImageView:v0.7'
}
```

## License
```
Copyright 2019 Toxa2033

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
