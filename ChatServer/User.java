public class User {
   private String nick;
   private ClientConnection conn;

   public User(String var1, ClientConnection var2) {
      this.nick = var1;
      this.conn = var2;
   }

   public String getNick() {
      return this.nick;
   }

   public void sendMessage(String var1) {
      this.conn.sendMessage(var1);
   }
}
