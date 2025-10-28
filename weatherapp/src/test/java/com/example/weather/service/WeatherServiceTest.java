// src/test/java/com/example/weather/service/WeatherServiceTest.java
package com.example.weather.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link WeatherService} class.
 * Verifies that cache-clearing does not throw when constructed with an API key.
 */
public class WeatherServiceTest {

    private WeatherService service;

    /**
     * Initializes a WeatherService with a dummy API key before each test.
     */
    @BeforeEach
    public void setUp() {
        // Your WeatherService constructor requires a String (the API key)
        service = new WeatherService("dummy-api-key");
    }

    /**
     * Ensures that calling {@code clearCache()} on the service
     * invalidates any internal cache without throwing an exception.
     */
    @Test
    public void testClearCacheDoesNotThrow() {
        service.clearCache();
        // If no exception is thrown, the test passes
    }
}
