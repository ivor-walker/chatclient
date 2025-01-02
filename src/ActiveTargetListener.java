/**
 * Listener interface for classes that need to listen to changes in the active target (i.e messageController)
 */

public interface ActiveTargetListener {
    // Listen for a new active target
    void onSetActiveTarget(Target activeTarget);
    // Listen for a new message in current active target
    void onMessageRecieved(Message message);
}
