/**
 * Interface for listening to changes in a given target (i.e for messageController)
 */
public interface TargetListener {
    // A user quits the server
    void onQuit(String usernameWhoQuit);
    // A user joins a channel in the server
    void onJoinChannel(String usernameWhoJoined, Channel channel);
    // A user leaves a channel in the server 
    void onPartChannel(String usernameWhoQuit, Channel channel);
    // A target receives a message 
    void onMessageRecieved(Message message);
}
