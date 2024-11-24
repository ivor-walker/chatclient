import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.NoRouteToHostException;
import java.net.ConnectException;
import java.net.BindException;
import java.net.SocketException;
import java.io.EOFException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Channel extends Target {
        private List<String> users;

        public Channel(String name, String[] users) {
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

        public void overwriteUsers(String[] users) {
                this.users = new ArrayList<>(Arrays.asList(users));
        }

	public boolean isChannel() {
		return true;
	}
}
                                      
