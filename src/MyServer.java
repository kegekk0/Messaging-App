import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class MyServer {

    private final int port = 1701;
    private final String name = "ChatServer";
    private final List<String> bannedPhrases = Arrays.asList("spam", "curse", "ban");

    private final ConcurrentHashMap<String, Socket> clients = new ConcurrentHashMap<>();

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(name + " started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        System.out.println("New client connected: " + clientSocket);

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Ask for username
            out.println("Enter your username:");
            String username = in.readLine();

            if (username == null || clients.containsKey(username)) {
                out.println("Invalid username or already taken. Disconnecting...");
                clientSocket.close(); // Close only on invalid input
                return;
            }

            // Register the client
            clients.put(username, clientSocket);
            broadcastMessage("Server", username + " has joined the chat.");
            System.out.println(username + " connected.");

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(username + " says: " + message);

                if (bannedPhrases.stream().anyMatch(message::contains)) {
                    out.println("Message contains a banned phrase and was not sent.");
                } else {
                    broadcastMessage(username, message);
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            removeClient(clientSocket);
        }
    }




    private void broadcastMessage(String sender, String message) {
        for (String username : clients.keySet()) {
            if (!username.equals(sender)) {
                try (PrintWriter out = new PrintWriter(clients.get(username).getOutputStream(), true)) {
                    out.println(sender + ": " + message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void removeClient(Socket clientSocket) {
        String disconnectedUser = null;
        for (Map.Entry<String, Socket> entry : clients.entrySet()) {
            if (entry.getValue().equals(clientSocket)) {
                disconnectedUser = entry.getKey();
                clients.remove(entry.getKey());
                break;
            }
        }
        if (disconnectedUser != null) {
            System.out.println(disconnectedUser + " disconnected.");
            broadcastMessage("Server", disconnectedUser + " has left the chat.");
        }
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        new MyServer().start();
    }
}
