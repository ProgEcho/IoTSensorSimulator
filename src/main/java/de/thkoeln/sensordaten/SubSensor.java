package de.thkoeln.sensordaten;

abstract public class SubSensor {
    private String unterPfad;
    private String datenTyp;
    private double wertIntervalMax;
    private double wertIntervalMin;
    private boolean zeitStempel;

    public String getUnterPfad() {
        return unterPfad;
    }
    abstract public String getDatenTyp();

    abstract public double getWertIntervalMax();

    abstract public double getWertIntervalMin();

    abstract public boolean isZeitStempel();

    abstract public void setDatenTyp(String datenTyp);

    abstract public void setWertIntervalMax(double wertIntervalMax);

    abstract public void setWertIntervalMin(double wertIntervalMin);

    abstract public void setZeitStempel(boolean zeitStempel);
}
