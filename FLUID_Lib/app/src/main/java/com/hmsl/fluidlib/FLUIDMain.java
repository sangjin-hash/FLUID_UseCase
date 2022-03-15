package com.hmsl.fluidlib;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FLUIDMain {
    private static final String TAG = "FLUID(FLUIDMain)";
    public static com.hmsl.fluidmanager.IFLUIDService mRemoteService = null;
    private static final int MAX_BUFFER = 1024;

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

    // todo : FLUID_TargetApp 에서 이미지 url이 저장된 String 배열과, Target_App에서 rendering 중인 이미지가 몇번째인지 알아야 한다.
    public static void runtest(String[] sampleArr, Object index)
    {
        Log.d(TAG, "sampleArr 주소값 : " + sampleArr);
        Log.d(TAG, "Index : " + (int)index);

        Bundle bundle = new Bundle();

        try {
            bundle.putByteArray("key",generate_byteArray(sampleArr, (int)index));
            mRemoteService.test(bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // byte[] 에 들어가야 할 것 -> flag(boolean) / string[] / index
    public static byte[] generate_byteArray(String[] sampleArr, int index) throws IOException{
        byte[] dtoByteArray=null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        dataOutputStream.writeBoolean(false);  // update x

        // String[] 은 지원 x
        for(int i = 0; i< sampleArr.length; i++){
            dataOutputStream.writeUTF(sampleArr[i]);
            Log.d(TAG, "sampleArr["+i+"] : "+sampleArr[i]);
        }

        // TargetApp의 이미지에 해당하는 index
        dataOutputStream.writeInt(index);
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
        dataOutputStream.writeBoolean(true);  // update o
        dataOutputStream.writeInt(index);
        dataOutputStream.flush();
        dtoByteArray = byteArrayOutputStream.toByteArray();
        return dtoByteArray;
    }
}