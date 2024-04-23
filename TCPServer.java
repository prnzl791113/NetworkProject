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
                    String equation = line.substring(5);
                    String result = handleCalculation(equation);
                    outToClient.writeBytes("RESULT:" + result + "\n");
                    logActivity(equation, "calculation requested");
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

    private void logActivity(String message, String activity) {
        System.out.println("Activity: " + message + ", " + activity + " at " + LocalDateTime.now());
    }

    private String handleCalculation(String equation) {
        try {
            for (int i = 0; i < equation.length(); i++) {
                if (!Character.isDigit(equation.charAt(i))) {
                    int num1 = Integer.parseInt(equation.substring(0, i));
                    int num2 = Integer.parseInt(equation.substring(i + 1));
                    char op = equation.charAt(i);
                    switch (op) {
                        case '+': return String.valueOf(num1 + num2);
                        case '-': return String.valueOf(num1 - num2);
                        case '*': return String.valueOf(num1 * num2);
                        case '/': return num2 == 0 ? "Error: Division by zero" : String.valueOf(num1 / num2);
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
