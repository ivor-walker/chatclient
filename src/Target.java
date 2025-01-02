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
import java.util.Comparator;

/**
 * Target class represents a target for messages
 * It can be a user or a channel
 */
public class Target {
    // Name of target 
    protected String name;
    // Message history of target 
    protected List<Message> messages = new ArrayList<>();
    // Server model target exists in (i.e server that a user is connected t or server that a channel is on)
    protected ServerModel model;

    /**
     * Constructor for Target
     * @param model ServerModel that target exists in
     * @param name Name of target
     */
    public Target(ServerModel model, String name) {
            this.name = name;
            this.model = model;
    }

    /**
     * Getter for message history
     * @return List of messages
     */
    public List<Message> getMessages() {
        // Sort messages by server time before returning
        sortMessages();

        return messages;
    }

    /**
     * Get time of most recent message in target
     * @return LocalDateTime of most recent message
     */
    public LocalDateTime getServerTimeOfLastMessage() {
        // Sort messages by server time 
        sortMessages();
        // Return time of most recent message
        return messages.get(messages.size() - 1).getServerTime();
    }

    /**
     * Sort messages by server time in place 
     */
    public void sortMessages() {
        messages.sort(
                Comparator.comparing(Message::getServerTime)
        );
    }

    /**
     * Send message to target
     * @param target Name of target
     * @param messageContent Content of message
     */
    public void sendMessage(String target, String messageContent) {
        // Send message via server model     
        model.sendMessage(target, messageContent);
    }

    /**
     * Add message to target's message history
     * @param message Message to add
     */
    public void addMessage(Message message) {
            messages.add(message);
    }

    /**
     * Get name of target
     * @return Name of target
     */
    public String getName() {
            return name;
    }

    /**
     * Get server model target exists in 
     * @return ServerModel target exists in
     */
    public String getServer() {
        return model.toString();
    }

    /**
     * Check if target is a channel
     * @return False
     * @see Channel
     */
    public boolean isChannel() {
    	return false;
    }
}

