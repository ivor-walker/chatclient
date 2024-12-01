public class MessageController {
    private MessagePanel view;
    private Target activeTarget;
    
    public MessageController(MessagePanel view) {
        this.view = view;
        setupListeners();
    }

    public void setupListeners() {
        view.addSendButtonListener(e -> sendMessage());
    }

    public void sendMessage() {
        String message = view.getMessageInput();
        activeTarget.sendMessage(activeTarget.getName(), message);
    }
}
