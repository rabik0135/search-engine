package searchengine;

import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.services.lemmaService.LemmaServiceImpl;

import java.io.IOException;

public class TestLemma {
    public static void main(String[] args) throws IOException {
        String text = "Повторное появление леопарда в Осетии позволяет предположить, что леопард постоянно обитает в некоторых районах Северного Кавказа.";

       /* LemmaServiceImpl lemmaServiceImpl = new LemmaServiceImpl(new RussianLuceneMorphology());
        System.out.println(lemmaServiceImpl.collectLemmas(text));*/
    }
}