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
	
	private boolean isActive;

	public void setActivity(boolean activityState) {
		this.isActive = activityState;	
	}

	public boolean getActivity() {
		return isActive;
	}	

	private String getCallerInfo() {
	return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
		      .walk(frames -> frames
		      .skip(2) // Skip 2 levels: current method + immediate caller
		      .findFirst()
		      .map(frame -> frame.getClassName() + "." + frame.getMethodName() + 
			  "(" + frame.getFileName() + ":" + frame.getLineNumber() + ")")
		      .orElse("Unknown Caller"));
	}

	//Listeners only for unexpected non-replies from the server
	private List<MessageListener> messageListeners = new ArrayList<>();
	
	public void addMessageListener(MessageListener listener) {
		messageListeners.add(listener);
	}

	private List<ServerListener> serverListeners = new ArrayList<>();
	
	public void addServerListener(ServerListener listener) {
		serverListeners.add(listener);
	}

	private void handleException(Exception e, CompletableFuture future) {
		handleException(e);		
		future.completeExceptionally(e);	
	}

	private void handleException(Exception e) {
		String errorMessage = getErrorMessage(e);	
		for(ServerListener listener : serverListeners) {
			System.out.println("Server listener notified of " + errorMessage);
			listener.onError(errorMessage);
		}
		
		for(MessageListener listener : messageListeners) {
			System.out.println("Message listener notified of " + errorMessage);
			listener.onError(errorMessage);
		}

		e.printStackTrace();
		Thread.dumpStack();
	}
 
	private String getErrorMessage(Exception e) {
		String errorMessage;
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
		    	errorMessage = "Unknown error: " + e.getMessage();
		}
	
		return errorMessage;
	}

	//Initialise and connect to a new server
	public ServerModel(String host, int port, String nickname) {
		this.host = host;
		this.port = port;
		this.nickname = nickname;	
	}

	private HashMap<String, Target> targets = new HashMap<>();

	private CompletableFuture<Void> connectFuture;
    private boolean keepListening;
	
    public CompletableFuture<Void> connect() {
        connectFuture = new CompletableFuture<>();

        try {	
			connection = new Socket(host, port);

			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		    keepListening = true;
	
            writer = new PrintWriter(connection.getOutputStream(), true);
			
			listenForMessages();
			sendNickname(nickname).thenRun(() -> {
                connectFuture.complete(null);
            });
			
        } catch(Exception e) {
            handleException(e, connectFuture);	
		
        } finally {
			return connectFuture;
		}
	}
    
    //Nicknames
	private CompletableFuture<String> nicknameFuture;

	private CompletableFuture<String> sendNickname(String nickname) {
	    nicknameFuture = new CompletableFuture<>(); 
        try {	
			writer.println("NICK " + nickname);
		} catch (Exception e) {
			handleException(e, namesFuture);
		} finally {
			return nicknameFuture;
		}
	}
	
	private void onNickname(String messageContent) {
		String nickname = messageContent.split(", ")[1];

		this.nickname = nickname;

		if(nicknameFuture != null && !nicknameFuture.isDone()) {
			nicknameFuture.complete(messageContent);
		}
	}
	
	public String getNickname() {
		return nickname;
	}

	//Waiting for and handling server messages	
   	private void listenForMessages() {
        System.out.println("called listenformessages");
		new Thread(() -> {
            System.out.println("started thread");
			while(keepListening) {
				String message;
				try {
					while((message = reader.readLine()) != null) {
					    System.out.println("read line");	
                        handleServerMessage(message);
					}	
				} catch (Exception e) {
					handleException(e);
				}
			}

            try {
                writer.close();
		    	reader.close();
                connection.close();

		    } catch (Exception e) {
                handleException(e, disconnectFuture);
            }

        }).start();
	}
	
	private int TARGET_POSITION = 2;
	private int MESSAGE_POSITION = 3;

	//TODO refactor
	private void handleServerMessage(String serverMessage) { 
		System.out.println("FROM | " + LocalDateTime.now().toString() + " | " + toString() + " | " + serverMessage);
		//Server notifications	
		if(serverMessage.startsWith(":")) {	
			String filteredMessage = serverMessage.replaceFirst(":", "");
			String[] splitMessage = filteredMessage.split(" ");

			String nickname = splitMessage[0];
			String messageCode = splitMessage[1];
			
			String target = "";
			if(splitMessage.length > TARGET_POSITION) {
				target = splitMessage[TARGET_POSITION];
			}

			String message = "";
			if(splitMessage.length > MESSAGE_POSITION) {
				String[] splitMessageContent = Arrays.copyOfRange(splitMessage, MESSAGE_POSITION, splitMessage.length);
				message = String.join(" ", splitMessageContent); 
			}
			

			boolean refersToSelf = this.nickname == nickname;
			
		    System.out.println(serverMessage + " | " + filteredMessage + " | " + nickname + " | " + messageCode	+ " | " + target + " | " + message + " | " + refersToSelf);
			if(refersToSelf) {	
				switch(messageCode) {
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
			} else {
				switch(messageCode) {
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

		//Server replies	
		//Only noticeable pattern is that the message code has capital letters in it 
		String regex = "[A-Z_]+";
		Matcher matcher = Pattern.compile(regex).matcher(serverMessage);
		
		List<String> messageCodes = new ArrayList<>();
		while (matcher.find()) {
			messageCodes.add(matcher.group());
		}

		for (String messageCode : messageCodes) {
			String messageContent = serverMessage.replaceFirst(messageCode, "");
			messageContent = messageContent.replaceFirst(":", "");
			//TODO fix message processing	
			
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

	//Disconnect
	private CompletableFuture<Void> disconnectFuture;

	public CompletableFuture<Void> disconnect() {
	    disconnectFuture = new CompletableFuture<>();
	
        try {
			writer.println("QUIT");
            //Remove this call if you can get the server to send a disconnect message
            selfQuit();
		} catch (Exception e) {
			handleException(e, disconnectFuture);
        } finally {
			return disconnectFuture;
		}
	}

	private void selfQuit() {
        System.out.println("found onquit");	
        keepListening = false;	
	    
	    quitUpdateModel(this.nickname);
        	
	    if(disconnectFuture != null && !disconnectFuture.isDone()) {
	    	disconnectFuture.complete(null);
	    }

    }

	private void onQuit(String nickname) {
		for(MessageListener listener : messageListeners) {
			listener.onQuit(nickname);
		}
		quitUpdateModel(nickname);	
	}

	private void quitUpdateModel(String nickname) {
		targets.remove(nickname);
	}
	
	//Join channel
	private CompletableFuture<Channel> joinChannelFuture;

	public CompletableFuture<Channel> joinChannel(String channel) {
        joinChannelFuture = new CompletableFuture<>();

		try {	
			writer.println("JOIN " + channel);	
		
		} catch (Exception e) {
			handleException(e, joinChannelFuture);
		}	

		return joinChannelFuture;
	}

	private void onJoinChannel(String channelName) {
		getNamesInChannel(channelName).thenAccept(users -> {
			Channel channel = new Channel(channelName, users);	
			targets.put(channel.getName(), channel);	
			
			if(joinChannelFuture != null && !joinChannelFuture.isDone()) {
				joinChannelFuture.complete(channel);
			}
		});
	}

	private void onJoinChannel(String nickname, String channelName) {
		Channel channel = (Channel) targets.get(channelName);
		if(channel == null) {
			return;
		}
	
		channel.addUser(nickname);

		for(MessageListener listener : messageListeners) {
			listener.onJoinChannel(nickname, channel);
		}
	}
	
	//Leave channel
	private CompletableFuture<Void> partChannelFuture;

	public CompletableFuture<Void> partChannel(String channel) {
	    partChannelFuture = new CompletableFuture<>();	
        try {
			writer.println("PART " + channel);
		
		} catch (Exception e) {
			handleException(e, partChannelFuture);
		
		} finally {
			return partChannelFuture;
		}
	}
	
	private void onPartChannel(String channelName) {
		targets.remove(channelName);
		
		if(partChannelFuture != null && !partChannelFuture.isDone()) {
			partChannelFuture.complete(null);
		}
	}

	private void onPartChannel(String nickname, String channelName) {
		Channel channel = (Channel) targets.get(channelName);
		
		if(channel == null) {
			return;	
		}		

		channel.removeUser(nickname);	
	
		for(MessageListener listener : messageListeners) {
			listener.onPartChannel(nickname, channel);
		}	
	}
	
	//Messages
	public void sendMessage(String target, String messageContent) {
		try {
			writer.println("PRIVMSG " + target + " :" + messageContent);
		} catch (Exception e) {
			handleException(e);
		}
	}

	private void onMessage(String sender, String targetName, String messageContent) {
		getTimeFuture().thenAccept(serverTime -> {
			Message message = new Message(sender, targetName, messageContent, serverTime);
			Target target = targets.get(message.getTarget()); 

			if(target == null) {
				target = new Target(message.getTarget());
				targets.put(target.getName(), target);
			}

			target.addMessage(message);

			for(MessageListener listener : messageListeners) {
				listener.onMessageRecieved(sender, target, message);
			}
		});
	}
	
	//Get names from channel	
	private CompletableFuture<String[]> namesFuture;

	public CompletableFuture<String[]> getNamesInChannel(String channel) {
        namesFuture = new CompletableFuture<>();
		try {	
			writer.println("NAMES " + channel);
		} catch (Exception e) {
			handleException(e, namesFuture);
		} finally {
			return namesFuture;
		}
	}

	private static int CHANNEL_INDEX_POSITION = 0;	

	private void onNamesInChannel(String messageContent) {
		String[] splitMessageContent = messageContent.split(" ");
		
		String channelName = splitMessageContent[CHANNEL_INDEX_POSITION];
		Channel channel = (Channel) targets.get(channelName);
		if(channel == null) {
			return;
		}		
		
		String[] users = Arrays.copyOfRange(splitMessageContent, CHANNEL_INDEX_POSITION, splitMessageContent.length-1);
		
		channel.overwriteUsers(users);
	
		if(namesFuture != null && !namesFuture.isDone()) {
			namesFuture.complete(users);
		}	
	}

	

	//Get all channels	
	private CompletableFuture<String[]> channelsFuture;

	public CompletableFuture<String[]> getOfferedChannels() {
	    channelsFuture = new CompletableFuture<>();
        try {
			writer.println("LIST");
		} catch (Exception e) {
			handleException(e, channelsFuture);
		} finally {
			return channelsFuture;
		}
	}
	
	private void onOfferedChannels(String messageContent) {
		String[] channels = messageContent.split(" ");

		if(channelsFuture != null && !channelsFuture.isDone()) {
			channelsFuture.complete(channels);
		}	
	}

	//Getting current server time	
	private CompletableFuture<String> timeFuture;

	public CompletableFuture<String> getTimeFuture() {
        timeFuture = new CompletableFuture<>();
		try {
			writer.println("TIME");
		} catch (Exception e) {
			handleException(e, timeFuture);
		} finally {
			return timeFuture; 
		}
	}

	private void onTime(String messageContent) {
		if(timeFuture != null && !timeFuture.isDone()) {
			timeFuture.complete(messageContent);
		}	
	}

	//Get info
	private CompletableFuture<String> infoFuture;
	
	public CompletableFuture<String> getInfo() {
        infoFuture = new CompletableFuture<>();
		try {
			writer.println("INFO");
		} catch (Exception e) {
			handleException(e, timeFuture);
		} finally {
			return infoFuture;
		}
	}

	private void onInfo(String messageContent) {
		if(infoFuture != null && !infoFuture.isDone()) {
			infoFuture.complete(messageContent);
		}
	}

	//Ping-pong	
	private CompletableFuture<String> pongFuture;
	
	public CompletableFuture<String> ping(String messageContent) {
        pongFuture = new CompletableFuture<>(); 
		try {	
			writer.println("PING " + messageContent);
		} catch (Exception e) {
			handleException(e, pongFuture);
		} finally {
			return pongFuture;
		}
	}

	private void onPong(String messageContent) {
		if(pongFuture != null && !pongFuture.isDone()) {
			pongFuture.complete(messageContent);
		}
	}
	
	//Error listening
	private void onError(String messageContent) {
        if(messageContent.contains("Nick")) {
            disconnect();
        }

		handleException(new RuntimeException(messageContent));	
	}

	//Getters of connection info
	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;	
	}
	
	public String toString() {
		return nickname + " | " + host + ":" + Integer.toString(port); 
	}

	//Getters of targets
	public Target[] getUsers() {
		return targets.values().stream()
			.filter(target -> !(target.isChannel()))
			.toArray(Target[]::new);
	}

	public Channel[] getChannels() {
		return targets.values().stream()
			.filter(target -> target.isChannel())
			.toArray(Channel[]::new);
	}
}
