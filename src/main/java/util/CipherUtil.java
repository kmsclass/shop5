package util;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;
//해쉬알고리즘(SHA-256)을 이용하여 비밀번호 암호화.
//대칭키알고리즘(AES)이용하여 문자열을 암호화 복호화를 위한 기능. 
@Component
public class CipherUtil {
	private final static byte[] iv = new byte[] {
			(byte) 0x8E, 0x12, 0x39,(byte) 0x9C, 
			       0x07, 0x72, 0x6F, (byte) 0x5A,
			(byte) 0x8E, 0x12, 0x39, (byte) 0x9C, 
			       0x07, 0x72, 0x6F, (byte) 0x5A };
	static Cipher cipher; //암호객체
	static {
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); // 암호화알고리즘/블럭암호화모드/패딩방법
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	//plain : 입력된 비밀번호
	public String makehash(String plain) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] pbyte = plain.getBytes();
		byte[] hash = md.digest(pbyte); //해쉬암호화 실행
		return byteToHex(hash);		
	}
	private String byteToHex(byte[] hash) {
		if(hash == null) return null;
		String str = "";
		for (byte b : hash) str+= String.format("%02X", b);
		return str;
	}
	//16진수로 나열된 문자열값을 byte[]배열로 리턴
	private  byte[] hexToByte(String str) {
		if (str == null || str.length() < 2)	return null;
		int len = str.length() / 2;
		byte[] buf = new byte[len];
		for (int i = 0; i < len; i++) {
			buf[i] = (byte) Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16);
		}
		return buf;
	}	
	//키 설정 후 암호화 하기
	public String encrypt(String plain, String key) {
		byte[] cipherMsg = new byte[1024];
		try {
			Key genKey = new SecretKeySpec(makeKey(key), "AES");
			AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
			cipher.init(Cipher.ENCRYPT_MODE, genKey, paramSpec);
			cipherMsg = cipher.doFinal(plain.getBytes()); //암호화 실행
		} catch (Exception e) {
			e.printStackTrace();
		}
		return byteToHex(cipherMsg); //16진수로 표시된 문자열
	}
	private  byte[] makeKey(String key) {
		int len = key.length();
		char ch = 'A';
		for (int i = len; i < 16; i++)
			key += ch++;
		return key.substring(0, 16).getBytes();
	}
	public  String decrypt(String cipher1, String key) {
		byte[] plainMsg = new byte[1024];
		try {
			Key genKey = new SecretKeySpec(makeKey(key), "AES");
			AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
			cipher.init(Cipher.DECRYPT_MODE, genKey, paramSpec);
			plainMsg = cipher.doFinal(hexToByte(cipher1.trim())); //복호화 실행
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new String(plainMsg).trim();
	}
}