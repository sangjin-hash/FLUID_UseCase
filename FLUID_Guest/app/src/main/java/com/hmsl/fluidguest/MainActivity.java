package com.hmsl.fluidguest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "[SOCKET] Guest";

    private int port = 5673;
    private String ip = "192.168.0.28";

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
                socket = new Socket(ip, port);
                Log.d(TAG, "Connected");

                while (true) {
                    InputStream is = socket.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(is);

                    byte[] imageBuffer = null;
                    byte[] flagBuffer = new byte[1];
                    int size = 0;

                    byte[] buffer = new byte[MAX_BUFFER];
                    int read;
                    while ((read = bis.read(buffer)) != -1) {
                        if (imageBuffer == null) {
                            /*
                            FLUIDManager에서 Bitmap이 담긴 byte 배열의 길이를 별도의 4 바이트 배열을 만들어 가장 처음에
                            보내기 때문에, 해당 크기만큼 가장 먼저 받아야 한다.
                             */
                            Log.i(TAG, "[TIME] Socket Init received " + getTS());
                            byte[] sizeBuffer = new byte[4];
                            System.arraycopy(buffer, 0, sizeBuffer, 0, sizeBuffer.length);

                            // 크기 다음으로 isUpdate flag 값을 받으므로 1바이트만 읽어온다.
                            System.arraycopy(buffer, sizeBuffer.length, flagBuffer, 0, flagBuffer.length);

                            size = getInt(sizeBuffer);

                            // 위에서 읽은 5바이트 만큼 빼준다. 그 뒤로 Bitmap이 담긴 byte 배열을 받는다.
                            read -= (sizeBuffer.length + 1);

                            imageBuffer = new byte[read];
                            System.arraycopy(buffer, sizeBuffer.length + 1, imageBuffer, 0, read);
                        } else {
                            byte[] preImageBuffer = imageBuffer.clone();
                            imageBuffer = new byte[read + preImageBuffer.length];
                            System.arraycopy(preImageBuffer, 0, imageBuffer, 0, preImageBuffer.length);
                            System.arraycopy(buffer, 0, imageBuffer, imageBuffer.length - read, read);
                        }

                        // 읽고자 하는 Bitmap byte 배열 크기만큼 모두 읽은 이후 처리
                        if (imageBuffer.length >= size) {
                            Log.i(TAG, "[TIME] Socket Receiving Complete " + getTS());
                            Bundle bundle = new Bundle();
                            bundle.putByteArray("Data", imageBuffer);

                            Message msg = workerHandler.obtainMessage();
                            msg.setData(bundle);
                            msg.what = flagBuffer[0];

                            workerHandler.sendMessage(msg);
                            imageBuffer = null;
                            size = 0;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private int getInt(byte[] sizeBuffer) {
            int s1 = sizeBuffer[0] & 0xFF;
            int s2 = sizeBuffer[1] & 0xFF;
            int s3 = sizeBuffer[2] & 0xFF;
            int s4 = sizeBuffer[3] & 0xFF;

            return ((s1 << 24) + (s2 << 16) + (s3 << 8) + (s4 << 0));
        }
    }

    class WorkerThread extends Thread {

        public void run() {
            Looper.prepare();

            workerHandler = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    byte[] data = msg.getData().getByteArray("Data");
                    int flag = msg.what;
                    boolean isUpdate = (flag == 1) ? true : false;

                    try {
                        Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (!isUpdate) {
                            Log.d(TAG, "distribute mode");
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
                    } catch (Exception e) {
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

    public String getTS()
    {
        Long tsLong = System.nanoTime();
        String ts = tsLong.toString();
        return ts;
    }
}