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
import java.io.DataOutputStream;
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
            Log.i(TAG, "[TIME] RPC Distribute Method Received");
            bundle.setClassLoader(getClass().getClassLoader());
            byte[] recvBuffer = bundle.getByteArray("key");

            if(!isDistribute){
                Message msg = Message.obtain();
                msg.obj = recvBuffer;
                distributeHandler.sendMessage(msg);
                Log.d(TAG, "Distribute Message Send");
            }
        }

        // update
        public void update(Bundle bundle) {
            Log.i(TAG, "[TIME] RPC Update Method Received");
            bundle.setClassLoader(getClass().getClassLoader());
            byte[] recvBuffer = bundle.getByteArray("key");

            Message msg = Message.obtain();
            msg.obj = recvBuffer;
            updateHandler.sendMessage(msg);
            Log.d(TAG, "Update Message Send");
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
                    Log.e(TAG, "Distribute Message Receive");

                    // isDistribute => Guest에 최초 Distribute가 되었는지 판단하는 flag
                    isDistribute = true;

                    try {
                        OutputStream os = socket.getOutputStream();
                        DataOutputStream dataOutputStream = new DataOutputStream(os);
                        byte[] data = (byte[]) msg.obj;
                        byte[] size = getByte(data.length);

                        dataOutputStream.write(size, 0, size.length);
                        dataOutputStream.flush();

                        dataOutputStream.writeBoolean(false);
                        dataOutputStream.flush();

                        dataOutputStream.write(data, 0, data.length);
                        dataOutputStream.flush();

                        Log.i(TAG, "[TIME] Socket Send");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            // Update 담당 Handler
            updateHandler = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    Log.e(TAG, "Update Message Receive");
                    try {
                        if (isDistribute) {
                            OutputStream os = socket.getOutputStream();
                            DataOutputStream dataOutputStream = new DataOutputStream(os);
                            byte[] data = (byte[]) msg.obj;
                            byte[] size = getByte(data.length);

                            dataOutputStream.write(size, 0, size.length);
                            dataOutputStream.flush();

                            dataOutputStream.writeBoolean(true);
                            dataOutputStream.flush();

                            dataOutputStream.write(data, 0, data.length);
                            dataOutputStream.flush();
                            Log.i(TAG, "[TIME] Socket Send");
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

        private byte[] getByte(int num) {
            byte[] buf = new byte[4];
            buf[0] = (byte)((num >>> 24) & 0xFF);
            buf[1] = (byte)((num >>> 16) & 0xFF);
            buf[2] = (byte)((num >>> 8) & 0xFF);
            buf[3] = (byte)((num >>> 0) & 0xFF);

            return buf;
        }
    }
}