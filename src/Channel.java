private class Channel extends Target {
        private List<String> users = new ArrayList<>();

        public Channel(String name, List<String> users) {
                super(name);
                overwriteUsers(users);
        }

        public List<String> getUsers() {
                return users;
        }

        public void addUser(String username) {
                users.add(username);
        }

        public void removeUser(String username) {
                users.remove(username);
        }

        public void overwriteUsers(List<String> users) {
                this.users = users;
        }

	public boolean isChannel() {
		return true;
	}
}
                                      
