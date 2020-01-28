import org.ini4j.*;

// Logging related. Print statement might not be thread-safe.
import java.util.logging.Level; 
import java.util.logging.Logger;
import java.net.*;
import java.io.*;
import java.util.logging.*; 

public class HttpdServer {

	// Configuration error exit code
	final static int EX_CONFIG = 78;

	// You are free to change the fields if you want
	protected Wini server_config;
	protected int port;
	protected String doc_root;
	
	private ServerSocket serverSocket = null;
	private Socket clientSocket = null;

	private DataInputStream clientInput = null;
    private DataOutputStream clientOutput = null;

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
		
		try {
            // Establish the socket and listen to the port
			serverSocket = new ServerSocket(port);
            System.out.println("Server starts on port:" + port);
        } catch (SocketException socketException) {
            System.err.println("Received Socket Exception" + socketException);
        } catch (Exception e) {
            System.err.println(e);
        }
	}

	public void launch() {
		LOGGER.log(Level.INFO, "Launching Web Server");
		LOGGER.log(Level.INFO, "Port: " + port);
		LOGGER.log(Level.INFO, "doc_root: " + doc_root);

		// Put code here that actually launches your webserver...

		while(true) {
			
			try {
				clientSocket = serverSocket.accept();
				System.out.println("Accepted a client");
				

				clientInput = new DataInputStream(clientSocket.getInputStream());
				clientOutput = new DataOutputStream(clientSocket.getOutputStream());
				

				ByteArrayOutputStream dynamicBuffer = new ByteArrayOutputStream();

				int readSize = 0;
				byte[] readBuffer = new byte[1024];

				int iterationCount = 0;
				

				while((readSize = clientInput.read(readBuffer, 0, 1024)) != -1) {

					try {
						// Let's see what got put into the buffer
						System.out.println("Round: " + iterationCount + ", Size of data: " + readSize);
						System.out.println("[Temp Buf]:" + new String(readBuffer, 0, readSize));

						// Echo back to client
						clientOutput.write(readBuffer, 0, readSize);

					} catch (Exception e) {
						e.printStackTrace();
					}

					iterationCount += 1;
				}
				

				clientInput.close();
				clientOutput.close();
				clientSocket.close();
				serverSocket.close();

			} catch (SocketException socketException) {
				System.err.println("Received Socket Exception" + socketException);
			} catch (Exception e) {
				System.err.println(e);
			}
			
					
		}
		
		
		
	}

}