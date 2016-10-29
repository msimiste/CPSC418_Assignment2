import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

import javax.crypto.spec.SecretKeySpec;

/**
 * Client program. Connects to the server and sends text accross.
 */

public class Client {
	private Socket sock; // Socket to communicate with.
	private String seed;
	private String inFile;
	private String outFile;

	private SecureFile encrypter;
	private SecretKeySpec key;
	private int messageLength;

	/**
	 * Main method, starts the client.
	 * 
	 * @param args
	 *            args[0] needs to be a hostname, args[1] a port number.
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: java Client hostname port#");
			System.out.println("hostname is a string identifying your server");
			System.out.println("port is a positive integer identifying the port to connect to the server");
			return;
		}

		try {
			Client c = new Client(args[0], Integer.parseInt(args[1]));
		} catch (NumberFormatException e) {
			System.out.println("Usage: java Client hostname port#");
			System.out.println("Second argument was not a port number");
			return;
		}
	}

	/**
	 * Constructor, in this case does everything.
	 * 
	 * @param ipaddress
	 *            The hostname to connect to.
	 * @param port
	 *            The port to connect to.
	 */
	public Client(String ipaddress, int port) {
		/* Allows us to get input from the keyboard. */
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String userinput;
		InputStream in = null;
		OutputStream outStream = null;

		/* Try to connect to the specified host on the specified port. */
		try {
			sock = new Socket(InetAddress.getByName(ipaddress), port);
		} catch (UnknownHostException e) {
			System.out.println("Usage: java Client hostname port#");
			System.out.println("First argument is not a valid hostname");
			return;
		} catch (IOException e) {
			System.out.println("Could not connect to " + ipaddress + ".");
			return;
		}

		/* Status info */
		System.out.println("Connected to " + sock.getInetAddress().getHostAddress() + " on port " + port);

		try {
			System.out.println("Please Provide a seed value: ");
			setSeed(stdIn.readLine());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			in = sock.getInputStream();
			outStream = sock.getOutputStream();
			getFileNames();
			// setEncryption();
			// byte[] encrypted = encrypter.encryptWithAES();
			// byte[] arr1 = glueFiles("#~files~#",encrypted);
			byte[] plainFile = this.getFileInBytes();
			encryptOnly(plainFile);
			//String len = plainFile.length + "";					
			encryptSendMessage(outStream, ("Destination File:" + this.outFile).getBytes());
			encryptSendMessage(outStream, ("Message Length:" + this.messageLength).getBytes());	
			encryptSendMessage(outStream, plainFile);
			//outStream.flush();
			// outStream.write(encrypted);
			// outStream.flush();

			
			// outStream.write(plainFile);
			// outStream.flush();
			/*
			 * encryptSendMessage(outStream, ("Destination File:" +
			 * this.outFile).getBytes()); outStream.flush();
			 */
		} catch (IOException e) {
			System.out.println("Could not create output stream.");
			return;
		} catch (Exception e) {
			System.out.println("Issue with encryption");
			return;
		}

		/* Wait for the user to type stuff. */
		try {
			while ((userinput = stdIn.readLine()) != null) {
				outStream.write(userinput.getBytes());
				outStream.flush();
				/*
				 * Tricky bit. Since Java does short circuiting of logical
				 * expressions, we need to checkerror to be first so it is
				 * always executes. Check error flushes the outputstream, which
				 * we need to do every time after the user types something,
				 * otherwise, Java will wait for the send buffer to fill up
				 * before actually sending anything. See PrintWriter.flush(). If
				 * checkerror has reported an error, that means the last packet
				 * was not delivered and the server has disconnected, probably
				 * because another client has told it to shutdown. Then we check
				 * to see if the user has exitted or asked the server to
				 * shutdown. In any of these cases we close our streams and
				 * exit.
				 */
				if ((userinput.compareTo("exit") == 0) || (userinput.compareTo("die") == 0)) {
					System.out.println("Client exiting.");
					stdIn.close();
					sock.close();
					return;
				}
				spitInput(in);
			}

		} catch (IOException e) {
			System.out.println("Could not read from input.");
			return;
		}
	}

	private byte[] messageToBytes(String fileName) {
		byte[] fileInBytes = new byte[10];
		try {
			FileInputStream in_file = new FileInputStream(fileName);
			fileInBytes = new byte[in_file.available()];

		} catch (FileNotFoundException e) {
			System.out.println("The file does not exist");
			return fileInBytes;

		} catch (IOException e) {
			System.out.println("IO Error");
			return fileInBytes;
		}
		return fileInBytes;
	}

	private void encryptOnly(byte[] message) {
		// get key
		this.key = CryptoUtilities.key_from_seed(this.seed.getBytes());
		// hash message and append hash
		byte[] hashedMessage = CryptoUtilities.append_hash(message, key);
		// encrypt
		byte[] encryptedMessage = CryptoUtilities.encrypt(hashedMessage, key);
		
		this.messageLength = encryptedMessage.length;
	}

	private void encryptSendMessage(OutputStream out, byte[] message) throws InterruptedException {

		// get key
		this.key = CryptoUtilities.key_from_seed(this.seed.getBytes());
		// hash message and append hash
		byte[] hashedMessage = CryptoUtilities.append_hash(message, key);
		// encrypt
		byte[] encryptedMessage = CryptoUtilities.encrypt(hashedMessage, key);
		// send to server
		try {
			out.write(encryptedMessage);
			out.flush();
			System.out.println("Sent Bytes = " + encryptedMessage.length);
			Thread.sleep(100);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void getFileNames() {
		Scanner in = new Scanner(System.in);
		System.out.println("Enter Source File name :");
		setInputFileName(in.next());
		System.out.println("Enter Destination File name :");
		setOutputFileName(in.next());
	}

	private void setOutputFileName(String next) {
		this.outFile = next;
	}

	private void setInputFileName(String next) {
		this.inFile = next;
	}

	private byte[] glueFiles(String head, byte[] tail) {

		byte[] whole = new byte[head.length() + tail.length];
		System.arraycopy(head, 0, whole, 0, head.length());
		System.arraycopy(tail, 0, whole, head.length() + 1, tail.length);
		return whole;
	}

	/**
	 * for debugging purposese
	 * 
	 * @param in
	 *            is an inputstream from server
	 */
	private void spitInput(InputStream in) {
		/*
		 * String s = ""; // read the inputstream ie, the get request into a
		 * byte array byte[] buf = new byte[1024]; int count = 0; try { count =
		 * in.read(buf); } catch (IOException e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); }
		 * 
		 * // extract the entire request into a string if (count > 0) { s += new
		 * String(buf, 0, count); System.out.println(s); }
		 */

		// read the inputstream ie, the get request into a byte array
		byte[] buf = new byte[32767];
		int count, off = 0;
		String info = "";
		int byteCounter = 0;

		try {
			while ((count = in.read(buf)) > 0) {
				System.out.println("Decrypting ack message: " + count);
				byte[] toBdecrypt = new byte[count];
				System.arraycopy(buf, 0, toBdecrypt, 0, count);
				byte[] decrypted = this.decrypt(toBdecrypt);
				info = new String(decrypted, 0, decrypted.length);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setSeed(String s) {
		seed = s;
	}

	private String getSeed() {
		return seed;
	}

	private void setEncryption() {
		encrypter = new SecureFile(this.inFile, this.seed);

	}

	private byte[] getFileInBytes() {
		byte[] returnFile = new byte[1];
		try {
			FileInputStream file_in = new FileInputStream(this.inFile);
			returnFile = new byte[file_in.available()];
			file_in.read(returnFile);
			file_in.close();
		} catch (FileNotFoundException e) {
			System.out.println("Cant find the input file:");
		} catch (IOException e) {
			System.out.println("IO Problem in getFileInBytes");
		}
		return returnFile;
	}

	private byte[] decrypt(byte[] toDecrypt) {
		System.out.println("decrypting");
		SecretKeySpec key = CryptoUtilities.key_from_seed(this.seed.getBytes());
		byte[] decrypted = CryptoUtilities.decrypt(toDecrypt, key);
		// byte[] messageHash = CryptoUtilities.extract_message(decrypted);
		boolean isValid = CryptoUtilities.verify_hash(decrypted, key);
		if (isValid) {
			return CryptoUtilities.extract_message(decrypted);
		} else
			return new byte[1];
	}
}