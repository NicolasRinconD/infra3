package main;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
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

public class ProtocoloCliente {

	public static void procesar(BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut) throws IOException, CertificateException, NoSuchAlgorithmException, 
	InvalidKeyException, IllegalBlockSizeException, SignatureException{
		
		
		System.out.println("Escriba el mensaje para enviar: ");

		
		
		String fromUser = stdIn.readLine(); // Enviar HOLA
		pOut.println(fromUser); 
		
		
		String fromServer ="";
		if((fromServer = pIn.readLine()) != null){
			System.out.println(fromServer);
			if(fromServer.equals("ERROR")) System.exit(-1);
		}
		
		System.out.println("Escriba los Algoritmos a usar:");
		fromUser = stdIn.readLine(); //Enviar Algoritmos (¡OJO!: Poner Blowfish como esta escrito aqui, no completamente en mayuscula ni en minuscula)
		String[] arr = fromUser.split(":");
		String algSimetrico = arr[1], algAsimetrico = arr[2], algHMAC = arr[3];
		
		pOut.println(fromUser);
		if((fromServer = pIn.readLine()) != null){
			System.out.println(fromServer); //OK||ERROR
			if(fromServer.equals("ERROR")) System.exit(-1);
		}
		
		
		KeyGenerator keygen  = KeyGenerator.getInstance(algSimetrico);
		SecretKey llaveSimetrica = keygen.generateKey();
		
		if((fromServer = pIn.readLine()) != null){
			if(fromServer.equals("ERROR")) System.exit(-1);
			System.out.println("Recibe y crea llave pública");
		}
		String certificadoDigital = fromServer;  //Certificado del Servidor Web
		
		PublicKey llavePublica = validar(certificadoDigital);
		
		
		String llaveSimetricaCifrada = DatatypeConverter.printBase64Binary(cifrar(llavePublica, algAsimetrico, llaveSimetrica.getEncoded()));
			
		
		pOut.println(llaveSimetricaCifrada);
		System.out.println("Escriba reto:");
		fromUser = stdIn.readLine();
		String reto = pasarMultiploDe4(fromUser);
		pOut.println(reto);
		if((fromServer = pIn.readLine()) != null){
			if(fromServer.equals("ERROR")) System.exit(-1);
			System.out.println("Reto Cifrado: " + fromServer); //Reto Cifrado
		}
		
		String retoCifrado =  fromServer;
		String retoCifradoLocalmente = DatatypeConverter.printBase64Binary((cifrar(llaveSimetrica, algSimetrico, DatatypeConverter.parseBase64Binary(reto))));

		System.out.println("Reto Cifrado localmente: " + retoCifradoLocalmente);
		
		if(retoCifrado.equals(retoCifradoLocalmente)) {System.out.println("Autenticación Exitosa");pOut.println("OK");}
		else pOut.println("ERROR");
		
		//ETAPA 3//
		System.out.println("Escriba cc:");
		fromUser = stdIn.readLine();
		String cc = pasarMultiploDe4(fromUser);
		String ccCifrado = DatatypeConverter.printBase64Binary((cifrar(llaveSimetrica, algSimetrico, cc.getBytes())));
		
		System.out.println("Escriba clave:");
		fromUser = stdIn.readLine();
		String clave = pasarMultiploDe4(fromUser);
		String claveCifrada = DatatypeConverter.printBase64Binary((cifrar(llaveSimetrica, algSimetrico, clave.getBytes())));
		
		pOut.println(ccCifrado); 
		pOut.println(claveCifrada);
		
		if((fromServer = pIn.readLine()) != null){
			if(fromServer.equals("ERROR")) System.exit(-1);
			System.out.println("Recibe Valor Cifrado"); 
		}
		String valorCifradoKS = fromServer; //CKS(<valor>)
		
		if((fromServer = pIn.readLine()) != null){
			if(fromServer.equals("ERROR")) System.exit(-1);
			System.out.println("Recibe Codigo de autenticacion"); 
		}
		String valorCifradoHMAC = fromServer; //CKW-(HMAC_KS(<valor>))
		
		String ValorClaro = DatatypeConverter.printBase64Binary(descifrar(llaveSimetrica, algSimetrico, DatatypeConverter.parseBase64Binary(valorCifradoKS)));
		System.out.println("VALOR descifrado: " + ValorClaro);

		String ValorHMAC = DatatypeConverter.printBase64Binary(descifrar(llavePublica, algAsimetrico, DatatypeConverter.parseBase64Binary(valorCifradoHMAC))); //
		System.out.println("VALOR HMAC: " + ValorHMAC);
		
		//TODO HACER HMAC y AUTENTICAR
		String ValorHMACLocal = DatatypeConverter.printBase64Binary(calcularHMAC(llaveSimetrica, algHMAC, DatatypeConverter.parseBase64Binary(ValorClaro)));
		System.out.println("VALOR HMAC LOCAL: " + ValorHMACLocal);
		
		if(ValorHMAC.equals(ValorHMACLocal)) {System.out.println("Autenticacion Exitosa");pOut.println("OK");}
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



}
