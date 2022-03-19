package com.hmsl.fluidguest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "[SOCKET] Guest";

    private int port = 5673;
    private String ip = "192.168.0.22";

    private Handler mHandler = new Handler();
    private Handler workerHandler;

    public Socket socket;
    private LinearLayout container;
    private ImageView sample;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        container = (LinearLayout) findViewById(R.id.layout);

        ClientThread socketThread = new ClientThread();
        socketThread.start();

        WorkerThread worker = new WorkerThread();
        worker.start();
    }

    class ClientThread extends Thread {
        private static final int MAX_BUFFER = 1024;

        @Override
        public void run() {
            try {
                Log.d("TAG", "before make socket");
                socket = new Socket(ip, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (socket != null) {
                try {
                    InputStream is = socket.getInputStream();
                    byte[] input = new byte[MAX_BUFFER];
                    if (is.read(input) != 0) {
                        Message msg = Message.obtain();
                        msg.obj = input;
                        workerHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class WorkerThread extends Thread {

        public void run() {
            Looper.prepare();

            workerHandler = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    byte[] input = (byte[]) msg.obj;
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(input);
                    DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
                    try {
                        boolean isUpdate = dataInputStream.readBoolean();
                        Log.d(TAG, "" + isUpdate);
                        Bitmap bm = BitmapFactory.decodeByteArray(input, 1, input.length - 1);
                        if (!isUpdate) {
                            Log.d(TAG, "distribute mode");
                            Log.e(TAG, "Bitmap = " + bm);
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    load_image(bm);
                                }
                            });
                        } else {
                            Log.d("TAG", "update mode");
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    set_image_bipmap(bm);
                                }
                            });
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            Looper.loop();
        }

    }

    private void set_image_bipmap(Bitmap bm) {
        sample.setImageBitmap(bm);
    }

    public void load_image(Bitmap bm) {
        sample = new ImageView(this);
        sample.setImageBitmap(bm);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        sample.setLayoutParams(lp);
        container.addView(sample);
    }
}