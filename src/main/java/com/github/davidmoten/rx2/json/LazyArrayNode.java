package com.github.davidmoten.rx2.json;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.reactivex.Flowable;

public final class LazyArrayNode implements Supplier<ArrayNode> {

    private final JsonParser parser;
    private final ObjectMapper mapper;

    LazyArrayNode(JsonParser parser, ObjectMapper mapper) {
        this.parser = parser;
        this.mapper = mapper;
    }

    @Override
    public ArrayNode get() {
        try {
            return (ArrayNode) mapper.readTree(parser);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Flowable<JsonNode> values() {
        return Flowable.defer(() -> {
            // skip array start
            return Flowable.generate(emitter -> {
                JsonToken token = parser.nextToken();
                if (token.equals(JsonToken.END_ARRAY)) {
                    emitter.onComplete();
                } else {
                    TreeNode v = mapper.readTree(parser);
                    emitter.onNext((JsonNode) v);
                }
            });
        });
    }

    public <T> Flowable<T> values(Class<T> cls) {
        return Flowable.defer(() -> {
            // skip array start
            parser.nextToken();
            return Flowable.generate(emitter -> {
                if (parser.isClosed()) {
                    emitter.onComplete();
                } else {
                    T v = mapper.readValue(parser, cls);
                    if (v == null) {
                        emitter.onComplete();
                    } else {
                        emitter.onNext(v);
                    }
                }
            });
        });
    }

}
