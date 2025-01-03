import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.stream.Stream;
import java.util.stream.Collectors;

import java.util.Arrays;
import java.util.Map;

/**
 * Controller for the TargetPanel
 */
public class TargetController implements ActiveServerListener, TargetListener {
    // Instance of TargetPanel to control
    private TargetPanel view;

    // Active model and target
    private ServerModel activeModel;
    private Target activeTarget;
    private String activeTargetName;
  
    // Local history of users and channels 
    private List<String> localHistoryUsers;
    private List<String> localChannels;

    // Methods to join and part channels
    private JoinChannelMethod joinChannelMethod;
    private PartChannelMethod partChannelMethod;

    /**
     * Constructor for the TargetController
     * @param view TargetPanel to control
     */ 
    public TargetController(TargetPanel view) {
        this.view = view;
        setupListeners();
    }

    /**
     * Setup listeners for the TargetPanel
     */
    private void setupListeners() {
        // Create new channel 
        view.addCreateNewChannelListener(e -> createNewChannel());
        // Message new user
        view.addMessageNewUserListener(e -> messageNewUser());    
        // Leave channel
        view.addLeaveChannelListener(e -> leaveChannel());    
        // Refresh channel list
        view.addRefreshChannelsListener(e -> refreshChannels());    
    }
   
    /**
     * Add all users in the server to view
     */ 
    private void addUsersInHistory() {
        // Get users
        Target[] users = activeModel.getUsers();
        // For each user
        for(Target user: users) {
            // Add user to view
            String userKey = user.getName();
            view.addToUserHistory(userKey);
            // Add listener to the user's button
            view.addActiveTargetListener(userKey, e -> setActiveTarget(userKey));
        }
    } 

    /**
     * Refresh users in user's message history
     */
    public void refreshUsersInHistory() {
        // Remove all previous users
        view.resetUsersInHistory();
        // Add all current users
        addUsersInHistory();
    } 
    
    /**
     * Add all users in the channel to view
     */
    private void addUsersInChannel() {
        // Get active channel
        Channel activeChannel = (Channel) activeTarget; 
        // Get all users in the channel
        List<String> userKeys = activeChannel.getUsers();    
        // For each user 
        for(String userKey: userKeys) {
            // Add user to view
            view.addToChannelUsers(userKey);
            // Add listener to the user's button
            view.addActiveTargetListener(userKey, e -> setActiveTarget(userKey));
        }
    }

    /**
     * Refresh users in the active channel
     */
    private void refreshUsersInChannel() {
        // Remove all previous users
        view.resetUsersInChannel();
        // Add all current users
        addUsersInChannel();
    }
    
    /**
     * Add all channels in the server to view
     */ 
    private void addChannels() {
        // Attempt to get all channels in server
        activeModel.getOfferedChannels().thenAccept(channelKeys -> {
            // Get all channel keys in server
            channelKeys = Arrays.stream(channelKeys)
                .filter(e -> !e.isEmpty())
                .toArray(String[]::new); 
       
            // Get all channel keys that client has joined 
            List<String> joinedChannelKeys = getJoinedChannelKeys();

            // For each server channel key 
            for(String channelKey: channelKeys) {
                // If client has joined channel, add as joined channel
                if (joinedChannelKeys.contains(channelKey)) {
                    view.addJoinedChannel(channelKey);

                // Else, add as unjoined channel
                } else {
                    view.addNewChannel(channelKey);
                }

                // Add listener to the channel's button
                view.addActiveTargetListener(channelKey, e -> setActiveTarget(channelKey)); 
            }            
        });
    }
   
    /**
     * Helper method to get all channel keys that client has joined
     * @return List of channel keys that client has joined
     */ 
    private List<String> getJoinedChannelKeys() {
        // Get all joined channels
        Channel[] joinedChannels = activeModel.getJoinedChannels(); 
        // Map all joined channels to their name (i.e their key)
        return Arrays.stream(joinedChannels) 
                .map(Channel::getName)
                .toList();
    }

    /**
     * Refresh list of offered channels in server
     */
    private void refreshChannels() {
        // Remove all previous channels
        view.resetChannels();
        addChannels();
    }
    
    /**
     * Set active target to a given target
     * @param target Target to set as active
     */
    private void setActiveTarget(Target target) {
        // Get key of new target
        String targetKey = target.getName();

        // If existing target isn't set, change active target from previous active target to new
        if(activeTarget != null) { 
            view.setActiveTarget(activeTarget.getName(), targetKey);
        } else {
            view.setActiveTarget(targetKey);
        }

        activeTarget = target;
        activeTargetName = targetKey;

        if(activeTarget.isChannel()) { 
            refreshUsersInChannel();
        }

        for(ActiveTargetListener listener: activeListeners) {
            listener.onSetActiveTarget(target);
        }
    }
    
    /**
     * Wrapper around setActiveTarget to handle users and channels seperately
     * @param targetKey Key of target to set as active
     */ 
    private void setActiveTarget(String targetKey) {
        // Get target with key
        Target target = activeModel.getTargets().get(targetKey);
        
        // If target does not exist 
        if(target == null) { 
            // If non-existent target is a channel, attempt to join channel
            if (targetKey.startsWith("#")) {
                activeModel.joinChannel(targetKey).thenAccept(channel -> {
                    // Set newly discovered channel as active
                    setActiveTarget(channel); 
                });
            // Else, client is DMing a new user
            } else {
                // Create new target representing new user
                Target newUser = new Target(activeModel, targetKey);
                activeModel.addTarget(newUser);

                // Set new DM as active target
                setActiveTarget(newUser);
            }

        // Else, set target as active target as normal
        } else {
           setActiveTarget(target); 
        }
    }
   
    
    /**
     * Unset active target
     */
    private void unsetActiveTarget() {
        activeTarget = null;
        activeTargetName = null;
        
        // Notify all listeners of active target change
        for(ActiveTargetListener listener: activeListeners) {
            listener.onSetActiveTarget(null);
        }       
    } 

    /**
     * Listener for when active server changes
     * @param activeModel New active server
     */
    public void onSetActiveServer(ServerModel activeModel) {
        // Unset current active server 
        unsetActiveTarget();

        // Set new active server
        this.activeModel = activeModel;
        this.activeModel.addTargetListener(this);

        // Show new users and channels
        refreshUsersInHistory(); 
        refreshChannels();
    }

    /**
     * Create a new channel based on view input
     */
    private void createNewChannel() {
        // Cannot create a new channel if no active server 
        if(activeModel == null) {
            return;
        }

        // Get new channel key from view
        String newChannelKey = view.getNewChannelFieldValue();
      
        // TODO shouldn't this check be for all channels, not just joined ones? 
        // If channel already exists, return 
        List<String> joinedChannelKeys = getJoinedChannelKeys();
        if (joinedChannelKeys.contains(newChannelKey)) {
            return;
        }

        // Create new channel in view
        view.addNewChannel(newChannelKey); 
        view.addActiveTargetListener(newChannelKey, e -> setActiveTarget(newChannelKey)); 

        // Set newly created channel as active
        setActiveTarget(newChannelKey); 
    }

    /**
     * Leave active channel
     */
    private void leaveChannel() {
        // Cannot leave a user's DM, only channels
        if(!activeTarget.isChannel()) {
            return;
        }
        
        String channelToLeave = activeTargetName;
        // Attempt to part channel
        activeModel.partChannel(channelToLeave).thenRun(() -> { 
            // Remove channel from view
            view.leaveChannel(channelToLeave);

            // Unset old active target
            unsetActiveTarget();    
        });
    }
    
    // List of all keys of users that have been messaged 
    List<String> userHistoryKeys = new ArrayList<>();

    /**
     * Message a new user
     */
    private void messageNewUser() {
        // Cannot message a new user if no active server
        if(activeModel == null) {
            return;
        }

        // Get new user key from view
        String newUserKey = view.getNewUserFieldValue();
       
        // If user already messaged, return 
        if(userHistoryKeys.contains(newUserKey)) {
            return;
        }
        
        // Add new user to view 
        view.addToUserHistory(newUserKey);
        view.addActiveTargetListener(newUserKey, e -> setActiveTarget(newUserKey)); 
        // Add new user to history
        userHistoryKeys.add(newUserKey); 

        // Set new user as active target
        setActiveTarget(newUserKey);
    }

    /** 
     * Listener for a given user quitting the server
     * @param usernameWhoQuit Username of user who quit
     */
    public void onQuit(String usernameWhoQuit) {
        // Remove from user history
        view.deleteFromUserHistory(usernameWhoQuit);
        userHistoryKeys.remove(usernameWhoQuit);
    }

    /**
     * Listener for a given user joining a channel
     * @param usernameWhoJoined Username of user who joined
     * @param channel Channel user joined
     */
    public void onJoinChannel(String usernameWhoJoined, Channel channel) {
        // If channel is not in the active server, return
        if (!isTargetInActive(channel)) {
            return;
        }

        // Add new user to view
        view.addToChannelUsers(usernameWhoJoined);
    }

    /**
     * Listener for a given user parting a channel
     * @param usernameWhoQuit Username of user who quit
     * @param channel Channel user quit
     */
    public void onPartChannel(String usernameWhoQuit, Channel channel) {
        // If channel is not in the active server, return 
        if (!isTargetInActive(channel)) {
            return;
        } 
       
        // Remove user from view 
        view.deleteFromChannelUsers(usernameWhoQuit);
    }

    /**
     * Listener for recieving a message
     * @param message Message recieved
     */
    public void onMessageRecieved(Message message) {
        // If message is not from active target, return
        if (!isMessageFromActive(message)) {
            return;
        }
        
        // Notify all listeners (i.e messageController) of new message
        for(ActiveTargetListener listener: activeListeners) {
            listener.onMessageRecieved(message);
        } 
    }

    /**
     * Helper method to check if given message is from currently active target
     * @param message Message to check
     * @return True if message is from active target, false otherwise
     */
    private boolean isMessageFromActive(Message message) {
        // Check if message is from active server and target
        return message.getServer().equals(activeModel.toString()) && message.getTarget().equals(activeTargetName); 
    }

    /**
     * Helper method to check if given target is currently active
     * @param target Target to check
     * @return True if target is active, false otherwise
     */
    private boolean isTargetInActive(Target target) {
        // Check if target is in active server and equal to currently active target
        return target.getServer().equals(activeModel.toString()) && target.getName().equals(activeTargetName);
    }

    // List of all active target listeners
    private List<ActiveTargetListener> activeListeners = new ArrayList<>(); 

    /**
     * Method to register new listener to active target changes (i.e messageController)
     * @param activeListener Listener to add
     */
    public void addActiveListener(ActiveTargetListener activeListener) {
        activeListeners.add(activeListener);
    }
}
