// src/test/java/com/example/weather/model/ForecastEntryTest.java
package com.example.weather.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link ForecastEntry} data class.
 * Verifies that getters and setters behave correctly.
 */
public class ForecastEntryTest {

    /**
     * Tests that the timestamp setter and getter work as expected.
     */
    @Test
    public void testTimestampGetterAndSetter() {
        ForecastEntry entry = new ForecastEntry();
        long ts = 1_687_724_800L;
        entry.setTimestamp(ts);
        assertEquals(ts, entry.getTimestamp(),
            "getTimestamp() should return the value set by setTimestamp()");
    }

    /**
     * Tests that the temperature setter and getter work as expected.
     */
    @Test
    public void testTempGetterAndSetter() {
        ForecastEntry entry = new ForecastEntry();
        double temp = 23.5;
        entry.setTemp(temp);
        assertEquals(temp, entry.getTemp(), 0.0001,
            "getTemp() should return the value set by setTemp()");
    }
}
