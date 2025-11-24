package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;
import java.util.Set;

public interface LemmaRepository extends JpaRepository<Lemma, Long> {

    List<Lemma> findAllBySiteAndLemmaIn(Site site, Set<String> lemmaTexts);

    List<Lemma> findAllByLemmaInAndSiteIn(Set<String> lemmas, List<Site> sites);

}
