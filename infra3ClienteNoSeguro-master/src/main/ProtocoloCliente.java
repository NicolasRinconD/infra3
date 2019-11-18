package main;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ProtocoloCliente {
	
	public static final String[] AlG_S = new String[]{"AES", "Blowfish"};
	public static final String ALG_A = "RSA";
	public static final String[] ALG_H = new String[]{"HMACSHA1", "HMACSHA256", "HMACSHA384", "HMACSHA512"};
	
	public static void procesar(BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut) throws IOException, CertificateException, NoSuchAlgorithmException, 
	InvalidKeyException, IllegalBlockSizeException, SignatureException{
		
		
		System.out.println("Intentando conectarse.. ");

		pOut.println("HOLA"); 
		
		
		String fromServer ="";
		if((fromServer = pIn.readLine()) != null){
			System.out.println(fromServer);
			if(fromServer.equals("ERROR")) System.exit(-1);
			else if(fromServer.equals("OK")) System.out.println("Conexion Exitosa.");
		}
		
		System.out.println("Eligiendo los Algoritmos a usar...");
		int randomALGS = ThreadLocalRandom.current().nextInt(0, 2);
		int randomALGH = ThreadLocalRandom.current().nextInt(0, 4);
		
		pOut.println("ALGORITMOS:" + AlG_S[randomALGS] + ":RSA:" + ALG_H[randomALGH]);
		if((fromServer = pIn.readLine()) != null){
			System.out.println(fromServer); //OK||ERROR
			if(fromServer.equals("ERROR")) System.exit(-1);
		}
		
		
		KeyGenerator keygen  = KeyGenerator.getInstance(AlG_S[randomALGS]);
		SecretKey llaveSimetrica = keygen.generateKey();
		
		if((fromServer = pIn.readLine()) != null){
			if(fromServer.equals("ERROR")) System.exit(-1);
			System.out.println("Recibe y crea llave pública");
		}
		String certificadoDigital = fromServer;  //Certificado del Servidor Web
		
		PublicKey llavePublica = validar(certificadoDigital);
		
		
		String llaveSimetricaStr = DatatypeConverter.printBase64Binary(llaveSimetrica.getEncoded());
			
		
		pOut.println(llaveSimetricaStr);
		System.out.println("Escribiendo reto...");
		String reto = randString(12);
		pOut.println(reto);
		if((fromServer = pIn.readLine()) != null){
			if(fromServer.equals("ERROR")) System.exit(-1);
			System.out.println("Reto del servidor: " + fromServer); //Reto 
		}
		
		String retoServidor =  fromServer;

		
		if(retoServidor.equals(reto)) {System.out.println("Autenticación Exitosa");pOut.println("OK");}
		else {pOut.println("ERROR"); System.exit(-1);}
		
		//ETAPA 3//
		System.out.println("Escribiendo cc..");
		String cc = randString(12);
		
		System.out.println("Escribiendo clave...");
		String clave = randString(16);
		
		pOut.println(cc); 
		pOut.println(clave);
		
		if((fromServer = pIn.readLine()) != null){
			if(fromServer.equals("ERROR")) System.exit(-1);
			System.out.println("Recibe Valor"); 
		}
		String valorClaro = fromServer; //CKS(<valor>)
		
		if((fromServer = pIn.readLine()) != null){
			if(fromServer.equals("ERROR")) System.exit(-1);
			System.out.println("Recibe H(Valor)"); 
		}
		String valorCifradoHMAC = fromServer; //CKW-(HMAC_KS(<valor>))
		
		
		String ValorHMACLocal = DatatypeConverter.printBase64Binary(calcularHMAC(llaveSimetrica, ALG_H[randomALGH], DatatypeConverter.parseBase64Binary(valorClaro)));
		System.out.println("VALOR HMAC LOCAL: " + ValorHMACLocal);
		
		if(valorCifradoHMAC.equals(ValorHMACLocal)) {System.out.println("Autenticacion Exitosa");pOut.println("OK");}
		else pOut.println("ERROR");
	
		
		
	}

	
	/**
	 * 
	 * METODOS DE AYUDA
	 * 
	 */
	
	/**
	 * Valida el certificado digital y crea la llave publica a partir de este
	 * @param certificadoDigital El CD que envía el servidor
	 * @return La llave publica contenida en el CD
	 * @throws CertificateException Si hay un error al obtener el certificado
	 */
	private static PublicKey validar(String certificadoDigital) throws CertificateException {
		byte[] certificadoBytes= DatatypeConverter.parseBase64Binary(certificadoDigital);
		
		X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(certificadoBytes));
		try{cert.checkValidity();}
		catch (Exception e) {e.printStackTrace();}
		return cert.getPublicKey();
	}

	/**
	 * Cifra un texto
	 * @param llave LLave con la que se cifra
	 * @param algoritmo Algoritmo con el que se cifra el texto
	 * @param texto Dato que se va a cifrar
	 * @return El texto cifrado en un arreglo de bytes.
	 */
	public static byte[] cifrar(Key llave, String algoritmo, byte[] texto) {
		byte[] textoCifrado;
		
		try {
			Cipher cifrador = Cipher.getInstance(algoritmo);
			byte[] textoClaro = texto;
			
			cifrador.init(Cipher.ENCRYPT_MODE, llave);
			textoCifrado = cifrador.doFinal(textoClaro);
			
			return textoCifrado;
		}catch (Exception e) {
			System.out.println("Excepcion: " + e.getMessage());
			return null;
		}
	}
	
	
	public static byte[] descifrar(Key llave, String algoritmo, byte[] texto) {
		byte[] textoClaro;
		
		try {
			Cipher descifrador = Cipher.getInstance(algoritmo);
			descifrador.init(Cipher.DECRYPT_MODE, llave);
			textoClaro = descifrador.doFinal(texto);
			return textoClaro;
		}catch (Exception e) {
			System.out.println("Excepcion: " + e.getMessage());
			return null;
		}
		
	}
	
	/**
	 * Añade ceros al final del string hasta que su longitud sea un multiplo de 4
	 * @param text Texto a convertir
	 * @return Texto con longitud multiplo de 4
	 */
	public static String pasarMultiploDe4(String text){
		while(text.length()%4 != 0){
			text += "0";
		}
		return text;
	}
	

	/**
	 * Da la 
	 * @param key
	 * @param algoritmo
	 * @param texto
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	public static byte[] calcularHMAC(SecretKey key, String algoritmo, byte[] texto)
		throws NoSuchAlgorithmException, InvalidKeyException
	{
		Mac mac = Mac.getInstance(algoritmo);
		mac.init(key);
		return (mac.doFinal(texto));
	}

	public static String randString(int length) {
		int leftLimit = 97; // letter 'a'
		int rightLimit = 122; // letter 'z'
		Random random = new Random();
		StringBuilder buffer = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			int randomLimitedInt = leftLimit + (int)(random.nextFloat() * (rightLimit - leftLimit + 1));
			buffer.append((char) randomLimitedInt);
		}
		return buffer.toString();
	}


}
