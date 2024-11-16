public class ChatServerMain {
   public static void main(String[] var0) {
      try {
         int var1 = Integer.parseInt(var0[0]);
         ChatServer var2 = new ChatServer(var1);
         var2.run();
      } catch (NumberFormatException | IndexOutOfBoundsException var3) {
         System.out.println("Usage: java -jar ChatServer.jar <port>");
      }

   }
}
