package com.github.davidmoten.rx2.json;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class Util {
    
    private Util() {
        // prevent instantiation
    }
    
    public static final ObjectMapper MAPPER = new ObjectMapper();

}
