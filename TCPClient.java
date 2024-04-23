import java.io.*;
import java.net.*;
import java.util.Random;

public class TCPClient {
    public static void main(String[] args) throws Exception {
        // Code to establish connection
        try (Socket socket = new Socket("localhost", 6789);
                BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
                DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Input from the user for identification
            System.out.print("Enter your name: ");
            String name = inFromUser.readLine();

            // Joining the server
            outToServer.writeBytes("JOIN:" + name + '\n');
            System.out.println("Server response: " + inFromServer.readLine());

            // Sending 3 math calculation requests
            for (int i = 0; i < 3; i++) {
                String calculation = generateRandomCalculation();
                System.out.println("Sending calculation: " + calculation);
                outToServer.writeBytes("CALC:" + calculation + '\n');
                System.out.println("Server response: " + inFromServer.readLine());
            }

            // Terminating the connection
            outToServer.writeBytes("QUIT\n");
            System.out.println("Server response: " + inFromServer.readLine());
        }
    }

    // Creating a random expression generator
    private static String generateRandomCalculation() {
        Random random = new Random();
        int operand1 = random.nextInt(20) + 1; // 1 to 20
        int operand2 = random.nextInt(20) + 1; // 1 to 20
        char[] operations = { '+', '-', '*', '/' };
        char operation = operations[random.nextInt(operations.length)];

        return operand1 + "" + operation + "" + operand2;
    }
}
