public interface MessageListener {
	void onQuit(String usernameWhoQuit);

	void onJoinChannel(String usernameWhoJoined, Channel channel);

	void onPartChannel(String usernameWhoQuit, Channel channel);

	void onMessageRecieved(String nickname, Object target, Message message);
	
	void onError(String error);
}
