package com.github.davidmoten.rx2.json;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;

public class JsonTest {

    @Test
    public void test() throws JsonParseException, IOException {

        JsonFactory factory = new JsonFactory();
        JsonParser p = factory
                .createParser(new BufferedInputStream(input(1), 4));
        while (p.nextToken() != null) {
            System.out.println(p.currentToken() + ": " + p.getCurrentName() + "=" + p.getText());
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
                .map(JsonNode::asText) // s
                .test() //
                .assertValues("New", "Open", "Close") //
                .assertComplete();
    }

    @Test
    public void testFlowableObjectNode() {
        Json.stream(input(1)) //
                .field("menu") //
                .objectNode() //
                .map(on -> on.get().fieldNames().next()).test() //
                .assertValues("id") //
                .assertComplete();
    }

    @Test
    public void testFlowableValueNode() {
        Json.stream(input(1)) //
                .field("menu") //
                .field("id") //
                .valueNode() //
                .map(n -> n.asText()).test() //
                .assertValues("file") //
                .assertComplete();
    }

    @Test
    public void testFlowableNestedArrays() {
        Json.stream(input(1)) //
                .fieldArray("menu") //
                .field("id") //
                .map(n -> n.asText()).test() //
                .assertValues("file") //
                .assertComplete();
    }

    private static InputStream input(int i) {
        return JsonTest.class.getResourceAsStream("/test" + i + ".json");
    }

}
