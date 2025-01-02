/**
 * Controller of the MessagePanel
 * Handles sending and receiving messages from the active target
 */
public class MessageController implements ActiveTargetListener {
    // MessagePanel being controlled by this controller 
    private MessagePanel view;
    
    // The active target that the user is currently messaging
    private Target activeTarget;
    private String activeTargetName;

    // Title of the message history when no target is selected
    private String EMPTY_TARGET_TITLE = "Nothing"; 
   
    /**
     * Constructor for the MessageController 
     * @param view The MessagePanel being controlled by this controller
     */
    public MessageController(MessagePanel view) {
        this.view = view;
       
        // Configure listeners  
        setupListeners();

        // Set the initial title of the message history
        view.setHistoryName(EMPTY_TARGET_TITLE);
    }

    /**
     * Sets up the listeners for the MessagePanel
     */
    public void setupListeners() {
        // Listener for when client sends a message         
        view.addSendButtonListener(e -> sendMessage());
    }

    /**
     * Sends a message to the active target
     */
    public void sendMessage() {
        // Get the message from the input area
        String message = view.getMessageInput();

        // Send message to the active target
        activeTarget.sendMessage(activeTargetName, message);

        // Reset the input area
        view.resetInputArea(); 
    }

    /**
     * Sets the active target to the given target
     * @param target The target to set as the active target
     */
    public void onSetActiveTarget(Target target) {
        // Remove previous target's message history
        view.resetMessageHistory();
         
        // Set the new active target 
        activeTarget = target;

        // If the target is null (e.g user connected to the server but hasn't selected a target yet)
        if(target == null) {
            // Reset the message history title 
            view.setHistoryName(EMPTY_TARGET_TITLE);

            activeTargetName = null;
            return;
        
        }
        
        // Add the target's messages to the message history    
        readdMessages();

        // Set the title of the message history to the target's name
        activeTargetName = activeTarget.getName();
        view.setHistoryName(activeTargetName);
    }

    /**
     * Adds the messages of the active target to the message history
     */
    public void readdMessages() {
        // For each message in active target's message history
        for(Message message : activeTarget.getMessages()) {
            // Add the message to the message history
            view.addMessage(message.toString()); 
        }
    }

    /**
     * Listener for when a message is received from the active target
     * @param message The message that was received
     */
    public void onMessageRecieved(Message message) {
        //TODO redrawing messages is inefficient, should be able to do view.addMessage(message.toString()) but am experiencing a phantom message bug where this listener is called multiple times for multiple connections.
        view.resetMessageHistory();
        readdMessages();
        // view.addMessage(message.toString());
    }
}
