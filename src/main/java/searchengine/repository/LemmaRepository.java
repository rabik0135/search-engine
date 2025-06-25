package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.Collection;
import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    Lemma findLemmaBySiteAndLemma(Site site, String lemma);
    List<Lemma> findAllBySite(Site site);
    List<Lemma> findAllBySiteAndLemmaIn(Site site, Collection<String> lemmas);
}