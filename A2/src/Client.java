import java.io.*;
import java.net.*;

/**
 * Client program.  Connects to the server and sends text accross.
 */

public class Client 
{
    private Socket sock;  //Socket to communicate with.
	
    /**
     * Main method, starts the client.
     * @param args args[0] needs to be a hostname, args[1] a port number.
     */
    public static void main (String [] args)
    {
	if (args.length != 2) {
	    System.out.println ("Usage: java Client hostname port#");
	    System.out.println ("hostname is a string identifying your server");
	    System.out.println ("port is a positive integer identifying the port to connect to the server");
	    return;
	}

	try {
	    Client c = new Client (args[0], Integer.parseInt(args[1]));
	}
	catch (NumberFormatException e) {
	    System.out.println ("Usage: java Client hostname port#");
	    System.out.println ("Second argument was not a port number");
	    return;
	}
    }
	
    /**
     * Constructor, in this case does everything.
     * @param ipaddress The hostname to connect to.
     * @param port The port to connect to.
     */
    public Client (String ipaddress, int port)
    {
	/* Allows us to get input from the keyboard. */
	BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
	String userinput;
	PrintWriter out;
	InputStream in = null;
		
	/* Try to connect to the specified host on the specified port. */
	try {
	    sock = new Socket (InetAddress.getByName(ipaddress), port);
	}
	catch (UnknownHostException e) {
	    System.out.println ("Usage: java Client hostname port#");
	    System.out.println ("First argument is not a valid hostname");
	    return;
	}
	catch (IOException e) {
	    System.out.println ("Could not connect to " + ipaddress + ".");
	    return;
	}
		
	/* Status info */
	System.out.println ("Connected to " + sock.getInetAddress().getHostAddress() + " on port " + port);
	
		
	try {
	    out = new PrintWriter(sock.getOutputStream());
	    in = sock.getInputStream();
	}
	catch (IOException e) {
	    System.out.println ("Could not create output stream.");
	    return;
	}
		
	/* Wait for the user to type stuff. */
	try {
	    while ((userinput = stdIn.readLine()) != null) {
		/* Echo it to the screen. */
		//out.println(userinput);
		//userinput = stdIn.readLine();
		out.println(userinput);
		out.flush();
	
		/* Tricky bit.  Since Java does short circuiting of logical 
		 * expressions, we need to checkerror to be first so it is always 
		 * executes.  Check error flushes the outputstream, which we need
		 * to do every time after the user types something, otherwise, 
		 * Java will wait for the send buffer to fill up before actually 
		 * sending anything.  See PrintWriter.flush().  If checkerror
		 * has reported an error, that means the last packet was not 
		 * delivered and the server has disconnected, probably because 
		 * another client has told it to shutdown.  Then we check to see
		 * if the user has exitted or asked the server to shutdown.  In 
		 * any of these cases we close our streams and exit.
		 */
		if ((out.checkError()) || (userinput.compareTo("exit") == 0) || (userinput.compareTo("die") == 0)) {
		    System.out.println ("Client exiting.");
		    stdIn.close ();
		    out.close ();
		    sock.close();
		    return;
		}
		
		String s = "";

		// read the inputstream ie, the get request into a byte array
		byte[] buf = new byte[1024];
		int count = in.read(buf);

		// extract the entire request into a string
		if (count > 0) {
			s += new String(buf, 0, count);
		}
		System.out.println(s);
	   }
	    
		
		
	} catch (IOException e) {
	    System.out.println ("Could not read from input.");
	    return;
	}		
    }
}