import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

import javax.crypto.spec.SecretKeySpec;

/**
 * Thread to deal with clients who connect to Server. Put what you want the
 * thread to do in it's run() method.
 */

public class ServerThread extends Thread {
	private Socket sock; // The socket it communicates with the client on.
	private Server parent; // Reference to Server object for message passing.
	private int idnum; // The client's id number.

	private String seed;
	private String destFile;

	//private boolean debug;
	private int messageLength;
	byte[] messageArr;
	int briancounter = 0;
	boolean buildArray = false;
	byte[] destinationFile;

	/**
	 * Constructor, does the usual stuff.
	 * 
	 * @param s
	 *            Communication Socket.
	 * @param p
	 *            Reference to parent thread.
	 * @param id
	 *            ID Number.
	 */
	public ServerThread(Socket s, Server p, int id) {
		parent = p;
		sock = s;
		idnum = id;
		System.out.println("Please enter seed: ");
		Scanner in = new Scanner(System.in);
		setSeed(in.next());
		in.close();
		// decrypter = new DecryptFile(this.seed);
	}

	/**
	 * Getter for id number.
	 * 
	 * @return ID Number
	 */
	public int getID() {
		return idnum;
	}

	/**
	 * Getter for the socket, this way the parent thread can access the socket
	 * and close it, causing the thread to stop blocking on IO operations and
	 * see that the server's shutdown flag is true and terminate.
	 * 
	 * @return The Socket.
	 */
	public Socket getSocket() {
		return sock;
	}

	/**
	 * This is what the thread does as it executes. Listens on the socket for
	 * incoming data and then echos it to the screen. A client can also ask to
	 * be disconnected with "exit" or to shutdown the server with "die".
	 */
	public void run() {
		// BufferedReader in = null;
		String incoming = null;
		DataOutputStream out = null;
		DataInputStream inStream = null;

		String s = "";
		/* Try to read from the socket */
		try {
			boolean buildArray = false;

			inStream = new DataInputStream(sock.getInputStream());
			out = new DataOutputStream(sock.getOutputStream());

			byte[] buf = new byte[32767];
			int count, off = 0;
			String info = "";
			int byteCounter = 0;
			System.out.println("Counts: " + briancounter++);

			while ((count = inStream.read(buf)) > 0) {
				info = "";
				if(this.parent.getDebug()){
					System.out.println("Gathering bytes from stream " + count);
				}
				byte[] decrypted = new byte[1];
				if (!(buildArray)) {
					byte[] toBdecrypt = new byte[count];
					System.arraycopy(buf, 0, toBdecrypt, 0, count);
					decrypted = this.decrypt(toBdecrypt);
					info = new String(decrypted, 0, decrypted.length);
				}
			

				// info = new String(decryptedBytes,0,count);
				if (info.compareTo("exit") == 0) {
					parent.kill(this);
					try {
						inStream.close();
						sock.close();
					} catch (IOException e) {// nothing to do
					}
					return;
				}

				/*
				 * If the client has sent "die", instruct the server to signal
				 * all threads to shutdown, then exit.
				 */

				else if (info.compareTo("die") == 0) {
					parent.killall();

					return;
				} else if (info.contains("Destination File:")) {
					int start = info.indexOf(":") + 1;
					this.destFile = info.substring(start);

					 if(parent.getDebug()){
					System.out.println("Stored dest file:" + this.destFile);
					 }
				} else if (info.contains("Message Length:")) {
					 if(parent.getDebug()){
						 System.out.println(info);
					 }
					int start = info.indexOf(":") + 1;
					String mLength = info.substring(start);
					this.messageLength = Integer.parseInt(mLength);
					messageArr = new byte[this.messageLength];
					buildArray = true;
				}

				else {
					 if(parent.getDebug()){
						 System.out.println("Message Array length:  " + messageArr.length);
					 }
					int temp = count + off;
					System.out.println("Count + off" + count + " " + off + " " + temp);
					System.arraycopy(buf, 0, messageArr, off, count);
					off += count;
					System.out.println("Offset :" + off);
				}

				// Otherwise, just echo what was recieved.
				 if(parent.getDebug()){
					 System.out.println("Client " + idnum + ": " + incoming);
				 }

			}

			byte[] decrypted = this.decrypt(messageArr);
			boolean saveOk = this.saveFile(decrypted);
			if (saveOk) {
				sendEncryptedAck(out);

			}

			// Scanner input = new Scanner(System.in);
			// setSeed(incoming);
		} catch (IOException e) {
			if (parent.getFlag()) {
				System.out.println("shutting down.");
				return;
			}
			return;
		} catch (Exception e1) {
			System.out.println("Decryption issues:");
			e1.printStackTrace();
			return;
		}
	}

	private void sendEncryptedAck(OutputStream out) {
		 if(parent.getDebug()){
			 System.out.println("Sending Acknowledgement");
		 }
		SecretKeySpec key = CryptoUtilities.key_from_seed(this.seed.getBytes());

		byte[] temp = ("File Saved Succesfully").getBytes();
		byte[] encryptedAck = CryptoUtilities.encrypt(temp, key);
		try {
			out.write(encryptedAck);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private byte[] decrypt(byte[] toDecrypt) {
		 if(parent.getDebug()){
			 System.out.println("decrypting");
		 }
		SecretKeySpec key = CryptoUtilities.key_from_seed(this.seed.getBytes());
		byte[] decrypted = CryptoUtilities.decrypt(toDecrypt, key);
		 if(parent.getDebug()){
			 System.out.println("Source File: ");
		 }
		CryptoUtil.toHexString(decrypted);
		boolean isValid = CryptoUtilities.verify_hash(decrypted, key);
		if (isValid) {
			byte[] temp = CryptoUtilities.extract_message(decrypted);		
			CryptoUtil.toHexString(temp);
			return temp;
		}

		else
			return new byte[1];
	}

	private boolean saveFile(byte[] fileToSave) {
		boolean fileSaved = false;
		 if(parent.getDebug()){
			 System.out.println("Saving File");
		 }
		try {
			FileOutputStream file_out = new FileOutputStream(this.destFile);
			file_out.write(fileToSave);
			fileSaved = true;
		} catch (IOException e) {
			System.out.println("Trouble saving file");
			return fileSaved;
		}
		return fileSaved;
	}

	private void setDestFile(String s) {
		this.destFile = s;
	}

	private void setSeed(String k) {
		this.seed = k;
	}

}