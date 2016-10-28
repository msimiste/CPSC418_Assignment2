import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

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
	private String fileDelim = "#~files~#";
	private String outFileDelim = "#$#"; 
	private int fileLength;
	private DecryptFile decrypter;

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
		decrypter = new DecryptFile(this.seed);
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
		OutputStream out = null;
		InputStream inStream = null;

		String s = "";
		/* Try to read from the socket */
		try {

			inStream = sock.getInputStream();
			out = sock.getOutputStream();

			// read the inputstream ie, the get request into a byte array
			byte[] buf = new byte[2048];
			byte[] test = new byte[32];
			byte[] fileBytes;
			int count, off = 0;
			String info = "";

			while ((count = inStream.read(test)) > 0) {
				byte[] decryptedBytes = decrypter.decryptAES(buf);
				//info = new String(buf, 0, count);
				info = new String(decryptedBytes,0,count);
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
				}
				
				else if (info.contains(fileDelim)){
					off = info.indexOf(fileDelim);
					String dest = new String(buf, 0, off);
					off = off + fileDelim.length();
					fileBytes = Arrays.copyOfRange(buf, off, count-20);
					incoming = new String(fileBytes,0,fileBytes.length);
				} else {
					incoming = info;
				}

				// Otherwise, just echo what was recieved.
				System.out.println("Client " + idnum + ": " + incoming);
				
				byte[] temp = ("Spitting back " + incoming).getBytes();
				out.write(temp);
				out.flush();				
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
			return;
		}
	}

	private void setDestFile(String s) {
		this.destFile = s;
	}

	private void setSeed(String k) {
		this.seed = k;
	}
}