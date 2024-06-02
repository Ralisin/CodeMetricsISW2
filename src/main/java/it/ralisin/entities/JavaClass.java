/*
- Size (LOC) (linee di codice): È il numero totale di linee di codice nel file.
- LOC Touched (linee di codice toccate): È la somma delle linee di codice aggiunte e cancellate durante tutte le revisioni del file. Questo può dare un'idea del grado di modifica del file nel tempo.
- NR (numero di revisioni): È il numero totale di revisioni (commit) in cui il file è stato modificato.
- Nfix (numero di correzioni di difetti): È il numero totale di volte in cui il file è stato modificato per correggere difetti o bug.
- Nauth (numero di autori): È il numero totale di autori distinti che hanno contribuito al file.
- LOC Added (linee di codice aggiunte): È la somma delle linee di codice aggiunte durante tutte le revisioni del file.
- Max LOC Added (massimo delle linee di codice aggiunte): È il massimo numero di linee di codice aggiunte in una singola revisione del file.
- Average LOC Added (media delle linee di codice aggiunte): È il numero medio di linee di codice aggiunte per revisione.
- Churn (movimento): È la somma delle linee di codice aggiunte e cancellate durante tutte le revisioni del file. Questo può fornire un'indicazione della frequenza e dell'entità delle modifiche al file nel tempo.
- Max Churn (massimo movimento): È il massimo churn (movimento) osservato durante tutte le revisioni del file.
- Average Churn (media del movimento): È il churn medio (movimento) osservato durante tutte le revisioni del file.
- Change Set Size (dimensione del set di modifiche): È il numero di file modificati insieme in una singola revisione.
- Max Change Set (massimo set di modifiche): È il numero massimo di file modificati insieme in una singola revisione.
- Average Change Set (media del set di modifiche): È la dimensione media del set di modifiche, cioè il numero medio di file modificati insieme in una singola revisione.
- Age (età): È l'età del rilascio, misurata in unità di tempo (come giorni, settimane, mesi o anni) dalla sua creazione o dal suo rilascio.
- Weighted Age (età ponderata): È l'età del rilascio ponderata per le linee di codice toccate. Questo fornisce un'indicazione dell'età del rilascio considerando anche l'entità delle modifiche apportate.

Come calcolarli:
* Size (LOC) (linee di codice): calcolo il numero di righe di una classe
* LOC Touched (linee di codice toccate): |# righe aggiunte| + |# righe tolte|
* NR (numero di revisioni): numero di commit che questa ha
* Nfix (numero di correzioni di difetti): Si calcola prendendo dai ticket solo i commit che hanno effettivamente corretto un ticket e si
- Nauth (numero di autori): È il numero totale di autori distinti che hanno contribuito al file.
- LOC Added (linee di codice aggiunte): È la somma delle linee di codice aggiunte durante tutte le revisioni del file.
- Max LOC Added (massimo delle linee di codice aggiunte): È il massimo numero di linee di codice aggiunte in una singola revisione del file.
- Average LOC Added (media delle linee di codice aggiunte): È il numero medio di linee di codice aggiunte per revisione.
- Churn (movimento): È la somma delle linee di codice aggiunte e cancellate durante tutte le revisioni del file. Questo può fornire un'indicazione della frequenza e dell'entità delle modifiche al file nel tempo.
- Max Churn (massimo movimento): È il massimo churn (movimento) osservato durante tutte le revisioni del file.
- Average Churn (media del movimento): È il churn medio (movimento) osservato durante tutte le revisioni del file.
- Change Set Size (dimensione del set di modifiche): È il numero di file modificati insieme in una singola revisione.
- Max Change Set (massimo set di modifiche): È il numero massimo di file modificati insieme in una singola revisione.
- Average Change Set (media del set di modifiche): È la dimensione media del set di modifiche, cioè il numero medio di file modificati insieme in una singola revisione.
- Age (età): È l'età del rilascio, misurata in unità di tempo (come giorni, settimane, mesi o anni) dalla sua creazione o dal suo rilascio.
- Weighted Age (età ponderata): È l'età del rilascio ponderata per le linee di codice toccate. Questo fornisce un'indicazione dell'età del rilascio considerando anche l'entità delle modifiche apportate.

* Indica le metriche considerate
 */

package it.ralisin.entities;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
public class JavaClass {
    private final String classPath;
    private final String classContent;
    private final List<RevCommit> commitList = new ArrayList<>();

    // Metrics
    int size = 0; // LOC
    int locTouched = 0; // Sum over revisions of LOC added and deleted. |added| + |deleted|
    int nr = 0; // Number of commit that touched the class
    int nFix = 0; // Number of commit related to a fix
    int nAuth = 0; // Number of authors
    int locAdded = 0; // Sum over revisions of LOC added
    int maxLocAdded = 0; // Maximum over revisions of LOC added
    List<Integer> listLocAdded = new ArrayList<Integer>(); // List of loc added
    double averageLocAdded = 0; // Average LOC added per revision
    int churn = 0; // Sum of LOC added less LOC deleted
    int maxChurn = 0; // Max churn in a single commit
    List<Integer> listOfChurn = new ArrayList<>();
    double averageChurn = 0; //

    public JavaClass(String classPath, String classContent) {
        this.classPath = classPath;
        this.classContent = classContent;
    }

    public String getClassPath() {
        return classPath;
    }

    public List<RevCommit> getCommitList() {
        return commitList;
    }

    public void addCommit(RevCommit commit) {
        commitList.add(commit);
    }

    public int getSize() {
        return size;
    }
}
*/

public class JavaClass {
    private final String classPath;
    private final List<RevCommit> commitList = new ArrayList<>();

    // Metrics
    private int size = 0; // LOC
    private int locTouched = 0; // Sum over revisions of LOC added and deleted. |added| + |deleted|
    private int locAdded = 0; // Sum over revisions of LOC added
    private int maxLocAdded = 0; // Maximum over revisions of LOC added
    private final List<Integer> listLocAdded = new ArrayList<>(); // List of loc added
    private double averageLocAdded = 0; // Average LOC added per revision
    private int churn = 0; // Sum of LOC added less LOC deleted
    private int maxChurn = 0; // Max churn in a single commit
    private final List<Integer> listOfChurn = new ArrayList<>();
    private double averageChurn = 0;
    private int nr = 0; // Number of commit that touched the class
    private int nFix = 0; // Number of commit related to a fix
    private final Set<String> authors = new HashSet<>();  // Set of authors

    public JavaClass(String classPath, String classContent) {
        this.classPath = classPath;
        this.size = calculateSize(classContent);
    }

    public String getClassPath() {
        return classPath;
    }

    public void addCommit(RevCommit commit, int linesAdded, int linesDeleted, boolean isFix, String author) {
        commitList.add(commit);

        locTouched += (linesAdded + linesDeleted);
        locAdded += linesAdded;
        maxLocAdded = Math.max(maxLocAdded, linesAdded);
        listLocAdded.add(linesAdded);

        churn += linesAdded - linesDeleted;
        maxChurn = Math.max(maxChurn, linesAdded - linesDeleted);
        listOfChurn.add(linesAdded - linesDeleted);

        nr++;
        if (isFix) nFix++;
        authors.add(author);

        // Recalculate averages
        int sumLocs = 0;
        for (Integer loc : listLocAdded) sumLocs += loc;
        averageLocAdded = sumLocs / (double) listLocAdded.size();

        int sumChurn = 0;
        for (Integer churn : listOfChurn) sumChurn += churn;
        averageChurn = sumChurn / (double) listOfChurn.size();
    }

    public int getSize() {
        return size;
    }

    // This method calculates the number of lines of code (LOC) in the class content
    private int calculateSize(String content) {
        return content.split("\r\n|\r|\n").length;
    }

    // Other getters for the metrics can be added as needed
    public int getLocTouched() {
        return locTouched;
    }

    public int getNr() {
        return nr;
    }

    public int getNFix() {
        return nFix;
    }

    public int getLocAdded() {
        return locAdded;
    }

    public int getMaxLocAdded() {
        return maxLocAdded;
    }

    public double getAverageLocAdded() {
        return averageLocAdded;
    }

    public int getChurn() {
        return churn;
    }

    public int getMaxChurn() {
        return maxChurn;
    }

    public double getAverageChurn() {
        return averageChurn;
    }

    public int getNAuth() {
        return authors.size();
    }
}