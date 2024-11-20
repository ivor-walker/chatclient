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
		//Only noticeable pattern is that the message code has capital letters in it 
		String regex = "[A-Z_]+"
		Matcher matcher = Pattern.compile(regex).matcher(message);
		String messageCode = matcher.group();
		String messageContent = message.replaceFirst(messageCode, "");
		messageContent = messageContent.replaceFirst(":", "");
	
		switch (messageCode) {
			case "REPLY_NICK":
				recieveNickname(messageContent);
			case "REPLY_TIME":
				recieveTime(messageContent);
			case "JOIN":
				recieveJoin(messageContent);		
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
	private HashMap<String, String[]> participatedChannels = new HashMap<>();
	//private HashMap<String, String[]> offeredChannels = new HashMap<>();


	//Join channel
	public void joinChannel(String channel) {
		throughSocket(e -> {
			writer.println("JOIN " + channel);	
		});

		getNamesInChannel(channel).thenAccept(users -> {
			participatedChannels.put(channel, users);
		)}
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
		
		participatedChannels.remove(channel);
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
	public void privateMessage(String target, String message) {
		throughSocket(e -> {
			writer.println("PRIVMSG " + target + ":" + message);
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
