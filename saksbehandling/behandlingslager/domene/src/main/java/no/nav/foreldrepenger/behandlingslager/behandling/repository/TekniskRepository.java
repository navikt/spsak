package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import no.nav.vedtak.felles.jpa.savepoint.RunWithSavepoint;
import no.nav.vedtak.felles.jpa.savepoint.Work;

public class TekniskRepository {

    private GrunnlagRepositoryProviderImpl repositoryProvider;

    public TekniskRepository(GrunnlagRepositoryProvider repositoryProvider) {
        // tar en cast
        this.repositoryProvider = (GrunnlagRepositoryProviderImpl) repositoryProvider;  // NOSONAR
        
    }
    
    public <V> V doWorkInSavepoint(Work<V> work) {
        RunWithSavepoint setJdbcSavepoint = new RunWithSavepoint(repositoryProvider.getEntityManager());
        return setJdbcSavepoint.doWork(work);
    }
 }
