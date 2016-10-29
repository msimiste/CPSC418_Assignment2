/**
 * 
 * @author Mike Simister 10095107 
 * October 1, 2016
 * Some portions and concepts taken from demo.java, Authoer: Heather Crawford
 * found here: people.ucalgary.ca/~rscheidl/418/assignments/a1/demo.java
 *
 */

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.*;

import javax.crypto.spec.*;


public class CryptoUtil {

	private SecretKeySpec sec_key_spec = null;
	private Cipher sec_cipher = null;
	private String seed;
	
	private static KeyGenerator key_gen = null;
	private static SecretKey sec_key = null;
	private static byte[] raw = null;
	private IvParameterSpec ivSpec;
	
	private static SecureRandom secRan = null;

	

	/**
	 * Constructor method
	 * @param s
	 * @throws Exception
	 */
	public CryptoUtil(String s) throws Exception {
		seed = s;
		setupUtil();
	}

	/**
	 * Initializes variables required for CryptoUtil class
	 * @throws Exception
	 */
	private void setupUtil() throws Exception{

		// encrypt file with AES
		// key setup - generate 128 bit key
		key_gen = KeyGenerator.getInstance("AES");

		// get the seed for the random number generator
		byte[] seedBytes = seed.getBytes();

		// generate the random number from seed
		secRan = new SecureRandom(seedBytes);
		secRan = SecureRandom.getInstance("SHA1PRNG");
		secRan.setSeed(seedBytes);

		// initialize the KeyGenerator
		key_gen.init(128, secRan);

		// generate the key
		sec_key = key_gen.generateKey();

		// get key material in raw form
		raw = sec_key.getEncoded();
		sec_key_spec = new SecretKeySpec(raw, "AES");

		// create the cipher object that uses AES as the algorithm
		sec_cipher = Cipher.getInstance("AES");
		
		
	}

	/**
	 * Performs a SHA1-hash on an array of bytes
	 * @param input_data
	 * @return the hash value in bytes
	 * @throws Exception
	 */
	public byte[] sha1_hash(byte[] input_data) throws Exception {
		byte[] hashval = null;
		try {
			// create message digest object
			MessageDigest sha1 = MessageDigest.getInstance("SHA1");

			// make message digest
			hashval = sha1.digest(input_data);
		} catch (NoSuchAlgorithmException nsae) {
			System.out.println(nsae);
		}
		return hashval;
	}

	/**
	 * Performs an HMAC-SHA1 hash on an array of bytes
	 * @param in_data
	 * @return the hash in bytes
	 * @throws Exception
	 */
	public byte[] hmac_sha1(byte[] in_data) throws Exception {
		byte[] result = null;

		try {
			// generate the HMAC key			
			Mac theMac = Mac.getInstance("HMACSHA1");
			theMac.init(sec_key);

			// create the hash
			result = theMac.doFinal(in_data);
			
		} catch (Exception e) {
			System.out.println(e);
		}
		return result;
	}

	/**
	 * Performs an aes-encryption on an array of bytes
	 * @param data_in
	 * @return the encrypted data in bytes
	 * @throws Exception
	 */
	public byte[] aes_encrypt(byte[] data_in) throws Exception {
		byte[] out_bytes = null;
		try {
			// set cipher object to encrypt mode
			sec_cipher.init(Cipher.ENCRYPT_MODE, sec_key_spec);

			// create ciphertext
			out_bytes = sec_cipher.doFinal(data_in);			
		} catch (Exception e) {
			System.out.println(e);
		}
		return out_bytes;
	}

	/**
	 * Performs an aes-decryption on an array of bytes
	 * @param data_in
	 * @return the decypted values in bytes
	 * @throws Exception
	 */
	public byte[] aes_decrypt(byte[] data_in) throws Exception {
		byte[] decrypted = null;
		String dec_str = null;
		try {
			// set cipher to decrypt mode
			sec_cipher.init(Cipher.DECRYPT_MODE, sec_key_spec);

			// do decryption		
			decrypted = sec_cipher.doFinal(data_in);

			// convert to string
			dec_str = new String(decrypted);
		} catch (Exception e) {
			System.out.println(e);
		}
		return decrypted;
	}

	/*
	 * Converts a byte array to hex string this code from
	 * http://java.sun.com/j2se
	 * /1.4.2/docs/guide/security/jce/JCERefGuide.html#HmacEx
	 */
	public static String toHexString(byte[] block) {
		StringBuffer buf = new StringBuffer();

		int len = block.length;

		for (int i = 0; i < len; i++) {
			byte2hex(block[i], buf);
			if (i < len - 1) {
				buf.append(":");
			}
		}
		return buf.toString();
	}

	/*
	 * Converts a byte to hex digit and writes to the supplied buffer this code
	 * from
	 * http://java.sun.com/j2se/1.4.2/docs/guide/security/jce/JCERefGuide.html
	 * #HmacEx
	 */
	public static void byte2hex(byte b, StringBuffer buf) {
		char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };
		int high = ((b & 0xf0) >> 4);
		int low = (b & 0x0f);
		buf.append(hexChars[high]);
		buf.append(hexChars[low]);
	}
}
