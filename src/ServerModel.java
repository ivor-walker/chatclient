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

public class ServerModel {
	private Socket connection;
	private BufferedReader reader;
	private PrintWriter writer;

	private String host;
	private int port;	
	private String nickname;
	
	private List<TargetListener> targetListeners = new ArrayList<>();
    
    public void addTargetListener(TargetListener listener) {
        targetListeners.add(listener);
    }

	private List<ServerListener> serverListeners = new ArrayList<>();
	
	public void addServerListener(ServerListener listener) {
		serverListeners.add(listener);
	}

/**
 * Handles exceptions by logging the error, notifying listeners, and completing a CompletableFuture with the exception.
 *
 * @param e the exception to handle
 * @param future the CompletableFuture to complete exceptionally
 */
private void handleException(Exception e, CompletableFuture future) {
    // Log and notify listeners about the exception
    handleException(e);
    // Complete the CompletableFuture with the exception
    future.completeExceptionally(e);
}

/**
 * Logs the exception, dumps the stack trace, and notifies all registered server listeners with the error message.
 *
 * @param e the exception to handle
 */
private void handleException(Exception e) {
    // Generate a user-friendly error message for the exception
    String errorMessage = getErrorMessage(e);

    // Notify all registered server listeners about the error
    for (ServerListener listener : serverListeners) {
        listener.onError(errorMessage);
    }

    // Print the stack trace to the console for debugging
    e.printStackTrace();
    Thread.dumpStack();
}

/**
 * Generates a user-friendly error message based on the type of exception.
 *
 * @param e the exception for which to generate the error message
 * @return a string containing the error message
 */
private String getErrorMessage(Exception e) {
    String errorMessage;

    // Handle specific exception types with tailored messages
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
    } else if (e instanceof RuntimeException) {
        errorMessage = "Server responded with error message: " + e.getMessage();
    } else {
        // Generic message for unhandled exceptions
        errorMessage = "Unknown error: " + e.getMessage();
    }

    return errorMessage;
}

/**
 * Initializes a new server connection with the specified host, port, and nickname.
 * Also, registers a shutdown hook to ensure proper disconnection when the application terminates.
 *
 * @param host the hostname or IP address of the server
 * @param port the port number to connect to
 * @param nickname the nickname to use for the connection
 */
public ServerModel(String host, int port, String nickname) {
    // Initialize instance variables with the provided parameters
    this.host = host;
    this.port = port;
    this.nickname = nickname;

    // Add a shutdown hook to ensure the server is disconnected properly when the application shuts down
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        // Attempt to disconnect from the server and log the disconnection
        disconnect().thenRun(() -> {
            System.out.println("Disconnecting from " + toString());
        });
    }));
}

// A map to store targets identified by a unique string key.
private HashMap<String, Target> targets = new HashMap<>();

// A future that signals when the connection process has been completed.
private CompletableFuture<Void> connectFuture;

// Flag to determine if the connection should keep listening for messages.
private boolean keepListening;

/**
 * Establishes a connection to the server and handles initialization tasks.
 * 
 * @return A CompletableFuture that completes when the connection process is successful.
 */
public CompletableFuture<Void> connect() {
    // Initialize the future for the connection process.
    connectFuture = new CompletableFuture<>();

    try {
        // Establish a socket connection to the specified host and port.
        connection = new Socket(host, port);

        // Initialize the reader for incoming messages from the server.
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        
        // Enable the listening loop for incoming messages.
        keepListening = true;

        // Initialize the writer for sending messages to the server.
        writer = new PrintWriter(connection.getOutputStream(), true);

        // Start a separate thread to listen for messages.
        listenForMessages();

        // Send the nickname to the server and complete the connection process.
        sendNickname(nickname).thenRun(() -> {
            connectFuture.complete(null); // Mark the connection future as completed.
        });

    } catch (Exception e) {
        // Handle any exceptions that occur during the connection process.
        handleException(e, connectFuture);

    } finally {
        // Return the future representing the connection process.
        return connectFuture;
    }
}

// A future representing the result of sending the nickname.
private CompletableFuture<String> nicknameFuture;

/**
 * Sends the specified nickname to the server.
 * 
 * @param nickname The nickname to be sent to the server.
 * @return A CompletableFuture that completes when the nickname is acknowledged by the server.
 */
private CompletableFuture<String> sendNickname(String nickname) {
    // Initialize the future for the nickname sending process.
    nicknameFuture = new CompletableFuture<>();

    try {
        // Send the nickname command to the server.
        writer.println("NICK " + nickname);
    } catch (Exception e) {
        // Handle any exceptions that occur while sending the nickname.
        handleException(e, nicknameFuture);
    } finally {
        // Return the future representing the nickname sending process.
        return nicknameFuture;
    }
}

/**
 * Handles an incoming nickname message from the server.
 * 
 * @param messageContent The message content received from the server.
 */
private void onNickname(String messageContent) {
    // Extract the nickname from the message content.
    String nickname = messageContent.split(", ")[1];

    // Update the current nickname with the received value.
    this.nickname = nickname;

    // Complete the nickname future if it's not already completed.
    if (nicknameFuture != null && !nicknameFuture.isDone()) {
        nicknameFuture.complete(messageContent);
    }
}

/**
 * Getter for nickname
*/	
	public String getNickname() {
		return nickname;
	}

/**
 * Starts a background thread to continuously listen for messages from the server.
 * Handles received messages and ensures proper cleanup when listening stops.
 */
private void listenForMessages() {
    // Start a new thread to handle server messages asynchronously.
    new Thread(() -> {
        while (keepListening) {
            String message;
            try {
                // Continuously read messages from the server while the reader has input.
                while ((message = reader.readLine()) != null) {
                    handleServerMessage(message); // Process each message.
                }
            } catch (Exception e) {
                // Handle any exceptions that occur during message reading.
                handleException(e);
            }
        }

        // Cleanup resources after listening has stopped.
        try {
            writer.close(); // Close the writer to release resources.
            reader.close(); // Close the reader to release resources.
            connection.close(); // Close the connection to the server.
        } catch (Exception e) {
            // Handle exceptions that occur during cleanup.
            handleException(e, disconnectFuture);
        }
    }).start(); // Start the thread.
}
	
	private int TARGET_POSITION = 2;
	private int MESSAGE_POSITION = 3;

    /**
     * Handles incoming server messages by parsing them and triggering appropriate actions.
     * 
     * @param serverMessage The raw message received from the server.
     */
    private void handleServerMessage(String serverMessage) {
        System.out.println("FROM | " + LocalDateTime.now().toString() + " | " + toString() + " | " + serverMessage);
    
        // Check if the server message is a notification (starts with ":").
        if (serverMessage.startsWith(":")) {
            // Remove the leading colon and split the message into parts.
            String filteredMessage = serverMessage.replaceFirst(":", "");
            String[] splitMessage = filteredMessage.split(" ");
    
            // Extract nickname and message code from the parsed message.
            String nickname = splitMessage[0];
            String messageCode = splitMessage[1];
    
            // Attempt to extract the target of the message if it exists.
            String target = "";
            if (splitMessage.length > TARGET_POSITION) {
                target = splitMessage[TARGET_POSITION];
            }
    
            // Attempt to extract the actual message content if it exists.
            String message = "";
            if (splitMessage.length > MESSAGE_POSITION) {
                String[] splitMessageContent = Arrays.copyOfRange(splitMessage, MESSAGE_POSITION, splitMessage.length);
                message = String.join(" ", splitMessageContent);
                message = message.replaceFirst(":", ""); // Remove leading colon from the message content.
            }
    
            // Check if the message refers to this client.
            boolean refersToSelf = this.nickname.equals(nickname);
    
            // Handle notifications specifically for this client.
            if (refersToSelf) {
                switch (messageCode) {
                    case "QUIT":
                        selfQuit(); // Handle self-quit notification.
                        return;
                    case "JOIN":
                        onJoinChannel(target); // Handle joining a channel.
                        return;
                    case "PART":
                        onPartChannel(target); // Handle leaving a channel.
                        return;
                    case "PRIVMSG":
                        onMessage(nickname, target, message); // Handle a private message.
                        return;
                }
            } else {
                // Handle notifications related to other users.
                switch (messageCode) {
                    case "QUIT":
                        onQuit(nickname); // Handle a user quitting.
                        return;
                    case "JOIN":
                        onJoinChannel(nickname, target); // Handle a user joining a channel.
                        return;
                    case "PART":
                        onPartChannel(nickname, target); // Handle a user leaving a channel.
                        return;
                    case "PRIVMSG":
                        onMessage(nickname, target, message); // Handle a private message from another user.
                        return;
                }
            }
        }
    
        // Process server replies where the message code typically consists of capital letters.
        String regex = "[A-Z_]+";
        Matcher matcher = Pattern.compile(regex).matcher(serverMessage);
    
        // Extract all matching message codes from the server message.
        List<String> messageCodes = new ArrayList<>();
        while (matcher.find()) {
            messageCodes.add(matcher.group());
        }
    
        // Handle each extracted message code.
        for (String messageCode : messageCodes) {
            // Extract and clean up the message content.
            String messageContent = serverMessage.replaceFirst(messageCode, "");
            messageContent = messageContent.replaceFirst(":", "").trim();
    
            // Trigger the appropriate action based on the message code.
            switch (messageCode) {
                case "REPLY_NAMES":
                    onNamesInChannel(messageContent); // Handle channel names reply.
                    return;
                case "REPLY_NICK":
                    onNickname(messageContent); // Handle nickname reply.
                    return;
                case "REPLY_LIST":
                    onOfferedChannels(messageContent); // Handle offered channels list reply.
                    return;
                case "REPLY_TIME":
                    onTime(messageContent); // Handle server time reply.
                    return;
                case "REPLY_INFO":
                    onInfo(messageContent); // Handle server info reply.
                    return;
                case "PONG":
                    onPong(messageContent); // Handle PONG reply.
                    return;
                case "ERROR":
                    onError(messageContent); // Handle error message.
                    return;
            }
        }
    }

    // A future that represents the completion of the disconnection process.
    private CompletableFuture<Void> disconnectFuture;
    
    /**
     * Disconnects from the server by sending a quit message and stopping the connection.
     * 
     * @return A CompletableFuture that completes when the disconnection process is successful.
     */
    public CompletableFuture<Void> disconnect() {
        // Initialize the future for the disconnection process.
        disconnectFuture = new CompletableFuture<>();
    
        try {
            // Send the QUIT command to the server to initiate disconnection.
            writer.println("QUIT");
    
            // Call selfQuit immediately if the server does not send a disconnect message.
            // This can be removed if the server handles the quit and sends a proper response.
            selfQuit();
        } catch (Exception e) {
            // Handle any exceptions that occur during disconnection.
            handleException(e, disconnectFuture);
        } finally {
            // Return the future representing the disconnection process.
            return disconnectFuture;
        }
    }

    /**
     * Handles the disconnection logic for the client itself.
     * Stops message listening and updates the model to reflect the disconnection.
     */
    private void selfQuit() {
        // Stop listening for messages.
        keepListening = false;
    
        // Update the model to remove this client's nickname.
        quitUpdateModel(this.nickname);
    
        // Complete the disconnect future if it hasn't been completed yet.
        if (disconnectFuture != null && !disconnectFuture.isDone()) {
            disconnectFuture.complete(null);
        }
    }
    
    /**
     * Handles a quit notification for a specific user.
     * Notifies all target listeners about the user quitting and updates the model.
     * 
     * @param nickname The nickname of the user who quit.
     */
    private void onQuit(String nickname) {
        // Notify all target listeners that the user has quit.
        for (TargetListener listener : targetListeners) {
            listener.onQuit(nickname);
        }
    
        // Update the model to remove the quitting user's data.
        quitUpdateModel(nickname);
    }
    
    /**
     * Updates the internal model to reflect that a user has quit.
     * Removes the target associated with the specified nickname.
     * 
     * @param nickname The nickname of the user to remove from the model.
     */
    private void quitUpdateModel(String nickname) {
        // Remove the target corresponding to the given nickname.
        targets.remove(nickname);
    }
	
    // A future that represents the completion of the join channel process.
    private CompletableFuture<Channel> joinChannelFuture;
    
    /**
     * Sends a request to join a specified channel.
     * 
     * @param channel The name of the channel to join.
     * @return A CompletableFuture that completes with the Channel object when successfully joined.
     */
    public CompletableFuture<Channel> joinChannel(String channel) {
        // Initialize the future for the join channel process.
        joinChannelFuture = new CompletableFuture<>();
    
        try {
            // Send the JOIN command to the server with the specified channel name.
            writer.println("JOIN " + channel);
    
        } catch (Exception e) {
            // Handle any exceptions that occur during the join channel process.
            handleException(e, joinChannelFuture);
        }
    
        // Return the future representing the join channel process.
        return joinChannelFuture;
    }
    
    /**
     * Handles a server notification that the client has joined a channel.
     * Retrieves the names of users in the channel, creates a Channel object,
     * and updates the internal state.
     * 
     * @param channelName The name of the channel that the client has joined.
     */
    private void onJoinChannel(String channelName) {
        // Fetch the list of users in the channel and process them.
        getNamesInChannel(channelName).thenAccept(users -> {
            // Create a new Channel object with the current client instance, channel name, and users.
            Channel channel = new Channel(this, channelName, users);
    
            // Add the new channel to the targets map.
            targets.put(channel.getName(), channel);
    
            // Complete the joinChannelFuture if it hasn't already been completed.
            if (joinChannelFuture != null && !joinChannelFuture.isDone()) {
                joinChannelFuture.complete(channel);
            }
        });
    }
    
    /**
     * Handles a server notification that a user has joined a channel.
     * Updates the channel's user list and notifies listeners.
     * 
     * @param nickname The nickname of the user who joined the channel.
     * @param channelName The name of the channel that the user joined.
     */
    private void onJoinChannel(String nickname, String channelName) {
        // Retrieve the channel object from the targets map.
        Channel channel = (Channel) targets.get(channelName);
        
        // If the channel does not exist, exit early.
        if (channel == null) {
            return;
        }
    
        // Add the user to the channel's user list.
        channel.addUser(nickname);
    
        // Notify all target listeners about the user joining the channel.
        for (TargetListener listener : targetListeners) {
            listener.onJoinChannel(nickname, channel);
        }
    }
	
// A future that represents the completion of the part channel process.
private CompletableFuture<Void> partChannelFuture;

/**
 * Sends a request to leave a specified channel.
 * 
 * @param channel The name of the channel to leave.
 * @return A CompletableFuture that completes when the client successfully leaves the channel.
 */
public CompletableFuture<Void> partChannel(String channel) {
    // Initialize the future for the part channel process.
    partChannelFuture = new CompletableFuture<>();

    try {
        // Send the PART command to the server with the specified channel name.
        writer.println("PART " + channel);
    } catch (Exception e) {
        // Handle any exceptions that occur during the part channel process.
        handleException(e, partChannelFuture);
    } finally {
        // Return the future representing the part channel process.
        return partChannelFuture;
    }
}

    /**
     * Handles the event when the client successfully leaves a channel.
     * Removes the channel from the internal targets map and completes the future.
     * 
     * @param channelName The name of the channel that the client left.
     */
    private void onPartChannel(String channelName) {
        // Remove the channel from the targets map.
        targets.remove(channelName);
    
        // Complete the partChannelFuture if it hasn't already been completed.
        if (partChannelFuture != null && !partChannelFuture.isDone()) {
            partChannelFuture.complete(null);
        }
    }
    
    /**
     * Handles the event when another user leaves a channel.
     * Updates the channel's user list and notifies listeners.
     * 
     * @param nickname The nickname of the user who left the channel.
     * @param channelName The name of the channel that the user left.
     */
    private void onPartChannel(String nickname, String channelName) {
        // Retrieve the channel object from the targets map.
        Channel channel = (Channel) targets.get(channelName);
    
        // If the channel does not exist, exit early.
        if (channel == null) {
            return;
        }
    
        // Remove the user from the channel's user list.
        channel.removeUser(nickname);
    
        // Notify all target listeners that the user has left the channel.
        for (TargetListener listener : targetListeners) {
            listener.onPartChannel(nickname, channel);
        }
    }
	
    /**
     * Sends a message to a specified target.
     * If the target is not a channel (does not start with '#'), it also triggers local handling.
     * 
     * @param target The name of the channel or user to send the message to.
     * @param messageContent The content of the message being sent.
     */
    public void sendMessage(String target, String messageContent) {
        try {
            // Log the message being sent.
            System.out.println("ServerModel: sending message | " + target + " | " + messageContent);
    
            // Send the PRIVMSG command with the target and message content.
            writer.println("PRIVMSG " + target + " :" + messageContent);
    
            // If the target is not a channel (starts with '#'), process the message locally.
            if (!target.startsWith("#")) {
                onMessage(nickname, target, messageContent);
            }
        } catch (Exception e) {
            // Handle any exceptions that occur while sending the message.
            handleException(e);
        }
    }
    
    /**
     * Handles incoming messages and updates the relevant target's message list.
     * Also notifies listeners about the received message.
     * 
     * @param sender The nickname of the sender of the message.
     * @param targetName The name of the channel or user to which the message was sent.
     * @param messageContent The content of the message received.
     */
    private void onMessage(String sender, String targetName, String messageContent) {
    
        // Fetch the server time asynchronously and process the message.
        getTimeFuture().thenAccept(serverTime -> {
            // Create a new Message object with the details.
            Message message = new Message(sender, targetName, messageContent, serverTime, toString());
    
            // Retrieve the target (channel or user) associated with the message.
            Target target = targets.get(message.getTarget());
    
            // If the target does not exist, create a new one and add it to the targets map.
            if (target == null) {
                target = new Target(this, message.getTarget());
                targets.put(target.getName(), target);
            }
    
            // Add the message to the target's message list.
            target.addMessage(message);
    
            // Notify all listeners that a new message has been received.
            for (TargetListener listener : targetListeners) {
                listener.onMessageRecieved(message);
            }
        });
    }
	
    // A future representing the completion of the process to fetch names from a channel.
    private CompletableFuture<String[]> namesFuture;
    
    /**
     * Sends a request to retrieve the names of users in a specified channel.
     * 
     * @param channel The name of the channel for which user names are requested.
     * @return A CompletableFuture that completes with an array of user names in the channel.
     */
    public CompletableFuture<String[]> getNamesInChannel(String channel) {
        // Initialize the future for retrieving names.
        namesFuture = new CompletableFuture<>();
        try {
            // Send the NAMES command to the server for the specified channel.
            writer.println("NAMES " + channel);
        } catch (Exception e) {
            // Handle any exceptions that occur during the request.
            handleException(e, namesFuture);
        } finally {
            // Return the future representing the retrieval process.
            return namesFuture;
        }
    }
    
    // The position in the split message array where the channel name is expected.
    private static int CHANNEL_INDEX_POSITION = 0;
    
    /**
     * Handles the server's response with the list of user names in a channel.
     * Updates the channel's user list and completes the future with the names.
     * 
     * @param messageContent The raw message content containing the channel name and user names.
     */
    private void onNamesInChannel(String messageContent) {
        // Split the message content into parts to extract details.
        String[] splitMessageContent = messageContent.split(" ");
    
        // Extract the channel name from the predefined index position.
        String channelName = splitMessageContent[CHANNEL_INDEX_POSITION];
    
        // Retrieve the corresponding channel object from the targets map.
        Channel channel = (Channel) targets.get(channelName);
    
        // Extract the list of user names, excluding the trailing information.
        String[] users = Arrays.copyOfRange(splitMessageContent, CHANNEL_INDEX_POSITION + 1, splitMessageContent.length - 1);
    
        // Update the channel's user list if the channel exists.
        if (channel != null) {
            channel.overwriteUsers(users);
        }
    
        // Complete the namesFuture with the retrieved user names if it's not already completed.
        if (namesFuture != null && !namesFuture.isDone()) {
            namesFuture.complete(users);
        }
    }
    
    // A future representing the completion of the process to retrieve the list of offered channels.
    private CompletableFuture<String[]> channelsFuture;
    
    /**
     * Sends a request to retrieve the list of all available channels on the server.
     * 
     * @return A CompletableFuture that completes with an array of available channel names.
     */
    public CompletableFuture<String[]> getOfferedChannels() {
        // Initialize the future for retrieving the list of offered channels.
        channelsFuture = new CompletableFuture<>();
        try {
            // Send the LIST command to the server to request the available channels.
            writer.println("LIST");
        } catch (Exception e) {
            // Handle any exceptions that occur during the request.
            handleException(e, channelsFuture);
        } finally {
            // Return the future representing the retrieval process.
            return channelsFuture;
        }
    }

    /**
     * Handles the server's response containing the list of available channels.
     * Completes the channelsFuture with the retrieved channel names.
     * 
     * @param messageContent The raw message content containing the list of channels.
     */
    private void onOfferedChannels(String messageContent) {
        // Split the message content into an array of channel names.
        String[] channels = messageContent.split(" ");
    
        // Complete the channelsFuture with the channel names if it's not already completed.
        if (channelsFuture != null && !channelsFuture.isDone()) {
            channelsFuture.complete(channels);
        }
    }

    // A future representing the completion of the process to get the current server time.
    private CompletableFuture<String> timeFuture;
    
    /**
     * Sends a request to retrieve the current server time.
     * 
     * @return A CompletableFuture that completes with the server's current time as a string.
     */
    public CompletableFuture<String> getTimeFuture() {
        // Initialize the future for the server time request.
        timeFuture = new CompletableFuture<>();
        try {
            // Send the TIME command to the server.
            writer.println("TIME");
        } catch (Exception e) {
            // Handle any exceptions that occur while requesting the server time.
            handleException(e, timeFuture);
        } finally {
            // Return the future representing the server time retrieval process.
            return timeFuture;
        }
    }
    
    /**
     * Handles the server's response containing the current time.
     * Completes the timeFuture with the retrieved server time.
     * 
     * @param messageContent The raw message content containing the server time.
     */
    private void onTime(String messageContent) {
        // Complete the timeFuture with the server time if it's not already completed.
        if (timeFuture != null && !timeFuture.isDone()) {
            timeFuture.complete(messageContent);
        }
    }
    
    // A future representing the completion of the process to get server information.
    private CompletableFuture<String> infoFuture;
    
    /**
     * Sends a request to retrieve information about the server.
     * 
     * @return A CompletableFuture that completes with the server information as a string.
     */
    public CompletableFuture<String> getInfo() {
        // Initialize the future for the server information request.
        infoFuture = new CompletableFuture<>();
        try {
            // Send the INFO command to the server.
            writer.println("INFO");
        } catch (Exception e) {
            // Handle any exceptions that occur while requesting server information.
            handleException(e, infoFuture);
        } finally {
            // Return the future representing the server information retrieval process.
            return infoFuture;
        }
    }

    /**
     * Handles the server's response containing server information.
     * Completes the infoFuture with the retrieved information.
     * 
     * @param messageContent The raw message content containing the server information.
     */
    private void onInfo(String messageContent) {
        // Complete the infoFuture with the server information if it's not already completed.
        if (infoFuture != null && !infoFuture.isDone()) {
            infoFuture.complete(messageContent);
        }
    }
    
    // A future representing the completion of the ping-pong process.
    private CompletableFuture<String> pongFuture;
    
    /**
     * Sends a PING request to the server with a specified message content.
     * 
     * @param messageContent The content to be included in the PING request.
     * @return A CompletableFuture that completes with the server's PONG response as a string.
     */
    public CompletableFuture<String> ping(String messageContent) {
        // Initialize the future for the ping-pong process.
        pongFuture = new CompletableFuture<>();
        try {
            // Send the PING command with the specified message content.
            writer.println("PING " + messageContent);
        } catch (Exception e) {
            // Handle any exceptions that occur during the PING request.
            handleException(e, pongFuture);
        } finally {
            // Return the future representing the ping-pong process.
            return pongFuture;
        }
    }
    
    /**
     * Handles the server's PONG response.
     * Completes the pongFuture with the received PONG message content.
     * 
     * @param messageContent The raw message content from the server's PONG response.
     */
    private void onPong(String messageContent) {
        // Complete the pongFuture with the PONG response if it's not already completed.
        if (pongFuture != null && !pongFuture.isDone()) {
            pongFuture.complete(messageContent);
        }
    }
    
    /**
     * Handles error messages received from the server.
     * If the error is related to a nickname issue, it triggers a disconnect.
     * 
     * @param messageContent The raw error message content from the server.
     */
    private void onError(String messageContent) {
        // Disconnect if the error message indicates a nickname issue.
        if (messageContent.contains("Nick")) {
            disconnect();
        }
    
        // Handle the error by creating and processing a runtime exception.
        handleException(new RuntimeException(messageContent));
    }
    
    /**
     * Retrieves the server's host address.
     * 
     * @return The server's host address as a string.
     */
    public String getHost() {
        return this.host;
    }
    
    /**
     * Retrieves the server's port number.
     * 
     * @return The server's port number as an integer.
     */
    public int getPort() {
        return this.port;
    }
    
    /**
     * Returns a string representation of the connection information.
     * 
     * @return A string containing the nickname, host, and port.
     */
    public String toString() {
        return nickname + " | " + host + ":" + Integer.toString(port);
    }
    
    /**
     * Retrieves the map of targets currently associated with this connection.
     * 
     * @return A HashMap of targets keyed by their name.
     */
    public HashMap<String, Target> getTargets() {
        return targets;
    }
    
    /**
     * Retrieves an array of users (targets that are not channels).
     * 
     * @return An array of Target objects representing users.
     */
    public Target[] getUsers() {
        // Filter the targets to include only users (not channels) and convert to an array.
        return targets.values().stream()
            .filter(target -> !(target.isChannel()))
            .toArray(Target[]::new);
    }

    /**
     * Retrieves an array of channels currently joined by this connection.
     * 
     * @return An array of Channel objects representing the joined channels.
     */
    public Channel[] getJoinedChannels() {
        // Filter the targets to include only channels and convert to an array.
        return targets.values().stream()
            .filter(target -> target.isChannel()) // Check if the target is a channel.
            .toArray(Channel[]::new); // Convert the filtered stream to an array.
    }
    
    /**
     * Adds a new target to the list of targets.
     * 
     * @param target The Target object to be added.
     */
    public void addTarget(Target target) {
        // Add the target to the targets map, using its name as the key.
        targets.put(target.getName(), target);
    }
}
