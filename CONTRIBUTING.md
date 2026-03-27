# Contributing

## Adding a New Event Type

To add support for a new AWS event source:

1. **Add a fixture** — create a sample JSON file in `src/test/resources/sample-json/` named after the event (e.g. `cognito-event.json`). Use a real AWS event sample with fake account IDs (`123456789012`).

2. **Add detection** in `StreamInput.java`:
   - Add a private `identifiesAsXxxEvent()` method using the parsed `parsedJson` map (structural key checks, not substring matching)
   - Add a public `asXxxEvent()` method that calls `map(XxxEvent.class)`
   - Add a call to `identifiesAsXxxEvent()` in the `identify()` method, in the correct position (be mindful of detection order — more specific checks must come before general ones)
   - Add the class name to the error message in `identify()`

3. **Add routing** in `AbstractRequestStreamHandler.java`:
   - Add a `public void handle(XxxEvent event)` method that calls `throwMissingHandlerException`
   - Add an `else if (eventType == XxxEvent.class)` branch in `handleRequest()`

4. **Add a test** in `AbstractRequestStreamHandlerTest.java`:
   - Add an InputStream field loading your fixture
   - Add a `handle(XxxEvent event)` override in the `UUT` inner class
   - Add a test method following the existing pattern

5. **Update the event type table** in `README.md` and `CHANGELOG.md`

## Running Tests

```bash
./gradlew test
```

## Code Style

- Use the shaded Jackson from `com.amazonaws.lambda.thirdparty.com.fasterxml.jackson` (not a standalone Jackson dependency) to avoid runtime conflicts in the Lambda environment.
- Detection must use structural JSON inspection (parsed map), not substring matching.
- All public methods must have JavaDoc.
