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
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
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
	CheckBox espejo;
	TextView pDataA;
	TextView pDataB;
	EditText nombre;
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
	public static String textA = "";
	public static String textB = "";
	File path;
	File[] fileList;
	List<Coord> coordA = new ArrayList<Coord>();
	List<Coord> coordB = new ArrayList<Coord>();
	int moverA = -1;
	int moverB = -1;
	int qpA = 0;
	int qpB = 0;
	boolean firstrun = true;
	List<String> list;

	boolean enEspejo = false;
	boolean SDread = false;
	boolean SDwrite = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent info = getIntent();
		nvag = info.getIntExtra(Dat_vag.NVag, nvag);
		setContentView(R.layout.activity_routine);
		path = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/Documents/VCCompost/Routines");
		path.mkdirs();
		setTitle("Rutina del Reactor " + nvag);
		reactorA = (SurfaceView) findViewById(R.id.reactorA);
		reactorB = (SurfaceView) findViewById(R.id.reactorB);
		spinnerA = (Spinner) findViewById(R.id.spinnerA);
		pTextA = (TextView) findViewById(R.id.ptextA);
		spinnerB = (Spinner) findViewById(R.id.spinnerB);
		pTextB = (TextView) findViewById(R.id.ptextB);
		espejo = (CheckBox) findViewById(R.id.espejo);
		// pDataA = (TextView)findViewById(R.id.pDataA);
		// pDataB = (TextView)findViewById(R.id.pDataB);
		nombre = (EditText) findViewById(R.id.nombre);
		jaba.setColor(Color.GRAY);
		jaba.setStrokeWidth(5);
		frio.setColor(Color.BLUE);
		bueno.setColor(Color.GREEN);
		tibio.setColor(Color.YELLOW);
		tibio.setTextSize(30);
		caliente.setColor(Color.RED);
		touch.setColor(Color.DKGRAY);
		touch.setAntiAlias(true);
		touch.setStrokeWidth(2);
		touch.setStrokeCap(Paint.Cap.ROUND);
		touch.setStyle(Paint.Style.STROKE);
		coordA.add(new Coord(0, 0, 60));
		coordB.add(new Coord(0, 0, 60));
		coordA.add(new Coord(0, 0, 60));
		coordB.add(new Coord(0, 0, 60));
		getNames();

		spinnerA.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// pTextA.setText(list.get(position).split(".vccr")[0]);
				getDataA(list.get(position).split(".vccr")[0]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		spinnerB.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// pTextB.setText(list.get(position).split(".vccr")[0]);
				getDataB(list.get(position).split(".vccr")[0]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		espejo.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				enEspejo = isChecked;
				if (enEspejo) {
					coordB.clear();
					for (int i = 0; i < coordA.size(); i++) {
						Coord temp = coordA.get(i);
						coordB.add(new Coord(widthA - temp.x, temp.y, temp.r));
					}
				}
			}
		});
		holderA = reactorA.getHolder();
		holderB = reactorB.getHolder();

		reactorA.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent me) {
				xA = me.getX();
				yA = me.getY();
				if (xA <= 50 * vXA)
					xA = 50 * vXA;
				if (yA <= 50 * vYA)
					yA = 50 * vYA;
				if (xA >= 1050 * vXA)
					xA = 1050 * vXA;
				if (yA >= heightA)
					yA = heightA;
				if (me.getAction() == MotionEvent.ACTION_MOVE) {
					if (moverA != -1) {
						if (yA <= 100 * vYA) {
							coordA.set(moverA, new Coord(xA, yA, 60));
							if (enEspejo)
								coordB.set(moverA, new Coord(widthA - xA, yA,
										60));
						} else {
							coordA.set(moverA, new Coord(xA, yA, 90));
							if (enEspejo)
								coordB.set(moverA, new Coord(widthA - xA, yA,
										90));
						}
						if (yA > 1140 * vYA) {
							coordA.remove(moverA);
							if (enEspejo)
								coordB.remove(moverA);
							moverA = -1;
						}
					}
				}
				if (me.getAction() == MotionEvent.ACTION_UP) {
					moverA = -1;
					// for(int i = qpA; i >= 0; i--) {
					// float vx = coordA.get(i).x;
					// float vy = coordA.get(i).y;
					// if(vx < xA + 50 && vx > xA - 50 &&
					// vy < yA + 50 && vy > yA - 50) {
					// pDataA.setText(pointinfoA(i));
					// break;
					// }
					// }
				}
				return false;
			}
		});
		reactorA.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				boolean punto_con = false;
				for (int i = qpA; i >= 0; i--) {
					float vx = coordA.get(i).x;
					float vy = coordA.get(i).y;
					if (vx < xA + 50 && vx > xA - 50 && vy < yA + 50
							&& vy > yA - 50) {
						punto_con = true;
						moverA = i;
						break;
					}
				}
				if (!punto_con && yA <= 1100 * vYA) {
					Coord rs;
					if (yA <= 100 * vYA) {
						rs = coordA.get(qpA);
						coordA.set(qpA, new Coord(xA, yA, 60));
						coordA.add(rs);
						if (enEspejo) {
							rs = coordB.get(qpA);
							coordB.set(qpA, new Coord(widthA - xA, yA, 60));
							coordB.add(rs);
						}
					} else {
						rs = coordA.get(qpA);
						coordA.set(qpA, new Coord(xA, yA, 90));
						coordA.add(rs);
						if (enEspejo) {
							rs = coordB.get(qpA);
							coordB.set(qpA, new Coord(widthA - xA, yA, 90));
							coordB.add(rs);
						}
					}
				}
				return false;
			}
		});
		reactorB.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent me) {
				xB = me.getX();
				yB = me.getY();
				if (xB <= 50 * vXB)
					xB = 50 * vXB;
				if (yB <= 50 * vYB)
					yB = 50 * vYB;
				if (xB >= 1050 * vXB)
					xB = 1050 * vXB;
				if (yB >= heightB)
					yB = heightB;
				// for(int i = qpB; i >= 0; i--) {
				// float vx = coordB.get(i).x;
				// float vy = coordB.get(i).y;
				// if(vx < xB+ 50 && vx > xB - 50 &&
				// vy < yB + 50 && vy > yB - 50) {
				// pDataB.setText(pointinfoB(i));
				// break;
				// }
				// }
				if (!enEspejo) {
					if (me.getAction() == MotionEvent.ACTION_MOVE) {
						if (moverB != -1) {
							if (yB <= 100 * vYB)
								coordB.set(moverB, new Coord(xB, yB, 60));
							else
								coordB.set(moverB, new Coord(xB, yB, 90));
							if (yB > 1140 * vYB) {
								coordB.remove(moverB);
								moverB = -1;
							}
						}
					}
					if (me.getAction() == MotionEvent.ACTION_UP) {
						moverB = -1;
					}
				}
				return false;
			}
		});
		reactorB.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				boolean punto_con = false;
				for (int i = qpB; i >= 0; i--) {
					float vx = coordB.get(i).x;
					float vy = coordB.get(i).y;
					if (vx < xB + 50 && vx > xB - 50 && vy < yB + 50
							&& vy > yB - 50) {
						punto_con = true;
						moverB = i;
						break;
					}
				}
				if (!enEspejo && !punto_con && yB <= 1100 * vYB) {
					Coord rs;
					if (yB <= 100 * vYB) {
						rs = coordB.get(qpB);
						coordB.set(qpB, new Coord(xB, yB, 60));
						coordB.add(rs);
					} else {
						rs = coordB.get(qpB);
						coordB.set(qpB, new Coord(xB, yB, 90));
						coordB.add(rs);
					}
				}
				return false;
			}
		});
		runn = false;
		getNames();
		// checkSD();
		// write("prueba", "soy un texto D:");
		// pText.setText(read("prueba"));
	}

	void checkSD() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			SDread = true;
			SDwrite = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			SDread = true;
			SDwrite = false;
		} else {
			// oops
			SDread = false;
			SDwrite = false;
		}
	}

	void write(String name, String data) {
		checkSD();
		if (SDread && SDwrite) {
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
		if (SDread) {
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
		list = new ArrayList<String>();
		for (int i = 0; i < fileList.length; i++) {
			list.add(fileList[i].getName());
		}
		ArrayAdapter<String> fileAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list);
		spinnerA.setAdapter(fileAdapter);
		spinnerB.setAdapter(fileAdapter);
	}

	@Override
	protected void onStart() {

		super.onStart();
	}

	@Override
	public void run() {
		while (runn) {
			if (!holderA.getSurface().isValid()
					|| !holderB.getSurface().isValid())
				continue;
			canvasA = holderA.lockCanvas();
			canvasB = holderB.lockCanvas();
			heightA = canvasA.getHeight();
			heightB = canvasB.getHeight();
			widthA = canvasA.getWidth();
			widthB = canvasB.getWidth();
			vXA = (float) widthA / 1100;
			vXB = (float) widthB / 1100;
			vYA = (float) heightA / 1200;
			vYB = (float) heightB / 1200;
			qpA = coordA.size() - 1;
			qpB = coordB.size() - 1;
			if (firstrun) {
				coordA.get(0).x = 50 * vXA;
				coordB.get(0).x = 1050 * vXB;
				coordA.get(qpA).x = 1050 * vXA;
				coordB.get(qpB).x = 50 * vXB;
				firstrun = false;
			}
			coordA.get(0).y = 50 * vYA;
			coordA.get(0).r = 60;
			coordB.get(0).y = 50 * vYB;
			coordB.get(0).r = 60;
			coordA.get(qpA).y = 50 * vYA;
			coordA.get(qpA).r = 60;
			coordB.get(qpB).y = 50 * vYB;
			coordB.get(qpB).r = 60;
			canvasA.drawARGB(255, 100, 180, 100);
			canvasB.drawARGB(255, 100, 180, 100);
			// canvasA.drawLine(0, 100 * vYA, widthA, 100 * vYA, jaba);//inicio
			// lado inclinado A
			// canvasB.drawLine(0, 100 * vYB, widthB, 100 * vYB, jaba);//inicio
			// lado inclinado B
			// canvasA.drawLine(0, 50 * vYA, widthA, 50 * vYA, jaba);//centro
			// lado inclinado A
			// canvasB.drawLine(0, 50 * vYB, widthB, 50 * vYB, jaba);//centro
			// lado inclinado B
			canvasA.drawLine(0, 100 * vYA - 50 * vXA, widthA, 100 * vYA - 50
					* vXA, jaba);// inicio gut lado inclinado A
			canvasB.drawLine(0, 100 * vYB - 50 * vXB, widthB, 100 * vYB - 50
					* vXB, jaba);// inicio gut lado inclinado B
			// canvasA.drawLine(0, 1100 * vYA, widthA, 1100 * vYA, jaba);//fin
			// vagón lado A
			// canvasB.drawLine(0, 1100 * vYB, widthB, 1100 * vYB, jaba);//fin
			// vagón lado B
			canvasA.drawLine(0, 1100 * vYA - 50 * vXA, widthA, 1100 * vYA - 50
					* vXA, jaba);// fin gut vagón lado A
			canvasB.drawLine(0, 1100 * vYB - 50 * vXB, widthB, 1100 * vYB - 50
					* vXB, jaba);// fin gut vagón lado B
			// canvasA.drawLine(50 * vXA, 100 * vYA - 50 * vXA, 50 * vXA, 1100 *
			// vYA - 50 * vXA, jaba);//borde iz lado A
			// canvasB.drawLine(50 * vXB, 100 * vYB - 50 * vXB, 50 * vXB, 1100 *
			// vYB - 50 * vXB, jaba);//borde iz lado B
			// canvasA.drawLine(1050 * vXA, 100 * vYA - 50 * vXA, 1050 * vXA,
			// 1100 * vYA - 50 * vXA, jaba);//borde der lado A
			// canvasB.drawLine(1050 * vXB, 100 * vYB - 50 * vXB, 1050 * vXB,
			// 1100 * vYB - 50 * vXB, jaba);//borde der lado B
			canvasA.drawRect(0, 1140 * vYA, widthA, heightA, caliente);
			canvasB.drawRect(0, 1140 * vYB, widthB, heightB, caliente);
			// canvasA.drawLine(widthA-20, 0, widthA-20, heightA, jaba);
			// canvasB.drawLine(20, 0, 20, heightB, jaba);
			for (int i = 0; i < coordA.size(); i++) {
				if (i > 0)
					canvasA.drawLine(coordA.get(i - 1).x, coordA.get(i - 1).y,
							coordA.get(i).x, coordA.get(i).y, frio);
				drawMPoint(canvasA, coordA.get(i).x, coordA.get(i).y/*
																	 * , widthA,
																	 * heightA
																	 */, i, qpA);
				canvasA.drawText(i + "", coordA.get(i).x, coordA.get(i).y,
						tibio);
			}
			for (int i = 0; i < coordB.size(); i++) {
				if (i > 0)
					canvasB.drawLine(coordB.get(i - 1).x, coordB.get(i - 1).y,
							coordB.get(i).x, coordB.get(i).y, frio);
				drawMPoint(canvasB, coordB.get(i).x, coordB.get(i).y/*
																	 * , widthB,
																	 * heightB
																	 */, i, qpB);
				canvasB.drawText(i + "", coordB.get(i).x, coordB.get(i).y,
						tibio);
			}
			drawtouch(canvasA, xA, yA/* , widthA, heightA */);
			drawtouch(canvasB, xB, yB/* , widthB, heightB */);
			holderA.unlockCanvasAndPost(canvasA);
			holderB.unlockCanvasAndPost(canvasB);
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
	}

	public void drawtouch(Canvas canvas, float x, float y/*
														 * , float width, float
														 * height
														 */) {
		// if(x <= 50 * vXA)
		// x = 50 * vXA;
		// if(y <= 50 * vYA)
		// y = 50 * vYA;
		// if(x >= 1050 * vXA)
		// x = 1050 * vXA;
		// if(y >= height)
		// y = height;
		canvas.drawCircle(x, y, 50, touch);
	}

	public void drawMPoint(Canvas canvas, float x, float y/*
														 * , float width, float
														 * height
														 */, int index, int qp) {
		if (x <= 50 * vXA)
			x = 50 * vXA;
		if (y <= 50 * vYA)
			y = 50 * vYA;
		if (x >= 1050 * vXA)
			x = 1050 * vXA;
		// if(y >= height)
		// y = height;
		if (index != 0 && index != qp)
			canvas.drawCircle(x, y, 50 * vXA, frio);
		else if (index == 0) {
			// y = 50 * vYA;
			canvas.drawCircle(x, y, 50 * vXA, tibio);
		} else {
			canvas.drawCircle(x, y, 50 * vXA, caliente);
		}
	}

	public void Ejecutar(View v) {
		espejo.setChecked(false);
		enEspejo = false;
		textA = genFileA() + "W";
		textB = genFileB() + "W";
		Intent service = new Intent(this, MainService.class);
		service.putExtra(MainService.Dato, MainService.EJECUTAR_RUTINA);
		service.putExtra(MainService.textA, textA);
		service.putExtra(MainService.textB, textB);
		startService(service);
	}

	public void Save(View v) {
		String nom = nombre.getText().toString();
		if (nom == null || nom.equals(""))
			nom = "untitled";
		write(nom + "A", genFileA());
		write(nom + "B", genFileB());
		espejo.setChecked(false);
		enEspejo = false;
		getNames();
	}

	String pointinfoA(int i) {
		String sas = "";
		float x = 0;
		float y = 0;
		float r = 0;
		Coord temp = coordA.get(i);
		y = (float) temp.y / vYA - 100;
		x = (float) temp.x / vXA - 50;
		if(i != qpA)
			r = (float) temp.r;
		else
			r = 90;
		if (x <= 0)
			x = 0;
		if (x > 1000)
			x = 1000;
		if (y <= 0)
			y = 0;
		if (y > 1000)
			y = 1000;
		sas = "=X=" + y + "=Y=" + x + "=R=" + r + "=/";
		return sas;
	}

	private String genFileA() {
		String sas = "";
		if (!coordA.isEmpty()) {
			for (int i = 0; i < coordA.size(); i++) {
				sas += pointinfoA(i) + "\r\n";
			}
		} else
			sas += "=X=" + 0 + "=Y=" + 0 + "=R=" + 60 + "=/\r\n";
		return sas;
	}

	String pointinfoB(int i) {
		String sas = "";
		float x = 0;
		float y = 0;
		float r = 0;
		Coord temp = coordB.get(i);
		y = (float) temp.y / vYB - 100;
		x = (float) temp.x / vXB - 50;
		if(i != qpB)
			r = (float) temp.r;
		else
			r = 90;
		if (x <= 0)
			x = 0;
		if (x > 1000)
			x = 1000;
		if (y <= 0)
			y = 0;
		if (y > 1000)
			y = 1000;
		sas = "=X=" + y + "=Y=" + x + "=R=" + r + "=/";
		return sas;
	}

	private String genFileB() {
		String sas = "";
		if (!coordB.isEmpty()) {
			for (int i = 0; i < coordB.size(); i++) {
				sas += pointinfoB(i) + "\r\n";
			}
		} else
			sas += "=X=" + 0 + "=Y=" + 0 + "=R=" + 60 + "=/\r\n";
		return sas;
	}

	List<Coord> getData(String file) {
		List<Coord> res = new ArrayList<Coord>();
		String data = read(file);
		String[] points = data.split("/");
		int l = points.length - 1;
		for (int i = 0; i < l; i++) {
			String[] pData = points[i].split("=");
			float y = (Float.parseFloat(pData[2]) + 100) * vYA;
			float x = (Float.parseFloat(pData[4]) + 50) * vXA;
			float r = Float.parseFloat(pData[6]);
			res.add(new Coord(x, y, r));
		}
		return res;
	}

	void getDataA(String fileName) {
		coordA = getData(fileName);
	}

	void getDataB(String fileName) {
		coordB = getData(fileName);
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

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
	
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

	class Coord {

		float x;
		float y;
		float r;

		public Coord(float vx, float vy, float vr) {
			x = vx;
			y = vy;
			r = vr;
		}
	}
}