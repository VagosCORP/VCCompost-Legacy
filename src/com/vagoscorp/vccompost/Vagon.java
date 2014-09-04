package com.vagoscorp.vccompost;

public class Vagon {
	
	public int nvag;
	public int[] px;
	public int[] py;
	public String[] hora;
	public int[] temp1;
	public int[] temp2;
	public int[] temp3;
	public int[] temp4;
	public int resumen = MainService.BUENO;
	public String resumen1 = "Temperatura Mínima";
	public String resumen2 = "Temperatura Máxima";
	public String resumen3 = "Resumen de Estado";
	
	public Vagon() {
		
	}
	
	public Vagon(int xvag, String hr) {
		int tnvag = xvag;
		int [] tpx = {};
		int [] tpy = {};
		String[] thora = {hr};
		int[] t1 = {};
		int[] t2 = {};
		int[] t3 = {};
		int[] t4 = {};
		nvag = tnvag;
		px = tpx;
		py = tpy;
		hora = thora;
		temp1 = t1;
		temp2 = t2;
		temp3 = t3;
		temp4 = t4;
//		resumen = MainService.BUENO;
//		resumen1 = "Temperatura Mínima";
//		resumen2 = "Temperatura Máxima";
//		resumen3 = "Resumen de Estado";
		
	}

}