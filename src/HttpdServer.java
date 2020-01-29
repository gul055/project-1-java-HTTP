import org.ini4j.*;

// Logging related. Print statement might not be thread-safe.
import java.util.logging.Level; 
import java.util.logging.Logger; 
import java.util.logging.*; 
import java.net.*;

public class HttpdServer {

	// Configuration error exit code
	final static int EX_CONFIG = 78;

	// You are free to change the fields if you want
	protected Wini server_config;
	protected int port;
	protected String doc_root;

	// client sockets
	private Socket clientSocket = null;
	// server sockets
    private ServerSocket serverSocket = null;

	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	public HttpdServer(Wini server_config) {
		this.server_config = server_config;

		this.port  = server_config.get("httpd", "port", int.class);
		if (this.port == 0) {
			LOGGER.log(Level.SEVERE, "Failed to read port number from config file");
			System.exit(EX_CONFIG);
		}

		this.doc_root = server_config.get("httpd", "doc_root", String.class);
		if (this.doc_root  == null) {
			LOGGER.log(Level.SEVERE, "Failed to read doc_root from config file");
			System.exit(EX_CONFIG);
		}
	}

	public void launch() {
		LOGGER.log(Level.INFO, "Launching Web Server");
		LOGGER.log(Level.INFO, "Port: " + port);
		LOGGER.log(Level.INFO, "doc_root: " + doc_root);

		// Put code here that actually launches your webserver...

        try {
            // Establish the socket and listen to the port
            serverSocket = new ServerSocket(port);
            System.out.println("Server starts on port:" + port);
        } catch (SocketException socketException) {
            System.err.println("Received Socket Exception" + socketException);
        } catch (Exception e) {
            System.err.println(e);
		}
		
		try {
			// keep listening to clients
			while (true) {
				// Accept a client/new connection
				clientSocket = serverSocket.accept();
				System.out.println("Accepted a client");

				// Start a new thread to handle this connection
				new Thread(new Worker(clientSocket)).start();
			}

		} catch (SocketException socketException) {
			System.err.println("Received Socket Exception" + socketException);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

}