import java.util.concurrent.CompletableFuture;

// Functional interface for joinChannel method
@FunctionalInterface
public interface JoinChannelMethod {
    CompletableFuture<Channel> join(String channel);
}
