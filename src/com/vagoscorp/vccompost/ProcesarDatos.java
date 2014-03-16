package com.vagoscorp.vccompost;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.os.AsyncTask;

/**
 * The Class ProcesarDatos.
 */
public class ProcesarDatos extends AsyncTask<Void, Integer, Integer> {

	int pgru_donde;
	int pgru_llego;
//	String pgru_hora;
	Vagon tempvag;
	String[] Tn;
	String data;
	Calendar calendar;
	SimpleDateFormat date;
	String dateSt;
	
	public boolean processingData = false;
	// String rData;
	
	// ///////////////Código para Listener/////////////////
	OnDataProcessingListener dataPListener;

	public interface OnDataProcessingListener {
		
		public final int FALLA = 0;
		public final int VAGON = 1;
		public final int GRUA = 2;
		public final int TEMPS = 3;
		
		public void onDataProcessingStarted();
		public void onDataProcessingEnded(int resp);
	}

	public void setDataProcessingEndListener(
			OnDataProcessingListener onDataPListener) {
		dataPListener = onDataPListener;
	}
	// ///////////////Código para Listener/////////////////

	public ProcesarDatos() {
		
	}
	
	public ProcesarDatos(String datos) {
		data = datos;
		calendar = new GregorianCalendar();
		date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
		dateSt = date.format(new GregorianCalendar().getTime());
	}

	public int Trabajar_datos(String data) {
		String[] dat = data.split("#");
		String datos;
		int tip = OnDataProcessingListener.FALLA;
		try {
			if (dat[0].equals("1")) {
				int nvag = Integer.parseInt(dat[1]);
				int nmu = Integer.parseInt(dat[2]);
				datos = dat[3];
				Trab_temp(nvag, nmu, datos);
				tip = OnDataProcessingListener.VAGON;
			} else if (dat[0].equals("2")) {
				datos = dat[1];
				Trab_pgru(datos);
				tip = OnDataProcessingListener.GRUA;
			} else if (dat[0].equals("3")) {
				datos = dat[1];
				Trab_temps(datos);
				tip = OnDataProcessingListener.TEMPS;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tip;
	}

	private void Trab_temp(int nvag, int nmu, String datos) {
		/*
		 * <tip>#<n vag>#<n muestras>#
		 * <x>;<y>;<hora>;<d1>;<d2>;<d3>;<d4>&
		 * <x>;<y>;<hora>;<d1>;<d2>;<d3>;<d4>&
		 * <x>;<y>;<hora>;<d1>;<d2>;<d3>;<d4>&
		 * <x>;<y>;<hora>;<d1>;<d2>;<d3>;<d4>&/
		 */
		int[] px = new int[nmu];
		int[] py = new int[nmu];
		String[] hora = new String[nmu];
		int[] t1 = new int[nmu];
		int[] t2 = new int[nmu];
		int[] t3 = new int[nmu];
		int[] t4 = new int[nmu];
		int resumen = MainService.BUENO;
		String resumen1 = "Temperatura Mínima";
		String resumen2 = "Temperatura Máxima";
		String resumen3 = "Resumen de Estado";
		// String dat = "1$2$20:20:20$10$<20>$<30>$<40>&";
		String[] jabalina = datos.split("&");
		int cont = 0;
		int max = 0;
		int min = 999;
		int t1s;
		int t2s;
		int t3s;
		int t4s;
		for (String jaba : jabalina) {
			if (cont >= nmu)
				break;
			String[] djaba = jaba.split(";");
			px[cont] = Integer.parseInt(djaba[0]);
			py[cont] = Integer.parseInt(djaba[1]);
			hora[cont] = djaba[2].toString();
			t1[cont] = t1s = Integer.parseInt(djaba[3]);
			t2[cont] = t2s = Integer.parseInt(djaba[4]);
			t3[cont] = t3s = Integer.parseInt(djaba[5]);
			t4[cont] = t4s = Integer.parseInt(djaba[6]);
			if(t1s > max)
				max = t1s;
			if(t2s > max)
				max = t2s;
			if(t3s > max)
				max = t3s;
			if(t4s > max)
				max = t4s;
			if(t1s < min)
				min = t1s;
			if(t2s < min)
				min = t2s;
			if(t3s < min)
				min = t3s;
			if(t4s < min)
				min = t4s;
			cont++;
		}
		resumen1 = "Temperatura Mínima = "+min;
		resumen2 = "Temperatura Maxima = "+max;
		if(min >= 35 && max <= 40) {
			resumen3 = "Condiciones en Rango";
			resumen = MainService.BUENO;
		}else if(max > 40 && max <=60) {
			resumen3 = "Condiciones poco Favorables";
			resumen = MainService.TIBIO;
		}else if(max >60) {
			resumen3 = "PELIGRO! Procesos Anaerobicos";
			resumen = MainService.CALIENTE;
		}else if(min < 35) {
			resumen3 = "Baja Actividad Microbiana";
			resumen = MainService.FRIO;
		}
		// rData = "Sep = "+";"+" Vagon = " + nvag + " nMu = " + nmu;
		// for(int tas = 0; tas < cont;tas++) {
		// rData =
		// rData.concat("\npx = "+px[tas]+" py = "+py[tas]+" hora = "+hora[tas]+" t1 = "+t1[tas]+" t2 = "+
		// t2[tas]+" t3 = "+t3[tas]+" t4 = "+t4[tas]);
		// }
		tempvag = new Vagon();
		tempvag.nvag = nvag;
		tempvag.px = px;
		tempvag.py = py;
		tempvag.hora = hora;
		tempvag.temp1 = t1;
		tempvag.temp2 = t2;
		tempvag.temp3 = t3;
		tempvag.temp4 = t4;
		tempvag.resumen = resumen;
		tempvag.resumen1 = resumen1;
		tempvag.resumen2 = resumen2;
		tempvag.resumen3 = resumen3;
		// MainServices.vtext_str[nvag] = t1+" "+t2+" "+t3+" "+t4;
		// MainServices.upd_vags[nvag] = 1;
	}

	private void Trab_pgru(String datos) {
		/*
		 * <tip>#<a donde>;<desde>;/
		 */
		//<hora>;
		String[] pgru = datos.split(";");
		// rData = "PGRU: \n donde = " + Integer.parseInt(pgru[0]) +
		// "llegó?? = " + Integer.parseInt(pgru[1])
//      + "hora = " + pgru[2].toString();
		pgru_donde = Integer.parseInt(pgru[0]);
		pgru_llego = Integer.parseInt(pgru[1]);
//		pgru_hora = pgru[2].toString();
	}

	private void Trab_temps(String datos) {
		/*
		 * <tip>#<T1>;<T2>;<T3>;<T4>;<T5>;<T6>;<T7>;<T8>;/
		 */
		String[] Temps = datos.split(";");
		for (int i = 1; i < 9; i++) {
			// rData = rData.concat("T"+(i+1)+" = "+Temps[i]+"\n");
			Tn[i] = Temps[i];
		}
	}

	@Override
	protected void onPreExecute() {
		// rData = "";
		processingData = true;
		if (dataPListener != null)
			dataPListener.onDataProcessingStarted();
		super.onPreExecute();
	}

	@Override
	protected Integer doInBackground(Void... params) {
		return Trabajar_datos(data);
	}

	@Override
	protected void onPostExecute(Integer result) {
		processingData = false;
		if (dataPListener != null)
			dataPListener.onDataProcessingEnded(result);
		super.onPostExecute(result);
	}

	// pruebas XD
	// temp
	// "1#20#3#"
	// +
	// "100"+sep+"200"+sep+"17"+sep+"12"+sep+"26"+sep+"30"+sep+"31"+sep+"34"+sep+"37&"
	// +
	// "200"+sep+"400"+sep+"17"+sep+"16"+sep+"51"+sep+"35"+sep+"32"+sep+"35"+sep+"38&"
	// +
	// "300"+sep+"600"+sep+"17"+sep+"21"+sep+"02"+sep+"40"+sep+"33"+sep+"36"+sep+"39&/"
	//
	// pgru
	// "2#25;1;14:00:23;/"
	// temps
	// "3#35.15;34.20;33.98;34.11;35.02;33.95;34.53;35.06;/"

}
