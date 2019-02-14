package com.github.davidmoten.rx2.json;

import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.reactivex.Flowable;

public final class JsonArray {

    private Flowable<JsonParser> flowable;
    private final static ObjectMapper MAPPER = new ObjectMapper();

    JsonArray(Flowable<JsonParser> flowable) {
        this.flowable = flowable;
    }

    public <T> Flowable<TreeNode> nodes() {
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
                    .skipWhile(
                            p -> p.currentToken() == JsonToken.FIELD_NAME || p.currentToken() == JsonToken.START_ARRAY) //
                    .takeUntil(p -> depth.get() == 0) //
                    .filter(p -> p.currentToken() != JsonToken.END_ARRAY && p.currentToken() != JsonToken.END_OBJECT); //
        }).map(p -> MAPPER.readTree(p));
    }

    public Flowable<ObjectNode> objectNodes() {
        return nodes().filter(node -> node instanceof ObjectNode).cast(ObjectNode.class);
    }

}
