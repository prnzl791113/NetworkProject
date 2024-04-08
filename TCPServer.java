import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ClientHandler implements Runnable {
    private Socket connectionSocket;
    private LocalDateTime connectionTime;
    private static final ConcurrentHashMap<String, String> clientDetails = new ConcurrentHashMap<>();

    public ClientHandler(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
        this.connectionTime = LocalDateTime.now();
    }

    @Override
    public void run() {
        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            String line;
            while ((line = inFromClient.readLine()) != null) {
                if (line.startsWith("JOIN:")) {
                    String clientName = line.substring(5);
                    clientDetails.put(clientName, LocalDateTime.now().toString());
                    logActivity(clientName, "joined");
                    outToClient.writeBytes("ACK:" + clientName + "\n");
                } else if (line.startsWith("CALC:")) {
                    String[] parts = line.split(":");
                    String result = handleCalculation(parts[1], Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
                    outToClient.writeBytes("RESULT:" + result + "\n");
                    logActivity(parts[1], "calculation requested");
                } else if (line.equals("QUIT")) {
                    outToClient.writeBytes("BYE\n");
                    break;
                }
            }
            connectionSocket.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void logActivity(String clientName, String activity) {
        // Log client activity in a format that includes timestamp
        System.out.println("Client " + clientName + " " + activity + " at " + LocalDateTime.now());
    }

    private String handleCalculation(String operation, double num1, double num2) {
        switch (operation) {
            case "ADD":
                return String.valueOf(num1 + num2);
            case "SUB":
                return String.valueOf(num1 - num2);
            case "MUL":
                return String.valueOf(num1 * num2);
            case "DIV":
                if (num2 == 0) return "Error: Division by zero";
                return String.valueOf(num1 / num2);
            default:
                return "Error: Invalid operation";
        }
    }
}

public class TCPServer {
    public static void main(String[] args) throws Exception {
        ExecutorService clientPool = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(6789)) {
            System.out.println("Server is running...");
            while (true) {
                Socket connectionSocket = serverSocket.accept();
                clientPool.execute(new ClientHandler(connectionSocket));
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
        }
    }
}
