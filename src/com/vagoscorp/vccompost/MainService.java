package com.vagoscorp.vccompost;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import libraries.vagoscorp.comunication.Eventos.OnComunicationListener;
import libraries.vagoscorp.comunication.Eventos.OnConnectionListener;
import libraries.vagoscorp.comunication.Eventos.OnTimeOutListener;
import libraries.vagoscorp.comunication.android.Comunic;
import libraries.vagoscorp.comunication.android.TimeOut;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.vagoscorp.vccompost.ProcesarDatos.OnDataProcessingListener;

/**
 * The Class MainService.
 */
public class MainService extends Service implements OnDataProcessingListener,
		OnComunicationListener {

	// /////////////////Constantes///////////////////
	public static final int PASO_CLIENT = 1;
	public static final int PASO_SERVER = 2;
	public static final int EN_NOTIF = 3;
	public static final int APP_CREADA = 101;
	public static final int APP_INICIADA = 102;
	public static final int APP_DETENIDA = 103;
	public static final int APP_DESTRUIDA = 104;
	public static final int SERVER_CHANGED = 105;
	public static final int RESET_COM = 107;
	public static final int LOAD_FILE = 106;
	public static final int FRIO = 1;
	public static final int BUENO = 2;
	public static final int TIBIO = 3;
	public static final int CALIENTE = 4;
	public static final String defIP = "10.0.0.4";
	public static final String defHora = "01-01-2014 12:30:20";
	public static final int defPort = 2000;
	public static final String SI = "SIP";
	public static final String SP = "SPort";
	public static final String Hora = "Hora";
	public static final String Horagru = "Horagru";
	public static final String Dondegru = "Dondegru";
	public static final String Llegogru = "Llegogru";
	public static final String SPname = "com.vagoscorp.vccompost";
	public static final String DATA_UPDATE = "Data_Upd";
	public static final String Dato = "dato";
	final String CONSULTA = "T%/";
	static final String LLEGO = "Grúa en el Vagón";
	final String EN_CAMINO = "Grúa en Camino desde el vagón ";
	// /////////////////Constantes///////////////////
	static boolean Actualizando = false;
	static String LastUpd = "No Actualizado";
	static Vagon[] vagones = new Vagon[33];
	static String[] Tn = new String[9];
	static int pgru_donde = 1;
	static String pgru_llego = LLEGO;
	static String pgru_hora = defHora;
	SimpleDateFormat date;
	String dateSt;
	String tarea = "";
	boolean consultar = false;
	boolean timeOutEnabled = false;
	int NOTIF_ID = 545;
	NotificationManager nManager;
	WifiManager WFM;
	LocalBroadcastManager broadcaster;
	String serverip;
	int serverport;
	Context context;
	Comunic comunic = new Comunic();
	ProcesarDatos procesarDatos;
	TimeOut timeOut;
	static String text_loaded = "";
	List<String> filen;

	public MainService() {
	}

	void informar(String info, boolean toast) {// Debug: Imprimir mensaje
		Log.i("MainService", info);
		if(toast)
			Toast.makeText(this, "MainService: " + info, Toast.LENGTH_SHORT).show();
	}

	public void getData() {// Leer SharedPrefs de la aplicación
		SharedPreferences shapre = getSharedPreferences(SPname, MODE_PRIVATE);
		LastUpd = shapre.getString(Hora, defHora);
		pgru_hora = shapre.getString(Horagru, defHora);
		pgru_donde = shapre.getInt(Dondegru, pgru_donde);
		pgru_llego = shapre.getString(Llegogru, pgru_llego);
		serverip = shapre.getString(SI, defIP);
		serverport = shapre.getInt(SP, defPort);
	}

	public void updData() {// Actualizar sharedPrefs del Servicio
		SharedPreferences shapre = getSharedPreferences(SPname, MODE_PRIVATE);
		SharedPreferences.Editor editor = shapre.edit();
		editor.putString(Hora, LastUpd);
		editor.putString(Horagru, pgru_hora);
		editor.putInt(Dondegru, pgru_donde);
		editor.putString(Llegogru, pgru_llego);
		editor.commit();
		updUI();
	}

	public void updUI() {// Informar a la UI para actualizar datos
		Intent intent = new Intent(DATA_UPDATE);
		broadcaster.sendBroadcast(intent);
	}

	@Override
	public void onCreate() {// Servicio creado
		WFM = (WifiManager) getSystemService(WIFI_SERVICE);
		broadcaster = LocalBroadcastManager.getInstance(this);
		nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		getData();
		date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
		context = this;
		for (int i = 1; i < 33; i++) {
			vagones[i] = new Vagon(i, defHora); 
		}
//		pgru_donde = 1;
//		pgru_hora = defHora;
//		pgru_llego = LLEGO;
		updUI();
		informar("onCreate", true);
		timeOut = new TimeOut();
		load_files(1);
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		while (!WFM.isWifiEnabled())
			WFM.setWifiEnabled(true);
		if (intent != null) {
			int accion = intent.getIntExtra(Dato, 0);
			switch (accion) {
			// case APP_CREADA: {
			// informar("APP_CREADA");
			// load_file(20);
			// break;
			// }
			case APP_INICIADA: {
				informar("APP_INICIADA", false);
				dis_notificacion();
				break;
			}
			case SERVER_CHANGED: {
				informar("SERVER_CHANGED", false);
				getData();
				break;
			}
			case APP_DETENIDA: {
				// informar("APP_DETENIDA");
				notificacion(this, "Servicio en Ejecución", false);
				break;
			}
			// case APP_DESTRUIDA: {
			// informar("APP_DESTRUIDA");
			//
			// break;
			// }
			// case EN_NOTIF: {
			// informar("EN_NOTIF");
			//
			// break;
			// }
			case PASO_CLIENT: {
				informar("PASO_CLIENT", false);
				solicitar_Datos();
				break;
			}
			// case PASO_SERVER: {
			// informar("PASO_SERVER");
			//
			// break;
			// }
			case LOAD_FILE: {
				informar("LOAD_FILES", false);
//				for (int i = 1; i < 33; i++) {
//					vagones[i] = new Vagon(i, defHora); 
//				}
//				updData();
				load_files(1);
				break;
			}
			case RESET_COM: {
				informar("RESET_COM", false);
				comunic.Detener_Actividad();
				break;
			}
			// default: {
			// informar("onStartcommand");
			// break;
			// }
			}
		} else {

		}
		return START_STICKY/* super.onStartCommand(intent, flags, startId) */;
	}

	public void solicitar_Datos() {// Inicia una comunicacion Client-Server si
									// no hay comunicaciones activas
		Log.d("Client", "Solicitar Datos");
		if (comunic == null || !Actualizando
				&& comunic.estado != comunic.CONNECTED) {
			if (comunic != null) {
				comunic.Detener_Actividad();
			}
			comunic = new Comunic(context, serverip, serverport);
			comunic.setConnectionListener(new OnConnectionListener() {

				@Override
				public void onConnectionstablished() {
					Log.d("Client", "Connection Stablished");
					comunic.enviar(EstadoDB());
				}

				@Override
				public void onConnectionfinished() {
					Log.d("Client", "Connection Finished");
					iniciar_Server();
				}
			});
			// comunic.setComunicationListener(this);
			comunic.execute();
		}
	}

	public void iniciar_Server() {// Inicia un servidor a la espera de conexión
		Log.d("Server", "Iniciar Server");
		comunic = new Comunic(context, serverport);
		comunic.setConnectionListener(new OnConnectionListener() {

			@Override
			public void onConnectionstablished() {
				Log.d("Server", "Connection Stablished");
				timeOut = new TimeOut();
				timeOut.setTimeOutListener(new OnTimeOutListener() {

					@Override
					public void onTimeOutEnabled() {
						timeOutEnabled = true;
					}

					@Override
					public void onTimeOutCancelled() {
						timeOutEnabled = false;
					}

					@Override
					public void onTimeOut() {
						comunic.Cortar_Conexion();
						timeOutEnabled = false;
					}
				});
				timeOut.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (long)8000);
			}

			@Override
			public void onConnectionfinished() {
				Log.d("Server", "Connection Finished");
				if(timeOutEnabled)
					timeOut.cancel(true);
				if (!Actualizando) {
					if (consultar) {
						consultar = false;
						solicitar_Datos();
					} else if (comunic.tcon != comunic.CLIENT) {
						iniciar_Server();
					}
				}
			}
		});
		comunic.setComunicationListener(this);
		comunic.execute();
	}

	@Override
	public void onDataReceived(String dato) {// IL Dato Recibido
		tarea += dato;
		if (dato.endsWith("/")) {
			if (tarea.equals(CONSULTA)) {
				consultar = true;
				comunic.Detener_Actividad();
			} else {
				consultar = false;
				procesar(tarea);
			}
			tarea = "";
		}
	}

	@Override
	public void onDataProcessingStarted() {// IL procesamiento de datos iniciado
		Actualizando = true;
		comunic.Detener_Actividad();
	}

	@Override
	public void onDataProcessingEnded(int resp) {// IL procesamiento de datos
													// terminado
		switch (resp) {
		case VAGON: {
			int nvag = procesarDatos.tempvag.nvag;
			vagones[nvag] = procesarDatos.tempvag;
			save_file(nvag, procesarDatos.data.getBytes());
			// LastUpd = procesarDatos.dateSt;
			break;
		}
		case GRUA: {
			pgru_donde = procesarDatos.pgru_donde;
			pgru_hora = procesarDatos.dateSt;
			if (procesarDatos.pgru_llego == 0
					|| procesarDatos.pgru_llego == pgru_donde)
				pgru_llego = LLEGO;
			else
				pgru_llego = EN_CAMINO + procesarDatos.pgru_llego;
			break;
		}
		case TEMPS: {
			Tn = procesarDatos.Tn;
			break;
		}
		case FALLA: {
			informar("Falla en la Actualización!", true);
			comunic.Detener_Actividad();
		}
		}
		LastUpd = procesarDatos.dateSt;
		updData();
		Actualizando = false;
		notificacion(this, "Actualización Recibida", true);
		iniciar_Server();
	}

	public void notificacion(Context mainContext, String Data, boolean sound) {
		Intent init_VCCompost = new Intent(mainContext, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(mainContext);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(init_VCCompost);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				mainContext);
		if (sound)
			mBuilder.setDefaults(Notification.DEFAULT_SOUND);
		mBuilder.setContentIntent(resultPendingIntent);
		mBuilder.setSmallIcon(R.drawable.ic_launcher);
		mBuilder.setContentTitle("VC Compost");
		mBuilder.setContentText(Data);// "Aplicación en Ejecución"
		mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
		nManager.notify(NOTIF_ID, mBuilder.build());
	}

	public void dis_notificacion() {
		nManager.cancel(NOTIF_ID);
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		informar("onTaskRemoved", false);
		startActivity(new Intent(this, MainActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		// stopSelf();
		super.onTaskRemoved(rootIntent);
	}

	@Override
	public void onDestroy() {// Servicio Destruido
		informar("onDestroy", false);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void load_file(int nvag) {// Cargar datos desde archivo
		filen = getFilenames();
		if (filen.contains("vagon" + nvag)) {
			procesar(readFile("vagon" + nvag));
		}
	}

	public void load_files(final int nvag) {// Cargar datos desde archivo
		if(nvag == 1)
			filen = getFilenames();
		if (filen.size() != 0) {
//			Log.d("load_files",""+filen.size());
			if (filen.contains("vagon" + nvag)) {
				// procesar(readFile("vagon" + nvag));
				procesarDatos = new ProcesarDatos(readFile("vagon" + nvag));
				procesarDatos.setDataProcessingEndListener(new OnDataProcessingListener() {

							@Override
							public void onDataProcessingStarted() {
								Actualizando = true;
								comunic.Detener_Actividad();
							}

							@Override
							public void onDataProcessingEnded(int resp) {
								switch (resp) {
								case VAGON: {
									int nvag = procesarDatos.tempvag.nvag;
									vagones[nvag] = procesarDatos.tempvag;
									break;
								}
								case FALLA: {
									informar("Falla en la Actualización desde archivo!", true);
								}
								}
								if (nvag <= 32)
									load_files(nvag + 1);
								else {
									updUI();
									Actualizando = false;
									iniciar_Server();
								}
							}
						});
				procesarDatos.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				Actualizando = true;
			} else {
				if (nvag <= 32)
					load_files(nvag + 1);
				else {
					updUI();
					Actualizando = false;
					if (LastUpd.equals(defHora)) {
						solicitar_Datos();
					} else {
						iniciar_Server();
					}
				}
			}
		} else {
			solicitar_Datos();
		}
	}

	public void save_file(int nvag, byte[] data) {// Guardar Archivo
		try {
			FileOutputStream fos = openFileOutput("vagon" + nvag, MODE_PRIVATE);
			fos.write(data);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String EstadoDB() {// Estado actual de la base de datos
		String estado = "DB#";
		for (int i = 1; i < 33; i++) {
			estado += vagones[i].hora[vagones[i].hora.length - 1] + ";";
		}
		estado += "/";
		return estado;
	}

	private void procesar(String dato) {// Iniciar procesamiento de datos
		procesarDatos = new ProcesarDatos(dato);
		procesarDatos.setDataProcessingEndListener(this);
		procesarDatos.execute();
		Actualizando = true;
	}

	private List<String> getFilenames() {// Recurso: Lista de archivos guardados
		String[] filenames = getApplicationContext().fileList();
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < filenames.length; i++) {
			Log.d("Filename", filenames[i]);
			list.add(filenames[i]);
		}
		return list;
	}

	private String readFile(String selectedFile) {// Recurso: Leer archivo
		String value = "";
		FileInputStream fis;
		try {
			fis = openFileInput(selectedFile);
			byte[] input = new byte[fis.available()];
			while (fis.read(input) != -1) {
				value += new String(input);
			}
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return value;
	}

	// public void UpdUI(/* String message */) {
	// Intent intent = new Intent(DATA_UPDATE);
	// // if(message != null)
	// // intent.putExtra(COPA_MESSAGE, message);
	// broadcaster.sendBroadcast(intent);
	// }

}
