package it.ralisin.entities;

public class WekaObject {
    private int size;
    private double prediction;
    private String buggyness;

    public WekaObject() {
        //
    }

    public int getSize() {
        return size;
    }

    public double getPrediction() {
        return prediction;
    }

    public String getBuggyness() {
        return buggyness;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setPrediction(double prediction) {
        this.prediction = prediction;
    }

    public void setBuggyness(String buggyness) {
        this.buggyness = buggyness;
    }
}
