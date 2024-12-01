import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface PartChannelMethod {
    CompletableFuture<Void> part(String channel);
}
