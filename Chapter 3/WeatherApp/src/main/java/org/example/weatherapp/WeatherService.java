package org.example.weatherapp;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WeatherService extends Remote {
    String getWeather(String city) throws RemoteException;
}
