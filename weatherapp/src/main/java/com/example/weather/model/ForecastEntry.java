// src/main/java/com/example/weather/model/ForecastEntry.java
package com.example.weather.model;

/**
 * A single forecast data point, containing the time of the forecast
 * and the temperature at that time.
 */
public class ForecastEntry {
    private long timestamp;
    private double temp;

    /**
     * Gets the forecast time as epoch seconds.
     *
     * @return the timestamp in seconds since the epoch
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the forecast time.
     *
     * @param timestamp the timestamp in seconds since the epoch
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the temperature value for this forecast entry.
     *
     * @return the temperature in degrees (Celsius or Fahrenheit, depending on units)
     */
    public double getTemp() {
        return temp;
    }

    /**
     * Sets the temperature value for this forecast entry.
     *
     * @param temp the temperature in degrees (Celsius or Fahrenheit, depending on units)
     */
    public void setTemp(double temp) {
        this.temp = temp;
    }
}
