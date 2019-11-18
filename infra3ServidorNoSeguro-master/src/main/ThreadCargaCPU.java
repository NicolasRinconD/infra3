package main;

import java.io.File;
import java.io.FileWriter;
import java.lang.management.ManagementFactory;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class ThreadCargaCPU extends Thread {

	private String medicionCPU;
	private Object semaforo;
	private boolean primeraVez;
	private int cargaEsperada = 400;
	
	public ThreadCargaCPU()
	{
		primeraVez = true;
	}
	public void run()
	{
		semaforo = new Object();
		medicionCPU = "";
		while(cargaEsperada>0)
		{
			if(primeraVez)
			{
				try {
					synchronized(semaforo)
					{
					semaforo.wait();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				primeraVez=false;
			}
			try {
				medicionCPU+="Porcentaje de uso CPU: "+String.valueOf(getSystemCpuLoad()+"%\r\n");
				this.sleep(40);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e);
			}
		}
		escribirMensaje(medicionCPU);
	}
	public String darCPU()
	{
		return medicionCPU;
	}
	public static double getSystemCpuLoad() throws Exception{
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
		AttributeList list = mbs.getAttributes(name, new String[] {"SystemCpuLoad"});
		if(list.isEmpty()) 	return Double.NaN;
		
		Attribute att = (Attribute) list.get(0);
		Double val = (Double) att.getValue();
		
		if(val == -1.0) return Double.NaN;
		
		return ((int)(val *1000) /10.0);
	}
	public void activar()
	{
		if(primeraVez)
		{
			synchronized(semaforo)
			{
			semaforo.notify();
			}
		}
	}
	public void liberar()
	{
		cargaEsperada--;
	}
	private void escribirMensaje(String pCadena) {
		
		try {
			FileWriter fw = new FileWriter(new File("./resultadosCPU.txt"),true);
			fw.write(pCadena + "\r");
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
