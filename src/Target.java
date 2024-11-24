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
import java.util.Comparator;

public class Target {
        private String name;
        private List<Message> messages = new ArrayList<>();

        public Target(String name) {
                this.name = name;
        }

        public List<Message> getMessages() {
                sortMessages();
                return messages;
        }

        public LocalDateTime getServerTimeOfLastMessage() {
                sortMessages();
                return messages.get(messages.size() - 1).getServerTime();
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

