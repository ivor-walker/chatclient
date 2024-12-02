import java.awt.event.ActionListener;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * The ServerController class manages interactions between the user interface (ServerPanel) and the server models.
 * It handles server connection management, active server switching, and shutdown cleanup.
 */
public class ServerController implements ServerListener {
    // A map storing all server models, keyed by their string representation.
    private HashMap<String, ServerModel> serverModels = new HashMap<>();
    
    // The currently active server model.
    public ServerModel activeServerModel;
    
    // A string representation of the currently active server.
    private String activeServerString;
    
    // The view (UI panel) associated with this controller.
    private ServerPanel view;

    /**
     * Constructs a ServerController and sets up event listeners for the associated view.
     * 
     * @param view The ServerPanel instance that this controller will manage.
     */
    public ServerController(ServerPanel view) {
        this.view = view;
        setupListeners(); // Initialize the listeners for view actions.
    }

    /**
     * Sets up listeners for view events and adds a shutdown hook for cleanup on application exit.
     */
    private void setupListeners() {
        // Listener for creating a new server.
        view.viewNewServerListener(e -> viewNewServer());

        // Listener for committing the creation of a new server.
        view.commitNewServerListener(e -> commitNewServer());

        // Add a shutdown hook to disconnect all servers on application exit.
        addShutdownHook();
    }

    /**
     * Adds a shutdown hook to disconnect from all servers when the application exits.
     */
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Iterate through all server models and disconnect them.
            for (String serverKey : serverModels.keySet()) {
                serverModels.get(serverKey).disconnect();
            }
        }));
    }

    /**
     * Prepares the UI for creating a new server by displaying the server form.
     */
    private void viewNewServer() {
        view.setupServerForm(); // Display the form for setting up a new server.
        
        // Reattach the commit listener to handle new server submissions.
        view.commitNewServerListener(e -> commitNewServer());
    }

    /**
     * Creates a new server model from the form data in the view and connects to the server.
     * Adds the new server to the list of managed servers and updates the view.
     */
    private void commitNewServer() {
        // Create a new server model from the current view's input.
        ServerModel newServerModel = modelFromView();

        // Attempt to connect to the new server.
        newServerModel.connect().thenRun(() -> {
            // Get a string representation of the new server.
            String serverString = newServerModel.toString();

            // Add the server to the view and internal data structures.
            view.addServer(serverString);
            serverModels.put(serverString, newServerModel);

            // Set up a listener to view the existing server when clicked.
            view.viewExistingServerListener(serverString, e -> viewExistingServer(serverString));

            // Notify the user of the successful addition.
            view.setConnectionResult("Server added successfully!");
            System.out.println("ServerController: new server added " + serverString);

            // Set the new server as the active server and update the controller.
            setActive(newServerModel);
            addToController(newServerModel);
        });
    }

    /**
     * Prepares the UI for viewing an existing server's details.
     * 
     * @param serverString The string representation of the server to view.
     */
    private void viewExistingServer(String serverString) {
        // Retrieve the server model from the map.
        ServerModel serverModel = serverModels.get(serverString);

        // Populate the server form with the server's details.
        view.setupServerForm(serverModel.getHost(), serverModel.getPort(), serverModel.getNickname());

        // Set the selected server as the active server.
        setActive(serverModel);

        // Attach a listener for removing the existing server.
        view.removeExistingServerListener(e -> removeExistingServer());
    }
	
    private void removeExistingServer() {
		if(activeServerModel == null) {
			return;
		}	
		activeServerModel.disconnect().thenRun(() -> {
			view.removeServer(activeServerString);
			serverModels.remove(activeServerString);
			unsetActive();
		});	
	}

    /**
     * Creates a new ServerModel based on the data from the view's server form.
     * 
     * @return A new ServerModel instance with the input details.
     */
    private ServerModel modelFromView() {
        // Retrieve server details from the view.
        String host = view.getHost();
        String nickname = view.getNickname();
        String port = view.getPort();
    
        // Convert the port from String to int, handling empty input gracefully.
        int portInt = 0;
        if (!port.equals("")) {
            portInt = Integer.parseInt(port);
        }
    
        // Create a new ServerModel with the input data.
        ServerModel model = new ServerModel(host, portInt, nickname);
    
        // Add this controller as a listener for the server model's events.
        model.addServerListener(this);
    
        // Return the constructed server model.
        return model;
    }
    
    /**
     * Adds a ServerModel to the controller's list of managed servers.
     * 
     * @param model The ServerModel to add.
     */
    private void addToController(ServerModel model) {
        // Use the string representation of the model as the key in the serverModels map.
        serverModels.put(model.toString(), model);
    }
    
    /**
     * Sets the specified server as the active server and updates the view.
     * Notifies listeners of the active server change.
     * 
     * @param serverModel The ServerModel to set as active. Pass null to unset active server.
     */
    private void setActive(ServerModel serverModel) {
        // Update the active server string based on the server model.
        activeServerString = serverModel != null ? serverModel.toString() : null;
    
        if (activeServerModel == null) {
            // If there was no previously active server, just set the new one as active.
            view.setActive(activeServerString);
        } else {
            // Notify the view to change the active server from the old one to the new one.
            String oldServerString = activeServerModel.toString();
            view.changeActive(oldServerString, activeServerString);
        }
    
        // Update the active server model reference.
        activeServerModel = serverModel;
    
        // Notify all registered listeners about the active server change.
        for (ActiveServerListener activeListener : activeListeners) {
            activeListener.onSetActiveServer(serverModel);
        }
    
        // Log the change for debugging purposes.
        System.out.println("ServerController: active server changed to " + activeServerString);
    }
    
    /**
     * Unsets the current active server by passing null to the setActive method.
     */
    private void unsetActive() {
        setActive(null);
    }
    
    /**
     * Retrieves the currently active server model.
     * 
     * @return The currently active ServerModel, or null if none is set.
     */
    public ServerModel getActive() {
        return this.activeServerModel;
    }
    
    /**
     * Handles error messages and displays them in the view's connection result area.
     * 
     * @param errorMessage The error message to display.
     */
    public void onError(String errorMessage) {
        // Set the connection result in the view to display the error message.
        view.setConnectionResult(errorMessage);
    }
    
    // A list of listeners to be notified when the active server changes.
    private List<ActiveServerListener> activeListeners = new ArrayList<>();
    
    /**
     * Registers a new listener to be notified of active server changes.
     * 
     * @param activeListener The listener to add.
     */
    public void addActiveListener(ActiveServerListener activeListener) {
        // Add the listener to the activeListeners list.
        activeListeners.add(activeListener);
    }
}	 
