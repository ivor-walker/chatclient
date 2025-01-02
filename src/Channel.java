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

/**
 * Channel: specific type of Target that represents a channel in the server
 */

public class Channel extends Target {
        private List<String> users;

        public Channel(ServerModel model, String name, String[] users) {
                super(model, name);
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

        public void joinChannel() {
            model.joinChannel(name);
        }

        public void partChannel() {
            model.partChannel(name);
        }

        public void overwriteUsers(String[] users) {
                this.users = new ArrayList<>(Arrays.asList(users));
        }

        public boolean isChannel() {
            return true;
        }
}
                                      
