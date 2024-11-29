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

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.NoRouteToHostException;
import java.net.ConnectException;

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
	
	public interface MessageListener {
		void onQuit(String usernameWhoQuit);

		void onJoinChannel(String usernameWhoJoined, Channel channel);

		void onPartChannel(String usernameWhoQuit, Channel channel);

		void onMessageRecieved(String nickname, Object target, Message message);
		
		void onError(String error);
	}
	
	public void addMessageListener(MessageListener listener) {
		messageListeners.add(listener);
	}

	//TODO implement serverlistener for onError()

	public interface VoidCallable {
		void call() throws Exception;
	}
	
	private void throughSocket(VoidCallable action) {
		System.out.println("TO | " + LocalDateTime.now().toString() + " | " + toString() + " | " + getCallerInfo());
	
		try {
			action.call();
		} catch (Exception e) {
			e.printStackTrace();
			// Handle all exceptions in one block
			if (e instanceof IllegalArgumentException) {
				throw new RuntimeException("[400] Bad Request: Invalid port number. Port must be between 0 and 65535.");
			} else if (e instanceof NullPointerException) {
				throw new RuntimeException("[400] Bad Request: Host cannot be null.");
			} else if (e instanceof UnknownHostException) {
				throw new RuntimeException("[404] Not Found: Host not found. Check the spelling of the host or your network configuration.");
			} else if (e instanceof NoRouteToHostException) {
				throw new RuntimeException("[403] Forbidden: No route to host. Check your firewall or network settings.");
			} else if (e instanceof ConnectException) {
				throw new RuntimeException("[403] Forbidden: Connection refused by server. Verify the port or server configuration.");
			} else if (e instanceof SecurityException) {
				throw new RuntimeException("[403] Forbidden: Permission denied. Check server security settings.");
			} else if (e instanceof BindException) {
				throw new RuntimeException("[503] Service Unavailable: Port is already in use. Please try a different port.");
			} else if (e instanceof SocketException) {
				throw new RuntimeException("[500] Internal Server Error: Connection issue with the server.");
			} else if (e instanceof EOFException) {
				throw new RuntimeException("[500] Internal Server Error: End of stream encountered.");
			} else if (e instanceof IOException) {
				throw new RuntimeException("Unknown I/O error occurred: " + e.getMessage());
			} else if (e instanceof RuntimeException) {
				throw new RuntimeException("Server responded with error message: " + e.getMessage());
			} else {
				throw new RuntimeException("Unknown error: " + e.getMessage());
			}
		}
	}

	//Initialise and connect to a new server
	public ServerModel(String host, int port, String nickname) {
		this(host, port, nickname, true);	
	}

	public ServerModel(String host, int port, String nickname, boolean initialConnect) {
		if(initialConnect) {
			connect(host, port, nickname);
		}
	}

	private HashMap<String, Target> targets = new HashMap<>();

	private CompletableFuture<Void> connectFuture = new CompletableFuture<>();

	private CompletableFuture<Void> connect(String host, int port, String nickname) {
		try {	
			throughSocket(() -> {
				connection = new Socket(host, port);
				this.host = host;
				this.port = port;
				
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				writer = new PrintWriter(connection.getOutputStream(), true);
				
				listenForMessages();
				sendNickname(nickname);
			});

			connectFuture.complete(null);

		} catch (Exception e) {
			connectFuture.completeExceptionally(e);
		} finally {
			return connectFuture;
		}
	}

	public CompletableFuture<Void> getConnectFuture() {
		return connectFuture;
	}	

	//Waiting for and handling server messages	
	private void listenForMessages() {
		new Thread(() -> {
			while(true) {
				try {
					throughSocket(() -> {	
						String message;
						while((message = reader.readLine()) != null) {
							handleServerMessage(message);
						}	
					});
				} catch (RuntimeException e) {
					System.out.println("ERROR | " + e.getMessage());	
					for(MessageListener messageListener : messageListeners) {
						messageListener.onError(e.getMessage());
					}
				}
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
			
			
			if(refersToSelf) {	
				switch(messageCode) {
					case "QUIT":
						onQuit();
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
	private CompletableFuture<Void> disconnectFuture = new CompletableFuture<>();

	public CompletableFuture<Void> disconnect() {
		throughSocket(() -> {
			writer.println("QUIT");
			
		});

		return disconnectFuture;
	}

	private void onQuit() {
		throughSocket(() -> {
			writer.close();
			reader.close();
			connection.close();
		});
		
		onQuit(this.nickname, false);
		
		if(disconnectFuture != null && !disconnectFuture.isDone()) {
			disconnectFuture.complete(null);
		}
	}

	private void onQuit(String nickname) {
		onQuit(nickname, true);	
	}

	private void onQuit(String nickname, boolean tellListeners) {
		targets.remove(nickname);
		
		if(tellListeners) {
			for(MessageListener listener : messageListeners) {
				listener.onQuit(nickname);
			}
		}
	}
	
	//Join channel
	private CompletableFuture<Channel> joinChannelFuture = new CompletableFuture<>();

	public CompletableFuture<Channel> joinChannel(String channel) {
		throughSocket(() -> {
			writer.println("JOIN " + channel);	
		});
		
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
	private CompletableFuture<Void> partChannelFuture = new CompletableFuture<>();	
	
	public CompletableFuture<Void> partChannel(String channel) {
		throughSocket(() -> {
			writer.println("PART " + channel);
		});

		return partChannelFuture;
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
		System.out.println("HERE | " + target + " | " + messageContent);
	
		throughSocket(() -> {
			writer.println("PRIVMSG " + target + " :" + messageContent);
		});
	}

	private void onMessage(String sender, String targetName, String messageContent) {
		getServerTime().thenAccept(serverTime -> {
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
	private CompletableFuture<String[]> namesFuture = new CompletableFuture<>();

	public CompletableFuture<String[]> getNamesInChannel(String channel) {
		throughSocket(() -> {
			writer.println("NAMES " + channel);
		});

		return namesFuture;
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
		
		//TODO remove once issue is diagnosed	
		System.out.println(messageContent + splitMessageContent + channelName + users);	
		channel.overwriteUsers(users);
	
		if(namesFuture != null && !namesFuture.isDone()) {
			namesFuture.complete(users);
		}	
	}

	//Nicknames
	private CompletableFuture<String> nicknameFuture = new CompletableFuture<>();

	public CompletableFuture<String> sendNickname(String nickname) {
		throughSocket(() -> {
			writer.println("NICK " + nickname);
		});

		return nicknameFuture;
	}
	
	private void onNickname(String messageContent) {
		String nickname = messageContent.split(", ")[1];

		this.nickname = nickname;

		if(nicknameFuture != null && !nicknameFuture.isDone()) {
			nicknameFuture.complete(messageContent);
		}
	}
	
	public CompletableFuture<String> getNicknameFuture() {
		return nicknameFuture;
	}

	//Get all channels	
	private CompletableFuture<String[]> channelsFuture = new CompletableFuture<>();

	public CompletableFuture<String[]> getOfferedChannels() {
		throughSocket(() -> {
			writer.println("LIST");
		});

		return channelsFuture;
	}
	
	private void onOfferedChannels(String messageContent) {
		String[] channels = messageContent.split(" ");

		if(channelsFuture != null && !channelsFuture.isDone()) {
			channelsFuture.complete(channels);
		}	
	}

	//Getting current server time	
	private CompletableFuture<String> timeFuture = new CompletableFuture<>();

	public CompletableFuture<String> getServerTime() {
		throughSocket(() -> {
			writer.println("TIME");
		});

		return timeFuture; 
	}

	private void onTime(String messageContent) {
		if(timeFuture != null && !timeFuture.isDone()) {
			timeFuture.complete(messageContent);
		}	
	}

	//Get info
	private CompletableFuture<String> infoFuture = new CompletableFuture<>();
	
	public CompletableFuture<String> getInfo() {
		throughSocket(() -> {
			writer.println("INFO");
		});
	
		return infoFuture;
	}

	private void onInfo(String messageContent) {
		if(infoFuture != null && !infoFuture.isDone()) {
			infoFuture.complete(messageContent);
		}
	}

	//Ping-pong	
	private CompletableFuture<String> pongFuture = new CompletableFuture<>();
	
	public CompletableFuture<String> ping(String messageContent) {
		throughSocket(() -> {
			writer.println("PING " + messageContent);
		});

		return pongFuture;
	}

	private void onPong(String messageContent) {
		if(pongFuture != null && !pongFuture.isDone()) {
			pongFuture.complete(messageContent);
		}
	}
	
	//Error listening
	private void onError(String messageContent) {
		throw new RuntimeException(messageContent);	
	}

	//Getters of connection info
	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;	
	}
	
	public String toString() {
		return host + ":" + Integer.toString(port); 
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
