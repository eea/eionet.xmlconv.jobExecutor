package eionet.xmlconv.jobExecutor.jpa.services;

import eionet.xmlconv.jobExecutor.exceptions.DatabaseException;
import eionet.xmlconv.jobExecutor.jpa.entities.FmeJobsAsync;
import eionet.xmlconv.jobExecutor.jpa.repositories.FmeJobsAsyncRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FmeJobsAsyncServiceImpl implements FmeJobsAsyncService {

    private FmeJobsAsyncRepository fmeJobsAsyncRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(FmeJobsAsyncServiceImpl.class);

    @Autowired
    public FmeJobsAsyncServiceImpl(FmeJobsAsyncRepository fmeJobsAsyncRepository) {
        this.fmeJobsAsyncRepository = fmeJobsAsyncRepository;
    }

    @Override
    public Optional<FmeJobsAsync> findById(Integer id) {
        return fmeJobsAsyncRepository.findById(id);
    }

    @Override
    public List<FmeJobsAsync> findAll() {
        return fmeJobsAsyncRepository.findAll();
    }

    @Override
    public FmeJobsAsync save(FmeJobsAsync entry) throws DatabaseException {
        try {
           return fmeJobsAsyncRepository.save(entry);
        } catch (Exception e) {
            LOGGER.error("Exception when trying to save or update fme job async entry");
            throw new DatabaseException(e);
        }
    }

    @Override
    public void deleteById(Integer id) throws DatabaseException {
        try {
            fmeJobsAsyncRepository.deleteById(id);
        } catch (Exception e) {
            LOGGER.error("Exception during deletion of fme asynchronous job " + id);
            throw new DatabaseException(e);
        }
    }
}
