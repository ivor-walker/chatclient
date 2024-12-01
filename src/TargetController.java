import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.stream.Stream;
import java.util.stream.Collectors;

import java.util.Arrays;
import java.util.Map;


public class TargetController implements ActiveServerListener {
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
        activeModel.getNamesInChannel(activeTargetName).thenAccept(userKeys -> {
            for(String userKey: userKeys) {
                view.addToChannelUsers(userKey);
                view.addActiveTargetListener(userKey, e -> setActiveTarget(userKey));
            }
        });
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
        
            Channel[] joinedChannels = activeModel.getJoinedChannels();
            List<String> joinedChannelKeys = Arrays.stream(joinedChannels) 
                .map(Channel::getName)
                .toList();

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
            System.out.println("setting active target key: " + targetKey); 
            if(activeTarget != null) { 
                view.setActiveTarget(activeTarget.getName(), targetKey);
            } else {
                view.setActiveTarget(targetKey);
            }

            activeTarget = target;
            activeTargetName = targetKey;
            
            if(target.isChannel()) { 
                refreshUsersInChannel();
            } 
    }
 
    public void onSetActiveServer(ServerModel activeModel) {
        this.activeModel = activeModel;
        refreshUsersInHistory(); 
        refreshChannels();
    }

    private void createNewChannel() {
        String newChannelKey = view.getNewChannelFieldValue();
        view.addNewChannel(newChannelKey); 
        setActiveTarget(newChannelKey); 
        view.addActiveTargetListener(newChannelKey, e -> setActiveTarget(newChannelKey)); 
    }

    private void messageNewUser() {
        String newUserKey = view.getNewUserFieldValue();
        view.addToUserHistory(newUserKey);
        setActiveTarget(newUserKey); 
        view.addActiveTargetListener(newUserKey, e -> setActiveTarget(newUserKey)); 
    }

    //Listeners   
    private List<ActiveTargetListener> activeListeners = new ArrayList<>(); 

    public void addActiveListener(ActiveTargetListener activeListener) {
        activeListeners.add(activeListener);
    }
}
