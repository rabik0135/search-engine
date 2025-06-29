package searchengine;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Component;
import searchengine.config.LemmaConfiguration;
import searchengine.services.LemmaService.LemmaService;
import searchengine.services.LemmaService.LemmaServiceImpl;


import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class TestLemma {
    public static void main(String[] args) throws IOException {
        String text = "Повторное появление леопарда в Осетии позволяет предположить, что леопард постоянно обитает в некоторых районах Северного Кавказа.";

        List<Integer> list = List.of(1,7,544,2,89,3,9);
        List<Integer> sotredList = list.stream().sorted().toList();
        sotredList.forEach(System.out::println);




    }
}