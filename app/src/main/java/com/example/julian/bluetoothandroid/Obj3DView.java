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
import android.os.Looper;
import android.os.Message;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import static com.example.julian.bluetoothandroid.DisplaySensors.bluetoothIn;
import static com.example.julian.bluetoothandroid.MainActivity.btAdapter;
import static com.example.julian.bluetoothandroid.MainActivity.mDevice;


public class Obj3DView extends RendererActivity {

    public static float pitch = 0;
    public static float roll = 0;

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


    }



    @Override
    public void updateScene() {

        DisplaySensors.mConnectedThread.write("6".getBytes());
        faceObject3D.rotation().x = pitch;
        faceObject3D.rotation().z = roll;
        try {
            Thread.sleep(100);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }


}