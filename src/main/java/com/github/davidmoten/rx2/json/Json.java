package com.github.davidmoten.rx2.json;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import io.reactivex.Flowable;

public final class Json {

    private final Flowable<JsonParser> flowable;

    public static Json parse(InputStream in) {
        return new Json(Flowable.generate(() -> {
            JsonFactory factory = new JsonFactory();
            return factory.createParser(in);
        }, (p, emitter) -> {
            if (p.nextToken() != null) {
                emitter.onNext(p);
            } else {
                emitter.onComplete();
            }
        }));
    }

    private Json(Flowable<JsonParser> flowable) {
        this.flowable = flowable;
    }

    public Json field(String name) {
        return new Json(Flowable.defer(() -> {
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
                    .filter(p -> p.currentToken() == JsonToken.FIELD_NAME //
                            && p.currentName().equals(name) //
                            && depth.get() == 1);
        }));
    }

    public Flowable<JsonParser> get() {
        return flowable;
    }

}
