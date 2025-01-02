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
 * Message class to represent a single message sent in a target
 */
public class Message {

    // Data and metadata about the message
    private String sender;
    private String target;
    private String messageContent;
    private LocalDateTime serverTime;
    private LocalDateTime clientTime;
    private String serverString;

    /**
     * Constructor for a new Message
     * @param sender the sender of the message
     * @param target the target of the message
     * @param messageContent the content of the message
     * @param serverTime the time the message was sent according to server
     * @param serverString the server the message was sent from
     */
    public Message(String sender, String target, String messageContent, String serverTime, String serverString) {
        // Set the data and metadata 
        this.sender = sender;
        this.target = target;
        this.messageContent = messageContent;
        this.serverString = serverString;
        this.serverTime = LocalDateTime.parse(serverTime);
        this.clientTime = LocalDateTime.now();
    }

    // Format date and time to be user friendly
    DateTimeFormatter userFriendlyFormat = DateTimeFormatter.ofPattern("E dd-MM-yyyy HH:mm:ss");

    /**
     * Get a user friendly string representation of the message
     * @return a string representation of the message
     */
    public String toString() {
        // Format the client time to be user friendly 
        String userFriendlyClientTime = clientTime.format(userFriendlyFormat);
        
        // Return the message in the format: [clientTime] sender: messageContent
        return "[" + userFriendlyClientTime + "] " + sender + ": " + messageContent;
    }

    /**
     * Getter for serverTime
     * @return the serverTime of the message
     */
    public LocalDateTime getServerTime() {
            return this.serverTime;
    }

    /**
     * Getter for target that message was sent in
     * @return the target of the message
     */
    public String getTarget() {
    	return target;
    }

    /**
     * Getter for server that message was sent in
     * @return the server of the message
     */
    public String getServer() {
        return serverString;
    }

    /**
     * Getter for sender of the message
     * @return the sender of the message
     */
    public String getSender() {
        return sender;
    }

    /**
     * Getter for the message content
     * @return the message content
     */
    public String getMessage() {
        return messageContent;
    }
}

