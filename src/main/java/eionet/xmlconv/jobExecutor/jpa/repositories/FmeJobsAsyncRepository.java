package eionet.xmlconv.jobExecutor.jpa.repositories;

import eionet.xmlconv.jobExecutor.jpa.entities.FmeJobsAsync;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FmeJobsAsyncRepository extends JpaRepository<FmeJobsAsync, Integer> {
}
