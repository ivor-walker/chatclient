public class MessageController implements ActiveTargetListener {
    private MessagePanel view;
    private Target activeTarget;
    private String activeTargetName;
    private String EMPTY_TARGET_TITLE = "Nothing"; 
    
    public MessageController(MessagePanel view) {
        this.view = view;

        setupListeners();

        view.setHistoryName(EMPTY_TARGET_TITLE);
    }

    public void setupListeners() {
        view.addSendButtonListener(e -> sendMessage());
    }

    public void sendMessage() {
        String message = view.getMessageInput();
        activeTarget.sendMessage(activeTargetName, message);
        System.out.println("MessageController for " + activeTarget.getServer() + ": Sent message from " + activeTarget.getServer() + " in MessageController");
        view.resetInputArea(); 
    }

    public void onSetActiveTarget(Target target) {
        view.resetMessageHistory();
    
        activeTarget = target;
        if(target == null) {
            view.setHistoryName(EMPTY_TARGET_TITLE);
            activeTargetName = null;
            return;
        
        }
            
        readdMessages();
        activeTargetName = activeTarget.getName();
        view.setHistoryName(activeTargetName);
    }

    public void readdMessages() {
        for(Message message : activeTarget.getMessages()) {
            System.out.println("message in channel: " + message.toString()); 
            view.addMessage(message.toString()); 
        }
    }

    public void onMessageRecieved(Message message) {
            //TODO redrawing messages is inefficient, should be able to do view.addMessage(message.toString()) but am experiencing a phantom message bug where this listener is called multiple times for multiple connections.
            view.resetMessageHistory();
            readdMessages();
    }
}
