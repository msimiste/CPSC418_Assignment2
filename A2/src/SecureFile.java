/**
 * 
 * @author Mike Simister 10095107 
 * October 1, 2016
 * Some portions and concepts taken from demo.java, Authoer: Heather Crawford
 * found here: people.ucalgary.ca/~rscheidl/418/assignments/a1/demo.java
 *
 */

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class SecureFile {

	private String inFile = null;
	//private static String outFile = null;
	private  String seed = null;

/*	public static void main(String[] args) {

		inFile = args[0];
		
		// created the filename for the output file
		outFile = inFile.substring(0, inFile.indexOf(".")) + "_encrypted" + inFile.substring(inFile.indexOf("."));
		seed = args[1];

		try {
			setupAES();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	
	public SecureFile(String fileIn, String inputSeed){
		this.seed = inputSeed;
		this.inFile = fileIn;
	}

	public byte[] encryptWithAES() throws Exception {

		FileInputStream in_file = null;
		FileInputStream in_file2 = null;
		FileOutputStream out_file = null;		

		byte[] sha_hash = null;
		byte[] hmac_hash = null;
		byte[] aes_ciphertext = null;
		
		int read_bytes = 0;
		try {
			// open files
			in_file = new FileInputStream(inFile);
			//out_file = new FileOutputStream(outFile);

			// read file into a byte array
			byte[] msg = new byte[in_file.available()];			
			
			read_bytes = in_file.read(msg);
			
			CryptoUtil cryptoUtil = new CryptoUtil(seed);

			// SHA-1 Hash
			sha_hash = cryptoUtil.sha1_hash(msg);

			// print out hash in hex
			System.out.println("SHA-1 Hash: " + cryptoUtil.toHexString(sha_hash));

			// HMAC SHA-1 CBC Hash
			hmac_hash = cryptoUtil.hmac_sha1(msg);

			// Print out hash in hex
			System.out.println("SHA-1 HMAC: " + cryptoUtil.toHexString(hmac_hash));
			
			byte[] inFileHash = addHashToFile(msg,hmac_hash);
			
			// do AES encryption
			aes_ciphertext = cryptoUtil.aes_encrypt(inFileHash);			
			
			/*//write to file
			out_file.write(aes_ciphertext);
			out_file.close();*/
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			if (in_file != null) {
				in_file.close();
			}
			if (out_file != null) {
				out_file.close();
			}
			if (in_file2 != null) {
				in_file2.close();
			}
		}
		return aes_ciphertext;
	}	
	
	private static byte[] addHashToFile(byte[] file, byte[] hash){
		
		int tempFortest = file.length;
		byte[] combinedFile = new byte[file.length + hash.length];
		System.arraycopy(file, 0, combinedFile, 0, file.length);
		System.arraycopy(hash, 0, combinedFile, file.length, hash.length);
		return combinedFile;
	}
}
