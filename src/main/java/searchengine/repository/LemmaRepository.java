package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    Optional<Lemma> findBySiteAndLemma(Site site, String lemma);
    List<Lemma> findAllBySiteAndLemmaIn(Site site, Set<String> lemmaTexts);


    /*@Modifying
    @Query(value = "INSERT INTO lemma(site_id, lemma, frequency) VALUES (:siteId, :lemma, :freq)" +
            "ON DUPLICATE KEY UPDATE frequency = frequency + :freq", nativeQuery = true)
    void insertOrUpdateLemma(@Param("siteId") Integer siteId,
                             @Param("lemma") String lemma,
                             @Param("freq") float freq);*/
}
