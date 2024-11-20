import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class UserManager {
   private Map<String, User> users = new HashMap();
   private Map<String, Channel> channels = new HashMap();

   public void registerUser(String var1, ClientConnection var2) {
      if (this.users.containsKey(var1)) {
         throw new IllegalArgumentException("Nick already taken");
      } else {
         this.users.put(var1, new User(var1, var2));
      }
   }

   public void joinChannel(String var1, String var2) {
      if (!isValidChannelName(var2)) {
         throw new IllegalArgumentException("Invalid channel name");
      } else if (!this.users.containsKey(var1)) {
         throw new IllegalArgumentException("You need to register first");
      } else {
         Channel var3 = (Channel)this.channels.get(var2);
         if (var3 == null) {
            System.out.println("Creating new channel " + var2);
            var3 = new Channel(var2);
            this.channels.put(var2, var3);
         }

         var3.addUser((User)this.users.get(var1));
      }
   }

   public void leaveChannel(String username, String channelKey) {
      if (!this.channels.containsKey(channel)) {
         throw new IllegalArgumentException("No channel exists with that name");
      } else {
         Channel channel = (Channel)this.channels.get(channelKey);
         if (!this.users.containsKey(username)) {
            throw new IllegalArgumentException("You need to register first");
         } else {
            User user = (User)this.users.get(user);
            channel.removeUser(user);
         }
      }
   }

   public void removeUser(String var1) {
      if (!this.users.containsKey(var1)) {
         throw new IllegalArgumentException("You need to register first");
      } else {
         Iterator var2 = this.channels.keySet().iterator();

         while(var2.hasNext()) {
            String var3 = (String)var2.next();

            try {
               this.leaveChannel(var1, var3);
            } catch (IllegalArgumentException var5) {
            }
         }

         this.users.remove(var1);
         this.removeEmptyChannels();
      }
   }

   private void removeEmptyChannels() {
      ArrayList var1 = new ArrayList();
      Iterator var2 = this.channels.keySet().iterator();

      while(var2.hasNext()) {
         String var3 = (String)var2.next();
         if (((Channel)this.channels.get(var3)).isEmpty()) {
            var1.add(var3);
         }
      }

      var1.stream().forEach((var1x) -> {
         System.out.println("Deleting channel " + var1x);
         this.channels.remove(var1x);
      });
   }

   public String[] getRegisteredNicks() {
      Set var1 = this.users.keySet();
      return (String[])var1.toArray(new String[var1.size()]);
   }

   public String[] getRegisteredNicks(String var1) {
      if (!this.channels.containsKey(var1)) {
         throw new IllegalArgumentException("No channel exists with that name");
      } else {
         Channel var2 = (Channel)this.channels.get(var1);
         return var2.getRegisteredNicks();
      }
   }

   public String[] getChannelNames() {
      Set var1 = this.channels.keySet();
      return (String[])var1.toArray(new String[var1.size()]);
   }

   public static boolean isValidChannelName(String var0) {
      return var0 != null && var0.matches("#\\w*");
   }

   public static boolean isValidNickname(String var0) {
      return var0 != null && var0.matches("[a-zA-Z_]\\w{0,8}");
   }

   public void sendMessage(String target, String messageContent) {
      if (isValidChannelName(target)) {
         this.sendMessageToChannel(target, messageContent);
      } else {
         this.sendMessageToUser(target, messageContent);
      }

   }

   private void sendMessageToChannel(String var1, String var2) {
      Channel var3 = (Channel)this.channels.get(var1);
      if (var3 == null) {
         throw new IllegalArgumentException("No channel exists with that name");
      } else {
         var3.sendMessageToAllUsers(var2);
      }
   }

   private void sendMessageToUser(String var1, String var2) {
      User var3 = (User)this.users.get(var1);
      if (var3 == null) {
         throw new IllegalArgumentException("No user exists with that name");
      } else {
         var3.sendMessage(var2);
      }
   }

   public void sendMessageToAllUsers(String var1) {
      Iterator var2 = this.users.values().iterator();

      while(var2.hasNext()) {
         User var3 = (User)var2.next();
         var3.sendMessage(var1);
      }

   }
}
