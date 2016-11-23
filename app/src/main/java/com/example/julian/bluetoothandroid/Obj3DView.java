package com.example.julian.bluetoothandroid;

/**
 * Created by Julian on 11/17/2016.
 */
import min3d.core.Object3dContainer;
import min3d.core.RendererActivity;
import min3d.parser.IParser;
import min3d.parser.Parser;
import min3d.vos.Light;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.hardware.*;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.os.Message;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import static com.example.julian.bluetoothandroid.MainActivity.btAdapter;
import static com.example.julian.bluetoothandroid.MainActivity.mDevice;


public class Obj3DView extends RendererActivity {

    Handler bluetoothIn3D;
    public float pitch = 0;
    public float roll = 0;

    private StringBuilder recDataString = new StringBuilder();
    final int handlerRotation= 5;



    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    ConnectThread3D myConnectThread = null;
    ConnectedThread3D myConnectedThread = null;


    //setConnection();


    private Object3dContainer faceObject3D;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void initScene() {


        scene.lights().add(new Light());
        scene.lights().add(new Light());

        Light myLight = new Light();
        myLight.position.setZ(150);
        scene.lights().add(myLight);

        IParser myParser = Parser.createParser(Parser.Type.OBJ, getResources(), "com.example.julian.bluetoothandroid:raw/buoym_obj", true);
        myParser.parse();

        faceObject3D = myParser.getParsedObject();
        faceObject3D.position().x = faceObject3D.position().y = faceObject3D.position().z = 0;
        faceObject3D.scale().x = faceObject3D.scale().y = faceObject3D.scale().z = 0.5f;
// Depending on the model you will need to change the scale
        //faceObject3D.scale().x = faceObject3D.scale().y = faceObject3D.scale().z = 0.009f;

        scene.addChild(faceObject3D);


        setConnection();
    }

    private void setConnection() {


        bluetoothIn3D = new Handler()
        {
            public void handleMessage(Message msg) {


                byte[] writeBuff = (byte[]) msg.obj;


                System.out.println(writeBuff.length);
                System.out.println(msg.what);


                if (msg.what == handlerRotation) {

                    String readMessage = new String(writeBuff);
                    String stringVal;

                    System.out.println( readMessage );
                    recDataString.append(readMessage);




                    int initialPos = 0, finalPos;

                    for (int k = initialPos; k < recDataString.length(); k++)
                    {
                        if (recDataString.charAt(k) == '^')
                        {

                            finalPos = recDataString.indexOf("@") - 1;
                            stringVal = recDataString.substring(initialPos, finalPos);
                            pitch = Float.parseFloat(stringVal);
                            initialPos = k + 1;

                            System.out.println( k );
                        }
                        else if (recDataString.charAt(k) == '@')
                        {
                            finalPos = recDataString.indexOf("#") - 1;
                            stringVal = recDataString.substring(initialPos, finalPos);
                            roll = Float.parseFloat(stringVal);
                        }
                    }

                    recDataString.delete(0,recDataString.length());
                }
            }
        };


        myConnectThread = new ConnectThread3D(mDevice);
        myConnectThread.start();


    }

    @Override
    public void updateScene() {

        faceObject3D.rotation().x = pitch;
        faceObject3D.rotation().z = roll;
//        try {
//            Thread.sleep(1000);
//        } catch(InterruptedException ex) {
//            Thread.currentThread().interrupt();
//        }
    }



    public class ConnectThread3D extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread3D(BluetoothDevice device)
        {
            BluetoothSocket tmp = null;
            mmDevice = device;

            try
            {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            }
            catch(IOException e){
                System.out.println("1Got an IOException: " + e.getMessage());
            }

            mmSocket = tmp;

        }

        public void run()
        {

            if (btAdapter.isDiscovering())
            {
                btAdapter.cancelDiscovery();
            }
            try
            {
                mmSocket.connect();
            }
            catch(IOException connectExc)
            {
                System.out.println("2Got an IOException: " + connectExc.getMessage());
                try
                {
                    mmSocket.close();
                }
                catch(IOException closeExc)
                {
                    System.out.println("3Got an IOException: " + closeExc.getMessage());
                    return;
                }
            }

            myConnectedThread = new ConnectedThread3D( mmSocket);
            myConnectedThread.start();


        }

        public void cancel()
        {
            try
            {
                mmSocket.close();
            }
            catch(IOException closeExc){
                System.out.println("4Got an IOException: " + closeExc.getMessage());
            }

        }

    }

    public class ConnectedThread3D extends Thread
    {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread3D(BluetoothSocket socket)
        {

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch(IOException e){
                System.out.println("5Got an IOException: " + e.getMessage());
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;

        }

//
//        handlerStateAir = 0               "?"
//        handlerStateWater = 1;            "!"
//        handlerStateSpeed = 2;            "*"
//        handlerStateSalinity = 3;         "%"
//        handlerStateWave = 4;             "$"


        public void run()
        {

            byte[] buffer = new byte[1024];
            int begin = 0;
            int bytes = 0;
            int handlerType = -1;

            while (true)
            {
                try
                {
                    System.out.println("trying to get ROTATION DATA!");
                    bytes += mmInStream.read(buffer, bytes, buffer.length - bytes);
                    for( int i = begin; i < bytes; i++ )
                    {
                        if ( i == begin )
                        {

                            if (buffer[i] == "^".getBytes()[0])
                            {
                                handlerType = 5;
                            }

                            System.out.println( handlerType );

                        }


                        if (buffer[i] == "#".getBytes()[0])
                        {
                            System.out.println( i );
                            System.out.println( bytes );

                            bluetoothIn3D.obtainMessage(handlerType, begin, i, buffer).sendToTarget();
                            begin = 0;
                            bytes = 0;
//                            if( i == bytes - 1 )
//                            {
//                                bytes = 0;
//                                begin = 0;
//                            }

                        }
                    }
                }
                catch(IOException e)
                {
                    System.out.println("6Got an IOException: " + e.getMessage());
                    break;
                }
            }



        }

        public void write(byte[] bytes)
        {
            try
            {
                mmOutStream.write(bytes);
            }
            catch(IOException e)
            {
                System.out.println("7Got an IOException: " + e.getMessage());
            }
        }

        public void cancel()
        {
            try
            {
                mmSocket.close();

            }
            catch(IOException closeExc){
                System.out.println("8Got an IOException: " + closeExc.getMessage());
            }
        }

    }
}