package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.NoResultException;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetssjekkerSøknad;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.ManglendeVedlegg;
import no.nav.vedtak.util.FPDateUtil;

public abstract class AbstractKompletthetssjekkerSøknad implements KompletthetssjekkerSøknad {

    private KodeverkRepository kodeverkRepository;
    private SøknadRepository søknadRepository;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private Integer antallUkerVentefristVedForTidligSøknad;

    AbstractKompletthetssjekkerSøknad() {
        // CDI
    }

    AbstractKompletthetssjekkerSøknad(KodeverkRepository kodeverkRepository,
                                      SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                      Integer antallUkerVentefristVedForTidligSøknad,
                                      SøknadRepository søknadRepository) {
        this.kodeverkRepository = kodeverkRepository;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
        this.antallUkerVentefristVedForTidligSøknad = antallUkerVentefristVedForTidligSøknad;
        this.søknadRepository = søknadRepository;
    }

    @Override
    public Optional<LocalDateTime> erSøknadMottattForTidlig(Behandling behandling) {
        LocalDate permisjonsstart = skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(behandling);
        if (permisjonsstart == null) {
            return Optional.empty();
        }

        LocalDate ventefrist = permisjonsstart.minusWeeks(antallUkerVentefristVedForTidligSøknad);
        boolean erSøknadMottattForTidlig = ventefrist.isAfter(LocalDate.now(FPDateUtil.getOffset()));
        if (erSøknadMottattForTidlig) {
            LocalDateTime ventefristTidspunkt = LocalDateTime.of(ventefrist, LocalDateTime.now(FPDateUtil.getOffset()).toLocalTime());
            return Optional.of(ventefristTidspunkt);
        }
        return Optional.empty();
    }

    protected List<ManglendeVedlegg> identifiserManglendeVedlegg(Optional<Søknad> søknad, Set<DokumentTypeId> dokumentTypeIdSet) {
        return List.of();
    }

    private DokumentTypeId finnDokumentTypeId(String dokumentTypeIdKode) {
        DokumentTypeId dokumentTypeId;
        try {
            dokumentTypeId = kodeverkRepository.finnForKodeverkEiersKode(DokumentTypeId.class, dokumentTypeIdKode);
        } catch (NoResultException e) { //NOSONAR
            // skal tåle dette
            dokumentTypeId = kodeverkRepository.finn(DokumentTypeId.class, DokumentTypeId.UDEFINERT);
        }
        return dokumentTypeId;
    }

    @Override
    public Boolean erSøknadMottatt(Behandling behandling) {
        final Optional<Søknad> søknad = søknadRepository.hentSøknadHvisEksisterer(behandling);
        return søknad.isPresent();
    }
}
