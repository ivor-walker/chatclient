import java.awt.event.ActionListener;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Controller for server panel
 * Handles server creation, deletion, and selection
 */
public class ServerController implements ServerListener {
    // Data structure saving all server models 
    private HashMap<String, ServerModel> serverModels = new HashMap<>();

    // Active server model
    public ServerModel activeServerModel;
    private String activeServerString;

    private ServerPanel view;

    /**
     * Constructor
     * @param view ServerPanel view
     */
    public ServerController(ServerPanel view) {
        this.view = view;

        // Add listeners to view
        setupListeners(); 
    }

    /**
     * Set up listeners for view
     */
    private void setupListeners() {
        // Add listener for new server button
        view.viewNewServerListener(e -> viewNewServer());

        // Add listener for commit new server button
        view.commitNewServerListener(e -> commitNewServer());

        // Add listener for client exiting program
        addShutdownHook();
    }

    /**
     * Add shutdown hook to disconnect all servers when client exits
     */
    private void addShutdownHook() {
        // On shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // For each server key
            for (String serverKey : serverModels.keySet()) {
                // Get server model and disconnect
                serverModels.get(serverKey).disconnect();
            }
        }));
    }

    /**
     * View new server form
     */
    private void viewNewServer() {
        // Set up server form for a blank server
        view.setupServerForm(); 

        // Add listener for adding a new server 
        view.commitNewServerListener(e -> commitNewServer());
    }

    /**
     * Add a new server
     */
    private void commitNewServer() {
        // Retrieve server model from user's inputs in view
        ServerModel newServerModel = modelFromView();

        // Attempt to connect to server
        newServerModel.connect().thenRun(() -> {
            // Add server to controller 
            String serverString = newServerModel.toString();
            serverModels.put(serverString, newServerModel);
            
            // Add server to view (i.e to list of existing servers)
            view.addServer(serverString);
            
            // Add a listener for viewing this server's details 
            view.viewExistingServerListener(serverString, e -> viewExistingServer(serverString));

            // Set this server as active
            setActive(newServerModel);
            
            // Add new server to controller's data structures
            addToController(newServerModel);

            // Inform user that server was added successfully
            view.setConnectionResult("Server added successfully!");
        });
    }

    /**
     * Views an existing server
     * Happens when user selects an existing server
     * @param serverString String representation of requested server
     */
    private void viewExistingServer(String serverString) {
        // Retrieve server model from server string
        ServerModel serverModel = serverModels.get(serverString);
        
        // Set up server form with server's details
        view.setupServerForm(serverModel.getHost(), serverModel.getPort(), serverModel.getNickname());

        // Set selected server as active 
        setActive(serverModel);

        // Overwrite existing "add server" listener with "remove server" listener
        view.removeExistingServerListener(e -> removeExistingServer());
    }

    /**
     * Removes active server
     */
    private void removeExistingServer() {
        // If no active server, return
		if(activeServerModel == null) {
			return;
		}	

        // Attempt disconnect from active server
		activeServerModel.disconnect().thenRun(() -> {
            // Remove server from view
			view.removeServer(activeServerString);

            // TODO add seperate method for removing server from controller, complementing addToController
            // Remove server from controller's data structures
			serverModels.remove(activeServerString);

            // Unset active server
			unsetActive();
		});	
	}


    /**
     * Converts view inputs to server model
     * @return ServerModel object
     */
    private ServerModel modelFromView() {
        // Retrieve host, nickname, and port from view
        String host = view.getHost();
        String nickname = view.getNickname();
        String port = view.getPort();

        // Convert port to integer
        int portInt = 0;
        if (!port.equals("")) {
            portInt = Integer.parseInt(port);
        }

        // Create server model
        ServerModel model = new ServerModel(host, portInt, nickname);

        // Add controller as listener to new server model
        model.addServerListener(this);

        return model;
    }

    /**
     * Add a serverModel to all controller data structures
     * @param model ServerModel to add
     */
    private void addToController(ServerModel model) {
        // Add server to serverModels
        serverModels.put(model.toString(), model);
    }

    /**
     * Remove a serverModel from all controller data structures
     * @param model ServerModel to remove
     */
    private void removeFromController(ServerModel model) {
        // Remove server from serverModels
        serverModels.remove(model.toString());
    } 

    /**
     * Set active server
     * @param serverModel ServerModel to set as active
     */
    private void setActive(ServerModel serverModel) {
        // Convert server model to string 
        activeServerString = serverModel != null ? serverModel.toString() : null;

        // If no active server, simply set new server as active in view 
        if (activeServerModel == null) {
            view.setActive(activeServerString);
        // Otherwise, change active server in view
        } else {
            String oldServerString = activeServerModel.toString();
            view.changeActive(oldServerString, activeServerString);
        }

        // Set active server model
        activeServerModel = serverModel;

        // Inform all active listeners of active server change
        for (ActiveServerListener activeListener : activeListeners) {
            activeListener.onSetActiveServer(serverModel);
        }
    }

    /**
     * Unset active server
     */
    private void unsetActive() {
        setActive(null);
    }


    /**
     * Getter for current active server
     * @return Currently active ServerModel object
     */
    public ServerModel getActive() {
        return this.activeServerModel;
    }


    /**
     * Error handling from model
     * @param errorMessage Error message from model
     */
    public void onError(String errorMessage) {
        // Inform user of error
        view.setConnectionResult(errorMessage);
    }

    // List of active server listeners
    private List<ActiveServerListener> activeListeners = new ArrayList<>();

    /**
     * Add active server listener
     * @param activeListener ActiveServerListener to add
     */
    public void addActiveListener(ActiveServerListener activeListener) {
        activeListeners.add(activeListener);
    }
}	 
