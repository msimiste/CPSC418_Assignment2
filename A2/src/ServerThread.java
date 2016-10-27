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
		//BufferedReader in = null;
		String incoming = null;
		OutputStream out = null;
		InputStream inStream = null;

		/*
		 * try { //out = sock.getOutputStream();
		 * out.write("Please provide a keyString: ".getBytes()); out.flush();
		 * //in = new BufferedReader (new InputStreamReader
		 * (sock.getInputStream())); //inStream = sock.getInputStream();
		 * 
		 * setSeed(in.readLine()); } catch (UnknownHostException e) {
		 * System.out.println ("Unknown host error."); return; } catch
		 * (IOException e) { System.out.println
		 * ("Could not establish communication."); return; }
		 */

		String s = "";
		/* Try to read from the socket */
		try {
			// incoming = in.readLinestr ();
			inStream = sock.getInputStream();
			out = sock.getOutputStream();
			System.out.println("Do we ever get here " + seed);

			// read the inputstream ie, the get request into a byte array
			byte[] buf = new byte[2048];
			//int count = inStream.read(buf);

		/*	System.out.println("What about here  " + count);
			// extract the entire request into a string
			if (count > 0) {
				s += new String(buf, 0, count);
				incoming = s;
			}*/
			
			
			// incoming = in.readLine();
			// System.out.println("incoming :" +incoming);
			//byte[] buf = new byte[2048];
			byte[] fileBytes;
			int count, off = 0;
			String info = "";
			/*while ((count = inStream.read(buf)) <= 0) {
				System.out.println("Do we ever get here as well " + seed);
				inStream = sock.getInputStream();
			}*/
			while ((count = inStream.read(buf)) > 0) {
				info = new String(buf, 0, count);
				System.out.println("How about here ?" + info);

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
				 * If the client has sent "die", instruct the server to signal all
				 * threads to shutdown, then exit.*/
				 
				else if (info.compareTo("die") == 0) {
					parent.killall();
					return;
				}
				
				else if (info.contains("#~filestart#~")) {
					off = info.indexOf("#~filestart~#");
					String dest = new String(buf, 0, off);
					off = off + 13;
					fileBytes = Arrays.copyOfRange(buf, off, count);
				} else {
					incoming = info;
				}

				// Otherwise, just echo what was recieved. 
				System.out.println("Client " + idnum + ": " + incoming);
				try {
					byte[] temp = ("Spitting back " + incoming).getBytes();
					out.write(temp);
					out.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				

				
			}
			
			System.out.println("And this?" + incoming);

			// Scanner input = new Scanner(System.in);
			// setSeed(incoming);
		} catch (IOException e) {
			if (parent.getFlag()) {
				System.out.println("shutting down.");
				return;
			}
			return;
		}

		
	 //See if we've recieved something 
		/*while (incoming != null) {

			System.out.println("How about now ?");
			
			// * If the client has sent "exit", instruct the server to remove this
			// * thread from the vector of active connections. Then close the
			 //* socket and exit.
			 
			if (incoming.compareTo("exit") == 0) {
				parent.kill(this);
				try {
					inStream.close();
					sock.close();
				} catch (IOException e) {// nothing to do 
				}
				return;
			}

			
			 * If the client has sent "die", instruct the server to signal all
			 * threads to shutdown, then exit.
			 
			else if (incoming.compareTo("die") == 0) {
				parent.killall();
				return;
			}

			// Otherwise, just echo what was recieved. 
			System.out.println("Client " + idnum + ": " + incoming);
			try {
				byte[] temp = ("Spitting back " + incoming).getBytes();
				out.write(temp);
				out.flush();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			
			 * Try to get the next line. If an IOException occurs it is probably
			 * because another client told the server to shutdown, the server
			 * has closed this thread's socket and is signalling for the thread
			 * to shutdown using the shutdown flag.
			 
			try {
				
				byte[] buf = new byte[1024];
				//inStream = sock.getInputStream();
				int count = inStream.read(buf);
				System.out.println("What about line 191 " + count);
				// extract the entire request into a string
				if (count > 0) {
					s += new String(buf, 0, count);
					incoming = s;
				}
			} catch (IOException e) {
				if (parent.getFlag()) {
					System.out.println("shutting down.");
					return;
				} else {
					System.out.println("IO Error.");
					return;
				}
			}
		}*/
	}

	private void setDestFile(String s) {
		this.destFile = s;
	}

	private void setSeed(String k) {
		this.seed = k;
	}
}