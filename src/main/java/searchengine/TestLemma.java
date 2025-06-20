package searchengine;

import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.util.LemmaFinder;

import java.io.IOException;

public class TestLemma {
    public static void main(String[] args) throws IOException {
        String text = "Повторное появление леопарда в Осетии позволяет предположить, что леопард постоянно обитает в некоторых районах Северного Кавказа.";

        LemmaFinder lemmaFinder = new LemmaFinder(new RussianLuceneMorphology());
        System.out.println(lemmaFinder.collectLemmas(text));
    }
}