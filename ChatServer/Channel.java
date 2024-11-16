import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Channel {
   private String name;
   private Set<User> users;

   public Channel(String var1) {
      this.name = var1;
      this.users = new HashSet();
   }

   public void addUser(User var1) {
      this.users.add(var1);
   }

   public void removeUser(User var1) {
      this.users.remove(var1);
   }

   public String getName() {
      return this.name;
   }

   public void sendMessageToAllUsers(String var1) {
      Iterator var2 = this.users.iterator();

      while(var2.hasNext()) {
         User var3 = (User)var2.next();
         var3.sendMessage(var1);
      }

   }

   public boolean isEmpty() {
      return this.users.size() == 0;
   }

   public String[] getRegisteredNicks() {
      String[] var1 = new String[this.users.size()];
      int var2 = 0;

      for(Iterator var3 = this.users.iterator(); var3.hasNext(); ++var2) {
         User var4 = (User)var3.next();
         var1[var2] = var4.getNick();
      }

      return var1;
   }
}
