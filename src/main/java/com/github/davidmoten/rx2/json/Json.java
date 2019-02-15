package com.github.davidmoten.rx2.json;

import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

public final class Json {

    private final Flowable<JsonParser> flowable;

    public static Json stream(Callable<InputStream> in) {
        return new Json(Flowable.using(in, is -> flowable(is), is -> is.close(), true));
    }

    public static Json stream(InputStream in) {
        return new Json(flowable(in));
    }

    private static Flowable<JsonParser> flowable(InputStream in) {
        return Flowable.generate(() -> {
            JsonFactory factory = new JsonFactory();
            return factory.createParser(in);
        }, (p, emitter) -> {
            if (p.nextToken() != null) {
                emitter.onNext(p);
            } else {
                emitter.onComplete();
            }
        });
    }

    private Json(Flowable<JsonParser> flowable) {
        this.flowable = flowable;
    }

    public Json field(String name) {
        return new Json(Flowable.defer(() -> {
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
                    .skipWhile(p -> !(p.currentToken() == JsonToken.FIELD_NAME //
                            && p.currentName().equals(name) //
                            && depth.get() == 1)) //
                    .takeUntil(p -> depth.get() == 0);
        }));
    }

    public static String indent(int n) {
        StringBuilder s = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            s.append("  ");
        }
        return s.toString();
    }

    public Flowable<JsonParser> get() {
        return flowable;
    }

    public JsonArray fieldArray(String name) {
        return new JsonArray(field(name).flowable);
    }

    public Maybe<ObjectNode> objectNode() {
        return flowable //
                .skipWhile(p -> p.currentToken() == JsonToken.FIELD_NAME) //
                .map(p -> Util.MAPPER.readTree(p)).filter(x -> x instanceof ObjectNode) //
                .cast(ObjectNode.class) //
                .firstElement();
    }

    public Maybe<ValueNode> valueNode() {
        return flowable //
                .skipWhile(p -> p.currentToken() == JsonToken.FIELD_NAME) //
                .map(p -> Util.MAPPER.readTree(p)).filter(x -> x instanceof ValueNode) //
                .cast(ValueNode.class) //
                .firstElement();
    }

}
