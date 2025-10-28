package com.example.weather.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the current weather conditions as returned by the API.
 * Fields are bound to JSON properties via Jackson annotations.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherData {

    /** Current temperature in degrees (Celsius or Fahrenheit, depending on units). */
    @JsonProperty("temp")
    private double temp;

    /** “Feels like” temperature in degrees (accounting for wind chill or heat index). */
    @JsonProperty("feels_like")
    private double feelsLike;

    /** Humidity percentage (0–100%). */
    @JsonProperty("humidity")
    private int humidity;

    /** Pressure in hPa. */
    @JsonProperty("pressure")
    private int pressure;

    /** Wind speed in meters/sec or miles/hour. */
    private double windSpeed;

    /** Main weather category (e.g. "Rain", "Clear"). */
    private String main;

    /** More detailed description (e.g. "light rain"). */
    private String description;

    /** OpenWeather icon code (e.g. "10d"). */
    private String iconCode;

    /** Sunrise time as Unix timestamp. */
    private long sunrise;

    /** Sunset time as Unix timestamp. */
    private long sunset;

    /** Latitude. */
    private double lat;

    /** Longitude. */
    private double lon;

    /** City name returned by the API. */
    private String city;

    /**
     * @return the temperature
     */
    public double getTemp() {
        return temp;
    }

    /**
     * @param temp the temperature to set
     */
    public void setTemp(double temp) {
        this.temp = temp;
    }

    /**
     * @return the “feels like” temperature
     */
    public double getFeelsLike() {
        return feelsLike;
    }

    /**
     * @param feelsLike the “feels like” temperature to set
     */
    public void setFeelsLike(double feelsLike) {
        this.feelsLike = feelsLike;
    }

    /**
     * @return the humidity
     */
    public int getHumidity() {
        return humidity;
    }

    /**
     * @param humidity the humidity to set
     */
    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    /**
     * @return the pressure
     */
    public int getPressure() {
        return pressure;
    }

    /**
     * @param pressure the pressure to set
     */
    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    /**
     * @return the windSpeed
     */
    public double getWindSpeed() {
        return windSpeed;
    }

    /**
     * @param windSpeed the windSpeed to set
     */
    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    /**
     * @return the main
     */
    public String getMain() {
        return main;
    }

    /**
     * @param main the main to set
     */
    public void setMain(String main) {
        this.main = main;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the iconCode
     */
    public String getIconCode() {
        return iconCode;
    }

    /**
     * @param iconCode the iconCode to set
     */
    public void setIconCode(String iconCode) {
        this.iconCode = iconCode;
    }

    /**
     * @return the sunrise
     */
    public long getSunrise() {
        return sunrise;
    }

    /**
     * @param sunrise the sunrise to set
     */
    public void setSunrise(long sunrise) {
        this.sunrise = sunrise;
    }

    /**
     * @return the sunset
     */
    public long getSunset() {
        return sunset;
    }

    /**
     * @param sunset the sunset to set
     */
    public void setSunset(long sunset) {
        this.sunset = sunset;
    }

    /**
     * @return the latitude
     */
    public double getLat() {
        return lat;
    }

    /**
     * @param lat the latitude to set
     */
    public void setLat(double lat) {
        this.lat = lat;
    }

    /**
     * @return the longitude
     */
    public double getLon() {
        return lon;
    }

    /**
     * @param lon the longitude to set
     */
    public void setLon(double lon) {
        this.lon = lon;
    }

    /**
     * @return the city name
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city the city name to set
     */
    public void setCity(String city) {
        this.city = city;
    }
}
