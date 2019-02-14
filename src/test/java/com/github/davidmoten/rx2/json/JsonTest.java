package com.github.davidmoten.rx2.json;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.reactivex.Flowable;

public class JsonTest {

    @Test
    public void test() throws JsonParseException, IOException {

        JsonFactory factory = new JsonFactory();
        ObjectMapper m = new ObjectMapper();
        JsonParser p = factory
                .createParser(new BufferedInputStream(JsonTest.class.getResourceAsStream("/test1.json"), 4));
        while (p.nextToken() != null) {
            System.out.println(p.currentToken() + ": " + p.getCurrentName() + "=" + p.getText());
            if (false && p.currentToken() == JsonToken.START_ARRAY) {
                while (p.nextToken() == JsonToken.START_OBJECT) {
                    // read everything from this START_OBJECT to the matching END_OBJECT
                    // and return it as a tree model ObjectNode
                    ObjectNode node = m.readTree(p);
                    System.out.println(node.get("value"));
                }
            }
        }
    }

    @Test
    public void testFlowable() {
        InputStream input = JsonTest.class.getResourceAsStream("/test1.json");
        Json.stream(input) //
                .field("menu") //
                .field("popup") //
                .fieldArray("menuItem") //
                .field("value") //
                .map(JsonNode::asText) //
                .test() //
                .assertValues("New", "Open", "Close") //
                .assertComplete();
    }

    @Test
    public void testFlowableObjectNode() {
        InputStream input = JsonTest.class.getResourceAsStream("/test1.json");
        Json.stream(input) //
                .field("menu") //
                .objectNode() //
                .map(on -> on.fieldNames().next())
                .test() //
                .assertValues("id") //
                .assertComplete();
    }

}
