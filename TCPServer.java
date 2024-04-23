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
            // Set up input and output streams for communication with the client
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            String line;
            // Read input from the client until null (client disconnects)
            while ((line = inFromClient.readLine()) != null) {
                // Handle different types of requests from the client
                if (line.startsWith("JOIN:")) {
                    String clientName = line.substring(5);
                    // Store client details in a ConcurrentHashMap
                    clientDetails.put(clientName, LocalDateTime.now().toString());
                    logActivity(clientName, "joined");
                    outToClient.writeBytes("ACK:" + clientName + "\n"); // Send acknowledgment to the client
                } else if (line.startsWith("CALC:")) {
                    String equation = line.substring(5);
                    // Perform calculation and send result back to the client
                    String result = handleCalculation(equation);
                    outToClient.writeBytes("RESULT:" + result + "\n");
                    logActivity(equation, "calculation requested");
                } else if (line.equals("QUIT")) {
                    // If client sends QUIT command, send BYE and break out of the loop
                    outToClient.writeBytes("BYE\n");
                    break;
                }
            }
            // Close the connection with the client
            connectionSocket.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Method to log client activity
    private void logActivity(String message, String activity) {
        System.out.println("Activity: " + message + ", " + activity + " at " + LocalDateTime.now());
    }

    // Method to handle calculation requests
    private String handleCalculation(String equation) {
        try {
            for (int i = 0; i < equation.length(); i++) {
                if (!Character.isDigit(equation.charAt(i))) {
                    int num1 = Integer.parseInt(equation.substring(0, i));
                    int num2 = Integer.parseInt(equation.substring(i + 1));
                    char op = equation.charAt(i);
                    // Perform the calculation based on the operator
                    switch (op) {
                        case '+':
                            return String.valueOf(num1 + num2);
                        case '-':
                            return String.valueOf(num1 - num2);
                        case '*':
                            return String.valueOf(num1 * num2);
                        case '/':
                            return num2 == 0 ? "Error: Division by zero" : String.valueOf(num1 / num2);
                    }
                    break;
                }
            }
        } catch (NumberFormatException e) {
            return "Error: Invalid number format.";
        }
        return "Error: Invalid operation";
    }
}

public class TCPServer {
    public static void main(String[] args) throws Exception {
        // Create a thread pool for handling multiple clients concurrently
        ExecutorService clientPool = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(6789)) {
            System.out.println("Server is running...");
            // Continuously accept client connections
            while (true) {
                Socket connectionSocket = serverSocket.accept();
                // Handle each client connection in a separate thread
                clientPool.execute(new ClientHandler(connectionSocket));
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
        }
    }
}
