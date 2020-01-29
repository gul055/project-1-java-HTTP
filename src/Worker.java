import java.io.*;
import java.net.*;

public class Worker implements Runnable{

    // socket
    private Socket clientSocket = null;
    // io streams
    private DataInputStream clientInput = null;
	private DataOutputStream clientOutput = null;

    public Worker(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            clientInput = new DataInputStream(clientSocket.getInputStream());
			clientOutput = new DataOutputStream(clientSocket.getOutputStream());
            // For dynamic size of input from client
            ByteArrayOutputStream dynamicBuffer = new ByteArrayOutputStream();

            int readSize = 0;
            byte[] readBuffer = new byte[1024];
            String message;

            // Question 1: How do you tell if it's the end of message or network delay? Trick here :)
            // Question 2: How do you tell if it's the end of message or the client close the connection?

            // Read the data from client until EOF is reached
            while((readSize = clientInput.read(readBuffer, 0, 1024)) != -1) {
                dynamicBuffer.write(readBuffer, 0, readSize);
            }

            // write everything read into a message string
            message = dynamicBuffer.toString();

            // split the message string into single requests
            String[] requests = message.split("\r\n\r\n");

            // handle each request with a request handler
            for(int i = 0; i < requests.length; i++) {
                new RequestHandler(clientSocket, message);
            }


            //     try {
            //     // Let's see what got put into the buffer
            //     System.out.println("Round: " + iterationCount + ", Size of data: " + readSize);
            //     System.out.println("[Temp Buf]:" + new String(readBuffer, 0, readSize));

            //     // You can use this dynamic buffer to store undeterministic yet long data from client
            //     dynamicBuffer.write(readBuffer, 0, readSize);
            //     dynamicBuffer.toByteArray();
            //     System.out.println("[Dynamic Buf]: " + dynamicBuffer.toString());

            //     // Echo back to client
            //     clientOutput.write(readBuffer, 0, readSize);

            //     } catch (Exception e) {
            //         e.printStackTrace();
            //     }

            //     iterationCount += 1;
            // }

            clientInput.close();
            clientOutput.close();
            clientSocket.close();
            // serverSocket.close();

        } catch (IOException e) {
            //report exception somewhere.
            e.printStackTrace();
        }
    }
}