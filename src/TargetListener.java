public interface TargetListener {
    void onQuit(String usernameWhoQuit);
    
    void onJoinChannel(String usernameWhoJoined, Channel channel);
    
    void onPartChannel(String usernameWhoQuit, Channel channel);
    
    void onMessageRecieved(Message message);
}
