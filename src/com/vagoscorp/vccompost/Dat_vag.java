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

/**
 * The Class Dat_vag.
 */
public class Dat_vag extends Activity implements Runnable,OnTouchListener {

	/** The Constant NVag. */
	public static final String NVag = "nvag";
	
	/** The sfv_vag. */
	SurfaceView sfv_vag;
	
	/** The sfv_jab. */
	SurfaceView sfv_jab;
	
	/** The vhora. */
	TextView vhora;
	
	/** The temp1. */
	TextView temp1;
	
	/** The temp2. */
	TextView temp2;
	
	/** The temp3. */
	TextView temp3;
	
	/** The temp4. */
	TextView temp4;
	
	/** The canvas. */
	Canvas canvas;
	
	/** The jabcanvas. */
	Canvas jabcanvas;
	
	/** The vag holder. */
	SurfaceHolder vagHolder;
	
	/** The jab holder. */
	SurfaceHolder jabHolder;
	
	/** The thread. */
	Thread thread;
	
	/** The jaba. */
	Paint jaba = new Paint();
	
	/** The frio. */
	Paint frio = new Paint();
	
	/** The bueno. */
	Paint bueno = new Paint();
	
	/** The tibio. */
	Paint tibio = new Paint();
	
	/** The caliente. */
	Paint caliente = new Paint();
	
	/** The touch. */
	Paint touch = new Paint();
	
	/** The x. */
	float x = 0;
	
	/** The y. */
	float y = 0;
	
	/** The runn. */
	boolean runn = false;
	
	/** The vagon. */
	Vagon vagon;
	
	/** The px. */
	int[] px;
	
	/** The py. */
	int[] py;
	
	/** The hora. */
	String[] hora;
	
	/** The t1. */
	int[] t1;
	
	/** The t2. */
	int[] t2;
	
	/** The t3. */
	int[] t3;
	
	/** The t4. */
	int[] t4;
	
	/** The v x. */
	float vX;
	
	/** The v y. */
	float vY;
	
	/** The width. */
	float width;
	
	/** The height. */
	float height;
	
	BroadcastReceiver receiver;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent info = getIntent();
		int nvag = info.getIntExtra(NVag, 1);
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

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		boolean jya = false;
		while(runn) {
			if(!vagHolder.getSurface().isValid() || !jabHolder.getSurface().isValid())
				continue;
			if(!jya) {
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
			//canvas.drawColor(Color.GREEN);
			width = canvas.getWidth();
			height = canvas.getHeight();
			canvas.drawARGB(255, 100, 180, 100);
			//canvas.drawBitmap(hojas, 0, 0, null);
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

	/**
	 * Drawtouch.
	 *
	 * @param canvas the canvas
	 */
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
	
	/**
	 * Drawpoint.
	 *
	 * @param canvas the canvas
	 * @param cx the cx
	 * @param cy the cy
	 * @param temp the temp
	 */
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
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
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

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		runn = true;
		thread = new Thread(this);
		thread.start();
		super.onResume();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.dat_vag, menu);
		return true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
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

	/* (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
		super.onBackPressed();
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
	 */
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
