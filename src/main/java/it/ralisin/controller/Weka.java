package it.ralisin.controller;

import it.ralisin.entities.ClassifierEvaluation;

import it.ralisin.entities.JavaClass;
import it.ralisin.entities.Release;
import it.ralisin.tools.CsvTool;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.SMOTE;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Weka {
    private final List<Classifier> classifiers = new ArrayList<>(List.of(new RandomForest(), new NaiveBayes(), new IBk()));

    private final List<ClassifierEvaluation> classifierEvaluations = new ArrayList<>();

    private final List<Release> releaseList;
    private final CsvTool csvTool;

    private final List<Instances> trainingSourceList = new ArrayList<>();
    private final List<Instances> testingSourceList = new ArrayList<>();

    public Weka(String trainingDirStr, String testingDirStr, List<Release> releaseList, CsvTool csvTool) throws IOException {
        Path trainingDir = Paths.get(trainingDirStr);
        Path testingDir = Paths.get(testingDirStr);

        if (!Files.exists(trainingDir) || !Files.exists(testingDir))
            throw new IOException("Invalid training/testing directory");

        getInstances(trainingDir, this.trainingSourceList);
        getInstances(testingDir, this.testingSourceList);

        if (trainingSourceList.size() != testingSourceList.size() || trainingSourceList.isEmpty())
            throw new IOException("No arff files found");

        this.releaseList = releaseList;
        this.csvTool = csvTool;
    }

    private void getInstances(Path dir, List<Instances> instancesList) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file : stream) {
                if (!Files.isDirectory(file)) {
                    if (!file.toString().endsWith(".arff")) continue;

                    Instances data = loadARFF(file.toString());

                    // Set class index (last attribute)
                    data.setClassIndex(data.numAttributes() - 1);

                    instancesList.add(data);
                }
            }
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
        }
    }

    private Instances loadARFF(String filePath) throws Exception {
        DataSource source = new DataSource(filePath);
        return source.getDataSet();
    }

    public void wekaAnalyses() throws Exception {
        Classifier fc;
        ClassifierEvaluation ce;

        for (int i = 0; i < trainingSourceList.size(); i++) {
            Instances training = trainingSourceList.get(i);
            Instances testing = testingSourceList.get(i);

            for (Classifier classifier : classifiers) {
                // Classifier: no featureSelection, no sampling, no costSensitive
                ce = evaluateClassifier(classifier, training, testing, i, classifier.getClass().getSimpleName(), "");
                ce.setClassifierFilters(false, false, false);
                classifierEvaluations.add(ce);

                // Classifier: featureSelection, no sampling, no costSensitive
                fc = createFilteredClassifier(classifier, featureSelection());
                ce = evaluateClassifier(fc, training, testing, i, classifier.getClass().getSimpleName(), "featureSelection");
                ce.setClassifierFilters(true, false, false);
                classifierEvaluations.add(ce);

                // Classifier: no featureSelection, sampling, no costSensitive
                fc = createFilteredClassifier(classifier, sampling(training));
                ce = evaluateClassifier(fc, training, testing, i, classifier.getClass().getSimpleName(), "sampling");
                ce.setClassifierFilters(false, true, false);
                classifierEvaluations.add(ce);

                // Classifier: no featureSelection, no sampling, costSensitive
                fc = costSensitive(classifier);
                ce = evaluateClassifier(fc, training, testing, i, classifier.getClass().getSimpleName(), "costSensitive");
                ce.setClassifierFilters(false, false, true);
                classifierEvaluations.add(ce);

                // Classifier: featureSelection, sampling, no costSensitive
                fc = createFilteredClassifier(classifier, featureSelection(), sampling(training));
                ce = evaluateClassifier(fc, training, testing, i, classifier.getClass().getSimpleName(), "featureSelection_sampling");
                ce.setClassifierFilters(true, true, false);
                classifierEvaluations.add(ce);

                // Classifier: featureSelection, no sampling, costSensitive
                fc = costSensitive(classifier);
                fc = createFilteredClassifier(fc, featureSelection());
                ce = evaluateClassifier(fc, training, testing, i, classifier.getClass().getSimpleName(), "featureSelection_costSensitive");
                ce.setClassifierFilters(true, false, true);
                classifierEvaluations.add(ce);
            }
        }

        csvTool.csvWekaResult(classifierEvaluations);

        System.out.println(classifierEvaluations.size() + " classifier evaluations");
    }

    public static FilteredClassifier createFilteredClassifier(Classifier baseClassifier, Filter... filters) {
        // Create MultiFilter and add filters
        MultiFilter multiFilter = new MultiFilter();
        multiFilter.setFilters(filters);

        // Create FilterClassifier and set filters
        FilteredClassifier filteredClassifier = new FilteredClassifier();
        filteredClassifier.setClassifier(baseClassifier);
        filteredClassifier.setFilter(multiFilter);

        return filteredClassifier;
    }

    private Filter featureSelection() {
        // Filter for featureSelection
        AttributeSelection attributeSelection = new AttributeSelection();

        ASEvaluation eval = new CfsSubsetEval(); // Selects subsets of features based on the correlation between the features and the class
            // InfoGainAttributeEval() - Selects subsets of features based on the information gain
            // PrincipalComponents() - Selects subsets of features using principal component analysis (PCA)
            // ReliefFAttributeEval() - Selects subsets of features based on "ReliefF" algorithm
            // GainRatioAttributeEval() - Evaluates features using ratio gain
            // ChiSquaredAttributeEval() - Evaluates features using the chi-square test

        ASSearch search = new GreedyStepwise(); // Greedy approach to select features
            // BestFirst() - Search for the best subset of features using a best-first search
            // Ranker() - Rank features according to their scores
            // GeneticSearch() - It uses a genetic algorithm to search for the best subset of features
            // RandomSearch() - Use a random search to find the best subset of features
            // ExhaustiveSearch() - Search exhaustively for all possible combinations of features

        attributeSelection.setEvaluator(eval);
        attributeSelection.setSearch(search);

        return attributeSelection;
    }

    private Filter sampling(Instances dataset) throws Exception {
        // Define resampling
        SMOTE smote = new SMOTE();
        smote.setInputFormat(dataset);

        return smote;
    }

    private CostSensitiveClassifier costSensitive(Classifier classifier) {
        double wFP = 10.0;
        double wFN = 1.0;

        CostSensitiveClassifier csc = new CostSensitiveClassifier();
        csc.setClassifier(classifier);
        csc.setCostMatrix(createCostMatrix(wFP, wFN));

        return csc;
    }

    private CostMatrix createCostMatrix(double weightFP, double weightFN) {
        CostMatrix costMatrix = new CostMatrix(2);
        costMatrix.setCell(0, 0, 0.0);
        costMatrix.setCell(1, 0, weightFP);
        costMatrix.setCell(0, 1, weightFN);
        costMatrix.setCell(1, 1, 0.0);

        // | TP , FN |
        // | FP , TN |

        return costMatrix;
    }

    private ClassifierEvaluation evaluateClassifier(Classifier classifier, Instances training, Instances testing, int index, String classifierName, String filters) throws Exception {
        Evaluation eval = trainAndEvaluate(classifier, training, testing);

        List<JavaClass> javaClassList = releaseList.get(index + 2).getJavaClassList();

        for(int i = 0; i < javaClassList.size(); i++) {
            // Get the prediction distribution
            double[] predictionDistribution = classifier.distributionForInstance(testing.instance(i));

            // Take index 1 that is prediction label "yes"
            double prediction = predictionDistribution[1];

            javaClassList.get(i).setPredicted(prediction);
        }

        csvTool.csvAcume(javaClassList, classifierName, filters, index + 2);

        ClassifierEvaluation ce = new ClassifierEvaluation(
                classifierName,
                100.0 * training.size() / (training.size() + testing.size()),
                eval.precision(1),
                eval.recall(1),
                eval.kappa(),
                eval.areaUnderROC(1),
                eval.fMeasure(1)
        );

        ce.setTruePositives(eval.numTruePositives(1));
        ce.setFalsePositives(eval.numFalsePositives(1));
        ce.setTrueNegatives(eval.numTrueNegatives(1));
        ce.setFalseNegatives(eval.numFalseNegatives(1));

        return ce;
    }

    private Evaluation trainAndEvaluate(Classifier classifier, Instances training, Instances testing) throws Exception {
        classifier.buildClassifier(training);

        Evaluation eval = new Evaluation(testing);
        eval.evaluateModel(classifier, testing);

        return eval;
    }
}
