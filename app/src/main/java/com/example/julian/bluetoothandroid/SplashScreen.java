package com.example.julian.bluetoothandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

/**
 * Created by Julian on 11/15/16.
 */

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        //final ImageView statusUpdate = (ImageView) findViewById(R.id.imageView2);

        Thread myThread = new Thread(){

            @Override
            public void run()
            {
                try {
                    sleep(3000);
                    Intent startMainScreen = new Intent(getApplicationContext(), MainActivity.class );
                    startActivity(startMainScreen);
                    finish();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        myThread.start();

    }
}
