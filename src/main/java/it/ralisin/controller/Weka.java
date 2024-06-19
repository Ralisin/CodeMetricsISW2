package it.ralisin.controller;

import com.opencsv.exceptions.CsvValidationException;
import it.ralisin.entities.ClassifierEvaluation;
import it.ralisin.entities.WekaObject;
import it.ralisin.tools.CsvTool;
import weka.attributeSelection.*;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import com.opencsv.CSVReader;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.SMOTE;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Weka {
    private final List<Classifier> classifiers = new ArrayList<>(List.of(new RandomForest(), new NaiveBayes(), new IBk()));

    private final List<ClassifierEvaluation> classifierEvaluations = new ArrayList<>();

    private final CsvTool csvTool;

    private final List<Instances> trainingList = new ArrayList<>();
    private final List<Instances> testingList = new ArrayList<>();

    private final List<List<WekaObject>> listWekaJavaClassList = new ArrayList<>();

    public Weka(String projName, String srcDir) throws IOException {
        String trainingDirStr = srcDir + projName + "/training/arff";
        String testingDirStr = srcDir + projName + "/testing/arff";

        Path trainingDir = Paths.get(trainingDirStr);
        Path testingDir = Paths.get(testingDirStr);

        if (!Files.exists(trainingDir) || !Files.exists(testingDir))
            throw new IOException("Invalid training/testing directory");

        int numFilesTraining = filesInFolder(trainingDirStr);
        int numFilesTesting = filesInFolder(testingDirStr);
        if(numFilesTraining != numFilesTesting) throw new IOException("Given training and testing folders are different");

        getInstances(projName, trainingDirStr, numFilesTraining, "trainingSet", trainingList);
        getInstances(projName, testingDirStr, numFilesTesting, "testingSet", testingList);

        for(int i = 0; i < numFilesTesting; i++) {
            List<WekaObject> javaClassList = getWekaJavaFiles(projName, srcDir + projName + "/testing/csv", i + 3);

            if (javaClassList.size() != testingList.get(i).size())
                throw new IOException("Number of instances of WekaObject are different from number of instances of testingList");

            listWekaJavaClassList.add(javaClassList);
        }

        if(listWekaJavaClassList.size() != numFilesTesting)
            throw new IOException("Invalid testing csv directory");

        this.csvTool = new CsvTool(projName, srcDir + projName);
    }

    private void getInstances(String projName, String dir, int numFiles, String middleFileName, List<Instances> instancesList) {
        for(int i = 3; i < numFiles + 3; i++) {
            String file = dir + "/" + projName + "_" + middleFileName + "_" + i + ".arff";

            try {
                Instances data = loadARFF(file);

                if (data.classIndex() == -1) {
                    data.setClassIndex(data.numAttributes() - 1);
                }

                instancesList.add(data);
            } catch (Exception e) {
                // Ignore loadARFF exception
            }
        }
    }

    private int filesInFolder(String dir) {
        int numFiles = 0;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir), "*.arff")) {
            for (Path file : stream) {
                if (!Files.isDirectory(file) && file.toString().endsWith(".arff")) {
                    numFiles++;
                }
            }
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Error reading directory", e);
        }

        return numFiles;
    }

    private Instances loadARFF(String filePath) throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(filePath);
        return source.getDataSet();
    }

    private List<WekaObject> getWekaJavaFiles(String projName, String dir, int index) {
        List<WekaObject> wekaJavaClassList = new ArrayList<>();

        String file = dir + "/" + projName + "_testingSet_" + index + ".csv";

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] line = reader.readNext(); // Skip first line read

            while ((line = reader.readNext()) != null) {
                WekaObject wekaJavaClass = new WekaObject();
                wekaJavaClass.setSize(Integer.parseInt(line[2]));
                wekaJavaClass.setBuggyness(line[line.length-1]);

                wekaJavaClassList.add(wekaJavaClass);
            }
        } catch (IOException | CsvValidationException e) {
            String msg = "Error retrieving javaClass data from csv file " + index +  e.getMessage();
            Logger.getAnonymousLogger().log(Level.SEVERE, msg);
        }

        return wekaJavaClassList;
    }

    public void wekaAnalyses() throws Exception {
        Classifier fc;
        ClassifierEvaluation ce;

        String msg;
        for (int i = 0; i < trainingList.size(); i++) {
            msg = "training on instance " + (i + 3);
            Logger.getAnonymousLogger().log(Level.INFO, msg);

            Instances training = trainingList.get(i);
            Instances testing = testingList.get(i);

            Filter fs = featureSelection(training);
            Instances filterTraining = Filter.useFilter(training, fs);
            Instances filterTesting = Filter.useFilter(testing, fs);

            for (Classifier classifier : classifiers) {
                // Classifier: no featureSelection, no sampling, no costSensitive
                ce = evaluateClassifier(classifier, training, testing, i, classifier.getClass().getSimpleName(), "");
                ce.setClassifierFilters(false, false, false);
                classifierEvaluations.add(ce);

                // Classifier: featureSelection, no sampling, no costSensitive
                ce = evaluateClassifier(classifier, filterTraining, filterTesting, i, classifier.getClass().getSimpleName(), "featureSelection");
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
                fc = createFilteredClassifier(classifier, sampling(training));
                ce = evaluateClassifier(fc, filterTraining, filterTesting, i, classifier.getClass().getSimpleName(), "featureSelection_sampling");
                ce.setClassifierFilters(true, true, false);
                classifierEvaluations.add(ce);

                // Classifier: featureSelection, no sampling, costSensitive
                fc = costSensitive(classifier);
                ce = evaluateClassifier(fc, filterTraining, filterTesting, i, classifier.getClass().getSimpleName(), "featureSelection_costSensitive");
                ce.setClassifierFilters(true, false, true);
                classifierEvaluations.add(ce);
            }
        }

        csvTool.csvWekaResult(classifierEvaluations);
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

    private Filter featureSelection(Instances dataset) throws Exception {
        AttributeSelection featureSelected = new AttributeSelection();

        ASEvaluation eval = new CfsSubsetEval(); // Selects subsets of features based on the correlation between the features and the class
            // InfoGainAttributeEval() - Selects subsets of features based on the information gain
            // PrincipalComponents() - Selects subsets of features using principal component analysis (PCA)
            // ReliefFAttributeEval() - Selects subsets of features based on "ReliefF" algorithm
            // GainRatioAttributeEval() - Evaluates features using ratio gain
            // ChiSquaredAttributeEval() - Evaluates features using the chi-square test

        ASSearch search = new BestFirst();
            // BestFirst() - Search for the best subset of features using a best-first search
            // GreedyStepwise() - Greedy approach to select features
            // Ranker() - Rank features according to their scores
            // GeneticSearch() - It uses a genetic algorithm to search for the best subset of features
            // RandomSearch() - Use a random search to find the best subset of features
            // ExhaustiveSearch() - Search exhaustively for all possible combinations of features

        featureSelected.setEvaluator(eval);
        featureSelected.setSearch(search);
        featureSelected.setInputFormat(dataset);

        return featureSelected;
    }

    private Filter sampling(Instances dataset) throws Exception {
        SMOTE smote = new SMOTE();
        smote.setInputFormat(dataset);

        return smote;
    }

    private CostSensitiveClassifier costSensitive(Classifier classifier) {
        double wFP = 1.0;
        double wFN = 10.0;

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

        List<WekaObject> javaClassList = listWekaJavaClassList.get(index);

        for(int i = 0; i < testing.numInstances(); i++) {
            // Get the prediction distribution
            double[] predictionDistribution = classifier.distributionForInstance(testing.instance(i));

            // Take index 1 that is prediction label "yes"
            javaClassList.get(i).setPrediction(predictionDistribution[1]);
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

        Evaluation eval;
        if (classifier.getClass().getSimpleName().equals("CostSensitiveClassifier")) {
            CostSensitiveClassifier csc = (CostSensitiveClassifier) classifier;
            eval = new Evaluation(training, csc.getCostMatrix());
        } else
            eval = new Evaluation(testing);

        eval.evaluateModel(classifier, testing);

        return eval;
    }
}
