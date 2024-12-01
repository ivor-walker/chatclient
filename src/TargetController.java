import java.util.List;
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
 
    public TargetController(TargetPanel view) {
        this.view = view;
        setupListeners();
        refreshUsersInHistory();
    }

    private void setupListeners() {
        view.refreshChannelsListener(e -> refreshChannels());
        view.refreshUsersInChannelListener(e -> refreshUsersInChannel());    
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

    public void refreshUsersInChannel() {
        view.resetUsersInChannel();
        addUsersInChannel();
    }

    public void addUsersInChannel() {
        activeModel.getNamesInChannel(activeTargetName).thenAccept(userKeys -> {
            for(String userKey: userKeys) {
                view.addToChannelUsers(userKey);
                view.addActiveTargetListener(userKey, e -> setActiveTarget(userKey));
            }
        });
    }

    public void refreshChannels() {
        view.resetChannels();
        addChannels();
    }

    public void addChannels() {
        activeModel.getOfferedChannels().thenAccept(channelKeys -> {
            Channel[] joinedChannels = activeModel.getJoinedChannels();
            List<String> joinedChannelKeys = Arrays.stream(joinedChannels) 
                .map(Channel::getName)
                .toList();
 
            for(String channelKey: channelKeys) {
                view.addActiveTargetListener(channelKey, e -> setActiveTarget(channelKey)); 
                if (joinedChannelKeys.contains(channelKey)) {
                    view.addJoinedChannel(channelKey);
                    view.addLeaveChannelListener(channelKey, e -> leaveChannel(channelKey));
                } else {
                    view.addNewChannel(channelKey);
                    view.addJoinChannelListener(channelKey, e -> joinChannel(channelKey));
                }
            }            
        });
    }

    private void setActiveTarget(String targetKey) {
        Target target = activeModel.getTargets().get(targetKey);
         
        if(target == null) {
            activeModel.joinChannel(targetKey); 
        }

        activeTarget = target;
        activeTargetName = activeTarget.getName();
    }

    private void joinChannel(String channelKey) {
        activeModel.joinChannel(channelKey); 
    }

    private void leaveChannel(String channelKey) {
        activeModel.partChannel(channelKey); 
    }

    public void onSetActiveServer(ServerModel activeModel) {
        this.activeModel = activeModel;
        refreshChannels();
        refreshUsersInChannel();
        refreshUsersInHistory(); 
    }
}
