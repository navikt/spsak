package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl.fp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.NoResultException;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadVedlegg;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetssjekkerSøknad;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.ManglendeVedlegg;
import no.nav.vedtak.util.FPDateUtil;

public abstract class KompletthetssjekkerSøknadFP implements KompletthetssjekkerSøknad {

    private KodeverkRepository kodeverkRepository;
    private SøknadRepository søknadRepository;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private Integer antallUkerVentefristVedForTidligSøknad;
    private MottatteDokumentRepository mottatteDokumentRepository;

    KompletthetssjekkerSøknadFP() {
        // CDI
    }

    KompletthetssjekkerSøknadFP(KodeverkRepository kodeverkRepository,
                                SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                Integer antallUkerVentefristVedForTidligSøknad,
                                SøknadRepository søknadRepository,
                                MottatteDokumentRepository mottatteDokumentRepository) {
        this.kodeverkRepository = kodeverkRepository;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
        this.antallUkerVentefristVedForTidligSøknad = antallUkerVentefristVedForTidligSøknad;
        this.søknadRepository = søknadRepository;
        this.mottatteDokumentRepository = mottatteDokumentRepository;
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

        return getSøknadVedleggListe(søknad)
            .stream()
            .filter(SøknadVedlegg::isErPåkrevdISøknadsdialog)
            .map(SøknadVedlegg::getSkjemanummer)
            .map(this::finnDokumentTypeId)
            .filter(doc -> !dokumentTypeIdSet.contains(doc))
            .map(ManglendeVedlegg::new)
            .collect(Collectors.toList());
    }

    private Set<SøknadVedlegg> getSøknadVedleggListe(Optional<Søknad> søknad) {
        return søknad.map(Søknad::getSøknadVedlegg)
            .orElse(Collections.emptySet());
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
        List<MottattDokument> mottattDokumenter = mottatteDokumentRepository.hentMottatteDokument(behandling.getId());
        Optional<MottattDokument> mottattSøknad = mottattDokumenter.stream()
            .filter(mottattDokument -> DokumentTypeId.getSøknadTyper().contains(mottattDokument.getDokumentTypeId())
                || DokumentKategori.SØKNAD.equals(mottattDokument.getDokumentKategori()))
            .findFirst();
        // sjekker på både søknad og mottatte dokumenter siden søknad ikke lagres med en gang
        return søknad.isPresent() || mottattSøknad.isPresent();
    }
}
