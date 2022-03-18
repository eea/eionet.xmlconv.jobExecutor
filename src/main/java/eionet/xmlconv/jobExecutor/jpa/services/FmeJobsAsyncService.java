package eionet.xmlconv.jobExecutor.jpa.services;

import eionet.xmlconv.jobExecutor.exceptions.DatabaseException;
import eionet.xmlconv.jobExecutor.jpa.entities.FmeJobsAsync;

import java.util.List;
import java.util.Optional;

public interface FmeJobsAsyncService {

    Optional<FmeJobsAsync> findById(Integer id);

    List<FmeJobsAsync> findAll();

    FmeJobsAsync save(FmeJobsAsync entry) throws DatabaseException;

    void deleteById(Integer id) throws DatabaseException;
}
