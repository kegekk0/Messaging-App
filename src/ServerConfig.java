import java.io.*;
import java.util.*;

public class ServerConfig {
    private int port;
    private String name;
    private List<String> bannedPhrases;

    public ServerConfig(String configFile) throws IOException {
        bannedPhrases = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("port=")) port = Integer.parseInt(line.split("=")[1]);
                else if (line.startsWith("name=")) name = line.split("=")[1];
                else if (line.startsWith("banned_phrases="))
                    bannedPhrases = Arrays.asList(line.split("=")[1].split(","));
            }
        }
    }

    public int getPort() { return port; }
    public String getName() { return name; }
    public List<String> getBannedPhrases() { return bannedPhrases; }
}
