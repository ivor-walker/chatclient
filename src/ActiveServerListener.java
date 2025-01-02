/**
 * Listener interface for classes that need to listen for changes to active server
 */

public interface ActiveServerListener {
    // Listen for a new active server
    void onSetActiveServer(ServerModel activeModel);
}
