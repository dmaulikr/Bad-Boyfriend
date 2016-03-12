package com.clarifai.androidstarter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by FrankMAC on 2016-03-12.
 */
public class Splash extends AppCompatActivity{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        Thread myTread = new Thread(){
            public void run(){
                try{
                    sleep(3500);
                    Intent startMainScreen = new Intent(getApplicationContext(), RecognitionActivity.class);
                    startActivity(startMainScreen);
                    finish();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

            }
        };
        myTread.start();
    }
}
