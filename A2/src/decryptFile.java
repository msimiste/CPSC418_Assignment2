/**
 * 
 * @author Mike Simister 10095107 
 * October 1, 2016
 * Some portions and concepts taken from demo.java, Authoer: Heather Crawford
 * found here: people.ucalgary.ca/~rscheidl/418/assignments/a1/demo.java
 *
 */

import java.io.*;

public class decryptFile {

	private static String seed;
	private static String inFile = null;
	private static String outFile = null;

	public static void main(String args[]) throws Exception {

		inFile = args[0];
		outFile = inFile.substring(0, inFile.indexOf(".")) + "_OUT" + inFile.substring(inFile.indexOf("."));
		FileInputStream in_file = null;
		FileOutputStream out_file = null;

		seed = args[1];

		CryptoUtil cryptoUtil = new CryptoUtil(seed);

		byte[] hmac_hash = null;
		int read_bytes = 0;
		boolean verify = false;

		// decrypt file
		try {
			in_file = new FileInputStream(inFile);
			out_file = new FileOutputStream(outFile);
			byte[] ciphtext = new byte[in_file.available()];
			in_file.read(ciphtext);
			byte[] decryptedFile =null;
			byte[] fileNoHash = null;
			byte[] decryptedHash = null;
			byte[] tamperedFile = null;
			byte[] tamperedHash = null;
			

			// convert the decrypted file to bytes
			decryptedFile = cryptoUtil.aes_decrypt(ciphtext);
			

			// get a hash of the decrypted file
			// use this to determine the length of the hash to extract from the
			// array of decrypted bytes
			hmac_hash = cryptoUtil.hmac_sha1(decryptedFile);			
			

			// set lengths of the byte arrays for decryptedHash and fileNoHash
			decryptedHash = new byte[hmac_hash.length];
			
			
			fileNoHash = new byte[decryptedFile.length - hmac_hash.length];
			

			// chop the message digest from the file
			fileHashRemoved(decryptedFile, hmac_hash, fileNoHash, decryptedHash);
			
			tamperedFile = new byte[fileNoHash.length];
			tamperedFile = tamperWithFile(fileNoHash, tamperedFile);

			System.out.println("Decrypted hmac hash: "+ cryptoUtil.toHexString(decryptedHash));
			
			// get a hash of the chopped file, ie the file with the hash removed
			hmac_hash = cryptoUtil.hmac_sha1(fileNoHash);

			System.out.println("hmac_hash of fileNoHash: "+ cryptoUtil.toHexString(hmac_hash));
			// compare the hashed value of the file to the message digest
			verify = compareHash(hmac_hash, decryptedHash);

			System.out.println("File is OK: " + verify);
			
			if(verify){
				//write to file
				out_file.write(fileNoHash);
			}
			
			tamperedHash = cryptoUtil.hmac_sha1(tamperedFile);
			System.out.println("tampered hmac hash: " + cryptoUtil.toHexString(tamperedHash));
			
			verify = compareHash(tamperedHash, decryptedHash);
			
			System.out.println("tampered file is OK: "+verify);
			
			
			out_file.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
    
	/**
	 * Compares 2 byte arrays and returns true if all bytes are equal otherwise returns false
	 * @param inputHash first byte array, the input hash
	 * @param resultHash second byte array, the result hash
	 * @return
	 */
	private static boolean compareHash(byte[] inputHash, byte[] resultHash) {
		boolean compareResult = false;
		for (int i = 0; i < inputHash.length; i++) {
			compareResult = inputHash[i] == resultHash[i];
			if (!(compareResult)) {
				return compareResult;
			}
		}
		return compareResult;
	}

	/**
	 * Separated the file and the message digest from 1 array into 2 arrays.
	 * @param file - The decryped file, in bytes
	 * @param hash - a hash of the decrypted file, used to get proper length for extraction
	 * @param fileOut - a byte array to copy file portion (in bytes)
	 * @param hashOut - a byte array to compy the extracted hash (message digest) to
	 */
	private static void fileHashRemoved(byte[] file, byte[] hash,
			byte[] fileOut, byte[] hashOut) {

		System.arraycopy(file, 0, fileOut, 0, fileOut.length);
		System.arraycopy(file, fileOut.length, hashOut, 0, hash.length);

	}
	
	private static byte[] tamperWithFile(byte[] infile, byte[] tamperedFile){
		System.arraycopy(infile, 0, tamperedFile, 0, infile.length);
		
		byte tempByte = tamperedFile[tamperedFile.length/2];
		
		tempByte = (byte) (tempByte + 2);
		
		tamperedFile[tamperedFile.length/2] = tempByte;
		
		return tamperedFile;
	}

}
