package no.nav.foreldrepenger.domene.mottak.hendelser.impl;

import no.nav.foreldrepenger.domene.typer.AktørId;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.hendelser.sortering.HendelseSorteringRepository;
import no.nav.foreldrepenger.domene.mottak.hendelser.HendelseSorteringTjeneste;

@ApplicationScoped
public class HendelseSorteringTjenesteImpl implements HendelseSorteringTjeneste {

    private HendelseSorteringRepository sorteringRepository;

    HendelseSorteringTjenesteImpl() {
        // CDI
    }

    @Inject
    public HendelseSorteringTjenesteImpl(HendelseSorteringRepository sorteringRepository) {
        this.sorteringRepository = sorteringRepository;
    }

    @Override
    public List<AktørId> hentAktørIderTilknyttetSak(List<AktørId> aktørIdList) {
        return sorteringRepository.hentEksisterendeAktørIderMedSak(aktørIdList);
    }
}
