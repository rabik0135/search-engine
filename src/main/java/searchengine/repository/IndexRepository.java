package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface IndexRepository extends JpaRepository<Index, Integer> {
    @Query("""
            SELECT i.page
            FROM Index i
            WHERE i.lemma = :lemma
            """)
    Set<Page> findPagesByLemma(@Param("lemma") Lemma lemma);

    @Query("""
            SELECT DISTINCT i.page
            FROM Index i
            WHERE i.lemma = :lemma
            AND i.page IN :pages
            """)
    Set<Page> findPagesByLemmaAndPageIn(@Param("lemma") Lemma lemma,
                                        @Param("pages") Collection<Page> pages);

    List<Index> findAllByPageAndLemmaIdIn(Page page, List<Integer> lemmaIds);
}