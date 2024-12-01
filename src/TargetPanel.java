import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;

import java.util.HashMap;

public class TargetPanel extends JPanel {

    private JPanel channelList;
    private JTextField newChannelField; 

    private JPanel usersInChannelList;
    private JPanel usersInHistoryList;

    public TargetPanel(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        createChannelListPanel();
        createUsersInChannelPanel();
        createUsersInHistoryPanel();
    }

    private void createChannelListPanel() {
        channelList = new JPanel();
        channelList.setLayout(new BorderLayout());
        channelList.setBorder(BorderFactory.createTitledBorder("Channel list"));

        JPanel newChannelForm = new JPanel();
        newChannelForm.setLayout(new BoxLayout(newChannelForm, BoxLayout.Y_AXIS));
        
        newChannelField = new JTextField();
        newChannelField.setPreferredSize(new Dimension(100, 25));
        newChannelForm.add(newChannelField, BorderLayout.SOUTH);
        
        JButton createChannelButton = new JButton("Create new channel");
        newChannelForm.add(createChannelButton, BorderLayout.NORTH);

        channelList.add(newChannelForm, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(channelList);
        add(scrollPane);
    }

    private void createUsersInHistoryPanel() {
        usersInHistoryList = new JPanel();
        usersInHistoryList.setLayout(new BorderLayout());
        usersInHistoryList.setBorder(BorderFactory.createTitledBorder("Recent direct messages"));

        JPanel newChannelForm = new JPanel();
        newChannelForm.setLayout(new BoxLayout(newChannelForm, BoxLayout.Y_AXIS));
        
        newChannelField = new JTextField();
        newChannelField.setPreferredSize(new Dimension(100, 25));
        newChannelForm.add(newChannelField, BorderLayout.SOUTH);
        
        JButton createChannelButton = new JButton("Message new user");
        newChannelForm.add(createChannelButton, BorderLayout.NORTH);

        usersInHistoryList.add(newChannelForm, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(usersInHistoryList);
        add(scrollPane);
    }

    private void createUsersInChannelPanel() {
        usersInChannelList = new JPanel();
        usersInChannelList.setLayout(new BorderLayout());
        usersInChannelList.setBorder(BorderFactory.createTitledBorder("Users in channel"));

        JButton refreshButton = new JButton("Refresh");
        usersInChannelList.add(refreshButton, BorderLayout.NORTH);
        
               
         JScrollPane scrollPane = new JScrollPane(usersInChannelList);
        
        add(scrollPane);
    }

    //Public methods
    public void resetUsersInHistory() {
        //TODO
    }

    public void resetUsersInChannel() {
        //TODO
    }
    
    public void resetChannels() {
        //TODO
    }

    public void addToUserHistory(String userKey) {
        //TODO
    }

    public void addToChannelUsers(String userKey) {
        //TODO
    }

    public void addNewChannel(String channelKey) {
        //TODO
    } 

    public void addJoinedChannel(String channelKey) {
        //TODO
    } 
  
    //Listeners
    public void refreshChannelsListener(ActionListener listener) {
        //TODO 
    }

    public void refreshUsersInChannelListener(ActionListener listener) {
        //TODO 
    }

    public void addActiveTargetListener(String userKey, ActionListener listener) {
        //TODO 
    }

    public void addJoinChannelListener(String channelKey, ActionListener listener) {
        //TODO
    }

    public void addLeaveChannelListener(String channelKey, ActionListener listener) {
        //TODO
    }
}
