# rxjava2-json
RxJava 2 utilities for JSON.

Status: *in development*

## About
Project goals are these:

* present queries on streams of JSON text as an RxJava stream (`Flowable`, `Maybe`, `Single`)
* reduce memory, cpu and io use by only parsing JSON parts that are queried  
* support backpressure 
* make a certain class of JSON query easily expressable in RxJava 2.x

## Examples

We'll use this JSON to show some extraction methods:

```json
{
  "books": [
    {
      "isbn": "9781593275846",
      "title": "Eloquent JavaScript, Second Edition",
      "subtitle": "A Modern Introduction to Programming",
      "author": "Marijn Haverbeke",
      "published": "2014-12-14T00:00:00.000Z",
      "publisher": "No Starch Press",
      "pages": 472,
      "description": "JavaScript lies at the heart of almost every modern web application, from social apps to the newest browser-based games. Though simple for beginners to pick up and play with, JavaScript is a flexible, complex language that you can use to build full-scale applications.",
      "website": "http://eloquentjavascript.net/"
    },
    ...
}
```

### Extract a repeated element as a Flowable
Here we count the distinct authors from the input JSON:

```java
long count = 
  Json.stream(inputStream)
    .fieldArray("books") //
    .field("author") //
    .map(node -> node.asText()) //
    .distinct() //
    .count() //
    .blockingGet();;      .
```

