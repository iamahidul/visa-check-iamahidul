package com.chillbox.visacheckonline;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class splash extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGTH = 1000;
    Animation anim;
    Animation anim1;
    ImageView img;
    TextView tv;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();
        this.tv = (TextView) findViewById(R.id.copy);
        this.img = (ImageView) findViewById(R.id.splashscreen);

        this.anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
        this.anim1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
        this.img.startAnimation(this.anim);
        this.tv.startAnimation(this.anim1);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                splash.this.startActivity(new Intent(splash.this, ItemActivity.class));
                splash.this.finish();
            }
        }, 5000);
    }
}
