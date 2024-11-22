private class Target {
        private String name;
        private List<Message> messages = new ArrayList<>();

        public Target(String name) {
                this.name = name;
        }

        public Target(String name, List<String> users) {
                this = new Channel(name, users);
        }

        public List<Message> getMessages() {
                sortMessages();
                return messages;
        }

        public LocalDateTime getServerTimeOfLastMessage() {
                sortMessages();
                return messages[messages.length].getServerTime();
        }

        public void sortMessages() {
                messages.sort(
                        Comparator.comparing(Message::getServerTime)
                );
        }

        public void addMessage(Message message) {
                messages.add(message);
        }

        public String getName() {
                return name;
        }

	public boolean isChannel() {
		return false;
	}
}

