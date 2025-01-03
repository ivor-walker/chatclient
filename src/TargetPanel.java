import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;

import java.util.HashMap;

/**
 * Panel that displays the list of channels, users in the channel, and any recent direct messages
 * The panel also contains forms to create new channels and message new users
 */
public class TargetPanel extends JPanel {
    // Width and height of the panel
    private int width;
    private int height;

    // New channel panel components
    private JPanel channelPanel;
    private JPanel channelList;
    private JScrollPane channelListScrollPane; 
    private JTextField newChannelField; 
    private JButton createChannelButton;
    private JButton refreshChannelsButton;
    private JButton leaveChannelButton;
    
    // Users in channel panel components 
    private JPanel usersChannelPanel;
    private JPanel usersInChannelList;
    private JScrollPane usersChannelScrollPane;

    // Recent direct messages panel components
    private JPanel userHistoryPanel;
    private JPanel usersInHistoryList;
    private JScrollPane userHistoryScrollPane;
    private JTextField newUserField; 
    private JButton createUserButton;    

    /**
     * Constructor for the TargetPanel 
     * @param width Width of the panel
     * @param height Height of the panel
     */
    public TargetPanel(int width, int height) {
        // Set the width and height of the panel 
        this.width = width;
        this.height = height;
        setPreferredSize(new Dimension(width, height));
        
        // Set vertical layout 
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
       
        // Create all components 
        createChannelListPanel();
        createUsersInChannelPanel();
        createUsersInHistoryPanel();
    
        // Refresh the panel to display the components 
        refresh();
    }

    // Proportions for the channel list panel 
    private static final double WIDTH_PROPORTION_CHANNEL_LIST = 1;
    private static final double HEIGHT_PROPORTION_CHANNEL_LIST = 0.3;

    /**
     * Create the channel list panel with a form to create new channels
     */
    private void createChannelListPanel() {
        // Create the channel panel with BoxLayout 
        channelPanel = new JPanel();
        channelPanel.setLayout(new BoxLayout(channelPanel, BoxLayout.Y_AXIS));
        channelPanel.setBorder(BorderFactory.createTitledBorder("Channel list"));
    
        // Create the channel list panel
        channelList = new JPanel();
        channelList.setLayout(new BoxLayout(channelList, BoxLayout.Y_AXIS));
        channelList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
        // Scroll pane for the channel list
        channelListScrollPane = new JScrollPane(channelList);
        int finalWidth = (int) Math.round(WIDTH_PROPORTION_CHANNEL_LIST * width);
        int finalHeight = (int) Math.round(HEIGHT_PROPORTION_CHANNEL_LIST * height);
        channelListScrollPane.setPreferredSize(new Dimension(finalWidth, finalHeight));
    
        // New channel form 
        JPanel newChannelForm = new JPanel();
        newChannelForm.setLayout(new BoxLayout(newChannelForm, BoxLayout.Y_AXIS));
        newChannelForm.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));      
        // Create a panel for the # symbol and the text field
        JPanel channelFieldPanel = new JPanel();
        channelFieldPanel.setLayout(new BoxLayout(channelFieldPanel, BoxLayout.X_AXIS));  

        // Label with # symbol
        JLabel hashLabel = new JLabel("#");
        channelFieldPanel.add(hashLabel);
    
        // Text field for new channel name
        newChannelField = new JTextField();
        newChannelField.setPreferredSize(new Dimension(200, 25));
        newChannelField.setMaximumSize(new Dimension(200, 25));
        channelFieldPanel.add(newChannelField);
    
        // Add the channel field panel to the form
        newChannelForm.add(channelFieldPanel);
    
        // Button to create new channel
        createChannelButton = new JButton("Create new channel");
        newChannelForm.add(createChannelButton);
    
        newChannelForm.setMaximumSize(new Dimension(200, 50));
    
        // Button to leave active channel 
        leaveChannelButton = new JButton("Leave active channel");
        refreshChannelsButton = new JButton("Refresh channel list");

        channelPanel.add(newChannelForm);
        channelPanel.add(channelListScrollPane);
        channelPanel.add(refreshChannelsButton);
        channelPanel.add(leaveChannelButton);
    
        // Add the channel panel to the main panel 
        add(channelPanel);
    }

    // Proportions for the recent direct messages panel 
    private static final double WIDTH_PROPORTION_HISTORY_LIST = 1; 
    private static final double HEIGHT_PROPORTION_HISTORY_LIST = 0.3;

    /**
     * Create the recent direct messages panel with a form to message new users
     */
    private void createUsersInHistoryPanel() {
        // Create user history panel 
        userHistoryPanel = new JPanel();
        userHistoryPanel.setLayout(new BoxLayout(userHistoryPanel, BoxLayout.Y_AXIS));
        userHistoryPanel.setBorder(BorderFactory.createTitledBorder("Recent direct messages"));
    
        // Create the new user form 
        JPanel newUserForm = new JPanel();
        newUserForm.setLayout(new BoxLayout(newUserForm, BoxLayout.Y_AXIS));
        newUserForm.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));     
        // Text field for new user name
        newUserField = new JTextField();
        newUserField.setPreferredSize(new Dimension(200, 25));        
        newUserField.setMaximumSize(new Dimension(200, 25));  
        newUserForm.add(newUserField);
    
        // Button to send message to new user
        createUserButton = new JButton("Message new user");
        newUserForm.add(createUserButton);
    
        // Set maximum size for the form to avoid excessive stretching
        newUserForm.setMaximumSize(new Dimension(200, 100));
    
        // Add the form to the user history panel
        userHistoryPanel.add(newUserForm);
    
        // Users in history list panel 
        usersInHistoryList = new JPanel();
        usersInHistoryList.setLayout(new BoxLayout(usersInHistoryList, BoxLayout.Y_AXIS));
        usersInHistoryList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
        // Create scroll pane for the user history list
        userHistoryScrollPane = new JScrollPane(usersInHistoryList);
        int finalWidth = (int) Math.round(WIDTH_PROPORTION_HISTORY_LIST * width);
        int finalHeight = (int) Math.round(HEIGHT_PROPORTION_HISTORY_LIST * height);
        userHistoryScrollPane.setPreferredSize(new Dimension(finalWidth, finalHeight));
    
        // Add the scroll pane to the main panel
        userHistoryPanel.add(userHistoryScrollPane);
        
        // Add the user history panel to the main panel 
        add(userHistoryPanel);
    }


    // Proportions for the users in channel panel
    private static final double HEIGHT_PROPORTION_USERS_CHANNEL_LIST = 0.3;
    
    /**
     * Create the users in channel panel
     */
    private void createUsersInChannelPanel() {
        // Create the users in channel panel
        usersChannelPanel = new JPanel();

        // Create users in channel list panel
        usersInChannelList = new JPanel();
        usersInChannelList.setLayout(new BoxLayout(usersInChannelList, BoxLayout.Y_AXIS));
        usersInChannelList.setBorder(BorderFactory.createTitledBorder("Users in channel"));
        // Create scroll pane for the users in channel list
        JScrollPane scrollPane = new JScrollPane(usersInChannelList);
       
        // Add the scroll pane to the main panel 
        add(scrollPane);
    }
   
    // Maps to store buttons for channels, users in channel, and users in history 
    HashMap<String, JButton> channelListButtons = new HashMap<>();
    HashMap<String, JButton> usersInChannelButtons = new HashMap<>();
    HashMap<String, JButton> usersInHistoryButtons = new HashMap<>();

    /**
     * Add a button to a panel
     * @param key Key for the button
     * @param panel Panel to add the button to
     * @return The button that was added
     */
    public JButton addButton(String key, JPanel panel) {
        JButton button = new JButton(key);
        button.setPreferredSize(new Dimension(200, 30));        
        panel.add(button);
        return button;
    }

    /**
     * Refresh the panel to reflect any changes
     */
    public void refresh() {
        revalidate();
        repaint();
    }

    /**
     * Reset the users in history panel
     */ 
    public void resetUsersInHistory() {
        // For each key in the usersInHistoryButtons map 
        for (String userKey : usersInHistoryButtons.keySet()) {
            // Remove the button from the usersInHistoryList panel 
            JButton buttonToRemove = usersInHistoryButtons.get(userKey);
            usersInHistoryList.remove(buttonToRemove);         
        }

        // Clear the usersInHistoryButtons map 
        usersInHistoryButtons.clear();

        // Refresh panel to reflect view changes 
        refresh();
    }

    /**
     * Reset the users in channel panel
     */
    public void resetUsersInChannel() {
        // For each key in the usersInChannelButtons map 
        for (String userKey : usersInChannelButtons.keySet()) {
            // Remove the button from the usersInChannelList panel
            JButton buttonToRemove = usersInChannelButtons.get(userKey);
            usersInChannelList.remove(buttonToRemove);  
        }
        
        // Clear the usersInChannelButtons map
        usersInChannelButtons.clear();

        refresh();
    }

    /**
     * Reset the channel list panel
     */
    public void resetChannels() {
        // For each key in the channelListButtons map
        for(String buttonKey: channelListButtons.keySet()) {
            // Remove the button from the channelList panel
            JButton buttonToRemove = channelListButtons.get(buttonKey);
            channelList.remove(buttonToRemove); 
        }       

        // Clear the channelListButtons map
        channelListButtons.clear();

        refresh(); 
    }

    /**
     * Add a user to the users in history panel
     * @param userKey Key for the user
     */
    public void addToUserHistory(String userKey) {
        // Create new button and add it to panel and hashmap
        JButton userHistoryButton = addButton(userKey, usersInHistoryList); 
        usersInHistoryButtons.put(userKey, userHistoryButton); 
        
        refresh(); 
    }
   
    /**
     * Remove a user from the users in history panel 
     * @param userKey Key for the user
     */
    public void deleteFromUserHistory(String userKey) {
        // Get button from hashmap and remove it from panel
        JButton buttonToDelete = usersInHistoryButtons.get(userKey);
        usersInHistoryList.remove(buttonToDelete); 

        // Remove button from hashmap
        usersInHistoryButtons.remove(userKey);

        refresh();
    }

    /**
     * Add a user to the users in channel panel
     * @param userKey Key for the user
     */
    public void addToChannelUsers(String userKey) {
        // Create new button and add it to panel and hashmap
        JButton channelUserButton = addButton(userKey, usersInChannelList);
        usersInChannelButtons.put(userKey, channelUserButton);        
        
        refresh();
    }

    /**
     * Remove a user from the users in channel panel
     * @param userKey Key for the user
     */
    public void deleteFromChannelUsers(String userKey) {
        // Get button from hashmap and remove it from panel 
        JButton buttonToDelete = usersInChannelButtons.get(userKey);
        usersInChannelList.remove(buttonToDelete);

        // Remove button from hashmap
        usersInChannelButtons.remove(userKey);

        refresh();
    }

    /**
     * Add a new channel to the channel list panel
     * @param channelKey Key for the channel
     */
    public void addNewChannel(String channelKey) {
        // Create new button and add it to panel and hashmap
        JButton newChannelButton = addButton(channelKey, channelList);
        channelListButtons.put(channelKey, newChannelButton);         
        
        // As channel has not been joined, set button color to gray 
        newChannelButton.setForeground(Color.GRAY); 
        
        refresh();
    }

    /**
     * Add a joined channel to the channel list panel
     * @param channelKey Key for the channel
     */
    public void addJoinedChannel(String channelKey) {
        // Create new button and add it to panel and hashmap
        JButton joinedChannelButton = addButton(channelKey, channelList);
        channelListButtons.put(channelKey, joinedChannelButton);         
        
        refresh();
    }

    /**
     * Remove a channel from the channel list panel
     * @param channelKey Key for the channel
     */
    public void deleteChannel(String channelKey) {
        // Get button from hashmap and remove it from panel 
        JButton buttonToDelete = channelListButtons.get(channelKey);
        channelList.remove(buttonToDelete);

        // Remove button from hashmap
        channelListButtons.remove(channelKey);

        refresh();
    }
   
    /**
     * Leave the currently active channel
     * @param channelKey Key for the channel
     */ 
    public void leaveChannel(String channelKey) {
        // Get the channel button to leave
        JButton buttonToLeave = channelListButtons.get(channelKey);
        // Indicate channel is left by setting button color to gray
        buttonToLeave.setForeground(Color.GRAY); 
        
        // If chosen channel is the active target (i.e client leaves currently active channel), unset active target
        unsetActiveTarget(channelKey);

        refresh();
    }

    /**
     * Helper function to locate a button in any of the maps
     * @param key Key for the button
     * @return The button if found, null otherwise
     */
    public JButton findButtonInAllMaps(String key) {
        // Check in channelListButtons
        if (channelListButtons.containsKey(key)) {
            return channelListButtons.get(key);
        }
        
        // Check in usersInChannelButtons
        if (usersInChannelButtons.containsKey(key)) {
            return usersInChannelButtons.get(key);
        }
        
        // Check in usersInHistoryButtons
        if (usersInHistoryButtons.containsKey(key)) {
            return usersInHistoryButtons.get(key);
        }
       
        // If button not found in any of the maps, return null 
        return null;
    }
   
    /**
     * Set a target as active 
     * @param activeKey Key for the new active target
     */
    public void setActiveTarget(String activeKey) {
        // Find the button for the active target
        JButton activeButton = findButtonInAllMaps(activeKey);

        // Ensure button colour is black (e.g if it was previously gray)
        activeButton.setForeground(Color.BLACK); 
        // Set border to indicate active target
        activeButton.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
        
        refresh();
    }
   
    /**
     * Unset the active target 
     * @param oldActiveKey Key for the old active target
     */
    public void unsetActiveTarget(String oldActiveKey) {
        // Get currently active button
        JButton oldActiveButton = findButtonInAllMaps(oldActiveKey);

        // Remove border to indicate target is no longer active
        oldActiveButton.setBorder(BorderFactory.createEmptyBorder());

    }
    
   
    /**
     * Wrapper around both set and unset, to make changing active target easier 
     * @param oldActiveKey Key for the old active target
     * @param newActiveKey Key for the new active target
     */
    public void setActiveTarget(String oldActiveKey, String newActiveKey) {
        unsetActiveTarget(oldActiveKey); 
        setActiveTarget(newActiveKey);
    }

    /**
     * Add a listener to a target button, to allow for changing active target
     * @param userKey Key for the target
     * @param listener Listener to add to the button
     */
    public void addActiveTargetListener(String userKey, ActionListener listener) {
        // Find the given button
        JButton targetButton = findButtonInAllMaps(userKey);

        // Add the listener to the button
        targetButton.addActionListener(listener); 
    }

    /**
     * Get the new channel field value
     * @return The value in the new channel field
     */
    public String getNewChannelFieldValue() {
        // As channel names are prefixed with #, return the value with the # symbol
        return "#" + newChannelField.getText();
    }

    /**
     * Get the new user field value
     * @return The value in the new user field
     */
    public String getNewUserFieldValue() {
        // Get the value in the new user field
        String newUser = newUserField.getText();
       
        // If the user name is prefixed with #, remove the # symbol 
        if (newUser.startsWith("#")) {
            return newUser.substring(1);
        } 

        return newUser; 
    }
   
    /**
     * Add a listener to the create new channel button 
     * @param listener Listener to add to the button
     */
    public void addCreateNewChannelListener(ActionListener listener) {
        createChannelButton.addActionListener(listener); 
    }
   
    /**
     * Add a listener to the refresh channels button 
     * @param listener Listener to add to the button
     */
    public void addRefreshChannelsListener(ActionListener listener) {
        refreshChannelsButton.addActionListener(listener); 
    }

    /**
     * Add a listener to the leave channel button
     * @param listener Listener to add to the button
     */
    public void addLeaveChannelListener(ActionListener listener) {
        leaveChannelButton.addActionListener(listener); 
    }

    /**
     * Add a listener to the message new user button
     * @param listener Listener to add to the button
     */
    public void addMessageNewUserListener(ActionListener listener) {
        createUserButton.addActionListener(listener); 
    }
}
