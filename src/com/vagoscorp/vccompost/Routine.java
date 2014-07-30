package com.vagoscorp.vccompost;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class Routine extends Activity implements Runnable {

	SurfaceView reactorA;
	SurfaceView reactorB;
	SurfaceHolder holderA;
	SurfaceHolder holderB;
	TextView pTextA;
	Spinner spinnerA;
	TextView pTextB;
	Spinner spinnerB;
	Canvas canvasA;
	Canvas canvasB;
	Thread thread;
	boolean runn = false;
	
	Paint jaba = new Paint();
	Paint frio = new Paint();
	Paint bueno = new Paint();
	Paint tibio = new Paint();
	Paint caliente = new Paint();
	Paint touch = new Paint();
	
	float xA = 0;
	float yA = 0;
	float xB = 0;
	float yB = 0;
	float widthA;
	float heightA;
	float widthB;
	float heightB;
	float vXA;
	float vYA;
	float vXB;
	float vYB;
	int nvag = 1;
	File path;
	File[] fileList;
	
	boolean SDread = false;
	boolean SDwrite = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent info = getIntent();
		nvag = info.getIntExtra(Dat_vag.NVag, nvag);
		setContentView(R.layout.activity_routine);
		path = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Documents/VCCompost/Routines");
		path.mkdirs();
		setTitle("Rutina del Reactor "+nvag);
		reactorA = (SurfaceView)findViewById(R.id.reactorA);
		reactorB = (SurfaceView)findViewById(R.id.reactorB);
		spinnerA = (Spinner)findViewById(R.id.spinnerA);
		pTextA = (TextView)findViewById(R.id.ptextA);
		spinnerB = (Spinner)findViewById(R.id.spinnerB);
		pTextB = (TextView)findViewById(R.id.ptextB);
		
		jaba.setColor(Color.GRAY);
		jaba.setStrokeWidth(5);
		frio.setColor(Color.BLUE);
		bueno.setColor(Color.GREEN);
		tibio.setColor(Color.YELLOW);
		caliente.setColor(Color.RED);
		touch.setColor(Color.DKGRAY);
		touch.setAntiAlias(true);
		touch.setStrokeWidth(2);
		touch.setStrokeCap(Paint.Cap.ROUND);
		touch.setStyle(Paint.Style.STROKE);
		
		holderA = reactorA.getHolder();
		holderB = reactorB.getHolder();
		
		reactorA.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent me) {
				xA = me.getX();
				yA = me.getY();
				return true;
			}
		});
		reactorB.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent me) {
				xB = me.getX();
				yB = me.getY();
				return true;
			}
		});
		runn = false;
		getNames();
//		checkSD();
//		write("prueba", "soy un texto D:");
//		pText.setText(read("prueba"));
	}
	
	void checkSD() {
		String state = Environment.getExternalStorageState();
		if(Environment.MEDIA_MOUNTED.equals(state)) {
			SDread = true;
			SDwrite = true;
		}else if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			SDread = true;
			SDwrite = false;
		}else {
//			oops
			SDread = false;
			SDwrite = false;
		}
	}
	
	void write(String name, String data) {
		checkSD();
		if(SDread && SDwrite) {
			byte[] buff = data.getBytes();
			File file = new File(path, name + ".vccr");
			OutputStream os;
			try {
				os = new FileOutputStream(file);
				os.write(buff);
				os.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	String read(String name) {
		checkSD();
		String val = "";
		if(SDread) {
			byte[] buff;
			File file = new File(path, name + ".vccr");
			InputStream is;
			try {
				is = new FileInputStream(file);
				buff = new byte[is.available()];
				is.read(buff);
				is.close();
				val = new String(buff);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return val;
		
	}
	void getNames() {        
		fileList = path.listFiles();
//		String[] fileNames = {"aaa", "aab", "aac"};
		List<String> list = new ArrayList<String>();
		for(int i = 0; i < fileList.length; i++) {
			list.add(fileList[i].getName());
		}
		ArrayAdapter<String> fileAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
		spinnerA.setAdapter(fileAdapter);
		spinnerB.setAdapter(fileAdapter);
	}

	@Override
	protected void onStart() {
		
		super.onStart();
	}

	@Override
	public void run() {
		while(runn) {
			if(!holderA.getSurface().isValid() || !holderB.getSurface().isValid())
				continue;
			canvasA = holderA.lockCanvas();
			canvasB = holderB.lockCanvas();
			heightA = canvasA.getHeight();
			heightB = canvasB.getHeight();
			widthA = canvasA.getWidth();
			widthB = canvasB.getWidth();
			canvasA.drawARGB(255, 100, 180, 100);
			canvasB.drawARGB(255, 100, 180, 100);
//			canvasA.drawLine(widthA-20, 0, widthA-20, heightA, jaba);
//			canvasB.drawLine(20, 0, 20, heightB, jaba);
			drawtouch(canvasA, xA, yA, widthA, heightA);
			drawtouch(canvasB, xB, yB, widthB, heightB);
			holderA.unlockCanvasAndPost(canvasA);
			holderB.unlockCanvasAndPost(canvasB);
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
		}
	}
	
	public void drawtouch(Canvas canvas, float x, float y, float width, float height) {
		if(x <= 0)
			x = 0;
		if(y <= 0)
			y = 0;
		if(x >= width)
			x = width;
		if(y >= height)
			y = height;
		canvas.drawCircle(x, y, 50, touch);
	}

	@Override
	protected void onPause() {
		runn = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		thread = null;
		super.onPause();
	}

	@Override
	protected void onResume() {
		runn = true;
		thread = new Thread(this);
		thread.start();
		super.onResume();
	}



	@Override
	protected void onStop() {
		
		super.onStop();
	}



	@Override
	protected void onDestroy() {
		
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.routine, menu);
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
}
