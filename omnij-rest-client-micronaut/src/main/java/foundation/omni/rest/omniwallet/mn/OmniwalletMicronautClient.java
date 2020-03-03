package foundation.omni.rest.omniwallet.mn;


import foundation.omni.rest.omniwallet.json.RevisionInfo;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.uri.UriBuilder;
import io.reactivex.Maybe;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static io.micronaut.http.HttpRequest.GET;

/**
 * Experimental, proof-of-concept Micronaut-based client for Omniwallet
 * (Use at your own risk, incomplete, etc.)
 */
public class OmniwalletMicronautClient {
    final URL baseURL;
    final RxHttpClient client;

    public OmniwalletMicronautClient(URL baseURL) {
        this.baseURL = baseURL;
        this.client = RxHttpClient.create(baseURL);
    }

    //@Override
    public Integer currentBlockHeight() throws InterruptedException, IOException  {
        Integer height;
        try {
            height = currentBlockHeightAsync().get();
        } catch (ExecutionException ee) {
            throw new IOException(ee);
        }
        return height;
    }

    //@Override
    public CompletableFuture<Integer> currentBlockHeightAsync() {
        return fromMaybe(revisionInfoAsync().map(RevisionInfo::getLastBlock));
    }

    private Maybe<RevisionInfo> revisionInfoAsync() {
        String uri = UriBuilder.of("/v1/system/revision.json").toString();

        return client.retrieve(GET(uri), RevisionInfo.class).firstElement();
    }

    public static <T> CompletableFuture<T> fromMaybe(Maybe<T> maybe) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        maybe.subscribe(future::complete, future::completeExceptionally);
        return future;
    }
}
