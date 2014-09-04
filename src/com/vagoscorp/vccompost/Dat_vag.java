package com.vagoscorp.vccompost;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public class Dat_vag extends Activity implements Runnable,OnTouchListener {

	public static final String NVag = "nvag";
	
	SurfaceView sfv_vag;
	SurfaceView sfv_jab;
	TextView vhora;
	TextView temp1;
	TextView temp2;
	TextView temp3;
	TextView temp4;
	Canvas canvas;
	Canvas jabcanvas;
	SurfaceHolder vagHolder;
	SurfaceHolder jabHolder;
	Thread thread;
	Paint jaba = new Paint();
	Paint frio = new Paint();
	Paint bueno = new Paint();
	Paint tibio = new Paint();
	Paint caliente = new Paint();
	Paint touch = new Paint();
	
	float x = 0;
	float y = 0;
	boolean runn = false;
	Vagon vagon;
	int[] px;
	int[] py;
	String[] hora;
	int[] t1;
	int[] t2;
	int[] t3;
	int[] t4;
	float vX;
	float vY;
	float width;
	float height;
	int nvag = 1;
	
	BroadcastReceiver receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent info = getIntent();
		nvag = info.getIntExtra(NVag, nvag);
		setContentView(R.layout.activity_dat_vag);
		setTitle("Datos del Vagón "+nvag);
		sfv_vag = (SurfaceView)findViewById(R.id.sfv_vag);
		sfv_jab = (SurfaceView)findViewById(R.id.sfv_jab);
		vhora = (TextView)findViewById(R.id.horaJab);
		temp1 = (TextView)findViewById(R.id.temp1Jab);
		temp2 = (TextView)findViewById(R.id.temp2Jab);
		temp3 = (TextView)findViewById(R.id.temp3Jab);
		temp4 = (TextView)findViewById(R.id.temp4Jab);
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
		vagHolder = sfv_vag.getHolder();
		jabHolder = sfv_jab.getHolder();
		sfv_vag.setOnTouchListener(this);
		runn = false;
		vagon = MainService.vagones[nvag];
		
		px = vagon.px;
		py = vagon.py;
		hora = vagon.hora;
		t1 = vagon.temp1;
		t2 = vagon.temp2;
		t3 = vagon.temp3;
		t4 = vagon.temp4;
		
		//hojas = BitmapFactory.decodeResource(getResources(), R.drawable.hojas);
		////////////////////////////Debug////////////////////////////
//		int[] pxv = {200, 500, 600, 900, 200, 500, 500, 800};
//		int[] pyv = {200, 200, 400, 400, 600, 600, 800, 800};
//		String[] horav = {"07/11/2013 10:50:25", "07/11/2013 10:50:25", "07/11/2013 10:52:05", 
//				"07/11/2013 10:52:05", "07/11/2013 10:55:10", "07/11/2013 10:55:10", 
//				"07/11/2013 10:56:45", "07/11/2013 10:56:45"};
//		int[] t1v = {20, 50, 35, 38, 12, 78, 48, 91, 15};
//		int[] t2v = {25, 30, 50, 40, 16, 34, 56, 23, 20};
//		int[] t3v = {15, 15, 40, 30, 25, 83, 22, 49, 34};
//		int[] t4v = {20, 50, 60, 19, 24, 75, 12, 56, 32};
//		px = pxv;
//		py = pyv;
//		hora = horav;
//		t1 = t1v;
//		t2 = t2v;
//		t3 = t3v;
//		t4 = t4v;
		////////////////////////////Debug////////////////////////////
		
		receiver = new BroadcastReceiver() {// Servicio Actualizó Datos

			@Override
			public void onReceive(Context context, Intent intent) {
				finish();
			}
		};
	}
	
	@Override
	protected void onStart() {
		LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
				new IntentFilter(MainService.DATA_UPDATE));
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		super.onStop();
	}

	@Override
	public void run() {
		boolean jya = false;
		while(runn) {
			if(!vagHolder.getSurface().isValid())
				continue;
			if(!jya && jabHolder.getSurface().isValid()) {
				jya = true;
				jabcanvas = jabHolder.lockCanvas();
				int jabx = jabcanvas.getWidth()/2;
				int jaby = jabcanvas.getHeight();
				jabcanvas.drawLine(jabx, 0, jabx, jaby, jaba);
				jabcanvas.drawLine(jabx+7, 0, jabx, jaby, jaba);
				jabcanvas.drawLine(jabx-7, 0, jabx, jaby, jaba);
				jabcanvas.drawLine(jabx+5, 0, jabx, jaby, jaba);
				jabcanvas.drawLine(jabx-5, 0, jabx, jaby, jaba);
				jabcanvas.drawLine(jabx+3, 0, jabx, jaby, jaba);
				jabcanvas.drawLine(jabx-3, 0, jabx, jaby, jaba);
				jabHolder.unlockCanvasAndPost(jabcanvas);
			}
			canvas = vagHolder.lockCanvas();
//			canvas.drawColor(Color.GREEN);
			width = canvas.getWidth();
			height = canvas.getHeight();
			canvas.drawARGB(255, 100, 180, 100);
//			canvas.drawBitmap(hojas, 0, 0, null);
			vX = (float)width/1000;
			vY = (float)height/1000;
			int c = 0;
			int temp = 0;
			for(int vx:px) {
				temp = t1[c];
				if(t2[c] > temp)	temp = t2[c];
				if(t3[c] > temp)	temp = t3[c];
				if(t4[c] > temp)	temp = t4[c];
				drawpoint(canvas, vx, py[c], temp);
				c++;
			}
//			drawpoint(canvas, 500, 500, 45);
//			drawpoint(canvas, 120, 800, 35);
//			drawpoint(canvas, 700, 200, 80);
//			drawpoint(canvas, 900, 100, 20);
			drawtouch(canvas);
			vagHolder.unlockCanvasAndPost(canvas);
		}
	}

	public void drawtouch(Canvas canvas) {
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
	
	public void drawpoint(Canvas canvas, int cx, int cy, int temp) {
		Paint paint = null;
		if(temp < 35)
			paint = frio;
		if(35 <= temp && temp <= 40)
			paint = bueno;
		if(40 < temp && temp <= 60)
			paint = tibio;
		if(temp > 60)
			paint = caliente;
		canvas.drawCircle((float)vX*cx, (float)vY*cy, 20, paint);
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
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.iRoutine: {
			Intent routine = new Intent(this, Routine.class);
			routine.putExtra(NVag, nvag);
			startActivity(routine);
			overridePendingTransition(R.animator.slide_in_right,
					R.animator.slide_out_left);
			break;
		}
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.dat_vag, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
		super.onBackPressed();
	}

	@Override
	public boolean onTouch(View v, MotionEvent me) {
		x = me.getX();
		y = me.getY();
		int cont = 0;
		for(int vx:px) {
			if((float)vX*vx-30 <= x && (float)vX*vx+30 >= x &&
					(float)vY*py[cont]-30 <= y && (float)vY*py[cont]+30 >= y) {
				vhora.setText(hora[cont]);
				temp1.setText(""+t1[cont]);
				temp2.setText(""+t2[cont]);
				temp3.setText(""+t3[cont]);
				temp4.setText(""+t4[cont]);
				break;
			}
			cont++;
		}
		
		return true;
	}
}