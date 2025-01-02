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

import java.lang.StackWalker;
import java.lang.StackWalker.StackFrame;

/**
 * Class representing a connection to a single server
 */
public class ServerModel {
    // Socket connection to the server
    private Socket connection;
    // Reader for the server's messages
    private BufferedReader reader;
    // Writer for sending messages to the server
    private PrintWriter writer;

    // Host and port of the server, and the user's nickname in the server
    private String host;
    private int port;
    // Users must present a nickname before using server which cannot be changed after connecting   
    private String nickname;

    // List of listeners for target events (i.e targetController)
    private List<TargetListener> targetListeners = new ArrayList<>();

    /**
     * Add a listener for target events
     * @param listener the listener to add
     */
    public void addTargetListener(TargetListener listener) {
        targetListeners.add(listener);
    }

    // List of listeners for server events (i.e serverController)
    private List<ServerListener> serverListeners = new ArrayList<>();

    /**
     * Add a listener for server events
     * @param listener the listener to add
     */
    public void addServerListener(ServerListener listener) {
        serverListeners.add(listener);
    }

    /**
     * Handle any exceptions thrown by model
     * @param e the exception to handle
     */
    private void handleException(Exception e) {
        // Turn exception into a user-friendly error message 
        String errorMessage = getErrorMessage(e);

        // Notify all listeners of the error
        for (ServerListener listener : serverListeners) {
            listener.onError(errorMessage);
        }

        // Print the stack trace to the console
        e.printStackTrace();
        Thread.dumpStack();
    }

    /**
     * Exception handling and exceptional completion of a future
     * @param e the exception to handle
     * @param future the future to complete exceptionally
     */
    private void handleException(Exception e, CompletableFuture future) {
        handleException(e);
    
        future.completeExceptionally(e);
    }

    /**
     * Get a user-friendly error message from an exception
     * @param e the exception to get the message from
     * @return the user-friendly error message
     */
    private String getErrorMessage(Exception e) {
        String errorMessage;
   
        // Error messages for connection exceptions 
        if (e instanceof IllegalArgumentException) {
            errorMessage = "[400] Bad Request: Invalid port number. Port must be between 0 and 65535.";
        } else if (e instanceof NullPointerException) {
            errorMessage = "[400] Bad Request: Host cannot be null.";
        } else if (e instanceof UnknownHostException) {
            errorMessage = "[404] Not Found: Host not found. Check the spelling of the host or your network configuration.";
        } else if (e instanceof NoRouteToHostException) {
            errorMessage = "[403] Forbidden: No route to host. Check your firewall or network settings.";
        } else if (e instanceof ConnectException) {
            errorMessage = "[403] Forbidden: Connection refused by server. Verify the port or server configuration.";
        } else if (e instanceof SecurityException) {
            errorMessage = "[403] Forbidden: Permission denied. Check server security settings.";
        } else if (e instanceof BindException) {
            errorMessage = "[503] Service Unavailable: Port is already in use. Please try a different port.";
        } else if (e instanceof SocketException) {
            errorMessage = "[500] Internal Server Error: Connection issue with the server.";
        } else if (e instanceof EOFException) {
            errorMessage = "[500] Internal Server Error: End of stream encountered.";
        } else if (e instanceof IOException) {
            errorMessage = "Unknown I/O error occurred: " + e.getMessage();
        
        // If the server responds with an error message, print it
        } else if (e instanceof RuntimeException) {
            errorMessage = "Server responded with error message: " + e.getMessage();
        
        // If the exception is not recognized, print a generic error messag
        } else {
            errorMessage = "Unknown error: " + e.getMessage();
        }
    
        return errorMessage;
    }
    
   
    /**
     * Constructor for a server model 
     * @param host the host of the server
     * @param port the port of the server
     * @param nickname the user's nickname in the server
     */
    public ServerModel(String host, int port, String nickname) {
        this.host = host;
        this.port = port;
        this.nickname = nickname;
    
        // TODO shutdown hook added elsewhere, investigate removing 
        // Add a shutdown hook to disconnect from the server when the program exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Attempt disconnection 
            disconnect().thenRun(() -> {
                System.out.println("Disconnecting from " + toString());
            });
        }));
    }
    
    // Map of targets (users and channels) in the server 
    private HashMap<String, Target> targets = new HashMap<>();
    
    // Flag to keep listening for messages from the server 
    private boolean keepListening;
   
    // Future for connecting to the server 
    private CompletableFuture<Void> connectFuture;
   
    /**
     * Connect to the server 
     * @return a future that completes when the connection is established
     */
    public CompletableFuture<Void> connect() {
        // Initialise future for connecting to the server 
        connectFuture = new CompletableFuture<>();
    
        try {
            // Create a socket connection to the server 
            connection = new Socket(host, port);
    
            // Create a reader for the server's messages 
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    
            // Start listening for messages 
            keepListening = true;
            listenForMessages();

            // Create a writer for sending messages to the server 
            writer = new PrintWriter(connection.getOutputStream(), true);
    
            // Attempt to set nickname after connecting 
            sendNickname(nickname).thenRun(() -> {
                connectFuture.complete(null); 
            });
    
        } catch (Exception e) {
            handleException(e, connectFuture);
    
        } finally {
            return connectFuture;
        }
    }
    
    // Future for setting the user's nickname 
    private CompletableFuture<String> nicknameFuture;
   
    /**
     * Send the user's nickname to the server 
     * @param nickname the user's nickname
     * @return a future that completes when the nickname is set
     */
    private CompletableFuture<String> sendNickname(String nickname) {
        // Initialise future for setting the user's nickname 
        nicknameFuture = new CompletableFuture<>();
    
        try {
            // Send the nickname to the server 
            writer.println("NICK " + nickname);

        } catch (Exception e) {
            handleException(e, nicknameFuture);
        } finally {
            return nicknameFuture;
        }
    }
   
    /**
     * Handle the server's response to setting the user's nickname 
     * @param messageContent the server's response
     */
    private void onNickname(String messageContent) {
        // Extract the nickname from the server's response 
        String nickname = messageContent.split(", ")[1];
        this.nickname = nickname;
    
        // If future is not already completed
        if (nicknameFuture != null && !nicknameFuture.isDone()) {
            // TODO should this be the nickname or the message content?
            // Complete the future with the nickname
            nicknameFuture.complete(messageContent);
        }
    }
    
    /**
     * Getter for the user's nickname 
     * @return the user's nickname
     */
    public String getNickname() {
        return nickname;
    }
    
    /**
     * Listen for messages from the server 
     */ 
    private void listenForMessages() {
        // Start a new thread 
        new Thread(() -> {
            // While the flag is set to keep listening
            while (keepListening) {
                String message;
                try {
                    // While there are messages to read 
                    while ((message = reader.readLine()) != null) {
                        handleServerMessage(message); 
                    }

                } catch (Exception e) {
                    handleException(e);
                }
            }

            // Once no longer listening, close the connection  
            try {
                // Close the writer, reader, and connection
                writer.close(); 
                reader.close(); 
                connection.close(); 

            } catch (Exception e) {
                handleException(e, disconnectFuture);
            }
        }).start(); 
    }

    // Position of target if message is a notification from server
    private int TARGET_POSITION = 2;
    // Position of message if message is a notification of a PM from server
    private int MESSAGE_POSITION = 3;

    // TODO complete overhaul required
    /**
     * Handle a message from the server
     * @param serverMessage the message from the server
     */
    private void handleServerMessage(String serverMessage) {
        System.out.println("FROM | " + LocalDateTime.now().toString() + " | " + toString() + " | " + serverMessage);
        
        // If the message is a notification from the server
        if (serverMessage.startsWith(":")) {
            // Filter the message to remove the colon
            String filteredMessage = serverMessage.replaceFirst(":", "");
            String[] splitMessage = filteredMessage.split(" ");

            // Extract nickname and message code from the message
            String nickname = splitMessage[0];
            String messageCode = splitMessage[1];

            // If message has a target, extract it 
            String target = "";
            if (splitMessage.length > TARGET_POSITION) {
                target = splitMessage[TARGET_POSITION];
            }

            // If message has a PM message, extract it
            String message = "";
            if (splitMessage.length > MESSAGE_POSITION) {
                String[] splitMessageContent = Arrays.copyOfRange(splitMessage, MESSAGE_POSITION, splitMessage.length);
                message = String.join(" ", splitMessageContent);
                message = message.replaceFirst(":", ""); 
            }

            // Flag if the message refers to the client
            boolean refersToSelf = this.nickname.equals(nickname);

            // If message is self-referential, get client to perform the action (e.g quit should disconnect the client)
            if (refersToSelf) {
                switch (messageCode) {
                    case "QUIT":
                        selfQuit(); 
                        return;
                    case "JOIN":
                        onJoinChannel(target); 
                        return;
                    case "PART":
                        onPartChannel(target); 
                        return;
                    case "PRIVMSG":
                        onMessage(nickname, target, message); 
                        return;
                }

            // If message is not self-referential, tell client about change performed by user (e.g quit should tell serverModel that a different user has left) 
            } else {
                switch (messageCode) {
                    case "QUIT":
                        onQuit(nickname); 
                        return;
                    case "JOIN":
                        onJoinChannel(nickname, target); 
                        return;
                    case "PART":
                        onPartChannel(nickname, target); 
                        return;
                    case "PRIVMSG":
                        onMessage(nickname, target, message); 
                        return;
                }
            }
        }

        // If the message is a reply from the server
        
        // Regular expression to match message codes
        String regex = "[A-Z_]+";
        Matcher matcher = Pattern.compile(regex).matcher(serverMessage);

        // Get all message codes in the message (should only be one)
        List<String> messageCodes = new ArrayList<>();
        while (matcher.find()) {
            messageCodes.add(matcher.group());
        }

        // For each message code in the message
        for (String messageCode : messageCodes) {
            // Remove the message code and colon from the message
            String messageContent = serverMessage.replaceFirst(messageCode, "");
            messageContent = messageContent.replaceFirst(":", "").trim();

            // Handle the message based on the message code
            switch (messageCode) {
                case "REPLY_NAMES":
                    onNamesInChannel(messageContent); 
                    return;
                case "REPLY_NICK":
                    onNickname(messageContent); 
                    return;
                case "REPLY_LIST":
                    onOfferedChannels(messageContent); 
                    return;
                case "REPLY_TIME":
                    onTime(messageContent); 
                    return;
                case "REPLY_INFO":
                    onInfo(messageContent); 
                    return;
                case "PONG":
                    onPong(messageContent); 
                    return;
                case "ERROR":
                    onError(messageContent); 
                    return;
            }
        }
    }

    // Future for disconnecting from the server
    private CompletableFuture<Void> disconnectFuture;

    /**
     * Disconnect from the server
     * @return a future that completes when the connection is closed
     */
    public CompletableFuture<Void> disconnect() {
        // Initialise future for disconnecting from the server 
        disconnectFuture = new CompletableFuture<>();

        try {
            // Send a quit message to the server
            writer.println("QUIT");
            // Disconnect from the server
            selfQuit();

        } catch (Exception e) {
            handleException(e, disconnectFuture);
        } finally {
            return disconnectFuture;
        }
    }

    /**
     * Handle recieving disconnect message from server
     */
    private void selfQuit() {
        // Stop listening for messages, which will close reader, writer and connection
        keepListening = false;

        // Remove self from list of users in the server
        quitUpdateModel(this.nickname);

        // Complete the disconnect future
        if (disconnectFuture != null && !disconnectFuture.isDone()) {
            disconnectFuture.complete(null);
        }
    }

    /**
     * Handle a user quitting the server
     * @param nickname the nickname of the user who quit
     */ 
    private void onQuit(String nickname) {
        // For all listeners of target events
        for (TargetListener listener : targetListeners) {
            // Notify the listener that a user has quit
            listener.onQuit(nickname);
        }
        
        // Remove the user who quit from the model
        quitUpdateModel(nickname);
    }

    /**
     * Remove a user who has quit from the model
     * @param nickname the nickname of the user who quit
     */
    private void quitUpdateModel(String nickname) {
        // Remove the user who quit from the list of targets
        targets.remove(nickname);
        // TODO update all channels to remove the user who quit
    }

    // Future for joining a channel 
    private CompletableFuture<Channel> joinChannelFuture;

    /**
     * Join a channel in the server
     * @param channel the name of the channel to join
     * @return a future that completes when the channel is joined
     */
    public CompletableFuture<Channel> joinChannel(String channel) {
        // Initialise future for joining a channel
        joinChannelFuture = new CompletableFuture<>();

        try {
            // Send a join message to the server
            writer.println("JOIN " + channel);

        } catch (Exception e) {
            handleException(e, joinChannelFuture);
        }

        return joinChannelFuture;
    }


    /**
     * Handle the server's response to the client joining a channel
     * @param channelName the name of the channel that was joined
     */
    private void onJoinChannel(String channelName) {
        // Attempt to get all users in the new channel
        getNamesInChannel(channelName).thenAccept(users -> {
            // Create a new channel with the users in it
            Channel channel = new Channel(this, channelName, users);

            // Add the channel to the list of targets
            targets.put(channel.getName(), channel);
            
            // Complete the join channel future
            if (joinChannelFuture != null && !joinChannelFuture.isDone()) {
                joinChannelFuture.complete(channel);
            }
        });
    }

    /**
     * Handle a different user joining a channel
     * @param nickname the nickname of the user who joined
     * @param channelName the name of the channel that was joined
     */
    private void onJoinChannel(String nickname, String channelName) {
        // Get the channel that was joined
        Channel channel = (Channel) targets.get(channelName);
        
        // If the channel does not exist, return
        if (channel == null) {
            return;
        }

        // Add the user who joined to the channel
        channel.addUser(nickname);

        // Notify all target listeners of the user joining the channel 
        for (TargetListener listener : targetListeners) {
            listener.onJoinChannel(nickname, channel);
        }
    }

    // Future for leaving a channel
    private CompletableFuture<Void> partChannelFuture;
    
    /**
     * Part (leave) a channel in the server 
     * @param channel the name of the channel to leave
     * @return a future that completes when the channel is left
     */
    public CompletableFuture<Void> partChannel(String channel) {
        // Initialise future for leaving a channel 
        partChannelFuture = new CompletableFuture<>();
        try {
            // Send a part message to the server 
            writer.println("PART " + channel);

        } catch (Exception e) {
            handleException(e, partChannelFuture);
        } finally {
            return partChannelFuture;
        }
    }

    /**
     * Handle the server's response to the client leaving a channel
     * @param channelName the name of the channel that was left
     */
    private void onPartChannel(String channelName) {
        // Remove the channel from the list of targets
        targets.remove(channelName);

        // Complete the part channel future
        if (partChannelFuture != null && !partChannelFuture.isDone()) {
            partChannelFuture.complete(null);
        }
    }

    /**
     * Handle a different user leaving a channel
     * @param nickname the nickname of the user who left
     * @param channelName the name of the channel that was left
     */
    private void onPartChannel(String nickname, String channelName) {
        // Get the channel that was left
        Channel channel = (Channel) targets.get(channelName);

        // If the channel does not exist, return
        if (channel == null) {
            return;
        }

        // Remove the user who left from the channel
        channel.removeUser(nickname);

        // Notify all target listeners of the user leaving the channel
        for (TargetListener listener : targetListeners) {
            listener.onPartChannel(nickname, channel);
        }
    }

    /**
     * Send a message to a target in the server
     * @param target the name of the target to send the message to
     * @param messageContent the content of the message
     */
    public void sendMessage(String target, String messageContent) {
        try {
            // Send a message to the server
            writer.println("PRIVMSG " + target + " :" + messageContent);

            // If the target isn't a channel (i.e. a user), server will not send a message back
            // TODO replace with isChannel() check
            if (!target.startsWith("#")) {
                // Create a message object for the client without waiting for server response
                onMessage(nickname, target, messageContent);
            }

        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Handle client recieving a message from a target in the server 
     * @param sender the nickname of the user who sent the message
     * @param targetName the name of the target the message was sent to
     * @param messageContent the content of the message
     */
    private void onMessage(String sender, String targetName, String messageContent) {
        // Attempt to get server time
        getTimeFuture().thenAccept(serverTime -> {
            // Create a message object for the client
            Message message = new Message(sender, targetName, messageContent, serverTime, toString());

            // Get the target the message was sent to
            Target target = targets.get(message.getTarget());

            // If the target does not exist (e.g new user DMing the client), create it
            if (target == null) {
                target = new Target(this, message.getTarget());
                targets.put(target.getName(), target);
            }

            // Add message to target's message history
            target.addMessage(message);

            // Notify target listeners
            for (TargetListener listener : targetListeners) {
                listener.onMessageRecieved(message);
            }
        });
    }

    // Future for getting all users in a channel
    private CompletableFuture<String[]> namesFuture;

    /**
     * Get all users in a channel
     * @param channel the name of the channel to get the users of
     * @return a future that completes with the users in the channel
     */
    public CompletableFuture<String[]> getNamesInChannel(String channel) {
        // Initialise future for getting all users in a channel
        namesFuture = new CompletableFuture<>();

        try {
            // Send a names message to the server
            writer.println("NAMES " + channel);

        } catch (Exception e) {
            handleException(e, namesFuture);
        } finally {
            return namesFuture;
        }
    }

    // Position of channel name in message content
    private static int CHANNEL_INDEX_POSITION = 0;

    /**
     * Handle the server's response to getting all users in a channel
     * @param messageContent the content of the server's response
     */
    private void onNamesInChannel(String messageContent) {
        // Split the message content into an array of users
        String[] splitMessageContent = messageContent.split(" ");

        // Get the channel name from the message content
        String channelName = splitMessageContent[CHANNEL_INDEX_POSITION];
    
        // Get the channel from the list of targets
        Channel channel = (Channel) targets.get(channelName);

        // Get all users in the channel from the message content
        String[] users = Arrays.copyOfRange(splitMessageContent, CHANNEL_INDEX_POSITION + 1, splitMessageContent.length - 1);

        // If the channel exists, update the users in the channel
        if (channel != null) {
            channel.overwriteUsers(users);
        }

        // Complete the names future with the users in the channel
        if (namesFuture != null && !namesFuture.isDone()) {
            namesFuture.complete(users);
        }
    }

    // Future for getting all channels offered by the server
    private CompletableFuture<String[]> channelsFuture;

    /**
     * Get all channels offered by the server
     * @return a future that completes with the channels offered by the server
     */
    public CompletableFuture<String[]> getOfferedChannels() {
        // Initialise future for getting all channels offered by the server
        channelsFuture = new CompletableFuture<>();

        try {
            // Send a list message to the server
            writer.println("LIST");

        } catch (Exception e) {
            handleException(e, channelsFuture);
        } finally {
            return channelsFuture;
        }
    }

    /**
     * Handle the server's response to getting all channels offered by the server
     * @param messageContent the content of the server's response
     */
    private void onOfferedChannels(String messageContent) {
        // Split the message content into an array of channels
        String[] channels = messageContent.split(" ");

        // Complete the channels future with the channels offered by the server
        if (channelsFuture != null && !channelsFuture.isDone()) {
            channelsFuture.complete(channels);
        }
    }

    // Future for getting the server's time
    private CompletableFuture<String> timeFuture;

    /**
     * Get the server's time
     * @return a future that completes with the server's time
     */
    public CompletableFuture<String> getTimeFuture() {
        // Initialise future for getting the server's time 
        timeFuture = new CompletableFuture<>();

        try {
            // Send a time message to the server
            writer.println("TIME");

        } catch (Exception e) {
            handleException(e, timeFuture);
        } finally {
            return timeFuture;
        }
    }

    /**
     * Handle the server's response to getting the server's time
     * @param messageContent the content of the server's response
     */
    private void onTime(String messageContent) {
        // Complete the time future with the server's time
        if (timeFuture != null && !timeFuture.isDone()) {
            timeFuture.complete(messageContent);
        }
    }
    
    // Future for getting the server's information
    private CompletableFuture<String> infoFuture;

    /**
     * Get the server's information
     * @return a future that completes with the server's information
     */
    public CompletableFuture<String> getInfo() {
        // Initialise future for getting the server's information
        infoFuture = new CompletableFuture<>();

        try {
            // Send an info message to the server
            writer.println("INFO");

        } catch (Exception e) {
            handleException(e, infoFuture);
        } finally {
            return infoFuture;
        }
    }

    /**
     * Handle the server's response to getting the server's information
     * @param messageContent the content of the server's response
     */
    private void onInfo(String messageContent) {
        // Complete the info future with the server's information
        if (infoFuture != null && !infoFuture.isDone()) {
            infoFuture.complete(messageContent);
        }
    }

    // Future for sending a ping message to the server
    private CompletableFuture<String> pongFuture;

    /**
     * Send a ping message to the server
     * @param messageContent the content of the ping message
     * @return a future that completes with the server's response
     */
    public CompletableFuture<String> ping(String messageContent) {
        // Initialise future for sending a ping message to the server
        pongFuture = new CompletableFuture<>();

        try {
            // Send a ping message to the server with the message content
            writer.println("PING " + messageContent);

        } catch (Exception e) {
            handleException(e, pongFuture);
        } finally {
            return pongFuture;
        }
    }

    /**
     * Handle the server's response (pong) to a ping message
     * @param messageContent the content of the server's response
     */
    private void onPong(String messageContent) {
        // Complete the pong future with the server's response
        if (pongFuture != null && !pongFuture.isDone()) {
            pongFuture.complete(messageContent);
        }
    }

    /**
     * Handle an error message from the server
     * @param messageContent the content of the error message
     */
    private void onError(String messageContent) {
        // If setting nickname fails, connection is invalid so disconnect
        // Code is 'Nick' not 'NICK' because it is the first word in nickname error message
        if (messageContent.contains("Nick")) {
            disconnect();
        }
        
        // Throw and handle a RuntimeException with the server's error message
        handleException(new RuntimeException(messageContent));
    }

    /**
     * Getter for the host of the server
     * @return the host of the server
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Getter for the port of the server 
     * @return the port of the server
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Gets user-friendly string representation of the server
     * @return the user-friendly string representation of the server
     */
    public String toString() {
        return nickname + " | " + host + ":" + Integer.toString(port);
    }

    /**
     * Getter for all targets in server
     * @return all targets in server
     */
    public HashMap<String, Target> getTargets() {
        return targets;
    }

    /**
     * Getter for just users in server
     * @return all users in server
     */
    public Target[] getUsers() {
        // Filter out channels from targets
        return targets.values().stream()
            .filter(target -> !(target.isChannel()))
            .toArray(Target[]::new);
    }

    /**
     * Getter for channels client has joined
     * @return all channels client has joined
     */
    public Channel[] getJoinedChannels() {
        // Filter out users (i.e non-channels) from targets
        return targets.values().stream()
            .filter(target -> target.isChannel()) 
            .toArray(Channel[]::new); 
    }

    /**
     * Add a target to the server
     * @param target the target to add
     */
    public void addTarget(Target target) {
        targets.put(target.getName(), target);
    }
}
