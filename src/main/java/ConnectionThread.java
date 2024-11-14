import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Set;

public class ConnectionThread implements Runnable {

    private Set<Socket> clientsConnections;
    private int port;

    public ConnectionThread(Set<Socket> clientsConnections, int port) {
        this.clientsConnections = clientsConnections;
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is active...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // InputStream inputStream = clientSocket.getInputStream();
                //BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                for (Socket clientConnection : clientsConnections) {
                    PrintWriter output = new PrintWriter(clientConnection.getOutputStream(), true);
                    output.println("New member has joined the chat");
                }
                System.out.println("New connection established");
                clientsConnections.add(clientSocket);
            }
        } catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
