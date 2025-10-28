package com.example.weather.controller;

import com.example.weather.model.ForecastEntry;
import com.example.weather.model.WeatherData;
import com.example.weather.service.WeatherService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Controller for the main WeatherApp UI defined in main.fxml.
 * Handles fetching current weather, forecasts, unit toggling, auto-refresh,
 * cache clearing, CSV export functionality—and now Dark Mode.
 */
public class MainController {

    /** Root pane, used to toggle dark-mode style class on/off. */
    @FXML private BorderPane rootPane;

    /** ComboBox for selecting or entering a city name. */
    @FXML private ComboBox<String> cityBox;

    /** Button for toggling between metric and imperial units. */
    @FXML private Button unitBtn;

    /** CheckBox to toggle dark mode on/off. */
    @FXML private CheckBox darkModeCheck;

    /** Labels for displaying current temperature, feels-like temperature,
     *  wind speed, and humidity. */
    @FXML private Label tempLabel, feelsLabel, windLabel, humLabel;

    /** Labels for displaying pressure, UV index, and sunrise/sunset times. */
    @FXML private Label presLabel, uvLabel, sunLabel;

    /** ImageView for showing the current weather icon. */
    @FXML private ImageView iconView;

    /** CheckBox to enable or disable automatic data refresh. */
    @FXML private CheckBox autoRefreshCheck;

    /** Label for showing the last data update timestamp. */
    @FXML private Label statusLabel;

    // Service layer for API calls
    private final WeatherService service =
        new WeatherService("df9e5eed0b0c211c0136dbf89522f1b7");

    // Current unit system: "metric" or "imperial"
    private String units = "metric";

    // Formatters for display purposes
    private final DateTimeFormatter timeFmt =
        DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());
    private final DateTimeFormatter stampFmt =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    // Scheduler for auto-refresh
    private ScheduledExecutorService scheduler;

    /**
     * Initializes UI components and sets up the auto-refresh & dark-mode listeners.
     * Adds a default city "Prague" to the ComboBox.
     */
    @FXML
    public void initialize() {
        cityBox.setEditable(true);
        cityBox.getItems().add("Prague");
        scheduler = Executors.newSingleThreadScheduledExecutor();

        autoRefreshCheck.selectedProperty().addListener((obs, old, val) -> {
            if (val) {
                scheduler.scheduleAtFixedRate(
                    () -> Platform.runLater(this::onFetch),
                    15, 15, TimeUnit.MINUTES
                );
            } else {
                scheduler.shutdownNow();
                scheduler = Executors.newSingleThreadScheduledExecutor();
            }
        });

        darkModeCheck.selectedProperty().addListener((obs, old, isDark) -> {
            onToggleDarkMode();
        });
    }

    /**
     * Toggles temperature units between metric and imperial.
     * Updates the unitBtn text accordingly.
     */
    @FXML
    private void onToggleUnit() {
        if ("metric".equals(units)) {
            units = "imperial";
            unitBtn.setText("°F → °C");
        } else {
            units = "metric";
            unitBtn.setText("°C → °F");
        }
    }

    /**
     * Toggles the 'dark-mode' style class on the root pane.
     * When checked, applies dark-mode overrides defined in app.css.
     */
    @FXML
    private void onToggleDarkMode() {
        if (darkModeCheck.isSelected()) {
            if (!rootPane.getStyleClass().contains("dark-mode")) {
                rootPane.getStyleClass().add("dark-mode");
            }
        } else {
            rootPane.getStyleClass().remove("dark-mode");
        }
    }

    /**
     * Fetches current weather data and updates the UI.
     * Also retrieves UV index and weather icon.
     */
    @FXML
    private void onFetch() {
        String city = cityBox.getEditor().getText().trim();
        if (city.isEmpty()) return;
        if (!cityBox.getItems().contains(city)) cityBox.getItems().add(city);

        new Thread(() -> {
            try {
                WeatherData wd = service.getCurrent(city, units);
                double uvi = service.fetchUVIndex(wd.getLat(), wd.getLon());

                Platform.runLater(() -> {
                    tempLabel.setText(String.format("%.1f°%s",
                        wd.getTemp(), units.equals("metric")?"C":"F"
                    ));
                    feelsLabel.setText(String.format("%.1f°%s",
                        wd.getFeelsLike(), units.equals("metric")?"C":"F"
                    ));
                    windLabel.setText(String.format("%.1f %s",
                        wd.getWindSpeed(), units.equals("metric")?"m/s":"mph"
                    ));
                    humLabel.setText(wd.getHumidity() + "%");
                    presLabel.setText(wd.getPressure() + " hPa");
                    uvLabel.setText(String.format("%.1f", uvi));
                    sunLabel.setText(
                        timeFmt.format(Instant.ofEpochSecond(wd.getSunrise()))
                        + " / "
                        + timeFmt.format(Instant.ofEpochSecond(wd.getSunset()))
                    );

                    // update timestamp
                    statusLabel.setText(stampFmt.format(Instant.now()));

                    try (InputStream is = new URL(
                          "https://openweathermap.org/img/wn/"
                          + wd.getIconCode() + "@2x.png"
                        ).openStream()
                    ) {
                        iconView.setImage(new Image(is));
                    } catch (Exception ignored) {}
                });
            } catch (Exception ex) {
                Platform.runLater(() ->
                    new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait()
                );
            }
        }).start();
    }

    /**
     * Fetches and displays a 3-day (24-hour interval) forecast dialog.
     */
    @FXML
    private void onForecast3() {
        String city = cityBox.getEditor().getText().trim();
        if (city.isEmpty()) return;
        new Thread(() -> {
            try {
                List<ForecastEntry> list = service.getForecast(city, units, 24);
                StringBuilder sb = new StringBuilder();
                DateTimeFormatter fmt = DateTimeFormatter
                  .ofPattern("yyyy-MM-dd HH:mm")
                  .withZone(ZoneId.systemDefault());
                for (int i = 0; i < list.size(); i += 8) {
                    ForecastEntry e = list.get(i);
                    sb.append(fmt.format(Instant.ofEpochSecond(e.getTimestamp())))
                      .append(String.format(": %.1f°%s%n",
                          e.getTemp(), units.equals("metric")?"C":"F"
                      ));
                }
                Platform.runLater(() ->
                    new Alert(Alert.AlertType.INFORMATION, sb.toString()).showAndWait()
                );
            } catch (Exception ex) {
                Platform.runLater(() ->
                    new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait()
                );
            }
        }).start();
    }

    /**
     * Fetches and displays an hourly forecast dialog for the next 4 hours.
     */
    @FXML
    private void onForecastHourly() {
        String city = cityBox.getEditor().getText().trim();
        if (city.isEmpty()) return;
        new Thread(() -> {
            try {
                List<ForecastEntry> list = service.getForecast(city, units, 4);
                StringBuilder sb = new StringBuilder();
                DateTimeFormatter fmt = DateTimeFormatter
                  .ofPattern("HH:mm")
                  .withZone(ZoneId.systemDefault());
                for (ForecastEntry e : list) {
                    sb.append(fmt.format(Instant.ofEpochSecond(e.getTimestamp())))
                      .append(String.format(": %.1f°%s%n",
                          e.getTemp(), units.equals("metric")?"C":"F"
                      ));
                }
                Platform.runLater(() ->
                    new Alert(Alert.AlertType.INFORMATION, sb.toString()).showAndWait()
                );
            } catch (Exception ex) {
                Platform.runLater(() ->
                    new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait()
                );
            }
        }).start();
    }

    /**
     * Clears the internal cache and resets the city history to default.
     */
    @FXML
    private void onClearCache() {
        service.clearCache();
        Platform.runLater(() -> {
            cityBox.getItems().clear();
            cityBox.getItems().add("Prague");
            new Alert(Alert.AlertType.INFORMATION,
                      "Cache cleared and city list reset").showAndWait();
        });
    }

    /**
     * Exports the 3-day forecast to a CSV file in the user's home directory.
     * Columns: datetime, temperature.
     */
    @FXML
    private void onExportCsv() {
        String city = cityBox.getEditor().getText().trim();
        if (city.isEmpty()) return;
        new Thread(() -> {
            try {
                List<ForecastEntry> list = service.getForecast(city, units, 24);
                Path out = Paths.get(
                    System.getProperty("user.home"),
                    city + "_forecast.csv"
                );
                try (BufferedWriter w = Files.newBufferedWriter(out)) {
                    w.write("datetime,temp" + System.lineSeparator());
                    DateTimeFormatter fmt = DateTimeFormatter
                      .ofPattern("yyyy-MM-dd HH:mm")
                      .withZone(ZoneId.systemDefault());
                    for (ForecastEntry e : list) {
                        w.write(fmt.format(Instant.ofEpochSecond(e.getTimestamp()))
                                + "," + e.getTemp()
                                + System.lineSeparator());
                    }
                }
                Platform.runLater(() ->
                    new Alert(Alert.AlertType.INFORMATION,
                              "Exported to " + out.toString()
                    ).showAndWait()
                );
            } catch (Exception ex) {
                Platform.runLater(() ->
                    new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait()
                );
            }
        }).start();
    }

    /**
     * Displays a simple LineChart of the next four forecast points (hourly).
     */
    @FXML
    private void onShowHourlyChart() {
        String city = cityBox.getEditor().getText().trim();
        if (city.isEmpty()) return;

        new Thread(() -> {
            try {
                List<ForecastEntry> list = service.getForecast(city, units, 4);

                // build chart data
                CategoryAxis xAxis = new CategoryAxis();
                xAxis.setLabel("Time");
                NumberAxis yAxis = new NumberAxis();
                yAxis.setLabel("Temp (" + (units.equals("metric")?"°C":"°F") + ")");
                LineChart<String,Number> chart = new LineChart<>(xAxis,yAxis);
                chart.setTitle("Next 4 Points (Hourly)");

                XYChart.Series<String,Number> series = new XYChart.Series<>();
                for (ForecastEntry e : list) {
                    String t = timeFmt.format(Instant.ofEpochSecond(e.getTimestamp()));
                    series.getData().add(new XYChart.Data<>(t, e.getTemp()));
                }
                chart.getData().add(series);

                Platform.runLater(() -> {
                    Stage s = new Stage();
                    s.setTitle("Hourly Forecast Chart");
                    Scene scene = new Scene(chart, 600, 400);
                    scene.getStylesheets().add(
                      getClass().getResource("/styles/app.css").toExternalForm()
                    );
                    s.setScene(scene);
                    s.show();
                });
            } catch (Exception ignored) {}
        }).start();
    }

    /**
     * Displays a simple LineChart of a 3-day (daily) forecast using every 8th point.
     */
    @FXML
    private void onShowDailyChart() {
        String city = cityBox.getEditor().getText().trim();
        if (city.isEmpty()) return;

        new Thread(() -> {
            try {
                List<ForecastEntry> list = service.getForecast(city, units, 24);

                CategoryAxis xAxis = new CategoryAxis();
                xAxis.setLabel("Date");
                NumberAxis yAxis = new NumberAxis();
                yAxis.setLabel("Temp (" + (units.equals("metric")?"°C":"°F") + ")");
                LineChart<String,Number> chart = new LineChart<>(xAxis,yAxis);
                chart.setTitle("3-Day Forecast Chart");

                XYChart.Series<String,Number> series = new XYChart.Series<>();
                DateTimeFormatter fmt = DateTimeFormatter
                  .ofPattern("yyyy-MM-dd")
                  .withZone(ZoneId.systemDefault());
                for (int i = 0; i < list.size(); i += 8) {
                    ForecastEntry e = list.get(i);
                    String d = fmt.format(Instant.ofEpochSecond(e.getTimestamp()));
                    series.getData().add(new XYChart.Data<>(d, e.getTemp()));
                }
                chart.getData().add(series);

                Platform.runLater(() -> {
                    Stage s = new Stage();
                    s.setTitle("3-Day Forecast Chart");
                    Scene scene = new Scene(chart, 600, 400);
                    scene.getStylesheets().add(
                      getClass().getResource("/styles/app.css").toExternalForm()
                    );
                    s.setScene(scene);
                    s.show();
                });
            } catch (Exception ignored) {}
        }).start();
    }

}
