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

	//Listeners for server replies only
	private List<MessageListener> listeners = new ArrayList<>();
	public interface Listener {
		void onQuit();
		void onQuit(String usernameWhoQuit);

		void onJoinChannel(Channel channel);
		void onJoinChannel(String usernameWhoQuit, Channel channel);

		void onPartChannel(Channel channel);
		void onPartChannel(String usernameWhoQuit, Channel channel);

		void onMessage(String nickname, <T> target, Message message);

		void onInfo();
		
		void onPong();
		
		void onError();
	}

	public void addListener(Listener listener) {
		listeners.add(listener);
	}	
	
	public void addListener(Listener listener) {
		listeners.remove(listener);
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
			throw new Exception("Unknown connection exception. " + e.getMessage());
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

	private HashMap<String, ?> targets = new HashMap<>();

	private void connect(String host, int port, String nickname) {
		throughSocket(e -> {
			connection = new Socket(host, port);
			this.host = host;
			this.port = port;
			
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			writer = new PrintWriter(connection.getOutputStream(), true);
			
			listenForMessages();
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
	
	private int TARGET_POSITION = 2;
	private int MESSAGE_POSITION = 3;

	//TODO refactor
	private void handleServerMessage(serverMessage) { 
		//Server notifications	
		if(message.startsWith(":")) {	
			String filteredMessage = message.replaceFirst(":", "");
			String[] splitMessage = String.split(filteredMessage, " ");

			String nickname = splitMessage[0];
			String messageCode = splitMessage[1];
			
			String target;
			if(splitMessage.length > TARGET_POSITION) {
				target = splitMessage[TARGET_POSITION];
			}

			String message;
			if(splitMessage.length > MESSAGE_POSITION) {
				String[] splitMessageContent = Arrays.copyOfRange(splitMessage, MESSAGE_POSITION, splitMessage.length);
				message = String.join(splitMessageContent, " "); 
				
			}

			String message = message.replaceFirst(messageCode, "");
			message = message.replaceFirst(":", "");
		
			boolean refersSelf = this.nickname == nickname
			if(refersSelf) {	
				switch(messageCode) {
					case "QUIT":
						recieveQuit();
						return;
					case "JOIN":
						recieveJoinChannel(target);
						return;
					case "PART":
						recievePartChannel(target);
						return;
					case "PRIVMSG":
						recieveMessage(target, message);
						return;
				}
			} else {
				switch(messageCode) {
					case "QUIT":
						recieveQuit(nickname);
						return;
					case "JOIN":
						recieveJoinChannel(nickname, target);
						return;
					case "PART":
						recievePartChannel(nickname, target);
						return;
					case "PRIVMSG":
						recieveMessage(nickname, target, message);
						return;

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
			messageContent = serverMessage.replaceFirst(":", "");
			
			switch (messageCode) {
				case "REPLY_NAMES":
					recieveNamesInChannel(messageContent);
					return;
				case "REPLY_NICK":
					recieveNickname(messageContent);
					return;
				case "REPLY_LIST":
					recieveOfferedChannels(messageContent);
					return;
				case "REPLY_TIME":
					recieveTime(messageContent);
					return;
				case "REPLY_INFO":
					recieveInfo(messageContent);
					return;
				case "PONG":
					recievePong(messageContent);
					return;
				case "ERROR":
					recieveError(messageContent);
					return;
			}
		}
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
		
		recieveQuit(this.nickname, false);
		
		for(Listener listener : listeners) {
			listener.onQuit();
		}	
	}

	private void recieveQuit(String nickname) {
		recieveQuit(nickname, true);	
	}

	private void recieveQuit(String nickname, boolean addListeners) {
		targets.remove(nickname);
		
		if(addListeners) {
			for(Listener listener : listeners) {
				listener.onQuit(nickname);
			}
		}
	}

	
	//Join channel
	public void joinChannel(String channel) {
		throughSocket(e -> {
			writer.println("JOIN " + channel);	
		});
	}

	private void recieveJoinChannel(String channelName) {
		getNamesInChannel(channelName).thenAccept(users -> {
			Channel channel = new Channel(channelName, users);
			targets.add(channel.getName(), channel);	
			
			for(Listener listener : listeners) {
				listener.onJoinChannel(channel);
			}
		});
	}

	private void recieveJoinChannel(String nickname, String channelName) {
		Channel channel = targets.get(channelName);
		channels.add(nickname, channel);

		for(Listener listener : listeners) {
			listener.onJoinChannel(nickname, channel);
		}
	}
	
	//Leave channel
	public void leaveChannel(String channel) {
		throughSocket(e -> {
			writer.println("PART " + channel);
		});
	}
	
	private void recievePartChannel(String channelName) {
		targets.remove(channelName);
		
		for(Listener listener : listeners) {
			listener.onPartChannel(channel);
		}	
	}

	private void recievePartChannel(String nickname, String channelName) {
		Channel channel = targets.get(channelName);
		channel.removeUser(nickname);	
	
		for(Listener listener : listeners) {
			listener.onPartChannel(channel);
		}	
	}
	
	//Messages
	public void sendMessage(String target, String messageContent) {
		throughSocket(e -> {
			writer.println("PRIVMSG " + target + ":" + messageContent);
		});
	}

	private void recieveMessage(String sender, String target, String messageContent) {
		getServerTime().thenAccept(serverTime -> {
			Message message = new Message(sender, target, messageContent, serverTime);
			Target target = targets.get(message.target); 

			if(target == null) {
				target = new Target(message.target);
				targets.add(target);
			}

			target.addMessage(message);

			for(Listener listener : listeners) {
				listener.onMessageRecieved(sender, target, Message);
			}
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
	
	private void recieveNamesInChannel(String usersString) {
		String[] users = String.split(usersString, " ");
		if(namesFuture != null && !namesFuture.isDone()) {
			namesFuture.complete(users);
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
			nicknameFuture.complete(messageContent);
		}
	}
	
	public CompletableFuture<String> getNickname() {
		return nicknameFuture;
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

	//Miscellaneous
	public void getInfo() {
		throughSocket(e -> {
			writer.println("INFO");
		});
	}

	private void recieveInfo(String messageContent) {
		for(Listener listener : listeners) {
			listener.onInfo(messagContent);
		}	
	}

	public void ping(String messageContent) {
		throughSocket(e -> {
			writer.println("PING " + messageContent);
		});
	}

	
	private void recievePong(String messageContent) {
		for(Listener listener : listeners) {
			listener.onPong(messagContent);
		}
	
	}
	
	//Error listening
	private void recieveError(String messageContent) {
		String errorMessage = "Server response: " + messageContent	
		throw new Exception(errorMessage);	
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

	//Getters of targets
	public Target[] getUsers() {
		return targets.stream()
			.filter(target -> !(target.isChannel()))
			.toArray(Target[]::new);
	}

	public Channel[] getChannels() {
		return targets.stream()
			.filter(target -> target.isChannel())
			.toArray(Channel[]::new);
	}
	
}
