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


public class Message {
        private String sender;
        private String target;
        private String messageContent;
        private LocalDateTime serverTime;
        private LocalDateTime clientTime;
        private String serverString;

        public Message(String sender, String target, String messageContent, String serverTime, String serverString) {
                this.sender = sender;
                this.target = target;
                this.messageContent = messageContent;
                this.serverString = serverString;
                this.serverTime = LocalDateTime.parse(serverTime);
                this.clientTime = LocalDateTime.now();
        }

        DateTimeFormatter userFriendlyFormat = DateTimeFormatter.ofPattern("E dd-MM-yyyy HH:mm:ss");
        public String toString() {
                String userFriendlyClientTime = clientTime.format(userFriendlyFormat);
                return "[" + userFriendlyClientTime + "] " + sender + ": " + messageContent;
        }

        public LocalDateTime getServerTime() {
                return this.serverTime;
        }

	    public String getTarget() {
	    	return target;
	    }

        public String getServer() {
            return serverString;
        }

        public String getSender() {
            return sender;
        }

        public String getMessage() {
            return messageContent;
        }
}

