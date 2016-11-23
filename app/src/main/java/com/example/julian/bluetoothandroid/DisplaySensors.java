package com.example.julian.bluetoothandroid;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static com.example.julian.bluetoothandroid.MainActivity.btAdapter;
import static com.example.julian.bluetoothandroid.MainActivity.deviceList;
import static com.example.julian.bluetoothandroid.MainActivity.mDevice;
import static com.example.julian.bluetoothandroid.Obj3DView.pitch;
import static com.example.julian.bluetoothandroid.Obj3DView.roll;

public class DisplaySensors extends AppCompatActivity
{

    Button collectButton, previousButton;
    TextView airTemp, waterTemp, airSpeed, salinity, waveHeight;
    RadioButton air, water, speed, salt, wave, radioChosen;
    RadioGroup radioGroup;

    Button viewer3D;

    private StringBuilder recDataString = new StringBuilder();
    final int handlerStateAir = 0;
    final int handlerStateWater = 1;
    final int handlerStateSpeed = 2;
    final int handlerStateSalinity = 3;
    final int handlerStateWave = 4;
    final int handlerRotation= 5;
    public static Handler bluetoothIn;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");


    String datachosen;
    //MainActivity.ConnectThread mConnectThread = null;


    //BluetoothDevice btDevice = deviceList.get(position);


    ConnectThread mConnectThread = null;
    static ConnectedThread mConnectedThread = null;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_sensors);

        bluetoothIn = new Handler()
        {
            public void handleMessage(Message msg)
            {


                byte[] writeBuff = (byte[]) msg.obj;


                System.out.println( writeBuff.length );
                System.out.println( msg.what );


                if (msg.what == handlerStateAir)
                {
                    String readMessage = new String(writeBuff);

                    recDataString.append(readMessage);
                    int endOfLineIndex = recDataString.indexOf("#");
                    if (endOfLineIndex > 0 )
                    {
                        System.out.println( readMessage );
                        airTemp.setText(recDataString.substring(recDataString.indexOf("?") + 1 ,recDataString.indexOf("#")-1));

                    }
                }
                else if (msg.what == handlerStateWater)
                {

                    String readMessage = new String(writeBuff);

                    recDataString.append(readMessage);
                    int endOfLineIndex = recDataString.indexOf("#");
                    if (endOfLineIndex > 0 )
                    {
                       waterTemp.setText(recDataString.substring(recDataString.indexOf("!") + 1 ,recDataString.indexOf("#")-1));

                    }
                }
                else if (msg.what == handlerStateSpeed)
                {
                    String readMessage = new String(writeBuff);

                    recDataString.append(readMessage);
                    int endOfLineIndex = recDataString.indexOf("#");
                    if (endOfLineIndex > 0 )
                    {
                        airSpeed.setText(recDataString.substring(recDataString.indexOf("*") + 1 ,recDataString.indexOf("#")- 1));

                    }
                }
                else if (msg.what == handlerStateSalinity)
                {

                    String readMessage = new String(writeBuff);
                    System.out.println( readMessage );
                    String stringVal = readMessage.substring(readMessage.indexOf("%") + 1, readMessage.indexOf("#"));
                    System.out.println( stringVal );
                    float voltageValue, saltLevel;

                    voltageValue = Float.parseFloat(stringVal);

                    System.out.println( voltageValue );

                    //saltLevel = mapToSalt(voltageValue);

                    //System.out.println( saltLevel );

                    //recDataString.append(Integer.toString(saltLevel));
                    //salinity.setText(recDataString);
                    if (voltageValue < .54)
                    {
                        recDataString.append("There Is Salt!");
                    }
                    else
                        recDataString.append("No Salt!");

                    salinity.setText(recDataString);

                }
                else if (msg.what == handlerStateWave)
                {

                    String readMessage = new String(writeBuff);
                    float[] waveAccelerations = new float[150];

                    String stringVal;
                    int posArray = 0;

                    System.out.println( readMessage );
                    //System.out.println( );

                    recDataString.append(readMessage);


                    int initialPos = 1, finalPos;

                    for (int k = initialPos; k < recDataString.length(); k++)
                    {
                        if (recDataString.charAt(k) == '&')
                        {

                            //recDataString.charAt(k);
                            finalPos = k - 1;
                            //stringVal = readMessage.substring(initialPos, finalPos);
                            stringVal = recDataString.substring(initialPos, finalPos);
                            waveAccelerations[posArray] = Float.parseFloat(stringVal);
                            posArray++;
                            initialPos = k + 1;

                            System.out.println( k );
                        }
                    }
                    System.out.println( posArray );

                    double totalTime = 32;

                    recDataString.delete(0, recDataString.length());
                    recDataString.append(calculateHeight(waveAccelerations, totalTime));

                        waveHeight.setText(recDataString);
                }

                if (msg.what == handlerRotation) {

                    String readMessage = new String(writeBuff);
                    String stringVal;

                    System.out.println( readMessage );
                    recDataString.append(readMessage);




                    int initialPos = 0, finalPos;

                    for (int k = 0; k < recDataString.length(); k++)
                    {
                        if (recDataString.charAt(k) == '^')
                        {
                            initialPos = k + 1;
                            finalPos = recDataString.indexOf("@") - 1;
                            stringVal = recDataString.substring(initialPos, finalPos);
                            pitch = Float.parseFloat(stringVal);

                            System.out.println( k );
                        }
                        else if (recDataString.charAt(k) == '@')
                        {
                            initialPos = k + 1;
                            finalPos = recDataString.indexOf("#") - 1;
                            stringVal = recDataString.substring(initialPos, finalPos);
                            roll = Float.parseFloat(stringVal);
                        }
                    }

                    recDataString.delete(0,recDataString.length());
                }

                recDataString.delete(0, recDataString.length());
            }
        };


        mDevice.getName();

        int position = getIntent().getIntExtra("my Position", 0);

        System.out.println("Device: " + position);

        setupUI();

        BluetoothDevice btDevice = deviceList.get(position);

        System.out.println("Device: " + btDevice.getName());

        mConnectThread = new ConnectThread(btDevice);
        mConnectThread.start();

    }

    public String calculateHeight(float accele[], double totalTime)
    {
        int initialSign = 1, currentSign, j, accelCount = 0, initialPosition = 0, positionFinal, differencePosition = 0;
        int size = 150;
        double sum = 0, altitude = 0, timeTotal = 0, avg = 0;
        int opposite = 3;

        float avgAllwaves = 0;
        int wavesCount = 0;

        for( j = 0; j < size; j++)
        {

            if ( j == 0 )
            {
                if( accele[j] >= 0 )
                    initialSign = 1;
                else
                    initialSign = -1;
            }

            if ( accele[j] > 0 )
                currentSign = 1;
            else if (accele[j] < 0)
                currentSign = -1;
            else
                currentSign = 0;
            if ( ( currentSign != -1*initialSign ) )
            {
                sum  += accele[j];
                accelCount++;
                if ( ( opposite < 3 ) && ( currentSign == initialSign ) )
                {
                    opposite = 3;
                }
                else if (currentSign == 0)
                {
                    opposite--;
                }
            }
            else
                opposite--;

            if ( opposite == 0 )
            {
                System.out.println( "Sum:" + sum);
                System.out.println( "AccelCoun:" + accelCount);


                positionFinal = j - 2;

                if (accelCount>0)
                {
                    differencePosition = positionFinal - initialPosition;
                    avg = ( sum / accelCount );
                    timeTotal = ( differencePosition * totalTime * .001 );
                    altitude = (.5*avg*( timeTotal*timeTotal));
                }
                else
                {
                    avg = 0;
                    altitude = 0;
                }

                initialSign *= -1;
                sum = ( accele[j-2] + accele[j-1] + accele[j] );
                accelCount = 0;
                opposite = 3;
                initialPosition = j;

                avgAllwaves += altitude;
                wavesCount++;


                System.out.println( "altitude:" + altitude);
                System.out.println( "Difference:" + differencePosition);
                System.out.println( "avg:" + avg);
                System.out.println( "Avg all Waves:" + avgAllwaves);
            }

        }

        avgAllwaves = avgAllwaves/wavesCount;

        if(avgAllwaves < .1 && avgAllwaves > -.1)
            avgAllwaves = 0;

        System.out.println( avgAllwaves );

        String waveHeightString = Float.toString(avgAllwaves);

        return waveHeightString;

    }

    public void mapToSalt(float voltageValue)
    {
//        if (analogRead >= 0 && analogRead < 256)
//        {
//            return 0;
//        }
//        else if (analogRead >= 255 && analogRead < 512)
//        {
//            return 1;
//        }
//        else if (analogRead >=512  && analogRead < 768)
//        {
//            return 2;
//        }
//        else
//            return 3;

    }




    private void setupUI()
    {
        collectButton = (Button) findViewById(R.id.collectData);
        previousButton = (Button) findViewById(R.id.previousReading);
        airTemp = (TextView) findViewById(R.id.textViewAir);
        waterTemp = (TextView) findViewById(R.id.textViewWater);
        airSpeed = (TextView) findViewById(R.id.textViewSpeed);
        salinity = (TextView) findViewById(R.id.textViewSalt);
        waveHeight = (TextView) findViewById(R.id.textViewWave);
        air = (RadioButton) findViewById(R.id.radioAir);
        water = (RadioButton) findViewById(R.id.radioWater);
        speed = (RadioButton) findViewById(R.id.radioSpeed);
        salt = (RadioButton) findViewById(R.id.radioSalt);
        wave = (RadioButton) findViewById(R.id.radioWave);
        radioGroup = (RadioGroup) findViewById(R.id.sensorOptions);

        viewer3D = (Button) findViewById(R.id.view3D);


        collectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                radioChosen = (RadioButton) findViewById(selectedId);
                //String datachosen;

                System.out.println("selected ID: " + selectedId);

                if (radioChosen == air) {
                    datachosen = "1";
                    mConnectedThread.write(datachosen.getBytes());
                } else if (radioChosen == water) {
                    datachosen = "2";
                    mConnectedThread.write(datachosen.getBytes());
                } else if (radioChosen == speed) {
                    datachosen = "3";
                    mConnectedThread.write(datachosen.getBytes());
                } else if (radioChosen == wave) {
                    datachosen = "4";
                    mConnectedThread.write(datachosen.getBytes());
                } else if (radioChosen == salt) {
                    datachosen = "5";
                    mConnectedThread.write(datachosen.getBytes());
                }

            }

            });

        viewer3D.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v )
            {
                Toast.makeText(DisplaySensors.this, " You Selected 3D Viewer!", Toast.LENGTH_SHORT).show();
//
//            } else if (radioChosen == salt) {
//            datachosen = "5";
//            mConnectedThread.write(datachosen.getBytes());
                datachosen = "6";
                mConnectedThread.write(datachosen.getBytes());
                Intent intent = new Intent(DisplaySensors.this, Obj3DView.class);
                startActivity(intent);
            }
        });



    }






    public class ConnectThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;


        public ConnectThread(BluetoothDevice device)
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

            mConnectedThread = new ConnectedThread ( mmSocket);
            mConnectedThread.start();


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

    public class ConnectedThread extends Thread
    {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
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

            //Looper.prepare();

            byte[] buffer = new byte[1024];
            int begin = 0;
            int bytes = 0;
            int handlerType = -1;

            while (true)
            {
                try
                {
                    System.out.println("trying to get data!");
                    bytes += mmInStream.read(buffer, bytes, buffer.length - bytes);
                    for( int i = begin; i < bytes; i++ )
                    {
                        if ( i == begin )
                        {

                            if (buffer[i] == "?".getBytes()[0])
                            {
                                handlerType = 0;
                            }
                            else if (buffer[i] == "!".getBytes()[0])
                            {
                                handlerType = 1;
                            }
                            else if (buffer[i] == "*".getBytes()[0])
                            {
                                handlerType = 2;
                            }
                            else if (buffer[i] == "%".getBytes()[0])
                            {
                                handlerType = 3;
                            }
                            else if (buffer[i] == "$".getBytes()[0])
                            {
                                handlerType = 4;
                            }
                            else if(buffer[i] == "^".getBytes()[0])
                            {
                                handlerType = 5;
                            }

                            System.out.println( handlerType );

                        }

//                        System.out.println("This is the current value! ");
//                        System.out.print(buffer[i] + " : ");
//                        System.out.println((char)buffer[i]);


                        if (buffer[i] == "#".getBytes()[0])
                        {
                            System.out.println( i );
                            System.out.println( bytes );
                            bluetoothIn.obtainMessage(handlerType, begin, i, buffer).sendToTarget();
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

            //Looper.loop();

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
