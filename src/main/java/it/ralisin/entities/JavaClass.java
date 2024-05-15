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
import java.util.List;

public class JavaClass {
    private final String classPath;
    private final String classContent;
    private final List<RevCommit> commitList = new ArrayList<>();

    // Metrics
    int loc = 0;

    public JavaClass(String classPath, String classContent) {
        this.classPath = classPath;
        this.classContent = classContent;

//        this.loc = countLinesOfCode(classContent);
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

    public int getLoc() {
        return loc;
    }

    private int countLinesOfCode(String content) {
        return content.split("\r\n|\r|\n").length;
    }
}
