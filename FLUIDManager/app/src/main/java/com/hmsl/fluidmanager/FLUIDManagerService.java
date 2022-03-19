package com.hmsl.fluidmanager;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FLUIDManagerService extends Service {
    private static final String TAG = "[FLUIDManager]";
    private final int port = 5673;
    private static boolean isDistribute = false;

    private Handler distributeHandler;
    private Handler updateHandler;
    ServerSocket server;
    Socket socket;

    private final IBinder mBinder = new IFLUIDService.Stub() {
        // distribute
        public void test(Bundle bundle) {
            bundle.setClassLoader(getClass().getClassLoader());
            byte[] recvBuffer = bundle.getByteArray("key");
            Log.d("TAG", "Distribute byte[] : " + recvBuffer);

            if(!isDistribute){
                Message msg = Message.obtain();
                msg.obj = recvBuffer;
                distributeHandler.sendMessage(msg);
                Log.e(TAG, "Message 전송");
            }
        }

        // update
        public void update(Bundle bundle) {
            bundle.setClassLoader(getClass().getClassLoader());
            byte[] recvBuffer = bundle.getByteArray("key");
            Log.d("TAG", "Update byte[] : " + recvBuffer);

            Message msg = Message.obtain();
            msg.obj = recvBuffer;
            updateHandler.sendMessage(msg);
            Log.e(TAG, "Message 전송");
        }
    };

    public FLUIDManagerService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            ServerThread server = new ServerThread(port);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // ServerThread : ServerSocket 열기
    class ServerThread extends Thread {

        public ServerThread(int port) throws IOException {
            server = new ServerSocket(port);
        }

        @Override
        public void run() {
            try {
                socket = server.accept();
                Log.e(TAG, "Socket connected");
            } catch (IOException e) {
                e.printStackTrace();
            }

            Looper.prepare();

            // Distribute 담당 handler
            distributeHandler = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    byte[] input = (byte[]) msg.obj;
                    Log.e(TAG, "Distribute Message 받음 " + input);
                    isDistribute = true;
                    Log.e(TAG, "isDistribute : " + isDistribute);

                    try {
                        OutputStream os = socket.getOutputStream();
                        os.write(input);
                        Log.e(TAG, "UI distribute socket msg 전송 성공");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            // Update 담당 Handler
            updateHandler = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    byte[] input = (byte[]) msg.obj;
                    Log.e(TAG, "Update Message 받음"+ input);
                    try {
                        if (isDistribute) {
                            OutputStream os = socket.getOutputStream();
                            os.write(input);
                            Log.d("TAG", "update socket message sent");
                        } else {
                            Log.d("TAG", "undistributed UI's update");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            Looper.loop();
        }
    }
}