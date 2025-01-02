import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface JoinChannelMethod {
    CompletableFuture<Channel> join(String channel);
}
