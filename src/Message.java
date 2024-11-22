private class Message {
        private String sender;
        private String target;
        private String messageContent;
        private LocalDateTime serverTime;
        private LocalDateTime clientTime;

        private Message(String sender, String target, String messageContent, String serverTime) {
                this.sender = sender;
                this.target = target;
                this.messageContent = messageContent;

                this.serverTime = LocalDateTime.parse(serverTime);
                this.clientTime = LocalDateTime.now();
        }

        DateTimeFormatter userFriendlyFormat = DateTimeFormatter.ofPattern("E dd-MM-yyyy HH:mm:ss");
        public toString() {
                String userFriendlyClientTime = clientTime.format(userFriendlyFormat);
                return "[" + userFriendlyClientTime + "] " + sender + ": " + messageContent;
        }

        public LocalDateTime getServerTime() {
                return this.serverTime;
        }
}

