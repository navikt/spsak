package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import java.util.Collection;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynDokumentEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynEntitet;

public interface InnsynRepository extends BehandlingslagerRepository {

    void lagreInnsyn(Behandling behandling, InnsynEntitet innsyn, Collection<? extends InnsynDokument> innsynDokumenter);

    List<InnsynDokumentEntitet> hentDokumenterForInnsyn(long innsynId);

    List<InnsynEntitet> hentForBehandling(long behandlingId);
}
