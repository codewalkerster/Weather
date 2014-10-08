package com.hardkernel.odroid.weather;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.Arrays;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android_serialport_api.SerialPort;

import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewStyle.GridStyle;
import com.jjoe64.graphview.LineGraphView;

public class MainActivity extends Activity {

    protected Application mApplication;
    protected SerialPort mSerialPort;
    protected InputStream mInputStream;
    private ReadThread mReadThread;
    byte[] buf = new byte[64];
    byte[] rbuf = new byte[4];
    int index;

    private GraphViewSeries mPressureSeries;
    private GraphViewSeries mAltitudeSeries;
    private GraphViewSeries mTemperatureSeries;
    private GraphViewSeries mHumiditySeries;
    private GraphViewSeries mUVIndexSeries;
    private GraphViewSeries mVisibleSeries;
    private GraphViewSeries mIRSeries;

    private GraphViewData[] mPressureData;
    private GraphViewData[] mAltitudeData;
    private GraphViewData[] mTemperatueData;
    private GraphViewData[] mHumidityData;
    private GraphViewData[] mUVIndexData;
    private GraphViewData[] mVisibleData;
    private GraphViewData[] mIRData;
    private GraphView mGraphView1;
    private GraphView mGraphView2;
    
    private MyLinearLayout mLayout1;
    private MyLinearLayout mLayout2;

    private int mGraphX;
    private final static int MAX_DATA = 5000;
    private final static int VIEW_PORT_WIDTH = 100;

    private Handler mHandler;
    
    private TextView mTv_Visible;
    private TextView mTv_IR;
    private TextView mTv_UV;
    private TextView mTv_hPa;
    private TextView mTv_Altitude;
    private TextView mTv_Celsius;
    private TextView mTv_Humidity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mApplication = (Application) getApplication();
        try {
            mSerialPort = mApplication.getSerialPort();
            mInputStream = mSerialPort.getInputStream();
        } catch (InvalidParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        mTv_Visible = (TextView) findViewById(R.id.tv_visible);
        mTv_Visible.setTextColor(Color.YELLOW);
        mTv_IR = (TextView) findViewById(R.id.tv_ir);
        mTv_IR.setTextColor(Color.WHITE);
        mTv_UV = (TextView) findViewById(R.id.tv_uv);
        mTv_UV.setTextColor(Color.GRAY);
        mTv_hPa = (TextView) findViewById(R.id.tv_hpa);
        mTv_hPa.setTextColor(Color.CYAN);
        mTv_Altitude = (TextView) findViewById(R.id.tv_m);
        mTv_Altitude.setTextColor(Color.GREEN);
        mTv_Celsius = (TextView) findViewById(R.id.tv_celsius);
        mTv_Celsius.setTextColor(Color.RED);
        mTv_Humidity = (TextView) findViewById(R.id.tv_humidity);
        mTv_Humidity.setTextColor(Color.BLUE);

        mPressureData = new GraphViewData[MAX_DATA];
        mAltitudeData = new GraphViewData[MAX_DATA];
        mTemperatueData = new GraphViewData[MAX_DATA];
        mHumidityData = new GraphViewData[MAX_DATA];
        mUVIndexData = new GraphViewData[MAX_DATA];
        mVisibleData = new GraphViewData[MAX_DATA];
        mIRData = new GraphViewData[MAX_DATA];

        for (int i = 0; i < MAX_DATA; i++) {
            mPressureData[i] = new GraphViewData(i, 0);
            mAltitudeData[i] = new GraphViewData(i, 0);
            mTemperatueData[i] = new GraphViewData(i, 0);
            mHumidityData[i] = new GraphViewData(i, 0);
            mUVIndexData[i] = new GraphViewData(i, 0);
            mVisibleData[i] = new GraphViewData(i, 0);
            mIRData[i] = new GraphViewData(i, 0);
        }

        mPressureSeries = new GraphViewSeries("Pressure", new GraphViewSeriesStyle(Color.CYAN, 3), mPressureData);
        mAltitudeSeries = new GraphViewSeries("Altitude", new GraphViewSeriesStyle(Color.GREEN, 3), mAltitudeData);
        mTemperatureSeries = new GraphViewSeries("Celsius", new GraphViewSeriesStyle(Color.RED, 3), mTemperatueData);
        mHumiditySeries = new GraphViewSeries("Humidity", new GraphViewSeriesStyle(Color.BLUE, 3), mHumidityData);
        mUVIndexSeries = new GraphViewSeries("UV Index", new GraphViewSeriesStyle(Color.GRAY, 3), mUVIndexData);
        mVisibleSeries = new GraphViewSeries("Visible", new GraphViewSeriesStyle(Color.YELLOW, 3), mVisibleData);
        mIRSeries = new GraphViewSeries("IR", new GraphViewSeriesStyle(Color.WHITE, 3), mIRData);

        mGraphView1 = new BarGraphView(
            this, "Si1132"
        );

        mGraphView1.addSeries(mVisibleSeries);
        mGraphView1.addSeries(mIRSeries);
        mGraphView1.addSeries(mUVIndexSeries);

        mGraphView1.setViewPort(1, VIEW_PORT_WIDTH);
        mGraphView1.setScalable(true);
        mGraphView1.setScrollable(true);
        mGraphView1.setHorizontalLabels(new String[] {"Start", "", "", "", "", "", "", "", "", "End"});
        mGraphView1.setVerticalLabels(new String[] {"Max", "", "", "", "", "", "", "", "", "Min"});
        mGraphView1.getGraphViewStyle().setGridStyle(GridStyle.HORIZONTAL);

        mGraphView1.setShowLegend(true);
        mGraphView1.setLegendAlign(LegendAlign.BOTTOM);
        mGraphView1.setLegendWidth(200);
        
        mLayout1 = (MyLinearLayout) findViewById(R.id.graph_layout1);
        mLayout1.addView(mGraphView1);

        mGraphView2 = new LineGraphView(
            this, "BMP180 & Si7020"
        );

        mGraphView2.addSeries(mPressureSeries);
        mGraphView2.addSeries(mAltitudeSeries);
        mGraphView2.addSeries(mTemperatureSeries);
        mGraphView2.addSeries(mHumiditySeries);

        mGraphView2.setViewPort(1, VIEW_PORT_WIDTH);
        mGraphView2.setScalable(true);
        mGraphView2.setScrollable(true);
        mGraphView2.setHorizontalLabels(new String[] {"Start", "", "", "", "", "", "", "", "", "End"});
        mGraphView2.setVerticalLabels(new String[] {"Max", "", "", "", "", "", "", "", "", "Min"});
        mGraphView2.getGraphViewStyle().setGridStyle(GridStyle.HORIZONTAL);

        mGraphView2.setShowLegend(true);
        mGraphView2.setLegendAlign(LegendAlign.BOTTOM);
        mGraphView2.setLegendWidth(200);

        mLayout2 = (MyLinearLayout) findViewById(R.id.graph_layout2);
        mLayout2.addView(mGraphView2);

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                
                mPressureSeries.resetData(mPressureData);
                mAltitudeSeries.resetData(mAltitudeData);
                mTemperatureSeries.resetData(mTemperatueData);
                mHumiditySeries.resetData(mHumidityData);
                mUVIndexSeries.resetData(mUVIndexData);
                mVisibleSeries.resetData(mVisibleData);
                mIRSeries.resetData(mIRData);
                
                mTv_Visible.setText(mVisibleData[msg.what].getY() * 10 + "Lux");
                mTv_IR.setText(mIRData[msg.what].getY() * 10 + "Lux");
                mTv_UV.setText(mUVIndexData[msg.what].getY() + "");
                mTv_hPa.setText(mPressureData[msg.what].getY() + "hPa");
                mTv_Altitude.setText(mAltitudeData[msg.what].getY() + "m");
                mTv_Celsius.setText(mTemperatueData[msg.what].getY() + "°C");
                mTv_Humidity.setText(mHumidityData[msg.what].getY() + "%");
            }
        };    
    }

    private class ReadThread extends Thread {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            while (!isInterrupted()) {
                if (mLayout1.isTouching() || mLayout2.isTouching())
                    continue;
                try {
                    byte[] buffer = new byte[64];
                    if (mInputStream == null)
                        return;
                    if (updateData(buffer)) {
                        Message msg = mHandler.obtainMessage();
                        msg.what = mGraphX;
                        mHandler.sendMessage(msg);
                        if (mGraphX > 70) {
                            mGraphView1.setViewPort(mGraphX - 70, VIEW_PORT_WIDTH);
                            mGraphView2.setViewPort(mGraphX - 70, VIEW_PORT_WIDTH);
                        }
                        mGraphX++;
                        if (mGraphX >= MAX_DATA) {
                            mGraphX = 0;
                            for (int i = 0; i < MAX_DATA; i++) {
                                mPressureData[i] = new GraphViewData(i, 0);
                                mAltitudeData[i] = new GraphViewData(i, 0);
                                mTemperatueData[i] = new GraphViewData(i, 0);
                                mHumidityData[i] = new GraphViewData(i, 0);
                                mUVIndexData[i] = new GraphViewData(i, 0);
                                mVisibleData[i] = new GraphViewData(i, 0);
                                mIRData[i] = new GraphViewData(i, 0);
                            }
                            mGraphView1.setViewPort(1, VIEW_PORT_WIDTH);
                            mGraphView2.setViewPort(1, VIEW_PORT_WIDTH);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
            
        }       
    }

    public boolean updateData(byte[] buffer) throws IOException {
        boolean eol = false;
        Arrays.fill(buffer, (byte) 0);
        mInputStream.read(buffer, 0, 1);
        if (buffer[0] == 'w') {
            int i = 0;
            while (buffer[0] != 0x1b) {
                mInputStream.read(buffer, 0, 1);
                buf[i] = buffer[0];
                i++;
                if (i > 8)
                    break;
            }
            buf[i-1] = '\0';
            index = buf[0];
            buf = shiftByte(buf, i-1);
            switch (index) {
            case '0':   //bmp180 Temperature
                break;
            case '1':   //bmp180 Pressure
                String str = new String(buf).split("\0")[0];
                mPressureData[mGraphX] = new GraphViewData(mGraphX, Double.parseDouble(str) / 100);
                //Log.d("info", "bmp1800 Pressure : " + str);
                break;
            case '2':   //bmp180 Altitude
                str = new String(buf).split("\0")[0];
                mAltitudeData[mGraphX] = new GraphViewData(mGraphX, Double.parseDouble(str));
                //Log.d("info", "bmp1800 Altitude : " + str);
                break;
            case '3':   //si7020 Temperature
                str = new String(buf).split("\0")[0];
                mTemperatueData[mGraphX] = new GraphViewData(mGraphX, Double.parseDouble(str));
                //Log.d("info", "si7020 Temperature : " + str);
                break;
            case '4':   //si7020 Humidity
                str = new String(buf).split("\0")[0];
                mHumidityData[mGraphX] = new GraphViewData(mGraphX, Double.parseDouble(str));
                //Log.d("info", "si7020 Humidity : " + str);
                break;
            case '5':   //si1132 UV Index
                str = new String(buf).split("\0")[0];
                mUVIndexData[mGraphX] = new GraphViewData(mGraphX, Double.parseDouble(str));
                //Log.d("info", "si1132 UV Index : " + str);
                break;
            case '6':   //si1132 Visible
                str = new String(buf).split("\0")[0];
                mVisibleData[mGraphX] = new GraphViewData(mGraphX, Double.parseDouble(str) / 10);
                //Log.d("info", "si1132 Visible : " + str);
                break;
            case '7':   //si1132 IR
                str = new String(buf).split("\0")[0];
                mIRData[mGraphX] = new GraphViewData(mGraphX, Double.parseDouble(str) /  10);
                //Log.d("info", "si1132 IR : " + str);
                eol = true;
                break;
            }
        }
        return eol;
    }

    public byte[] shiftByte(byte[] buf, int index) {
        for (int i = 0; i < index; i++) {
            buf[i] = buf[i+1];
        }
        return buf;
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        
        mReadThread.interrupt();
        mReadThread = null;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        
        mGraphX = 0;
        mReadThread = new ReadThread();
        mReadThread.start();
    }
}