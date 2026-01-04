package org.example.weatherapp.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WeatherService extends Remote {
    String getWeatherJson(String city) throws RemoteException;
    String getForecastJson(String city) throws RemoteException;
    String getCityDescription(String city) throws RemoteException;
    String getCityFacts(String city) throws RemoteException;
    String getWeatherByCoordinates(double lat, double lon) throws RemoteException;
    String getForecastByCoordinates(double lat, double lon) throws RemoteException;
}
