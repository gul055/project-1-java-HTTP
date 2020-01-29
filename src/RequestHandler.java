import java.io.*;
import java.net.*;

public class RequestHandler{

    // socket
    private Socket clientSocket = null;
    private String message = null;

    public RequestHandler(Socket clientSocket, String message) {
        this.clientSocket = clientSocket;
        this.message = message;
        int errorCode = handleRequest();
        generateResponse(errorCode);
    }

    public int handleRequest() {
        // split the message string into lines
        String[] requestLines = message.split("\r\n");

        // Parse the initial line
        
        

        // Parse possible headers

        // Respond
    }

    public void generateResponse(int errorCode) {

    }

}