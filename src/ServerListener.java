/**
 * Interface for serverController to listen to a serverModel
 */
public interface ServerListener {
    // When the serverModel encounters an error
    void onError(String error);
}
