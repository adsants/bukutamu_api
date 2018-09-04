package com.adsants.bukutamusqllite;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class splashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Thread thread=new Thread(){
            public void run(){

                try{
                    sleep(1500); // 1000 = 1 Detik
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
                finally {
                    startActivity(new Intent(splashScreen.this, MainActivity.class));
                    finish(); // menutup activity splashscreen
                }

            }
        };

        thread.start();
    }
}
