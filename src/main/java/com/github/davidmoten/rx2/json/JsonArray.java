package com.github.davidmoten.rx2.json;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.reactivex.Flowable;


public final class JsonArray {

    private final Flowable<JsonParser> stream;
    private final ObjectMapper mapper;

    JsonArray(Flowable<JsonParser> flowable, ObjectMapper mapper) {
        this.stream = flowable;
        this.mapper = mapper;
    }

    public <T> Flowable<LazyTreeNode> nodes() {
        return nodes_().map(p -> Util.MAPPER.readTree(p));
    }

    public Flowable<LazyObjectNode> objectNodes() {
        return nodes_().map(p -> new LazyObjectNode(p));
    }

    public Flowable<LazyValueNode> valueNodes() {
        return nodes_().map(p -> new LazyValueNode(p));
    }

    public Flowable<LazyArrayNode> arrayNodes() {
        return nodes_().map(p -> new LazyArrayNode(p, mapper));
    }

    public Flowable<JsonNode> field(String name) {
        return objectNodes().map(on -> on.get().get(name));
    }

    private Flowable<JsonParser> nodes_() {
        return Flowable.defer(() -> {
            int[] depth = new int[1];
            return stream //
                    .doOnNext(p -> {
                        JsonToken t = p.currentToken();
                        if (t == JsonToken.START_OBJECT || t == JsonToken.START_ARRAY) {
                            depth[0]++;
                        } else if (t == JsonToken.END_OBJECT || t == JsonToken.END_ARRAY) {
                            depth[0]--;
                        }
                    }) //
                    .skipWhile(p -> p.currentToken() == JsonToken.FIELD_NAME
                            || p.currentToken() == JsonToken.START_ARRAY) //
                    .takeUntil(p -> depth[0] == 0) //
                    .filter(p -> p.currentToken() != JsonToken.END_ARRAY
                            && p.currentToken() != JsonToken.END_OBJECT); //
        });
    }

}
