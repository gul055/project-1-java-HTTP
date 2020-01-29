import java.io.*;
import java.net.*;
import java.util.*;

public class Worker implements Runnable {

    // socket
    private Socket clientSocket = null;
    // io streams
    BufferedReader clientOutput = null;
    BufferedOutputStream clientFileOutput = null;
    PrintWriter clientWriter = null;
    // doc root
    private String doc_root = null;
    // strings
    private static final String CRLF_STRING = "\r\n";
    // mime map
    protected HashMap<String, String> mimeMap = null;

    public Worker(Socket clientSocket, String doc_root, HashMap<String, String> mimeMap) {
        this.clientSocket = clientSocket;
        this.doc_root = doc_root;
        this.mimeMap = mimeMap;

        // set timeout
        try {
            this.clientSocket.setSoTimeout(5000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            // create io steams request handler
            this.clientOutput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.clientFileOutput = new BufferedOutputStream(this.clientSocket.getOutputStream());
            this.clientWriter = new PrintWriter(this.clientSocket.getOutputStream());
            RequestHandler r = new RequestHandler(doc_root, mimeMap);

            // a line from the host
            String line = null;
            // a complete request from the host
            String message = null;

            while(true) {
                // keep reading lines
                if((line = clientOutput.readLine()) != null) {
                    // end of a complete request, build and handle the request
                    if(line.equals("")) {
                        message += CRLF_STRING;
                        message = message.substring(4);

                        boolean stop = r.handleRequest(message);
                        r.generateResponse(this.clientFileOutput, this.clientWriter);

                        // if received 400 error code, close the connection;
                        // if 200 or 404, keep looping
                        if(stop) break;

                        // reset message
                        message = null;
                    } else {
                        message += line;
                        message += CRLF_STRING;
                    }
                }
            }

            // close all streams & socket, connection closed
            clientOutput.close();
            clientFileOutput.close();
            clientWriter.close();
            clientSocket.close();

        } catch (IOException e) {
            //report exception somewhere.
            e.printStackTrace();
        }
    }
}