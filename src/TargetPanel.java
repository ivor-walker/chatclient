import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;

import java.util.HashMap;


public class TargetPanel extends JPanel {
    
    public TargetPanel(int width, int height) {
        setPreferredSize(new Dimension(width, height)); 
    }


    //Public methods
    public void resetUsersInHistory() {
        //TODO
    }

    public void resetUsersInChannel() {
        //TODO
    }
    
    public void resetChannels() {

    }

    public void addToUserHistory(String userKey) {
        //TODO
    }

    public void addToChannelUsers(String userKey) {
        //TODO
    }

    public void addNewChannel(String channelKey) {

    } 

    public void addJoinedChannel(String channelKey) {

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
