import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;

import java.util.HashMap;

public class TargetPanel extends JPanel {
    private int width;
    private int height;

    private JPanel channelPanel;
    private JPanel channelList;
    private JScrollPane channelListScrollPane; 
    private JTextField newChannelField; 
    private JButton createChannelButton;
    private JButton refreshChannelsButton;
    private JButton leaveChannelButton;
 
    private JPanel userHistoryPanel;
    private JPanel usersInHistoryList;
    private JScrollPane userHistoryScrollPane;
    private JTextField newUserField; 
    private JButton createUserButton;    

    private JPanel usersChannelPanel;
    private JPanel usersInChannelList;
    private JScrollPane usersChannelScrollPane;
    
    public TargetPanel(int width, int height) {
        this.width = width;
        this.height = height;
 
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(width, height));
        
        createChannelListPanel();
        createUsersInChannelPanel();
        createUsersInHistoryPanel();
    
        refresh();
    }

    private static final double WIDTH_PROPORTION_CHANNEL_LIST = 1;
    private static final double HEIGHT_PROPORTION_CHANNEL_LIST = 0.3;

    private void createChannelListPanel() {
        // Using BorderLayout for better control over layout
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
    
        // New channel form (to be placed at the top)
        JPanel newChannelForm = new JPanel();
        newChannelForm.setLayout(new BoxLayout(newChannelForm, BoxLayout.Y_AXIS));
        newChannelForm.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));  // Padding around the form
    
        // Create a panel for the # symbol and the text field
        JPanel channelFieldPanel = new JPanel();
        channelFieldPanel.setLayout(new BoxLayout(channelFieldPanel, BoxLayout.X_AXIS));  // Horizontal layout
    
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
    
        // "Leave Channel" button (aligned to the right of the connection status)
        leaveChannelButton = new JButton("Leave active channel");
        refreshChannelsButton = new JButton("Refresh channel list");

        channelPanel.add(newChannelForm);
        channelPanel.add(channelListScrollPane);
        channelPanel.add(refreshChannelsButton);
        channelPanel.add(leaveChannelButton);
    
        // Finally, add the channel panel to the main window
        add(channelPanel);
    }



    private static final double WIDTH_PROPORTION_HISTORY_LIST = 1; 
    private static final double HEIGHT_PROPORTION_HISTORY_LIST = 0.3;

    private void createUsersInHistoryPanel() {
        // Create the user history panel with BoxLayout
        userHistoryPanel = new JPanel();
        userHistoryPanel.setLayout(new BoxLayout(userHistoryPanel, BoxLayout.Y_AXIS));
        userHistoryPanel.setBorder(BorderFactory.createTitledBorder("Recent direct messages"));
    
        // Create the new user form (similar to how we did with newChannelForm)
        JPanel newUserForm = new JPanel();
        newUserForm.setLayout(new BoxLayout(newUserForm, BoxLayout.Y_AXIS));
        newUserForm.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));  // Padding around the form
    
        // Text field for new user name
        newUserField = new JTextField();
        newUserField.setPreferredSize(new Dimension(200, 25));  // Set a reasonable preferred size
        newUserField.setMaximumSize(new Dimension(200, 25));    // Prevent stretching
        newUserForm.add(newUserField);
    
        // Button to send message to new user
        createUserButton = new JButton("Message new user");
        newUserForm.add(createUserButton);
    
        // Set maximum size for the form to avoid excessive stretching
        newUserForm.setMaximumSize(new Dimension(200, 100));
    
        // Add the form to the user history panel
        userHistoryPanel.add(newUserForm);
    
        // Users in history list panel (can be used to show a list of users, similar to channel list)
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
        
        
        // Finally, add the user history panel to the main window (or to a container in your UI)
        add(userHistoryPanel);
    }


    private static final double HEIGHT_PROPORTION_USERS_CHANNEL_LIST = 0.3;
    private void createUsersInChannelPanel() {
        usersChannelPanel = new JPanel();

         
        usersInChannelList = new JPanel();
        usersInChannelList.setLayout(new BoxLayout(usersInChannelList, BoxLayout.Y_AXIS));
        usersInChannelList.setBorder(BorderFactory.createTitledBorder("Users in channel"));

        JScrollPane scrollPane = new JScrollPane(usersInChannelList);
        
        add(scrollPane);
    }
    
    HashMap<String, JButton> channelListButtons = new HashMap<>();
    HashMap<String, JButton> usersInChannelButtons = new HashMap<>();
    HashMap<String, JButton> usersInHistoryButtons = new HashMap<>();

    public JButton addButton(String key, JPanel panel) {
        JButton button = new JButton(key);
        button.setPreferredSize(new Dimension(200, 30));  // Width is 200px, height is 30px
        panel.add(button);
        return button;
    }

    public void refresh() {
        revalidate();
        repaint();
    }

    //Public methods
    public void resetUsersInHistory() {
        // Iterate over the keys of the usersInHistoryButtons map and remove the buttons
        for (String userKey : usersInHistoryButtons.keySet()) {
            JButton buttonToRemove = usersInHistoryButtons.get(userKey);
            usersInHistoryList.remove(buttonToRemove);  // Remove the button from the panel
        }

        // Optionally, clear the map of buttons if no longer needed
        usersInHistoryButtons.clear();

        // Refresh the layout to reflect the changes
        refresh();
    }

    public void resetUsersInChannel() {
        // Iterate over the keys of the usersInChannelButtons map and remove the buttons
        for (String userKey : usersInChannelButtons.keySet()) {
            JButton buttonToRemove = usersInChannelButtons.get(userKey);
            usersInChannelList.remove(buttonToRemove);  // Remove the button from the panel
        }

        // Optionally, clear the map of buttons if no longer needed
        usersInChannelButtons.clear();

        // Refresh the layout to reflect the changes
        refresh();
    }

    public void resetChannels() {
       for(String buttonKey: channelListButtons.keySet()) {
            JButton buttonToRemove = channelListButtons.get(buttonKey);
            channelList.remove(buttonToRemove); 
       }       

       channelListButtons.clear();
       refresh(); 
    }

    public void addToUserHistory(String userKey) {
        JButton userHistoryButton = addButton(userKey, usersInHistoryList); 
        usersInHistoryButtons.put(userKey, userHistoryButton); 
        refresh(); 
    }
    
    public void deleteFromUserHistory(String userKey) {
        JButton buttonToDelete = usersInHistoryButtons.get(userKey);
        usersInHistoryList.remove(buttonToDelete); 
        usersInHistoryButtons.remove(userKey);
        refresh();
    }

    public void addToChannelUsers(String userKey) {
        JButton channelUserButton = addButton(userKey, usersInChannelList);
        usersInChannelButtons.put(userKey, channelUserButton);  // Assuming you maintain a map for quick lookup
        refresh();
    }

    public void deleteFromChannelUsers(String userKey) {
        JButton buttonToDelete = usersInChannelButtons.get(userKey);
        usersInChannelList.remove(buttonToDelete);
        usersInChannelButtons.remove(userKey);
        refresh();
    }

    public void addNewChannel(String channelKey) {
        JButton newChannelButton = addButton(channelKey, channelList);
        newChannelButton.setForeground(Color.GRAY); 
        channelListButtons.put(channelKey, newChannelButton);  // Assuming you maintain a map for channels
        refresh();
    }

    public void addJoinedChannel(String channelKey) {
        JButton joinedChannelButton = addButton(channelKey, channelList);
        channelListButtons.put(channelKey, joinedChannelButton);  // You might want a separate map here for joined channels
        refresh();
    }

    public void deleteChannel(String channelKey) {
        JButton buttonToDelete = channelListButtons.get(channelKey);
        channelList.remove(buttonToDelete);
        channelListButtons.remove(channelKey);
        refresh();
    }
    
    public void leaveChannel(String channelKey) {
        JButton buttonToLeave = channelListButtons.get(channelKey);
        buttonToLeave.setForeground(Color.GRAY); 
        unsetActiveTarget(channelKey);
        refresh();
    }

    //Listeners
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
        
        // If key is not found in any of the maps, return null or handle appropriately
        return null;
    }

    public void setActiveTarget(String oldActiveKey, String newActiveKey) {
        unsetActiveTarget(oldActiveKey); 
        setActiveTarget(newActiveKey);
    }

    public void unsetActiveTarget(String oldActiveKey) {
        JButton oldActiveButton = findButtonInAllMaps(oldActiveKey);
        oldActiveButton.setBorder(BorderFactory.createEmptyBorder());

    }
    public void setActiveTarget(String activeKey) {
        JButton activeButton = findButtonInAllMaps(activeKey);
        activeButton.setForeground(Color.BLACK); 
        activeButton.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
        
        refresh();
    }
    
    public void addActiveTargetListener(String userKey, ActionListener listener) {
        JButton targetButton = findButtonInAllMaps(userKey);
        targetButton.addActionListener(listener); 
    }

    public String getNewChannelFieldValue() {
        return "#" + newChannelField.getText();
    }

    public String getNewUserFieldValue() {
        String newUser = newUserField.getText();
        
        if (newUser.startsWith("#")) {
            return newUser.substring(1);
        } 

        return newUser; 
    }
    
    public void addCreateNewChannelListener(ActionListener listener) {
        createChannelButton.addActionListener(listener); 
    }
    
    public void addRefreshChannelsListener(ActionListener listener) {
        refreshChannelsButton.addActionListener(listener); 
    }

    public void addLeaveChannelListener(ActionListener listener) {
        leaveChannelButton.addActionListener(listener); 
    }

    public void addMessageNewUserListener(ActionListener listener) {
        createUserButton.addActionListener(listener); 
    }
}
