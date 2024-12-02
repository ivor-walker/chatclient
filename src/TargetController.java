import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.stream.Stream;
import java.util.stream.Collectors;

import java.util.Arrays;
import java.util.Map;


public class TargetController implements ActiveServerListener, TargetListener {
    private TargetPanel view;
    private ServerModel activeModel;
    private Target activeTarget;
    private String activeTargetName;
   
    private List<String> localHistoryUsers;
    private List<String> localChannels;

    private JoinChannelMethod joinChannelMethod;
    private PartChannelMethod partChannelMethod;
 
    public TargetController(TargetPanel view) {
        this.view = view;
        setupListeners();
    }

    private void setupListeners() {
        view.addCreateNewChannelListener(e -> createNewChannel());
        view.addMessageNewUserListener(e -> messageNewUser());    
        view.addLeaveChannelListener(e -> leaveChannel());    
        view.addRefreshChannelsListener(e -> refreshChannels());    
    }

    public void refreshUsersInHistory() {
        view.resetUsersInHistory();
        addUsersInHistory();
    } 
       
    private void addUsersInHistory() {
        Target[] users = activeModel.getUsers();
        for(Target user: users) {
            String userKey = user.getName();
            view.addToUserHistory(userKey);
            view.addActiveTargetListener(userKey, e -> setActiveTarget(userKey));
        }
    } 

    private void refreshUsersInChannel() {
        view.resetUsersInChannel();
        addUsersInChannel();
    }

    private void addUsersInChannel() {
        Channel activeChannel = (Channel) activeTarget; 
        List<String> userKeys = activeChannel.getUsers();     
        for(String userKey: userKeys) {
            view.addToChannelUsers(userKey);
            view.addActiveTargetListener(userKey, e -> setActiveTarget(userKey));
        }
    }

    private void refreshChannels() {
        view.resetChannels();
        addChannels();
    }

    private void addChannels() {
        activeModel.getOfferedChannels().thenAccept(channelKeys -> {
            channelKeys = Arrays.stream(channelKeys)
                .filter(e -> !e.isEmpty())
                .toArray(String[]::new); 
        
            List<String> joinedChannelKeys = getJoinedChannelKeys();
 
            for(String channelKey: channelKeys) {
                if (joinedChannelKeys.contains(channelKey)) {
                    view.addJoinedChannel(channelKey);
                } else {
                    view.addNewChannel(channelKey);
                }
                view.addActiveTargetListener(channelKey, e -> setActiveTarget(channelKey)); 
            }            
        });
    }

    private List<String> getJoinedChannelKeys() {
        Channel[] joinedChannels = activeModel.getJoinedChannels(); 
        return Arrays.stream(joinedChannels) 
                .map(Channel::getName)
                .toList();
    }

    private void setActiveTarget(String targetKey) {
        Target target = activeModel.getTargets().get(targetKey);
         
        if(target == null) { 
            if (targetKey.startsWith("#")) {
                activeModel.joinChannel(targetKey).thenAccept(channel -> {
                    setActiveTarget(channel); 
                });
            } else {
                Target newUser = new Target(activeModel, targetKey);
                activeModel.addTarget(newUser);

                setActiveTarget(newUser);
            }
        } else {
           setActiveTarget(target); 
        }
    }
   
    private void setActiveTarget(Target target) {
            String targetKey = target.getName();
            if(activeTarget != null) { 
                view.setActiveTarget(activeTarget.getName(), targetKey);
            } else {
                view.setActiveTarget(targetKey);
            }

            activeTarget = target;
            activeTargetName = targetKey;

            System.out.println("TargetController: new active target set in model " + activeTarget.getServer());  

            if(activeTarget.isChannel()) { 
                refreshUsersInChannel();
            }

        for(ActiveTargetListener listener: activeListeners) {
            listener.onSetActiveTarget(target);
        }
    }

    private void unsetActiveTarget() {
        activeTarget = null;
        activeTargetName = null;

        for(ActiveTargetListener listener: activeListeners) {
            listener.onSetActiveTarget(null);
        }       
    } 

    public void onSetActiveServer(ServerModel activeModel) {
            unsetActiveTarget();
            this.activeModel = activeModel;
            System.out.println("TargetController: new active model" + activeModel.toString());  
            this.activeModel.addTargetListener(this);
            refreshUsersInHistory(); 
            refreshChannels();
    }

    private void createNewChannel() {
        if(activeModel == null) {
            return;
        }

        String newChannelKey = view.getNewChannelFieldValue();
        
        List<String> joinedChannelKeys = getJoinedChannelKeys();
        if (joinedChannelKeys.contains(newChannelKey)) {
            return;
        }

        view.addNewChannel(newChannelKey); 
        setActiveTarget(newChannelKey); 
        view.addActiveTargetListener(newChannelKey, e -> setActiveTarget(newChannelKey)); 
    }

    private void leaveChannel() {
        if(!activeTarget.isChannel()) {
            return;
        }
        
        String channelToLeave = activeTargetName;
        activeModel.partChannel(channelToLeave).thenRun(() -> { 
            view.leaveChannel(channelToLeave);
            unsetActiveTarget();    
        });
    }
        
    List<String> userHistoryKeys = new ArrayList<>();
    private void messageNewUser() {
        if(activeModel == null) {
            return;
        }

        String newUserKey = view.getNewUserFieldValue();
        
        if(userHistoryKeys.contains(newUserKey)) {
            return;
        }
         
        view.addToUserHistory(newUserKey);
        setActiveTarget(newUserKey);
        userHistoryKeys.add(newUserKey); 
        view.addActiveTargetListener(newUserKey, e -> setActiveTarget(newUserKey)); 
    }

    public void onQuit(String usernameWhoQuit) {
        view.deleteFromUserHistory(usernameWhoQuit);
        userHistoryKeys.remove(usernameWhoQuit);
    }

    public void onJoinChannel(String usernameWhoJoined, Channel channel) {
        if (!isTargetInActive(channel)) {
            return;
        }

        view.addToChannelUsers(usernameWhoJoined);
    }

    public void onPartChannel(String usernameWhoQuit, Channel channel) {
       if (!isTargetInActive(channel)) {
            return;
        } 
        
        view.deleteFromChannelUsers(usernameWhoQuit);
    }

    public void onMessageRecieved(Message message) {
        if (!isMessageFromActive(message)) {
            return;
        }
        
        System.out.println("TargetController: Message notified from " + message.getSender() + " to " + message.getTarget() + " saying " + message.getMessage());

        for(ActiveTargetListener listener: activeListeners) {
            listener.onMessageRecieved(message);
        } 
    }

    private boolean isMessageFromActive(Message message) {
        System.out.println("Checking if message from active|" + message.getServer() + "|" + activeModel.toString() + "|" + message.getServer().equals(activeModel.toString()) + "||" + message.getTarget() + "|" + activeTargetName + "|" + message.getTarget().equals(activeTargetName)+ "|||" + (message.getServer().equals(activeModel.toString()) && message.getTarget().equals(activeTargetName))); 
        return message.getServer().equals(activeModel.toString()) && message.getTarget().equals(activeTargetName); 
    }

    private boolean isTargetInActive(Target target) {
        return target.getServer().equals(activeModel.toString()) && target.getName().equals(activeTargetName);
    }

    //Listeners   
    private List<ActiveTargetListener> activeListeners = new ArrayList<>(); 

    public void addActiveListener(ActiveTargetListener activeListener) {
        activeListeners.add(activeListener);
    }
}
