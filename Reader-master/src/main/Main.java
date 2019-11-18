package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.opencsv.CSVWriter;

public class Main {
	
	public static void main(String[] args) {
		int transPerdidas = 0;
		ArrayList<Double> cpuUsage = new ArrayList<>();
		ArrayList<Integer> tiempo = new ArrayList<>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("./resultados.txt"));
			String line = reader.readLine();
			while (line != null) {
				if(line.contains("TMS")) {
					tiempo.add(Integer.parseInt(line.substring(line.lastIndexOf(':')+2)));
				}
				else if(line.contains("Perdida")) {
					transPerdidas++;
				}
				line = reader.readLine();
			}
			reader.close();
			reader = new BufferedReader(new FileReader("./resultadosCPU.txt"));
			line = reader.readLine();
			while (line != null) {
				if(line.contains("CPU"))cpuUsage.add(Double.parseDouble(line.substring(line.lastIndexOf(':')+2, line.length()-1)));
				line = reader.readLine();
			}
			reader.close();
			File file = new File("./test.txt"); 
			    try { 
			        // create FileWriter object with file as parameter 
			        FileWriter outputfile = new FileWriter(file); 
			  
			        // create CSVWriter object filewriter object as parameter 
			        CSVWriter writer = new CSVWriter(outputfile); 
			  
			        // adding header to csv 
			        String[] header = { "CPU", "Tiempo", "Transacciones perdidas"}; 
			        writer.writeNext(header); 
			        Iterator<Integer> iter = tiempo.iterator();
			        // add data to csv 
			        String[] data1 = { "", "", String.valueOf(transPerdidas) }; 
			        writer.writeNext(data1); 
			        for(Double cpu: cpuUsage) {
			        	data1[0] = String.valueOf(cpu);
			        	data1[1] = "";
			        	data1[2] = "";
			        	if(iter.hasNext()) {
			        		data1[1] = String.valueOf(iter.next());
			        	}
			        	writer.writeNext(data1);
			        }
			   
			  
			        // closing writer connection 
			        writer.close(); 
			    } 
			    catch (IOException e) { 
			        // TODO Auto-generated catch block 
			        e.printStackTrace(); 
			    } 
			System.out.println(tiempo.toString());
			System.out.println(cpuUsage.toString());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	
	public static void writeDataLineByLine(String filePath) 
	{ 
	    // first create file object for file placed at location 
	    // specified by filepath 
	    File file = new File(filePath); 
	    try { 
	        // create FileWriter object with file as parameter 
	        FileWriter outputfile = new FileWriter(file); 
	  
	        // create CSVWriter object filewriter object as parameter 
	        CSVWriter writer = new CSVWriter(outputfile); 
	  
	        // adding header to csv 
	        String[] header = { "CPU", "Tiempo", "Num Perdidos"}; 
	        writer.writeNext(header); 
	  
	        // add data to csv 
	        String[] data1 = { "", "",  }; 
	        writer.writeNext(data1); 
	        String[] data2 = { "Suraj", "10", "630" }; 
	        writer.writeNext(data2); 
	  
	        // closing writer connection 
	        writer.close(); 
	    } 
	    catch (IOException e) { 
	        // TODO Auto-generated catch block 
	        e.printStackTrace(); 
	    } 
	} 


}
