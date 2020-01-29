import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.text.*;

public class RequestHandler{

    // socket
    private String message = null;
    private String doc_root = null;
    private String url = null;
    // file content
    private FileInputStream in = null;
    // check if closed or not
    private boolean closed = false;
    // error code
    private int errorCode = 0;
    // response string
    protected String response = null;

    // Final strings
    private static final String HTTPVERSION_STRING = "HTTP/1.1 ";
    private static final String SERVER_NAME = "My server 1.0";
    private static final String CRLF_STRING = "\r\n";

    protected HashMap<String, String> mimeMap = null;

    public RequestHandler(String doc_root, HashMap<String, String> mimeMap) {
        this.doc_root = doc_root;
        this.mimeMap = mimeMap;
    }

    public boolean handleRequest(String message) {
        this.message = message;
        //reset error code
        errorCode = 0;

        // split the message string into lines
        String[] requestLines = this.message.split("\r\n");

        // Parse the initial line
        String initialLine = requestLines[0];

        // Check the boiler plate (GET & HTTP)
        String front = initialLine.substring(0, 4);
        String end = initialLine.substring((initialLine.length() - 9), initialLine.length());
        if(!front.equals("GET ") || !end.equals(" HTTP/1.1")) {
            this.errorCode = 400;
            return true;
        }

        // get the url (middle part)
        url = initialLine.substring(4, (initialLine.length() - 9));

        // handle special case
        if(url.equals("/")) url = "/index.html";

        // check if the url is valid - not valid if the starting character is not "/"
        if(!url.substring(0, 1).equals("/")) {
            this.errorCode = 400;
            return true;
        }

        // convert the url to abosolute path
        this.url = this.doc_root + this.url;
        Path path = Paths.get(this.url);
        path = path.toAbsolutePath().normalize();

        // Check escape - resulting absolute path does not contain our relative root
        if(!(path.toString()).contains(this.doc_root)) this.errorCode = 404;
        // Check file not found
        File f = new File(path.toString());
        if(!f.exists()) this.errorCode = 404;

        // Parse possible headers
        boolean host = false;

        for(int i = 1; i < requestLines.length; i++) {
            // Check format
            if(!requestLines[i].contains(": ")) {
                this.errorCode = 400;
                return true;
            }

            // get the key value pair
            String[] pair = requestLines[i].split(": ", 2);

            // check for required key value pair
            if(pair[0].equals("Host")) host = true;
            // check for closed or not
            if(pair[0].equals("Connection") && pair[1].equals("close")) this.closed = true;
        }

        // if no required Host key exist
        if(!host) {
            this.errorCode = 400;
            return true;
        }

        // Respond
        if(this.errorCode != 404) this.errorCode = 200;

        // see if we close the connection or not
        if(this.closed) {
            return true;
        } else {
            return false;
        }
    }

    public void generateResponse(BufferedOutputStream clientOutput, PrintWriter clientWriter) {
        response = null;
        if (errorCode == 400) {
            response = HTTPVERSION_STRING + "400 Bad Request" + CRLF_STRING
                    + "Server: " + SERVER_NAME + CRLF_STRING;
            if (closed) {
                response += "Connection: close" + CRLF_STRING;
            }

            response += CRLF_STRING;
            clientWriter.write(response);
            clientWriter.flush();
        }
        else if (errorCode == 404){
            response = HTTPVERSION_STRING + "404 Not Found" + CRLF_STRING
                    + "Server: " + SERVER_NAME + CRLF_STRING;
            if (closed) {
                response += "Connection: close" + CRLF_STRING;
            }

            response += CRLF_STRING;
            clientWriter.write(response);
            clientWriter.flush();
        }
        else if (errorCode == 200){
            File file = new File(url);

            response = HTTPVERSION_STRING + "200 OK" + CRLF_STRING
                    + "Server: " + SERVER_NAME + CRLF_STRING
                    + "Last-Modified: " + new SimpleDateFormat("EEE, dd MMM yy HH:mm:ss Z").format(new Date(file.lastModified())) + CRLF_STRING;

            String contentType = "application/octet-stream";
            if (url.indexOf(".") != -1) {
                String cType = url.substring(url.indexOf("."));
                if(mimeMap.containsKey(cType)) contentType = mimeMap.get(cType);
            }

            response += "Content-Type: " + contentType + CRLF_STRING
                    + "Content-Length: " + file.length() + CRLF_STRING; //get the length of file
            if (closed) {
                response += "Connection: close" + CRLF_STRING;
            }

            response += CRLF_STRING;
            clientWriter.write(response);
            clientWriter.flush();

            try {
                in = new FileInputStream(file);
                int readSize = 0;
                byte[] readBuffer = new byte[1024];
                while((readSize = in.read(readBuffer, 0, 1024)) != -1) {
                    clientOutput.write(readBuffer, 0, readSize);
                }
                clientOutput.flush();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}