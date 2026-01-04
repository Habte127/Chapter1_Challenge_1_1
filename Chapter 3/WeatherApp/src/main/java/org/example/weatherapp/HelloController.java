package org.example.weatherapp;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class HelloController {

    @FXML
    private TextField cityTextField;

    @FXML
    private Label weatherLabel;

    private WeatherService weatherService;

    public void initialize() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            weatherService = (WeatherService) registry.lookup("WeatherService");
        } catch (Exception e) {
            e.printStackTrace();
            weatherLabel.setText("Failed to connect to the server.");
        }
    }

    @FXML
    protected void onGetWeatherClick() {
        try {
            String city = cityTextField.getText();
            if (city != null && !city.isEmpty()) {
                String weather = weatherService.getWeather(city);
                weatherLabel.setText(weather);
            } else {
                weatherLabel.setText("Please enter a city.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            weatherLabel.setText("Error fetching weather.");
        }
    }
}
