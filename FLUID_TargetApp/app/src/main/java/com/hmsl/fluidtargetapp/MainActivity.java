package com.hmsl.fluidtargetapp;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hmsl.fluidtargetapp.model.Sample;
import com.hmsl.fluidtargetapp.network.ApiClient;
import com.hmsl.fluidtargetapp.network.ApiInterface;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    private ImageView sample;
    private Button btn_prev;
    private Button btn_next;

    private static final String TAG = "TEST";

    private String[] sampleArr = new String[10];
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sample = (ImageView)findViewById(R.id.sample);
        btn_prev = (Button)findViewById(R.id.btn_prev);
        btn_next = (Button)findViewById(R.id.btn_next);

        init_url();

        btn_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(index != 0){
                    index--;
                    Glide.with(MainActivity.this).load(sampleArr[index]).into(sample);
                }else{
                    index = 10;
                }
            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(index != 10){
                    index++;
                    Glide.with(MainActivity.this).load(sampleArr[index]).into(sample);
                }else{
                    index = 0;
                }
            }
        });
    }

    public void init_url(){
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<List<Sample>> call = apiInterface.getSample();
        call.enqueue(new Callback<List<Sample>>() {
            @Override
            public void onResponse(Call<List<Sample>> call, Response<List<Sample>> response) {
                if(response.isSuccessful() && response.body() != null){
                    for(int i = 0; i<response.body().size(); i++){
                        sampleArr[i] = response.body().get(i).getUrl();
                    }
                    Glide.with(MainActivity.this).load(sampleArr[0]).into(sample);
                }
            }

            @Override
            public void onFailure(Call<List<Sample>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }


}