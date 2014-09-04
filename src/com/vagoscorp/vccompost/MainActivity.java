package com.vagoscorp.vccompost;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements Runnable, OnTouchListener, OnLongClickListener {

	final int SET_SERVER = 1;

//	TextView horaGrua;
	TextView posGrua;
	TextView llegoGrua;
	TextView Label_Ser;
	TextView hora;
	SurfaceView sfv_Grua;
	Canvas canvas;
	SurfaceHolder gruHolder;
	Thread thread;
	
	Paint grua = new Paint();
	Paint vagonNew = new Paint();
	Paint touch = new Paint();
	Paint info = new Paint();
	Paint infoText = new Paint();
	Paint infoTextm = new Paint();
	Paint frio = new Paint();
	Paint bueno = new Paint();
	Paint tibio = new Paint();
	Paint caliente = new Paint();
	
	boolean runn = false;
	float width;
	float height;
	int posGru;
	String horaGru;
	int[] resumen = new int[33];
	String[] resumen1 = new String[33];
	String[] resumen2 = new String[33];
	String[] resumen3 = new String[33];
	String serverip;// IP to Connect
	int serverport;// Port to Connect
	float x = 0;
	float y = 0;
	int vagon = 0;
	BroadcastReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(R.animator.slide_in_right,
				R.animator.slide_out_left);
		super.onCreate(savedInstanceState);
		getServerData();
		setContentView(R.layout.activity_main);
		Label_Ser = (TextView) findViewById(R.id.Label_Ser);
		posGrua = (TextView) findViewById(R.id.posGru);
		llegoGrua = (TextView) findViewById(R.id.llegoGru);
		sfv_Grua = (SurfaceView) findViewById(R.id.surfaceGrua);
		hora = (TextView) findViewById(R.id.hora);
		paint_Config();
		gruHolder = sfv_Grua.getHolder();
		runn = false;
		update_labels();
		ActualizarServicio(MainService.APP_CREADA);
		receiver = new BroadcastReceiver() {// Servicio Actualizó Datos

			@Override
			public void onReceive(Context context, Intent intent) {
				update_labels();
			}
		};
		sfv_Grua.setOnTouchListener(this);
		sfv_Grua.setOnLongClickListener(this);
		Log.i("F_Position", "onCreateView");
	}
	
	public void paint_Config() {
		grua.setColor(Color.WHITE);
		grua.setTextSize(30);
		
		info.setARGB(120, 100, 100, 250);//Azul Transparente
		infoText.setColor(Color.WHITE);
		infoText.setTextSize(30);
		infoTextm.setColor(Color.WHITE);
		infoTextm.setTextSize(20);
		
		vagonNew.setColor(Color.GREEN);
		vagonNew.setTextSize(30);
		
		frio.setColor(Color.BLUE);
		frio.setTextSize(30);
		bueno.setARGB(255, 100, 180, 100);
		bueno.setTextSize(30);
		tibio.setColor(Color.YELLOW);
		tibio.setTextSize(30);
		caliente.setColor(Color.RED);
		caliente.setTextSize(30);
		
		touch.setARGB(120, 100, 100, 250);//setColor(Color.DKGRAY);
		touch.setAntiAlias(true);
		touch.setStrokeWidth(2);
		touch.setStrokeCap(Paint.Cap.ROUND);
//		touch.setStyle(Paint.Style.STROKE);
	}
	
//	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//	@Override
//	public void onWindowFocusChanged(boolean hasFocus) {
//	        super.onWindowFocusChanged(hasFocus);
//	    if (hasFocus) {
//	    	getWindow().getDecorView()
//            .setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                            | View.INVISIBLE);
//	    }
//	}

	@Override
	protected void onStart() {// Activity Iniciada
		getServerData();
		Label_Ser.setText(serverip + ":" + serverport);
		update_labels();
		ActualizarServicio(MainService.APP_INICIADA);
		LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
				new IntentFilter(MainService.DATA_UPDATE));
		super.onStart();
	}

	public void ActualizarServicio(int dato) {// Informar al Servicio sobre
												// algun cambio
		Intent service = new Intent(this, MainService.class);
		service.putExtra(MainService.Dato, dato);
		startService(service);
	}

	@Override
	protected void onStop() {// Aplicación en Segundo plano
		ActualizarServicio(MainService.APP_DETENIDA);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		super.onStop();
	}

	@Override
	public void onBackPressed() {// Boton atras precionado
		finish();
		// Intent homeIntent= new Intent(Intent.ACTION_MAIN);
		// homeIntent.addCategory(Intent.CATEGORY_HOME);
		// homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// startActivity(homeIntent);
		overridePendingTransition(R.animator.slide_in_left,
				R.animator.slide_out_right);
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {// activity Destruida
		ActualizarServicio(MainService.APP_DESTRUIDA);
		super.onDestroy();
	}

	public void Chan_Ser(MenuItem item/* +View view */) {// Cambiar Servidor
														// (Menú)
		if (!MainService.Actualizando) {
			Intent CS = new Intent(this, Set_Server.class);
			CS.putExtra(MainService.SI, serverip);
			CS.putExtra(MainService.SP, serverport);
			startActivityForResult(CS, SET_SERVER);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == SET_SERVER) {
			serverip = data.getStringExtra(MainService.SI);
			serverport = data.getIntExtra(MainService.SP, MainService.defPort);
			SharedPreferences shapre = getSharedPreferences(MainService.SPname,
					MODE_PRIVATE);
			SharedPreferences.Editor editor = shapre.edit();
			editor.putString(MainService.SI, serverip);
			editor.putInt(MainService.SP, serverport);
			editor.commit();
			Label_Ser.setText(serverip + ":" + serverport);
			ActualizarServicio(MainService.SERVER_CHANGED);
		}
	}

	public void conect(MenuItem item/* +View view */) {
		if (!MainService.Actualizando)
			ActualizarServicio(MainService.PASO_CLIENT);
	}
	
	public void load_File(MenuItem item/* +View view */) {
		if (!MainService.Actualizando)
			ActualizarServicio(MainService.LOAD_FILE);
	}

	public void getServerData() {// Leer SharedPrefs de la aplicación
		SharedPreferences shapre = getSharedPreferences(MainService.SPname,
				MODE_PRIVATE);
		// MainService.LastUpd = shapre.getString(MainService.Hora,
		// MainService.defHora);
		serverip = shapre.getString(MainService.SI, MainService.defIP);
		serverport = shapre.getInt(MainService.SP, MainService.defPort);
	}

	public void update_labels() {
		hora.setText("Ultima Actualización de Datos: "+MainService.LastUpd/*pgru_hora*/);
		posGrua.setText("Vagón " + MainService.pgru_donde);
		llegoGrua.setText(MainService.pgru_llego);
		if (runn) {
			onPause();
			onResume();
		}
	}

	@Override
	public void onPause() {
		Log.i("F_Position", "onPause");
		runn = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		thread = null;
//		ontouch = false;
		super.onPause();
	}

	@Override
	public void onResume() {
		Log.i("F_Position", "onResume");
		posGru = MainService.pgru_donde;
		horaGru = MainService.pgru_hora;
		for(int i = 1; i<33; i++) {
			if(MainService.vagones[i] != null) {
				resumen[i] = MainService.vagones[i].resumen;
				resumen1[i] = MainService.vagones[i].resumen1;
				resumen2[i] = MainService.vagones[i].resumen2;
				resumen3[i] = MainService.vagones[i].resumen3;
			}else {
				resumen[i] = MainService.BUENO;
				resumen1[i] = "Temperatura Mínima";
				resumen2[i] = "Temperatura Máxima";
				resumen3[i] = "Resumen de Estado";
			}
		}
//		ontouch = false;
		runn = true;
		thread = new Thread(this);
		thread.start();
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.Chan_ser: {
			Chan_Ser(item);
			break;
		}
		case R.id.Act_dat: {
			conect(item);
			break;
		}
		case R.id.load_file: {
			load_File(item);
			break;
		}
		case R.id.Reset_COM: {
			resetcomunic(item);
			break;
		}
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	public void resetcomunic(MenuItem item) {
		ActualizarServicio(MainService.RESET_COM);
	}
	
	public void sel_Vag(int cont/*View button*/) {
		if (!MainService.Actualizando) {
//			int cont = 5;
			Intent svag = new Intent(this, Dat_vag.class);
//			for (int vag : selVag) {
//				if (button.getId() == vag) {
					svag.putExtra(Dat_vag.NVag, cont);
//					break;
//				}
//				cont++;
//			}
			startActivity(svag);
			overridePendingTransition(R.animator.slide_in_right,
					R.animator.slide_out_left);
		} else
			Toast.makeText(this, "Actualización en proceso", Toast.LENGTH_LONG)
					.show();
	}

	@Override
	public void run() {
		boolean ticktack = false;
		boolean area = false;
		boolean firstrun = true;
		float y0;
		float y1;
		while (runn) {
			if (!gruHolder.getSurface().isValid())
				continue;
			canvas = gruHolder.lockCanvas();
			width = canvas.getWidth();
			height = canvas.getHeight();
			if(firstrun) {
				x = width;
				y = height/2;
				firstrun = false;
			}
			canvas.drawColor(Color.BLACK);
			float wv = width / 20;
			float hv = 3 * height / 7;
			float ws = width / 5 / 15;
			float ya = height - hv;
			float recur = wv + ws;
			area = false;
			for (int i = 0; i < 16; i++) {
				float sas = i * recur;
				RectF sasU = new RectF(sas, 0, wv + sas, hv);
				canvas.drawRect(sasU, colorVag(resumen[i + 1]));
				if (posGru <= 16 && i == posGru - 1 && ticktack)
					canvas.drawRect(sasU, vagonNew);
				if(x >= sas && x <= wv + sas && y >= 0 && y <= hv) {
					vagon = i+1;
					area = true;
				}
			}
			for (int i = 0; i < 16; i++) {
				float sas = i * recur;
				RectF sasD = new RectF(sas, ya, wv + sas, height);
				canvas.drawRect(sasD, colorVag(resumen[32 - i]));
				if (posGru > 16 && i == 32 - posGru && ticktack)
					canvas.drawRect(sasD, vagonNew);
				if(x >= sas && x <= wv + sas && y >= height - hv && y <= height) {
					vagon = 32-i;
					area = true;
				}
			}
			if(!area)
				vagon = 0;
			drawtouch(canvas);
			canvas.drawText("Ultimo Movimiento de la Grúa: "+horaGru, 0, height/2, grua);
//			drawinfo(canvas);
			if(vagon != 0) {
				if(vagon < 17) {
					y0 = 0;
					y1 = hv;//y + height/3;
				}else {
					y0 = ya;//y - height/3;
					y1 = height;
				}
				if(vagon < 9 || vagon > 24) {
					canvas.drawRect(new RectF(x, y0 + 10, x+3*width/8, y1), info);
					canvas.drawText("Vagón "+vagon, x + width/8, y0+40, colortit(resumen[vagon]));
					canvas.drawText(resumen1[vagon], x + 10, y0+80, infoText);
					canvas.drawText(resumen2[vagon], x + 10, y0+120, infoText);
					canvas.drawText(resumen3[vagon], x + 10, y0+160, infoText);
					canvas.drawText("Mantener Presionado para Vista Detallada", x+10, y0+210, infoTextm);
				}else {
					float borde = 3*width/8;
					canvas.drawRect(new RectF(x - borde, y0 + 10, x, y1), info);
					canvas.drawText("Vagón "+vagon, x - borde + width/8, y0+40, colortit(resumen[vagon]));
					canvas.drawText(resumen1[vagon], x - borde + 10, y0+80, infoText);
					canvas.drawText(resumen2[vagon], x - borde + 10, y0+120, infoText);
					canvas.drawText(resumen3[vagon], x - borde + 10, y0+160, infoText);
					canvas.drawText("Mantener Presionado para Vista Detallada", x+10 - borde, y0+210, infoTextm);
				}
			}

			gruHolder.unlockCanvasAndPost(canvas);
			ticktack = !ticktack;
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Paint colorVag(int stvag) {
		Paint result = null;
		result = colorSelec(stvag);
		if(result == null)
			result = bueno;
		return result;
	}
	
	public Paint colortit(int stvag) {
		Paint result = null;
		result = colorSelec(stvag);
		if(result == null)
			result = vagonNew;
		return result;
	}
	
	public Paint colorSelec(int stvag) {
		Paint result = null;
		switch(stvag) {
		case MainService.FRIO:{
			result = frio;
			break;
		}
		case MainService.TIBIO:{
			result = tibio;
			break;
		}case MainService.CALIENTE:{
			result = caliente;
			break;
		}
		}
		return result;
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
		canvas.drawCircle(x, y, 40, touch);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		x = event.getX();
		y = event.getY();
		return false;
	}

	@Override
	public boolean onLongClick(View v) {
		if(vagon != 0)
			sel_Vag(vagon);
		return false;
	}

}