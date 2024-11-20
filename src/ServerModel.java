public class ServerModel {
	private Socket connection;
	private BufferedReader reader;
	private PrintWriter writer;
	
	private String nickname;
	
	private boolean isActive

	public void setActivity(boolean activityState) {
		this.isActive = activityState;	
	}

	public boolean getActivity() {
		return isActive;
	}		
		
	private throughSocket(Consumer<T> action) {
		try {
			action
		}
		//Client input errors 
		catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("[400] Bad Request: Invalid port number. Port must be between between 0 and 65535, inclusive.");
		} catch (NullPointerException e) {
			throw new NullPointerException("[400] Bad Request: Host cannot be null.");

		//Client connection errors (400)	
		} catch (UnknownHostException e) {
			throw new UnknownHostException("[404] Not Found: Host not found. Check the spelling of the host, your internet connection, and your network configuration.");
		} catch (NoRouteToHostException e) {
			throw new NoRouteToHostException("[403] Forbidden: Host found, but is unreachable. Check your firewall. If entering an IP address, double check its spelling. Otherwise, please contact the server's administrator.");
		} catch (ConnectException e) {
			throw new ConnectException("[403] Forbidden: Host found, but connection refused by server. Double check your port number is correct. Otherwise, please contact the server's administrator and ask them to open this port.");
		} catch (SecurityException e) {
			throw new SecurityException("[403] Forbidden: Permission to connect denied. If you believe this is in error, please contact the server's administrator and ask them to update their security manager.");

		//Server connection errors (500)
		} catch (BindException e) {
			throw new BindException("[503] Service Unavailable: Port is already in use at this server. Please try a different port.");
		} catch (SocketException e) {
			throw new SocketException("[500] Internal Server Error: Server has closed or reset the connection.");
		} catch (EOFException e) {
			throw new EOFException("[500] Internal Server Error: Server has closed the stream.");
		}

		//Unknown errors
		} catch (IOException e) {
			throw new IOException("Unkown IO exception. " + e.getMessage());
		} catch (e) {
			throw new Exception("Unknown exception. " + e.getMessage());
		}
	}

	public ServerModel(String host, int port, String nickname) {
		this(host, port, nickname, true);	
	}

	public ServerModel(String host, int port, String nickname, boolean initialConnect) {
		if(initialConnect) {
			connect(host, port, nickname);
		}
	}

	private void connect(String host, int port, String nickname) {
		throughSocket(e -> {
			connection = new Socket(host, port);
			this.host = host;
			this.port = port;
			
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			listenForMessages();

			writer = new PrintWriter(connection.getOutputStream(), true);
			sendNickname(nickname);
		});
	}
	
	//Waiting for and handling server messages	
	private void listenForMessages() {
		new Thread(e -> {
			throughSocket(f -> {	
				String message;
				while((message = reader.readLine()) != null) {
					handleServerMessage(message);
				}	
			});
		});
	}
	
	private void handleServerMessage(message) { 
		//Special cases
		if(message.startsWith(":")) {	
			//TODO extract messagecode from special instances
			String[] splitMessage = String.split(message, " ");
			String nickname = splitMessage[0];
			String messageCode = splitMessage[1];
			String messageContent = message.replaceFirst(messageCode, "");
			messageContent = messageContent.replaceFirst(":", "");

			switch(messageCode) {
				case "QUIT":
					recieveQuit();
					return;
				case "JOIN":
					recieveJoin(splitMessage[0], splitMessage[2]);
					return;
				case "PART":
					recievePart(splitMessage[0], splitMessage[2]);
					return;
				case "PRIVMSG":
					recieveMessage(splitMessage[0], splitMessage[2]);
					return;
			}
		}
	
		//Only noticeable pattern is that the message code has capital letters in it 
		String regex = "[A-Z_]+";
		Matcher matcher = Pattern.compile(regex).matcher(message);
		
		List<String> messageCodes = new ArrayList<>();
		while (matcher.find()) {
			messageCodes.add(matcher.group());
		}

		for (String messageCode : messageCodes) {
			String messageContent = message.replaceFirst(messageCode, "");
			messageContent = messageContent.replaceFirst(":", "");
			
			switch (messageCode) {
				case "REPLY_NAMES":
					recieveUsersInChannel(messageContent);
				case "REPLY_LIST":
					recieveOfferedChannels(messageContent);
				case "REPLY_TIME":
					recieveTime(messageContent);
				case "REPLY_INFO":
					recieveInfo(messageContent);
				case "PONG":
					recievePong(messageContent);
				case "ERROR":
					recieveError(messageContent);
			}
		}
	}

	//Nicknames
	private CompletableFuture<String> nicknameFuture = new CompletableFuture<>();

	public void sendNickname(String nickname) {
		throughSocket(e -> {
			writer.println("NICK " + nickname);
		});
	}
	
	private void recieveNickname(String messageContent) {
		String nickname = messageContent.split(", ")[1];

		this.nickname = nickname;

		if(nicknameFuture != null && !nicknameFuture.isDone()) {
			nicknameFuture.complete(nickname);
		}
	}
	
	public CompletableFuture<String> getNickname() {
		return nicknameFuture;
	}

	//Disconnect
	public void disconnect() {
		throughSocket(e -> {
			writer.println("QUIT");
			
		});
	}

	private void recieveQuit() {
		throughSocket(e -> {
			writer.close();
			reader.close();
			socket.close();
		});
	}

	//Getting current server time	
	private CompletableFuture<String> timeFuture = new CompletableFuture<>();

	public void getServerTime() {
		throughSocket(e -> {
			writer.println("TIME");
		});
		return timeFuture; 
	}

	private void recieveTime(String messageContent) {
		if(timeFuture != null && !timeFuture.isDone()) {
			timeFuture.complete(messageContent);
		}	
	}

	//Channels
	private HashMap<String, ?> targets = new HashMap<>();
	//private HashMap<String, String[]> offeredChannels = new HashMap<>();

	//Join channel
	public void joinChannel(String channel) {
		throughSocket(e -> {
			writer.println("JOIN " + channel);	
		});
	}

	private void recieveJoinChannel(String messageContent) {
		String channelName = //TODO

		getNamesInChannel(channelName).thenAccept(users -> {
			Channel channel = new Channel(channelName, users);
			targets.add(channel.getName(), channel);	
		});
	}
	
	//Get names from channel	
	private CompletableFuture<String> namesFuture = new CompletableFuture<>();

	public String[] getNamesInChannel(String channel) {
		throughSocket(e -> {
			writer.println("NAMES " + channel);
		});

		return namesFuture;
	}
	
	private void recieveNamesInChannel(String messageContent) {
		String[] users = String.split(messageContent, " ");
		if(namesFuture != null && !namesFuture.isDone()) {
			namesFuture.complete(users);
		}	
	}

	//Leave channel
	public void leaveChannel(String channel) {
		throughSocket(e -> {
			writer.println("PART " + channel);
		});
	}
	
	private void recieveLeaveChannel(String messageContent) {
		String channel = processLeaveChannel(messageContent);	
		targets.remove(channel);
	}
	
	private String processLeaveChannel(String messageContent) {
		//TODO		
		String channel;	
		return channel;
	}

	//Get all channels	
	private CompletableFuture<String> channelsFuture = new CompletableFuture<>();

	public String[] getOfferedChannels() {
		throughSocket(e -> {
			writer.println("LIST");
		});
		return channelsFuture;
	}
	
	private void recieveOfferedChannels(messageContent) {
		String[] channels = String.split(messageContent, " ");
		if(namesFuture != null && !namesFuture.isDone()) {
			channelsFuture.complete(channels);
		}	
	}
	
	//Messages
	public void sendMessage(String target, String message) {
		throughSocket(e -> {
			writer.println("PRIVMSG " + target + ":" + message);
		});
	}

	private void recieveMessage(String messageContent) {
		getServerTime().thenAccept(serverTime -> {
			Message message = new Message(messageContent, serverTime);
			Target target = targets.get(message.target); 

			if(target == null) {
				target = new Target(message.target);
				targets.add(target);
			}

			target.addMessage(message);
		});
	}
	
	//Miscellaneous
	public void getInfo() {
		throughSocket(e -> {
			writer.println("INFO");
		});
	}

	public void ping(String message) {
		throughSocket(e -> {
			writer.println("PING " + message);
		});
	}

	

	

	//Getters of connection info
	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;	
	}
	
	
	private String toString() {
		return host + ":" + toString(port); 
	}
}

private class Message {
	private String sender;
	private String target;
	private String message;
	private LocalDateTime serverTime;
	private LocalDateTime clientTime;

	private Message(String messageContent, String serverTime) {
		//TODO
		
		this.sender = sender;
		this.target = target;
		this.message = message;

		this.serverTime = LocalDateTime.parse(serverTime);
		this.clientTime = LocalDateTime.now();
	}

	DateTimeFormatter userFriendlyFormat = DateTimeFormatter.ofPattern("E dd-MM-yyyy HH:mm:ss");
	public toString() {
		String userFriendlyClientTime = clientTime.format(userFriendlyFormat);
		return "[" + userFriendlyClientTime + "] " + sender + ": " + message;
	}

	public LocalDateTime getServerTime() {
		return this.serverTime;
	}
}

private class Target {
	private String name;
	private List<Message> messages = new ArrayList<>();

	public Target(String name) {
		this.name = name;
	}	

	public Target(String name, List<String> users) {
		this = new Channel(name, users);	
	}

	public List<Message> getMessages() {
		sortMessages();			
		return messages;
	}

	public LocalDateTime getServerTimeOfLastMessage() {
		sortMessages();
		return messages[messages.length].getServerTime();
	}

	public void sortMessages() {
		messages.sort(
			Comparator.comparing(Message::getServerTime)
		);
	}

	public void addMessage(Message message) {
		messages.add(message);
	}
	
	public String getName() {
		return name;
	}
}

private class Channel extends Target {
	private List<String> users = new ArrayList<>();

	public Channel(String name, List<String> users) {
		super(name);
		overwriteUsers(users);	
	}
	
	public List<String> getUsers() {
		return users;
	}

	public void addUser(String username) {
		users.add(username);
	}
	
	public void removeUser(String username) {
		users.remove(username);
	}

	public void overwriteUsers(List<String> users) {
		this.users = users;	
	}
}
