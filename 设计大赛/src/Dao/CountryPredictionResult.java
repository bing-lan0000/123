package Dao;

public class CountryPredictionResult {
    public String country;
    public double confidence;

    public CountryPredictionResult(String country, double confidence) {
        this.country = country;
        this.confidence = confidence;
    }
}
