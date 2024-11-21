import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Arrays;

public class ClientConnection extends Thread {
   private static final String INFO_TEXT = "Simplified IRC server written by M Young, 2022–2024";
   private static int count = 0;
   private int id;
   private Socket socket;
   private OutputStream out;
   private BufferedReader reader;
   private PrintWriter writer;
   private UserManager userManager;
   private String nick;
   private boolean isRegistered;
   private boolean quitting;

   public ClientConnection(Socket var1, UserManager var2) {
      this.socket = var1;
      this.userManager = var2;
      this.id = count++;

      try {
         this.out = var1.getOutputStream();
         this.reader = new BufferedReader(new InputStreamReader(var1.getInputStream()));
         this.writer = new PrintWriter(new OutputStreamWriter(this.out));
      } catch (IOException var4) {
         System.err.println(var4.getMessage());
      }

      this.isRegistered = false;
      this.quitting = false;
   }

   public void run() {
      System.out.println("Connected to client " + this.id);

      try {
         while(!this.quitting) {
            String var1 = this.reader.readLine();
            System.out.println("<client " + this.id + "> " + var1);
            if (var1 == null) {
               break;
            }

            this.handleMessageFromUser(var1);
         }

         this.close();
      } catch (IOException var2) {
         System.err.println(var2.getMessage());
      }

      if (this.isRegistered) {
         this.userManager.sendMessageToAllUsers(":" + this.nick + " QUIT");
         this.userManager.removeUser(this.nick);
      }

      System.out.println("Disconnected from client " + this.id);
   }

   private void handleMessageFromUser(String var1) {
      String[] var2 = var1.split("\\s+", 2);
      String var3 = var2[0];
      String var4 = var2.length > 1 ? var2[1] : "";

      try {
         byte var6 = -1;
         switch(var3.hashCode()) {
         case 2251950:
            if (var3.equals("INFO")) {
               var6 = 8;
            }
            break;
         case 2282794:
            if (var3.equals("JOIN")) {
               var6 = 2;
            }
            break;
         case 2336926:
            if (var3.equals("LIST")) {
               var6 = 5;
            }
            break;
         case 2396003:
            if (var3.equals("NICK")) {
               var6 = 0;
            }
            break;
         case 2448371:
            if (var3.equals("PART")) {
               var6 = 3;
            }
            break;
         case 2455922:
            if (var3.equals("PING")) {
               var6 = 9;
            }
            break;
         case 2497103:
            if (var3.equals("QUIT")) {
               var6 = 1;
            }
            break;
         case 2575053:
            if (var3.equals("TIME")) {
               var6 = 7;
            }
            break;
         case 74047272:
            if (var3.equals("NAMES")) {
               var6 = 4;
            }
            break;
         case 403496530:
            if (var3.equals("PRIVMSG")) {
               var6 = 6;
            }
         }

         switch(var6) {
         case 0:
            this.nickCommand(var4);
            break;
         case 1:
            this.quitCommand();
            break;
         case 2:
            this.joinCommand(var4);
            break;
         case 3:
            this.partCommand(var4);
            break;
         case 4:
            this.namesCommand(var4);
            break;
         case 5:
            this.listCommand();
            break;
         case 6:
            this.privMsgCommand(var4);
            break;
         case 7:
            this.timeCommand();
            break;
         case 8:
            this.infoCommand();
            break;
         case 9:
            this.pingCommand(var4);
         }
      } catch (ClientConnection.NotRegisteredException var7) {
         this.sendPromptToRegister();
      } catch (ClientConnection.AlreadyRegisteredException | IllegalArgumentException var8) {
         this.sendErrorReply(var8.getMessage());
      }

   }

   private void nickCommand(String var1) throws ClientConnection.AlreadyRegisteredException {
      if (!UserManager.isValidNickname(var1)) {
         throw new IllegalArgumentException("Invalid nickname");
      } else {
         this.tryToRegister(var1);
      }
   }

   private void quitCommand() {
      this.quitting = true;
   }

   private void joinCommand(String var1) throws ClientConnection.NotRegisteredException {
      this.checkUserIsRegistered();
      this.userManager.joinChannel(this.nick, var1);
      String var2 = String.format(":%s JOIN %s", this.nick, var1);
      this.userManager.sendMessage(var1, var2);
   }

   private void partCommand(String channel) throws ClientConnection.NotRegisteredException {
      this.checkUserIsRegistered();
      String var2 = String.format(":%s PART %s", this.nick, channel);
      this.userManager.sendMessage(var1, var2);
      this.userManager.leaveChannel(this.nick, var1);
   }

   private void namesCommand(String var1) throws ClientConnection.NotRegisteredException {
      this.checkUserIsRegistered();
      String[] var2 = this.userManager.getRegisteredNicks(var1);
      Arrays.sort(var2);
      String var3 = var1 + " :" + String.join(" ", var2);
      this.sendReply("REPLY_NAMES", var3);
   }

   private void listCommand() throws ClientConnection.NotRegisteredException {
      this.checkUserIsRegistered();
      String[] var1 = this.userManager.getChannelNames();
      Arrays.sort(var1);
      String var2 = ":" + String.join(" ", var1);
      this.sendReply("REPLY_LIST", var2);
   }

   private void privMsgCommand(String clientMessage) throws ClientConnection.NotRegisteredException {
      this.checkUserIsRegistered();
      String[] messageContentSplit = clientMessage.split("\\s+", 2);
      if (messageContentSplit.length < 2) {
         throw new IllegalArgumentException("Invalid arguments to PRIVMSG command");
      } else {
         String target = messageContentSplit[0];
         String message = messageContentSplit[1];
         if (!message.startsWith(":")) {
            throw new IllegalArgumentException("Invalid arguments to PRIVMSG command");
         } else {
            String message = String.format(":%s PRIVMSG %s %s", this.nick, target, message);
            this.userManager.sendMessage(target, message);
         }
      }
   }

   private void timeCommand() {
      this.sendReply("REPLY_TIME", ":" + LocalDateTime.now().toString());
   }

   private void infoCommand() {
      this.sendReply("REPLY_INFO", ":Simplified IRC server written by M Young, 2022–2024");
   }

   private void pingCommand(String var1) {
      this.sendMessage("PONG " + var1);
   }

   private void tryToRegister(String var1) throws ClientConnection.AlreadyRegisteredException {
      if (this.isRegistered) {
         throw new ClientConnection.AlreadyRegisteredException();
      } else {
         assert var1 != null;

         this.userManager.registerUser(var1, this);
         this.nick = var1;
         this.sendWelcomeReply();
         this.isRegistered = true;
      }
   }

   private void checkUserIsRegistered() throws ClientConnection.NotRegisteredException {
      if (!this.isRegistered) {
         throw new ClientConnection.NotRegisteredException();
      }
   }

   private void sendWelcomeReply() {
      this.sendReply("REPLY_NICK", ":Welcome to the IRC network, " + this.nick);
   }

   private void sendPromptToRegister() {
      this.sendErrorReply("You need to register first");
   }

   private void sendErrorReply(String var1) {
      this.sendReply("ERROR", ":" + var1);
   }

   private void sendReply(String var1, String var2) {
      this.sendMessage(var1 + " " + var2);
   }

   public void sendMessage(String var1) {
      System.out.println("To client " + this.id + "> " + var1);
      this.writer.print(var1 + "\r\n");
      this.writer.flush();
   }

   private void close() throws IOException {
      this.reader.close();
      this.writer.close();
      this.socket.close();
   }

   private class NotRegisteredException extends Exception {
   }

   private class AlreadyRegisteredException extends Exception {
      public AlreadyRegisteredException() {
         super("You are already registered");
      }
   }
}
