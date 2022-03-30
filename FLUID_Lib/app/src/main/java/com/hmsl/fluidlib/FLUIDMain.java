package com.hmsl.fluidlib;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FLUIDMain {
    private static final String TAG = "FLUID(FLUIDMain)";
    public static com.hmsl.fluidmanager.IFLUIDService mRemoteService = null;
    static Bitmap[] bitmapArr;

    public ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRemoteService = com.hmsl.fluidmanager.IFLUIDService.Stub.asInterface(service);
            Log.d(TAG, "FLUIDManagerService connected = " + mRemoteService);

        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "FLUIDManagerService disconnected = " + mRemoteService);
            mRemoteService = null;
        }
    };

    public static void runtest(Bitmap[] sampleArr, Object index)
    {
        bitmapArr = sampleArr;
        Bundle bundle = new Bundle();

        try {
            bundle.putByteArray("key",generate_byteArray(sampleArr, (int)index));
            mRemoteService.test(bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] generate_byteArray(Bitmap[] sampleArr, int index) throws IOException{
        byte[] dtoByteArray=null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        sampleArr[index].compress(Bitmap.CompressFormat.JPEG,50,dataOutputStream);
        Log.d(TAG, "Distribute sampleArr["+index+"] : "+sampleArr[index]);

        dataOutputStream.flush();
        dtoByteArray = byteArrayOutputStream.toByteArray();
        return dtoByteArray;
    }

    public static void runUpdate(Object index){
        Bundle bundle = new Bundle();
        try {
            bundle.putByteArray("key",generate_ubyteArray((int)index));
            mRemoteService.update(bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] generate_ubyteArray(int index) throws IOException {
        byte[] dtoByteArray=null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        // Update가 발생하면, TargetApp에서 받은 index를 통해 해당 이미지의 Bitmap을 stream에 넣어준다.
        bitmapArr[index].compress(Bitmap.CompressFormat.JPEG,50,dataOutputStream);
        Log.d(TAG, "Update sampleArr["+index+"] : "+bitmapArr[index]);

        dataOutputStream.flush();
        dtoByteArray = byteArrayOutputStream.toByteArray();
        return dtoByteArray;
    }
}