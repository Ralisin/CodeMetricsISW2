package it.ralisin.entities;

public class ClassifierEvaluation {
    private final String classifierName;
    private final double trainingPerc;

    private final double precision;
    private final double recall;
    private final double kappa;
    private double truePositives;
    private double falsePositives;
    private double trueNegatives;
    private double falseNegatives;
    private final double areaUnderROC;
    private final double fMeasure;

    private boolean featureSelection;
    private boolean sampling;
    private boolean costSensitive;

    public ClassifierEvaluation(String classifierName, double trainingPerc, double precision, double recall, double kappa, double areaUnderROC, double fMeasure) {
        this.classifierName = classifierName;
        this.trainingPerc = trainingPerc;

        this.precision = precision;
        this.recall = recall;
        this.kappa = kappa;
        this.areaUnderROC = areaUnderROC;
        this.fMeasure = fMeasure;
    }

    public String getClassifierName() {
        return classifierName;
    }

    public double getTrainingPerc() {
        return trainingPerc;
    }

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public double getKappa() {
        return kappa;
    }

    public double getTruePositives() {
        return truePositives;
    }

    public double getFalsePositives() {
        return falsePositives;
    }

    public double getTrueNegatives() {
        return trueNegatives;
    }

    public double getFalseNegatives() {
        return falseNegatives;
    }

    public double getAreaUnderROC() {
        return areaUnderROC;
    }

    public double getfMeasure() {
        return fMeasure;
    }

    public String getFeatureSelection() {
        return featureSelection ? "featureSelection" : "no";
    }

    public String getSampling() {
        return sampling ? "sampling" : "no";
    }

    public String getCostSensitive() {
        return costSensitive ? "costSensitive" : "no";
    }

    public void setClassifierFilters(boolean featureSelection, boolean sampling, boolean costSensitive) {
        this.featureSelection = featureSelection;
        this.sampling = sampling;
        this.costSensitive = costSensitive;
    }

    public void setTruePositives(double truePositives) {
        this.truePositives = truePositives;
    }

    public void setFalsePositives(double falsePositives) {
        this.falsePositives = falsePositives;
    }

    public void setTrueNegatives(double trueNegatives) {
        this.trueNegatives = trueNegatives;
    }

    public void setFalseNegatives(double falseNegatives) {
        this.falseNegatives = falseNegatives;
    }
}
