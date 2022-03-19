package com.hmsl.fluidtargetapp;


import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ImageView sample;
    private Button btn_prev;
    private Button btn_next;

    private static final String TAG = "TEST";

    private static Bitmap[] sampleArr = new Bitmap[10];
    private static int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sample = (ImageView)findViewById(R.id.sample);
        btn_prev = (Button)findViewById(R.id.btn_prev);
        btn_next = (Button)findViewById(R.id.btn_next);

        AssetManager am = getResources().getAssets();
        InputStream is = null;
        try{
            is = am.open("0.jpg");
            Bitmap bm = BitmapFactory.decodeStream(is);
            init_bitmap();
            sample.setImageBitmap(bm);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sample.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                return true;
            }
        });


        btn_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(index != 0){
                    index--;
                    sample.setImageBitmap(sampleArr[index]);
                }else{
                    index = 9;
                    sample.setImageBitmap(sampleArr[index]);
                }
            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(index != 9){
                    index++;
                    sample.setImageBitmap(sampleArr[index]);
                }else{
                    index = 0;
                    sample.setImageBitmap(sampleArr[index]);
                }
            }
        });
    }

    private void init_bitmap() {
        AssetManager am = getResources().getAssets();
        InputStream is = null;
        try{
            for(int i = 0; i < 10; i++){
                is = am.open(""+i+".jpg");
                Bitmap bm = BitmapFactory.decodeStream(is);
                sampleArr[i] = bm;
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}