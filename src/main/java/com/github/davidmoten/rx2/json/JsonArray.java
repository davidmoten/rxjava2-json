package com.github.davidmoten.rx2.json;

import static com.github.davidmoten.rx2.json.Util.MAPPER;

import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;

import io.reactivex.Flowable;

public final class JsonArray {

    private final Flowable<JsonParser> flowable;

    JsonArray(Flowable<JsonParser> flowable) {
        this.flowable = flowable;
    }

    public <T> Flowable<LazyTreeNode> nodes() {
        return nodes_().map(p -> MAPPER.readTree(p));
    }

    public Flowable<LazyObjectNode> objectNodes() {
        return nodes_().map(p -> new LazyObjectNode(p));
    }

    public Flowable<LazyValueNode> valueNodes() {
        return nodes_().map(p -> new LazyValueNode(p));
    }

    public Flowable<LazyArrayNode> arrayNodes() {
        return nodes_().map(p -> new LazyArrayNode(p));
    }

    public Flowable<JsonNode> field(String name) {
        return objectNodes().map(on -> on.get().get(name));
    }

    private Flowable<JsonParser> nodes_() {
        return Flowable.defer(() -> {
            // TODO use single element array instead of AtomicXXX
            AtomicInteger depth = new AtomicInteger();
            return flowable //
                    .doOnNext(p -> {
                        JsonToken t = p.currentToken();
                        if (t == JsonToken.START_OBJECT || t == JsonToken.START_ARRAY) {
                            depth.incrementAndGet();
                        } else if (t == JsonToken.END_OBJECT || t == JsonToken.END_ARRAY) {
                            depth.decrementAndGet();
                        }
                    }) //
                    .skipWhile(p -> p.currentToken() == JsonToken.FIELD_NAME
                            || p.currentToken() == JsonToken.START_ARRAY) //
                    .takeUntil(p -> depth.get() == 0) //
                    .filter(p -> p.currentToken() != JsonToken.END_ARRAY
                            && p.currentToken() != JsonToken.END_OBJECT); //
        });
    }

}
