import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.NoRouteToHostException;
import java.net.ConnectException;
import java.net.BindException;
import java.net.SocketException;
import java.io.EOFException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Channel: specific type of Target that represents a channel in the server
 */

public class Channel extends Target {
    // List of users in the channel
    private List<String> users;
    
    /**
     * Constructor for Channel
     * @param model ServerModel object
     * @param name name of the channel
     * @param users list of users in the channel
     */
    public Channel(ServerModel model, String name, String[] users) {
        // Use target constructor as normal  
        super(model, name);
        // Add users to the channel
        overwriteUsers(users);
    }

    /**
     * Getter for users
     * @return list of users in the channel
     */
    public List<String> getUsers() {
            return users;
    }

    /**
     * Adder for users
     * @param username username to add to the channel
     */
    public void addUser(String username) {
            users.add(username);
    }

    /**
     * Remover for users
     * @param username username to remove from the channel
     */
    public void removeUser(String username) {
            users.remove(username);
    }

    /**
     * Method for the client to join the channel     
     */
    public void joinChannel() {
        // Get the serverModel to join a channel with the name of this object's channel
        model.joinChannel(name);
    }
    
    /**
     * Method for the client to leave (part) the channel
     */
    public void partChannel() {
        model.partChannel(name);
    }

    /**
     * Overwrite the users in the channel
     * @param users list of users to overwrite the current users
     */
    public void overwriteUsers(String[] users) {
            this.users = new ArrayList<>(Arrays.asList(users));
    }

    /**
     * Override Target's isChannel method
     * @return true
     */
    @Override
    public boolean isChannel() {
        return true;
    }
}
                                      
