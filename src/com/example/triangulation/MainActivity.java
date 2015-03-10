package com.example.triangulation;

 

 

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
 
 
 
 

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
 
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity    {
	TextView etTxt1;
	 
	private Button buttonStart;
	private Button buttonStop;
	float[] accelerometerValues = new float[3];
	float[] magneticFieldValues = new float[3];
	float[] orientationValues = new float[3];
	float[] values = new float[3];
	public double lat;
	public double lon;
	public float dis;
	float height=(float) 1.5;
	public LocationClient mLocationClient = null;
	public BDLocationListener myLocListener=null; 
	SensorManager sensorManager;
	private Sensor mSensor;
	private Sensor oSensor;
	private Camera mCamera;
	   private SurfaceView mSurfaceView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		etTxt1 = (TextView) findViewById(R.id.txt1);		 
		 
		 
		myLocListener = new MyLocationListener();
		mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
		mLocationClient.registerLocationListener( myLocListener );    //注册监听函数
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);//设置定位模式
		option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(1000);//设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(true);//返回的定位结果包含地址信息
		option.setNeedDeviceDirect(true);//返回的定位结果包含手机机头的方向
		option.setOpenGps(true);
		mLocationClient.setLocOption(option);
		// 获取系统的传感器管理服务		
		sensorManager = (SensorManager) getSystemService(
			Context.SENSOR_SERVICE);
        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);
        
        // Create our Preview view and set it as the content of our activity.
        mSurfaceView =  (SurfaceView) findViewById(R.id.crime_camera_surfaceView);
        SurfaceHolder holder = mSurfaceView.getHolder();
        // deprecated, but required for pre-3.0 devices
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {

            public void surfaceCreated(SurfaceHolder holder) {
                // tell the camera to use this surface as its preview area
                try {
                    if (mCamera != null) {
                        mCamera.setPreviewDisplay(holder);
                    }
                } catch (IOException exception) {
                   
                }
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                // we can no longer display on this surface, so stop the preview.
                if (mCamera != null) {
                    mCamera.stopPreview();
                }
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            	if (mCamera == null) return;
            	
                // the surface has changed size; update the camera preview size
                Camera.Parameters parameters = mCamera.getParameters();
                Size s = getBestSupportedSize(parameters.getSupportedPreviewSizes(), w, h);
                parameters.setPreviewSize(s.width, s.height);
                s = getBestSupportedSize(parameters.getSupportedPictureSizes(), w, h);
                parameters.setPictureSize(s.width, s.height);
                mCamera.setParameters(parameters);
                try {
                    mCamera.startPreview();
                } catch (Exception e) {
                    
                    mCamera.release();
                    mCamera = null;
                }
            }
        });
        sensorManager.registerListener(myListener,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);

		sensorManager.registerListener(myListener,
				sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_FASTEST);	
		sensorManager.registerListener(myListener,
				sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_FASTEST);
		mSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		oSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		 
		System.out.println( "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"+mSensor.getMinDelay());
	}
	protected void onResume()
	{
		super.onResume();
		// 为系统的加速度传感器注册监听器
		buttonStart=(Button)findViewById(R.id.button1);
		buttonStop=(Button)findViewById(R.id.button2);
		//buttonStop.setClickable(false);
		mLocationClient.start();
		buttonStop.setEnabled(false);
		buttonStart.setOnClickListener(new buttonStartListener());
		buttonStop.setOnClickListener(new buttonStopListener());
		
	}

	@Override
	protected void onPause()
	{
		//  取消定位
		mLocationClient.stop(); 
		super.onPause();
	}
	
	final SensorEventListener myListener=new SensorEventListener(){

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
				accelerometerValues=event.values;
			}
			if(event.sensor.getType()==Sensor.TYPE_ORIENTATION){
				orientationValues=event.values;
			}
			/*if(event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD){
				magneticFieldValues=event.values;
			}
			
			SensorManager.getRotationMatrix(rotate, null, accelerometerValues, magneticFieldValues);
			SensorManager.getOrientation(rotate, values);*/
			//经过SensorManager.getOrientation(rotate, values);得到的values值为弧度
			//转换为角度
			values=orientationValues;
			StringBuffer sb=new StringBuffer();
			
			
			if(values[1]>-90&&values[1]<0)
			{
			sb.append("\n向上夹角");
			sb.append(""+values[1]);
			sb.append("\n距离");
			  dis=MainActivity.getDis(0-values[1],height);
			sb.append(""+dis);
			}
			else
			{
				sb.append("\n请瞄准物体底部");
			}
			
			
			etTxt1.setText(sb.toString()); 
	       
			//values[1]=(float)Math.toDegrees(values[1]);
//			System.out.println("z="+values[1]);System.out.println("aaa="+accelerometerValues[2]);System.out.println(SystemClock.uptimeMillis());
		}};
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	class buttonStartListener implements View.OnClickListener //开始按钮监听器
	{
		//实现监听器类必须实现的方法，该方法将会作为事件处理器
		@SuppressWarnings("deprecation")
		@Override
	
		public void onClick(View sdw)
		{
		 
			buttonStop.setEnabled(true);
			buttonStart.setEnabled(false);
			sensorManager.registerListener(myListener,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_FASTEST);
 
			sensorManager.registerListener(myListener,
					sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
					SensorManager.SENSOR_DELAY_FASTEST);	
			sensorManager.registerListener(myListener,
					sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
					SensorManager.SENSOR_DELAY_FASTEST);
			mSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			oSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
			 
			System.out.println( "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"+mSensor.getMinDelay());
			
		}		
	}
	class buttonStopListener implements View.OnClickListener //停止按钮监听
	{
		@Override
		public void onClick(View sdw)
		{
		
			buttonStart.setEnabled(true);
			buttonStop.setEnabled(false);
			sensorManager.unregisterListener(myListener);
			StringBuffer sb=new StringBuffer();
			

			if(values[1]>-90&&values[1]<0)
			{
			sb.append("\n向上夹角");
			sb.append(""+values[1]);
			sb.append("\n距离");
			  dis=MainActivity.getDis(0-values[1],height);
			sb.append(""+dis);
			}
			else
			{
				sb.append("\n请瞄准物体底部");
			}
				sb.append("\n高度");
				sb.append(""+height);
				sb.append("\n与正北夹角");
				sb.append(""+values[0]);
				if((values[1]>-90)&&(values[1]<0))
				{
				
				}
				
				sb.append("\n目标经纬度：");
				sb.append(ConvertDistanceToLogLat(dis, lat,lon,values[0]));
			 
				 
			 
			etTxt1.setText(sb.toString()); 
		}
		
	}
	public class MyLocationListener implements BDLocationListener {
		 
		@Override
		public void onReceiveLocation(BDLocation location) {
			System.out.println("~~~~~~");
			if (location == null)
		            return ;
		 
			lat=location.getLatitude();
			lon=location.getLongitude();
		 
		}

		/**
		 * 显示请求字符串
		 * @param str
		 */
		public void logMsg(String str) {
			try {
				
				etTxt1.setText(str);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * 高精度地理围栏回调
		 * @author jpren
		 *
		 */
		
	}
	public static float getDis(float alpha,float height)//alpha为摄像头方向与竖直方向的夹角
	{
		float dis=0;
		dis=(float) (height*Math.tan(alpha*Math.PI/180));
		return dis;
	}
	
	private static String ConvertDistanceToLogLat(float distance, double lat1,double lng1, double angle)
    {
        String logLat = null;       
        double lon = lng1 + (distance* Math.sin(angle* Math.PI / 180)/1000) / (111 * Math.cos(lat1 * Math.PI / 180));//将距离转换成经度的计算公式
        double lat = lat1 + (distance* Math.cos(angle* Math.PI / 180)/1000) / 111;//将距离转换成纬度的计算公式

        logLat =   + lat + "," + lon   ;
        return logLat;
    }
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}
    private Size getBestSupportedSize(List<Size> sizes, int width, int height) {
        Size bestSize = sizes.get(0);
        int largestArea = bestSize.width * bestSize.height;
        for (Size s : sizes) {
            int area = s.width * s.height;
            if (area > largestArea) {
                bestSize = s;
                largestArea = area;
            }
        }
        return bestSize;
    }
	 
}
