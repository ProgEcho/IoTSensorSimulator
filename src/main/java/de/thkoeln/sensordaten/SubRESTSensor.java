package de.thkoeln.sensordaten;

public class SubRESTSensor extends SubSensor {
    private String unterPfad;
    private String datenTyp;
    private double wertIntervalMin;
    private double wertIntervalMax;
    private boolean zeitStempel;

    public SubRESTSensor(String unterPfad, String datenTyp, double wertIntervalMin, double wertIntervalMax, boolean zeitStempel) {
        this.unterPfad = unterPfad;
        this.datenTyp = datenTyp;
        this.wertIntervalMin = wertIntervalMin;
        this.wertIntervalMax = wertIntervalMax;

        this.zeitStempel = zeitStempel;
    }

    public String getUnterPfad() {
        return unterPfad;
    }

    @Override
    public String getDatenTyp() {
        return datenTyp;
    }

    @Override
    public double getWertIntervalMin() {
        return wertIntervalMin;
    }

    @Override
    public double getWertIntervalMax() {
        return wertIntervalMax;
    }

    @Override
    public boolean isZeitStempel() {
        return zeitStempel;
    }

    @Override
    public void setDatenTyp(String datenTyp) {

    }

    @Override
    public void setWertIntervalMax(double wertIntervalMax) {

    }

    @Override
    public void setWertIntervalMin(double wertIntervalMin) {

    }

    @Override
    public void setZeitStempel(boolean zeitStempel) {

    }
}
