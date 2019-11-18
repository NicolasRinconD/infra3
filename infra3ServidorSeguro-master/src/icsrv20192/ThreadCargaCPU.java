package icsrv20192;

import java.io.File;
import java.io.FileWriter;
import java.lang.management.ManagementFactory;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class ThreadCargaCPU extends Thread {

	private String medicionCPU;
	
	public ThreadCargaCPU()
	{
	}
	public void run()
	{
		medicionCPU = "";
		while(true)
		{
			try {
				medicionCPU ="Porcentaje de uso CPU: "+String.valueOf(getSystemCpuLoad()+"%\r\n");
				sleep(50);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e);
			}
			escribirMensaje(medicionCPU);
		}
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
