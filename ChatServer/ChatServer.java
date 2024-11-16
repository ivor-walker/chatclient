import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
   private int port;
   private UserManager userManager;

   public ChatServer(int var1) {
      this.port = var1;
      this.userManager = new UserManager();
   }

   public void run() {
      System.out.println("Running chat server on port " + this.port + "...");
      System.out.println("Press Ctrl-C to quit.");

      try {
         ServerSocket var1 = new ServerSocket(this.port);

         try {
            while(true) {
               Socket var2 = var1.accept();
               ClientConnection var3 = new ClientConnection(var2, this.userManager);
               var3.start();
            }
         } catch (Throwable var5) {
            try {
               var1.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }

            throw var5;
         }
      } catch (IOException var6) {
         System.err.println(var6.getMessage());
      }
   }
}
