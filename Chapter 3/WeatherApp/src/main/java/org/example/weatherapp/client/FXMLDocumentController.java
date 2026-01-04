package org.example.weatherapp.client;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.effect.DropShadow;
import javafx.util.Duration;
import org.example.weatherapp.shared.WeatherService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class FXMLDocumentController {

    @FXML private StackPane mainRoot;

    // Main Card UI
    @FXML private TextField cityField;
    @FXML private Label cityLabel;
    @FXML private Label localTimeLabel;
    @FXML private Label temperatureLabel;
    @FXML private Label descriptionLabel;
    @FXML private ImageView weatherIcon;
    @FXML private StackPane animationPane;
    @FXML private StackPane backgroundAnimationPane;
    @FXML private HBox forecastContainer;
    @FXML private Label humidityLabel;
    @FXML private Label windLabel;
    @FXML private Label precipLabel;
    
    // Favorites UI
    @FXML private HBox favoritesContainer;
    private final List<String> favoriteCities = new ArrayList<>();
    
    // Detailed Forecast Pane UI
    @FXML private VBox forecastDetailPane;
    @FXML private Label detailDayLabel;
    @FXML private Label detailTempMinMaxLabel;
    @FXML private Label detailFeelsLikeLabel;
    @FXML private Label detailPressureLabel;
    @FXML private Label detailCloudinessLabel;
    @FXML private Label detailWindDirectionLabel;
    @FXML private Label detailPopLabel;

    // Settings UI
    @FXML private VBox settingsPane;
    @FXML private CheckBox notificationToggle;
    @FXML private TextField defaultCityInput;

    private WeatherService weatherService;
    private final JSONParser parser = new JSONParser();
    private String currentCity = "Gonder";
    private boolean isCelsius = true;
    private JSONObject currentWeatherData;
    private String currentForecastJson;
    
    // Settings State
    private String currentBackground = "default";
    private boolean is3DIcons = false;
    private Preferences prefs;

    public void setWeatherService(WeatherService weatherService) {
        this.weatherService = weatherService;
        initialize();
    }

    private void initialize() {
        prefs = Preferences.userNodeForPackage(FXMLDocumentController.class);
        loadSettings();
        
        if (cityField != null) {
            cityField.setOnAction(event -> searchWeather());
        }

        if (weatherService == null) {
            if (cityLabel != null) cityLabel.setText("Server Not Connected");
            if (cityField != null) {
                cityField.setDisable(true);
                cityField.setPromptText("Connection failed");
            }
            if (temperatureLabel != null) temperatureLabel.setText("");
            if (descriptionLabel != null) descriptionLabel.setText("Please start the server and restart the client.");
        } else {
            loadDefaultWeather();
        }
    }
    
    private void loadSettings() {
        currentCity = prefs.get("defaultCity", "Gonder");
        isCelsius = prefs.getBoolean("isCelsius", true);
        is3DIcons = prefs.getBoolean("is3DIcons", false);
        currentBackground = prefs.get("background", "default");
        boolean notificationsEnabled = prefs.getBoolean("notificationsEnabled", false);
        
        if (notificationToggle != null) {
            notificationToggle.setSelected(notificationsEnabled);
        }
        if (defaultCityInput != null) {
            defaultCityInput.setText(currentCity);
        }
        
        applyBackground(currentBackground);
    }

    private void loadDefaultWeather() {
        new Thread(() -> fetchWeatherAndForecast(currentCity)).start();
    }

    @FXML
    private void searchWeather() {
        String city = cityField.getText().trim();
        if (city.isEmpty()) {
            cityLabel.setText("Enter a city name!");
            return;
        }
        new Thread(() -> fetchWeatherAndForecast(city)).start();
    }
    
    @FXML
    private void useCurrentLocation() {
        // Simulating London coordinates
        double lat = 51.5074;
        double lon = -0.1278;
        
        new Thread(() -> fetchWeatherByCoordinates(lat, lon)).start();
    }
    
    private void fetchWeatherByCoordinates(double lat, double lon) {
        if (weatherService == null) {
            Platform.runLater(() -> cityLabel.setText("Server not connected."));
            return;
        }

        try {
            String weatherJsonString = weatherService.getWeatherByCoordinates(lat, lon);
            JSONObject weatherData = (JSONObject) parser.parse(weatherJsonString);

            if (weatherData.isEmpty() || (weatherData.containsKey("cod") && !String.valueOf(weatherData.get("cod")).equals("200"))) {
                Platform.runLater(() -> {
                    clearUI();
                    cityLabel.setText("Location not found!");
                });
                return;
            }

            String forecastJsonString = weatherService.getForecastByCoordinates(lat, lon);

            Platform.runLater(() -> {
                try {
                    currentWeatherData = weatherData;
                    currentForecastJson = forecastJsonString;
                    updateWeatherUI(weatherData);
                    updateForecastUI(forecastJsonString);
                    // Update current city only if successful
                    currentCity = (String) weatherData.get("name");
                    
                    // Check for notifications
                    if (notificationToggle.isSelected()) {
                        checkAndNotify(weatherData);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    clearUI();
                    cityLabel.setText("Error parsing server data.");
                }
            });

        } catch (RemoteException | ParseException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                clearUI();
                cityLabel.setText("Error fetching data from server.");
            });
        }
    }
    
    @FXML
    private void addToFavorites() {
        if (currentCity != null && !currentCity.isEmpty() && !favoriteCities.contains(currentCity)) {
            favoriteCities.add(currentCity);
            updateFavoritesUI();
        }
    }
    
    @FXML
    private void toggleUnits() {
        isCelsius = !isCelsius;
        prefs.putBoolean("isCelsius", isCelsius);
        if (currentWeatherData != null) {
            updateWeatherUI(currentWeatherData);
        }
        if (currentForecastJson != null) {
            try {
                updateForecastUI(currentForecastJson);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void openSettings() {
        settingsPane.setVisible(true);
        settingsPane.setManaged(true);
        defaultCityInput.setText(prefs.get("defaultCity", "Gonder"));
    }

    @FXML
    private void closeSettings() {
        settingsPane.setVisible(false);
        settingsPane.setManaged(false);
    }
    
    @FXML
    private void showDashboard() {
        closeSettings();
        // Logic to switch to dashboard view if we had multiple views
    }
    
    @FXML
    private void clearCache() {
        try {
            prefs.clear();
            // Reload defaults
            loadSettings();
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Cache Cleared");
            alert.setHeaderText(null);
            alert.setContentText("Settings and cache have been cleared. Restarting default view.");
            alert.show();
            
            // Reset UI
            clearUI();
            loadDefaultWeather();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Weather App");
        alert.setHeaderText("Weather App v1.0");
        alert.setContentText("Developed with JavaFX and OpenWeatherMap API.\n\nFeatures:\n- Real-time Weather\n- 5-Day Forecast\n- Location Search\n- Favorites\n- Customizable Themes");
        alert.show();
    }
    
    @FXML
    private void saveDefaultCity() {
        String newDefault = defaultCityInput.getText().trim();
        if (!newDefault.isEmpty()) {
            prefs.put("defaultCity", newDefault);
            closeSettings();
        }
    }
    
    @FXML
    private void toggleNotifications() {
        prefs.putBoolean("notificationsEnabled", notificationToggle.isSelected());
    }
    
    @FXML
    private void setBackgroundDefault() {
        applyBackground("default");
        prefs.put("background", "default");
    }

    @FXML
    private void setBackgroundForest() {
        applyBackground("forest");
        prefs.put("background", "forest");
    }

    @FXML
    private void setBackgroundCity() {
        applyBackground("city");
        prefs.put("background", "city");
    }
    
    private void applyBackground(String type) {
        currentBackground = type;
        String style = "";
        switch (type) {
            case "forest":
                style = "-fx-background-image: url('https://images.unsplash.com/photo-1448375240586-dfd8f3793371?q=80&w=2670&auto=format&fit=crop'); -fx-background-size: cover;";
                break;
            case "city":
                style = "-fx-background-image: url('https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?q=80&w=2613&auto=format&fit=crop'); -fx-background-size: cover;";
                break;
            default:
                style = "-fx-background-image: url('https://images.unsplash.com/photo-1513002749550-c59d786b8e6c?q=80&w=2574&auto=format&fit=crop'); -fx-background-size: cover;";
                break;
        }
        if (mainRoot != null) {
            mainRoot.setStyle(style);
        }
    }
    
    @FXML
    private void setIconStyle2D() {
        is3DIcons = false;
        prefs.putBoolean("is3DIcons", false);
        if (currentWeatherData != null) updateWeatherUI(currentWeatherData);
        if (currentForecastJson != null) {
            try {
                updateForecastUI(currentForecastJson);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void setIconStyle3D() {
        is3DIcons = true;
        prefs.putBoolean("is3DIcons", true);
        if (currentWeatherData != null) updateWeatherUI(currentWeatherData);
        if (currentForecastJson != null) {
            try {
                updateForecastUI(currentForecastJson);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void updateFavoritesUI() {
        favoritesContainer.getChildren().clear();
        for (String city : favoriteCities) {
            Label chip = new Label(city);
            chip.getStyleClass().add("favorite-chip");
            chip.setOnMouseClicked(e -> {
                cityField.setText(city);
                searchWeather();
            });
            favoritesContainer.getChildren().add(chip);
        }
    }

    private void fetchWeatherAndForecast(String city) {
        if (weatherService == null) {
            Platform.runLater(() -> cityLabel.setText("Server not connected."));
            return;
        }

        try {
            String weatherJsonString = weatherService.getWeatherJson(city);
            JSONObject weatherData = (JSONObject) parser.parse(weatherJsonString);

            if (weatherData.isEmpty() || (weatherData.containsKey("cod") && !String.valueOf(weatherData.get("cod")).equals("200"))) {
                Platform.runLater(() -> {
                    clearUI();
                    cityLabel.setText("City not found!");
                });
                return;
            }

            String forecastJsonString = weatherService.getForecastJson(city);

            Platform.runLater(() -> {
                try {
                    currentWeatherData = weatherData;
                    currentForecastJson = forecastJsonString;
                    updateWeatherUI(weatherData);
                    updateForecastUI(forecastJsonString);
                    // Update current city only if successful
                    currentCity = (String) weatherData.get("name");
                    
                    // Check for notifications
                    if (notificationToggle.isSelected()) {
                        checkAndNotify(weatherData);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    clearUI();
                    cityLabel.setText("Error parsing server data.");
                }
            });

        } catch (RemoteException | ParseException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                clearUI();
                cityLabel.setText("Error fetching data from server.");
            });
        }
    }
    
    private void checkAndNotify(JSONObject data) {
        JSONArray weatherArray = (JSONArray) data.get("weather");
        JSONObject weatherObj = (JSONObject) weatherArray.get(0);
        String desc = (String) weatherObj.get("description");
        
        if (desc.toLowerCase().contains("rain") || desc.toLowerCase().contains("storm") || desc.toLowerCase().contains("snow")) {
            // Simple visual notification (could be expanded to system tray)
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Weather Alert");
            alert.setHeaderText("Bad Weather Warning");
            alert.setContentText("It looks like " + desc + " in " + currentCity + ". Stay safe!");
            alert.show();
        }
    }

    private void clearUI() {
        temperatureLabel.setText("");
        descriptionLabel.setText("");
        humidityLabel.setText("");
        windLabel.setText("");
        precipLabel.setText("");
        localTimeLabel.setText("");
        weatherIcon.setImage(null);
        animationPane.getChildren().clear();
        backgroundAnimationPane.getChildren().clear();
        forecastContainer.getChildren().clear();
        if (forecastDetailPane != null) {
            forecastDetailPane.setVisible(false);
            forecastDetailPane.setManaged(false);
        }
    }

    private void updateWeatherUI(JSONObject data) {
        String name = (String) data.get("name");
        JSONObject sys = (JSONObject) data.get("sys");
        String country = (String) sys.get("country");
        JSONObject main = (JSONObject) data.get("main");
        double temp = getDouble(main.get("temp"));
        JSONArray weatherArray = (JSONArray) data.get("weather");
        JSONObject weatherObj = (JSONObject) weatherArray.get(0);
        String desc = (String) weatherObj.get("description");
        String icon = (String) weatherObj.get("icon");
        double humidity = getDouble(main.get("humidity"));
        JSONObject windObj = (JSONObject) data.get("wind");
        double wind = getDouble(windObj.get("speed"));

        double precip = 0.0;
        if (data.containsKey("rain")) {
            JSONObject rainObj = (JSONObject) data.get("rain");
            if (rainObj.containsKey("1h")) precip = getDouble(rainObj.get("1h"));
        } else if (data.containsKey("snow")) {
            JSONObject snowObj = (JSONObject) data.get("snow");
            if (snowObj.containsKey("1h")) precip = getDouble(snowObj.get("1h"));
        }
        
        String localTime = "";
        if (data.containsKey("local_time")) {
            localTime = (String) data.get("local_time");
        }

        cityLabel.setText(name + ", " + country);
        if (!localTime.isEmpty()) {
            localTimeLabel.setText("Local Time: " + localTime);
        } else {
            localTimeLabel.setText("");
        }
        
        temperatureLabel.setText(formatTemp(temp));
        descriptionLabel.setText(capitalize(desc));
        
        String iconUrl = "https://openweathermap.org/img/wn/" + icon + "@2x.png";
        if (is3DIcons) {
             // Using a different set or effect for "3D" - for now, we'll just use the same URL but maybe we could apply an effect
             // Or if you have local 3D assets, load them here. 
             // For demonstration, let's just apply a drop shadow to simulate depth on the existing icon
             weatherIcon.setEffect(new DropShadow(20, Color.BLACK));
        } else {
             weatherIcon.setEffect(null);
        }
        weatherIcon.setImage(new Image(iconUrl));
        
        humidityLabel.setText("Humidity: " + humidity + "%");
        windLabel.setText("Wind: " + wind + " m/s");
        
        updatePrecipLabel(precipLabel, precip, desc);

        showWeatherAnimation(desc);
    }

    private void updateForecastUI(String forecastJsonString) throws ParseException {
        forecastContainer.getChildren().clear();
        JSONObject json = (JSONObject) parser.parse(forecastJsonString);

        if (json.isEmpty() || !String.valueOf(json.get("cod")).equals("200")) return;

        JSONArray list = (JSONArray) json.get("list");
        
        String previousDate = "";
        int daysCount = 0;
        LocalDate today = LocalDate.now();

        for (int i = 0; i < list.size(); i++) {
            JSONObject obj = (JSONObject) list.get(i);
            String dtTxt = (String) obj.get("dt_txt");
            String currentDate = dtTxt.split(" ")[0];

            if (!currentDate.equals(previousDate)) {
                previousDate = currentDate;
                
                LocalDate date = LocalDate.parse(currentDate);
                
                if (date.equals(today)) {
                    continue;
                }

                JSONObject main = (JSONObject) obj.get("main");
                double temp = getDouble(main.get("temp"));
                double tempMin = getDouble(main.get("temp_min"));
                double tempMax = getDouble(main.get("temp_max"));
                double feelsLike = getDouble(main.get("feels_like"));
                double pressure = getDouble(main.get("pressure"));
                
                JSONObject clouds = (JSONObject) obj.get("clouds");
                double cloudiness = getDouble(clouds.get("all"));
                
                JSONObject windObj = (JSONObject) obj.get("wind");
                double wind = getDouble(windObj.get("speed"));
                double windDeg = getDouble(windObj.get("deg"));
                
                double pop = getDouble(obj.get("pop")) * 100;
                
                double precip = 0.0; 
                if (obj.containsKey("rain")) {
                     JSONObject rain = (JSONObject) obj.get("rain");
                     if (rain.containsKey("3h")) precip = getDouble(rain.get("3h"));
                }

                JSONArray weatherArray = (JSONArray) obj.get("weather");
                JSONObject weatherObj = (JSONObject) weatherArray.get(0);
                String desc = (String) weatherObj.get("description");
                String icon = (String) weatherObj.get("icon");

                String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

                VBox dayBox = createForecastCard(dayName, icon, temp, desc, tempMin, tempMax, feelsLike, pressure, cloudiness, windDeg, pop, precip);
                forecastContainer.getChildren().add(dayBox);

                daysCount++;
                if (daysCount >= 5) break;
            }
        }
    }

    private VBox createForecastCard(String dayName, String icon, double temp, String desc, 
                                    double min, double max, double feelsLike, double pressure, 
                                    double clouds, double windDeg, double pop, double precip) {
        VBox container = new VBox(5);
        container.setAlignment(Pos.BOTTOM_CENTER); 
        container.setPrefHeight(140); 
        container.setMinHeight(140);
        container.setMaxHeight(140);
        container.setPrefWidth(120); 

        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("day");
        card.setPrefSize(120, 115); 
        
        card.setOnMouseClicked(e -> showForecastDetails(dayName, desc, min, max, feelsLike, pressure, clouds, windDeg, pop, precip));
        card.setCursor(javafx.scene.Cursor.HAND);

        ImageView ic = new ImageView("https://openweathermap.org/img/wn/" + icon + "@2x.png");
        ic.setFitWidth(45);
        ic.setFitHeight(45);
        ic.getStyleClass().add("icon");
        
        if (is3DIcons) {
            ic.setEffect(new DropShadow(10, Color.BLACK));
        } else {
            ic.setEffect(null);
        }

        Label t = new Label(formatTemp(temp));
        t.getStyleClass().add("forecast-temp");

        Label descLabel = new Label(capitalize(desc));
        descLabel.getStyleClass().add("forecast-desc");
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Label d = new Label(dayName);
        d.getStyleClass().add("forecast-date-outside");

        card.getChildren().addAll(ic, t, descLabel);
        container.getChildren().addAll(d, card);
        
        return container;
    }
    
    private void showForecastDetails(String day, String desc, double min, double max, double feelsLike, double pressure, double clouds, double windDeg, double pop, double precip) {
        forecastDetailPane.setVisible(true);
        forecastDetailPane.setManaged(true);
        
        detailDayLabel.setText("Detailed Forecast for " + day);
        detailTempMinMaxLabel.setText(String.format("Min/Max Temp: %s / %s", formatTemp(min), formatTemp(max)));
        detailFeelsLikeLabel.setText(String.format("Feels Like: %s", formatTemp(feelsLike)));
        detailPressureLabel.setText(String.format("Pressure: %.0f hPa", pressure));
        detailCloudinessLabel.setText(String.format("Cloudiness: %.0f%%", clouds));
        detailWindDirectionLabel.setText(String.format("Wind Direction: %.0f°", windDeg));
        detailPopLabel.setText(String.format("Precip. Chance: %.0f%%", pop));
    }
    
    private void updatePrecipLabel(Label label, double precip, String desc) {
        if (precip > 0) {
            label.setText("Precip: " + String.format("%.2f", precip) + " mm");
        } else {
            String lowerDesc = desc.toLowerCase();
            if (lowerDesc.contains("rain") || lowerDesc.contains("drizzle") || lowerDesc.contains("snow")) {
                 label.setText("Precip: Trace");
            } else {
                 label.setText("Precip: 0 mm");
            }
        }
    }

    private void showWeatherAnimation(String desc) {
        animationPane.getChildren().clear();
        backgroundAnimationPane.getChildren().clear();
        
        desc = desc.toLowerCase();
        
        if (desc.contains("rain") || desc.contains("drizzle")) {
            showRainAnimation(animationPane); 
        } else if (desc.contains("clear") || desc.contains("sun")) {
            showSunAnimation(animationPane);
        } else if (desc.contains("cloud")) {
            showCloudAnimation(animationPane);
        } else if (desc.contains("snow")) {
            showSnowAnimation(animationPane);
        } else if (desc.contains("thunder") || desc.contains("storm")) {
            showThunderAnimation(animationPane);
        }
    }
    
    private void showSunAnimation(Pane pane) {
        Circle sun = new Circle(30, Color.GOLD);
        sun.setEffect(new DropShadow(20, Color.ORANGE));
        pane.getChildren().add(sun);
        
        RotateTransition rot = new RotateTransition(Duration.seconds(10), sun);
        rot.setByAngle(360);
        rot.setCycleCount(Animation.INDEFINITE);
        rot.setInterpolator(Interpolator.LINEAR);
        rot.play();
        
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2), sun);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.1);
        pulse.setToY(1.1);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
    }
    
    private void showCloudAnimation(Pane pane) {
        for (int i = 0; i < 3; i++) {
            Circle c = new Circle(20, Color.LIGHTGRAY);
            c.setOpacity(0.8);
            c.setLayoutX((i * 30) - 30);
            c.setLayoutY(Math.random() * 20 - 10);
            pane.getChildren().add(c);
            
            TranslateTransition tt = new TranslateTransition(Duration.seconds(4 + i), c);
            tt.setFromX(-10);
            tt.setToX(10);
            tt.setCycleCount(Animation.INDEFINITE);
            tt.setAutoReverse(true);
            tt.play();
        }
    }

    private void showRainAnimation(Pane pane) {
        int dropCount = 30; 
        double width = 100;
        double height = 100;
        
        // Add a cloud for context
        Circle cloud = new Circle(25, Color.GRAY);
        cloud.setTranslateY(-30);
        pane.getChildren().add(cloud);
        
        for (int i = 0; i < dropCount; i++) {
            Line drop = new Line(0, 0, 0, 5);
            drop.setStroke(Color.LIGHTBLUE);
            drop.setStrokeWidth(1.5);
            drop.setTranslateX(Math.random() * width - (width/2));
            drop.setTranslateY(Math.random() * height - (height/2));
            pane.getChildren().add(drop);
            
            TranslateTransition fall = new TranslateTransition(Duration.seconds(0.5 + Math.random() * 0.5), drop);
            fall.setByY(height);
            fall.setCycleCount(Animation.INDEFINITE);
            fall.setDelay(Duration.seconds(Math.random()));
            fall.setInterpolator(Interpolator.LINEAR);
            fall.play();
        }
    }
    
    private void showSnowAnimation(Pane pane) {
        int flakeCount = 20;
        double width = 100;
        double height = 100;
        
        for (int i = 0; i < flakeCount; i++) {
            Circle flake = new Circle(2, Color.WHITE);
            flake.setOpacity(0.8);
            flake.setTranslateX(Math.random() * width - (width/2));
            flake.setTranslateY(Math.random() * height - (height/2));
            pane.getChildren().add(flake);
            
            TranslateTransition fall = new TranslateTransition(Duration.seconds(2 + Math.random() * 2), flake);
            fall.setByY(height);
            fall.setCycleCount(Animation.INDEFINITE);
            fall.setDelay(Duration.seconds(Math.random()));
            fall.setInterpolator(Interpolator.LINEAR);
            fall.play();
        }
    }
    
    private void showThunderAnimation(Pane pane) {
        // Dark cloud
        Circle cloud = new Circle(30, Color.DARKGRAY);
        pane.getChildren().add(cloud);
        
        // Lightning bolt (simple polygon or line)
        Line bolt = new Line(0, -10, -5, 10);
        bolt.setStroke(Color.YELLOW);
        bolt.setStrokeWidth(2);
        bolt.setVisible(false);
        pane.getChildren().add(bolt);
        
        Timeline thunder = new Timeline(
            new KeyFrame(Duration.seconds(0), e -> bolt.setVisible(false)),
            new KeyFrame(Duration.seconds(2), e -> bolt.setVisible(true)),
            new KeyFrame(Duration.seconds(2.1), e -> bolt.setVisible(false)),
            new KeyFrame(Duration.seconds(2.2), e -> bolt.setVisible(true)),
            new KeyFrame(Duration.seconds(2.4), e -> bolt.setVisible(false))
        );
        thunder.setCycleCount(Animation.INDEFINITE);
        thunder.play();
        
        // Add rain as well
        showRainAnimation(pane);
    }

    private String capitalize(String t) {
        if (t == null || t.isEmpty()) return "";
        return t.substring(0, 1).toUpperCase() + t.substring(1);
    }

    private double getDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }
    
    private String formatTemp(double tempCelsius) {
        if (isCelsius) {
            return String.format("%.1f°C", tempCelsius);
        } else {
            double tempFahrenheit = (tempCelsius * 9/5) + 32;
            return String.format("%.1f°F", tempFahrenheit);
        }
    }
}
