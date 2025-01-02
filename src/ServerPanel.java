import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;

import java.util.HashMap;

import java.util.Random;

/**
 * Panel for viewing server list, setting active servers and adding/removing new servers
 */
public class ServerPanel extends JPanel {
    // Width and height of the panel
    private int width;
	private int height;

	private JPanel optionsPanel;	
	private JButton addServerButton;

    // Server list is a panel containing a scrollable list of server buttons
	private JPanel serverListPanel;
    private JPanel serverList;	
    private JScrollPane serverListScrollPane;	
    // HashMap to store server buttons
    private HashMap<String, JButton> serverButtons = new HashMap<String, JButton>(); 

    // Form for adding/editing servers
	private JPanel formPanel;
	private JTextField hostField;
	private JTextField portField;
	private JTextField nicknameField;
	private JButton commitButton;
	private JLabel connectionResultLabel;

    /**
     * Constructor for the ServerPanel
     * @param width Width of the panel
     * @param height Height of the panel
     */
	public ServerPanel(int width, int height) {
        // Set the width and height of the panel
        this.width = width;
        this.height = height;
        setPreferredSize(new Dimension(width, height));

        // Set vertical layout for the panel
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Add the server list and form to the panel
		initialiseServerList();
		initialiseForm();

        // Refresh server panel
        redrawServerPanel();
	}
   
    // Proportions of height that the server list panel takes up 
	private static final double WIDTH_PROPORTION_SERVER_LIST = 1;	
	private static final double HEIGHT_PROPORTION_SERVER_LIST = 0.5;	

    // TODO refactor: are nested serverListPanel and serverListScrollPane necessary?
    /**
     * Initialises the server list panel
     */
	private void initialiseServerList() {
        // Create a panel for the server list 
        serverListPanel = new JPanel();
        serverListPanel.setLayout(new BorderLayout());
        serverListPanel.setBorder(BorderFactory.createTitledBorder("Server list"));
 
        // Create a panel for the server list content 
        serverList = new JPanel();
        serverList.setLayout(new BoxLayout(serverList, BoxLayout.Y_AXIS));         
        serverList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
        // Create a scroll pane for the server list 
        serverListScrollPane = new JScrollPane(serverList);
        int finalWidth = (int) Math.round(WIDTH_PROPORTION_SERVER_LIST * width);
        int finalHeight = (int) Math.round(HEIGHT_PROPORTION_SERVER_LIST * height);
        serverListScrollPane.setPreferredSize(new Dimension(finalWidth, finalHeight));
    
        // Add the serverListScrollPane to the center of the serverListPanel
        serverListPanel.add(serverListScrollPane, BorderLayout.CENTER);
   
        // Add the addServerButton to the bottom of the serverListPanel 
        addServerButton = new JButton("Add server");
        serverListPanel.add(addServerButton, BorderLayout.SOUTH);
    
        // Add the serverListPanel to the main container
        add(serverListPanel, BorderLayout.NORTH);
    }

    /**
     * Updates the size of the server list panel (e.g when user changes size of window     
     * @param width Width of the panel
     * @param height Height of the panel
     */
    public void updateServerListSize(int width, int height) {
        // Calculate and set the size of the server list panel
        int listWidth = (int) Math.round(WIDTH_PROPORTION_SERVER_LIST * width);
        serverListScrollPane.setPreferredSize(new Dimension(listWidth, height));
        serverListPanel.setPreferredSize(new Dimension(listWidth, height));

        // Refresh the server panel to show changes
        redrawServerPanel();
    }

    /**
     * Initialises the form for adding/editing servers
     */
    private void initialiseForm() {
        // Create a panel for the form
        formPanel = new JPanel(new GridBagLayout());
    	formPanel.setBorder(BorderFactory.createTitledBorder("New server"));
   
        // GridBagLayout constraints for the form 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
   
        // Host field 
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Host"), gbc);
        gbc.gridy = 1;        
        hostField = new JTextField();
        hostField.setText("mct25.teaching.cs.st-andrews.ac.uk");
        hostField.setPreferredSize(new Dimension(200, 25));
        formPanel.add(hostField, gbc);
   
        // Port field 
        gbc.gridx = 0; gbc.gridy = 2;         
        formPanel.add(new JLabel("Port"), gbc);
        gbc.gridy = 3;        
        portField = new JTextField();
        portField.setText("21801");
        portField.setPreferredSize(new Dimension(200, 25));
        formPanel.add(portField, gbc);
        
     
        // Nickname field
        gbc.gridx = 0; gbc.gridy = 4; 
        formPanel.add(new JLabel("Nickname"), gbc);
        gbc.gridy = 5; 
        nicknameField = new JTextField();
        nicknameField.setText(generateNewName());
        nicknameField.setPreferredSize(new Dimension(200, 25)); 
        formPanel.add(nicknameField, gbc);
   
        // Commit button (add/remove) 
        gbc.gridx = 0; gbc.gridy = 6;
        commitButton = new JButton("Add server");
        formPanel.add(commitButton, gbc);
    
        // Connection result label 
        gbc.gridx = 0; gbc.gridy = 7;
        connectionResultLabel = new JLabel("");
        formPanel.add(connectionResultLabel, gbc);
   
        // Add the completed form to the main serverPanel  
        add(formPanel);
    }

    /**
     * Generates a new example nickname as a default
     * @return New nickname
     */
    private String generateNewName() {
        // Generate random number between 0 and 9000
        Random rand = new Random();
        int randomNumber = rand.nextInt(9000) + 1000;

        // Test nickname is "Test" followed by the random number
        return "Test" + String.valueOf(randomNumber);
    }

    /**
     * Helper function to add a field to the specified form
     * @param label Label for the field
     * @param panel Panel to add the field to
     * @return New field
     */
	private JTextField addField(String label, JPanel panel) {
		panel.add(new JLabel(label));
		JTextField newField = new JTextField();
		panel.add(newField);
		return newField;
	}

    /**
     * Helper function to add a button to the specified form
     * @param label Label for the button
     * @param panel Panel to add the button to
     * @param direction Direction to add the button
     * @return New button
     */
	private JButton addButton(String label, JPanel panel, String direction) {
		JButton newButton = new JButton(label);
		panel.add(newButton, direction);
		return newButton;
	}

    /**
     * Set values in the form for adding/editing servers
     * @param host Host of the server
     * @param port Port of the server
     * @param nickname Nickname of the server
     * @param addingServer Whether the server is being added or edited
     */
    public void setupServerForm(String host, String port, String nickname, boolean addingServer) {
        // Set the values in the form
		hostField.setText(host);
		portField.setText(port);
		nicknameField.setText(nickname);
	    
        // Reset connection result    
		setConnectionResult("");	

        // Adding a new server        
		if(addingServer) {	
		    addingServerForm();	

        // Editing an existing server
        } else {
            editingServerForm();
		}
	}

    /**
     * For creating a new server, set all fields in form to blank
     */
	public void setupServerForm() {
		setupServerForm("", "", "", true);	
	}
	
    /**
     * For editing an existing server, set fields in form to the server details    
     * @param host Host of the server
     * @param port Port of the server
     * @param nickname Nickname of the server
     */
	public void setupServerForm(String host, int port, String nickname) {
        // Convert port to string
		String portString = Integer.toString(port);
		setupServerForm(host, portString, nickname, false);	
	}
    
    /**
     * Set the text of the commit button
     * @param addServer Whether the server is being added or removed
     */
    private void setCommitButton(boolean addServer) {
        if (addServer) {
            commitButton.setText("Add server");
        } else {
            commitButton.setText("Remove server");
        } 
    }

    /**
     * Setup the form to be for adding a new server
     */
    private void addingServerForm() {
        // Set the commit button to add server
        setCommitButton(true);
       
        // Enable the fields in the form 
        hostField.setEnabled(true);
        portField.setEnabled(true);
        nicknameField.setEnabled(true);
    }

    /** 
     * Setup the form to be for viewing an existing server
     */
    private void editingServerForm() {
        // Set the commit button to remove server
        setCommitButton(false);
       
        // Disable the fields in the form
        hostField.setEnabled(false);
        portField.setEnabled(false);
        nicknameField.setEnabled(false);
    }

    /**
     * Setter for the connection result label
     * @param result Result of the connection
     */ 
	public void setConnectionResult(String result) {
        // html tags used for multi-line text
		connectionResultLabel.setText("<html>" + result + "</html>");
	}

    /**
     * Add a server to the server list
     * @param serverKey Key of the server
     * @param refresh Whether to refresh the server list
     */
    public void addServer(String serverKey, boolean refresh) {
        // If the server is already in the list, do not add it again
        if(serverButtons.containsKey(serverKey)) {
			return;
		}

        // Create a new button for the server and add it to the server list 
		JButton serverValue = new JButton(serverKey);
		serverList.add(serverValue);

        // Add the server to the serverButtons hashmap
		serverButtons.put(serverKey, serverValue);

	    // Redraw server panel if necessary	
        if(refresh == true) {
			redrawServerPanel();	
		}
	}

    /**
     * Wrapper for addServer
     * By default, refresh the server list when adding a server
     * @param serverString Server to add
     */
	public void addServer(String serverString) {
		addServer(serverString, true);
	}	

    /**
     * Remove a server from the server list
     * @param serverKey Key of the server
     * @param refresh Whether to refresh the server list
     */
    public void removeServer(String serverKey, boolean refresh) {
	    // Get server button to remove	
        JButton buttonToRemove = serverButtons.get(serverKey);

        // Remove from list and hashmap    
		serverList.remove(buttonToRemove);
		serverButtons.remove(serverKey);

        // Redraw server panel if necessary    
		if(refresh == true) {	
			redrawServerPanel();
		}
	}

    /**
     * Wrapper for removeServer
     * By default, refresh the server list when removing a server
     * @param serverKey Server to remove
     */
	public void removeServer(String serverKey) {
		removeServer(serverKey, true);
	}

	/**
     * Change the active server	
     * @param oldActiveServerKey Key of the old active server
     * @param newServerKey Key of the new active server
     */
	public void changeActive(String oldActiveServerKey, String newServerKey) {
        // Get old active button and remove border
        JButton oldActiveButton = serverButtons.get(oldActiveServerKey);
        oldActiveButton.setBorder(BorderFactory.createEmptyBorder());
	    
        // Set new button to active    
		setActive(newServerKey);	
	}

    /**
     * Set a server as active    
     * @param activeServerKey Key of the new active server
     */
	public void setActive(String activeServerKey) {
        // Get new active button and set border to red
		JButton activeButton = serverButtons.get(activeServerKey);
		activeButton.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
	    // Refresh	
        redrawServerPanel();
	}

    /**
     * Refresh the server list panel only combining revalidate and repaint
     */
	private void redrawServerPanel() {
		serverListPanel.revalidate();
		serverListPanel.repaint();
	}

    /**
     * Getter for the host field	
     * @return Host field
     */
    public String getHost() {
		return hostField.getText(); 
	}

    /**
     * Getter for the port field
     * @return Port field
     */
	public String getPort() {
		return portField.getText(); 
	}

    /**
     * Getter for the nickname field
     * @return Nickname field
     */
	public String getNickname() {
		return nicknameField.getText(); 
	}

    /**
     * Listener for adding a new server	
     * @param listener Listener for the add server button
     */
    public void viewNewServerListener(ActionListener listener) {
		addServerButton.addActionListener(listener);
	}

    /**
     * Listener for removing a server    
     * @param listener Listener for the remove server button
     */
	public void commitNewServerListener(ActionListener listener) {
        removeCommitButtonListeners(); 
        commitButton.addActionListener(listener);
	}

    /**
     * Listener for viewing an existing server
     * @param serverString Server to view
     * @param listener Listener for the server button
     */    
	public void viewExistingServerListener(String serverString, ActionListener listener) {
		JButton selectedButton = serverButtons.get(serverString);
		selectedButton.addActionListener(listener);	
	}

    /**
     * Listener for removing an existing server
     * @param listener Listener for the server button
     */
	public void removeExistingServerListener(ActionListener listener) {
        removeCommitButtonListeners(); 
		commitButton.addActionListener(listener);
	}

    /**
     * Remove all listeners from the commit button
     */
    private void removeCommitButtonListeners() {
        for (ActionListener listener : commitButton.getActionListeners()) {
            commitButton.removeActionListener(listener);
        }
    }
}
