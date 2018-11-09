package no.nav.foreldrepenger.jsonfeed;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatusEvent;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.domene.feed.FeedRepository;
import no.nav.foreldrepenger.domene.feed.VedtakUtgåendeHendelse;
import no.nav.foreldrepenger.kontrakter.feed.vedtak.v1.ForeldrepengerEndret;
import no.nav.foreldrepenger.kontrakter.feed.vedtak.v1.ForeldrepengerInnvilget;
import no.nav.foreldrepenger.kontrakter.feed.vedtak.v1.ForeldrepengerOpphoert;
import no.nav.foreldrepenger.kontrakter.feed.vedtak.v1.Innhold;
import no.nav.foreldrepenger.kontrakter.feed.vedtak.v1.Meldingstype;
import no.nav.vedtak.felles.integrasjon.rest.JsonMapper;

@ApplicationScoped
public class HendelsePublisererTjenesteImpl implements HendelsePublisererTjeneste {
    private static final Logger log = LoggerFactory.getLogger(HendelsePublisererTjenesteImpl.class);
    private static final String FAGSAK_PREFIX = "FS";
    private static final String VEDTAK_PREFIX = "VT";


    private FeedRepository feedRepository;
    private UttakRepository uttakRepository;
    private FagsakRepository fagsakRepository;
    private BehandlingRepository behandlingRepository;

    private static class UttakFomTom {
        LocalDate førsteStønadsdag;
        LocalDate sisteStønadsdag;


    }

    public HendelsePublisererTjenesteImpl() {
        //Creatively Diversified Investments
    }

    @Inject
    public HendelsePublisererTjenesteImpl(FeedRepository feedRepository, UttakRepository uttakRepository, FagsakRepository fagsakRepository, BehandlingRepository behandlingRepository) {
        this.feedRepository = feedRepository;
        this.uttakRepository = uttakRepository;
        this.fagsakRepository = fagsakRepository;
        this.behandlingRepository = behandlingRepository;
    }

    @Override
    public void lagreVedtak(BehandlingVedtak vedtak) {
        log.info("lagrer utgående hendelse for vedtak {}", vedtak.getId());

        if (feedRepository.harHendelseMedKildeId(VedtakUtgåendeHendelse.class, VEDTAK_PREFIX + vedtak.getId())) {
            log.debug("Skipping lagring av hendelse av vedtakId {} fordi den allerede eksisterer", vedtak.getId());
            return;
        }

        BehandlingType behandlingType = vedtak.getBehandlingsresultat().getBehandling().getType();

        if (!(erInnvilgetFørstegangssøknad(vedtak, behandlingType) || erEndring(behandlingType))
            || erBeslutningsvedtak(behandlingType, vedtak.getBehandlingsresultat().getBehandlingResultatType())
            || erEndringUtenEndretPeriode(vedtak)) {
            return; //dette vedtaket trigger ingen hendelse
        }

        Innhold innhold = mapVedtakTilInnhold(vedtak);

        Meldingstype meldingstype;

        if (erFørstegangsSøknad(behandlingType) || erInnvilgetRevurdering(behandlingType, vedtak.getBehandlingsresultat().getBehandlingResultatType())) {
            meldingstype = Meldingstype.FORELDREPENGER_INNVILGET;
        } else if (erOpphørtRevurdering(behandlingType, vedtak.getBehandlingsresultat().getBehandlingResultatType())) {
            meldingstype = Meldingstype.FORELDREPENGER_OPPHOERT;
        } else {
            meldingstype = Meldingstype.FORELDREPENGER_ENDRET;
        }

        String payloadJason = JsonMapper.toJson(innhold);

        VedtakUtgåendeHendelse vedtakUtgåendeHendelse = VedtakUtgåendeHendelse.builder()
            .aktørId(vedtak.getBehandlingsresultat().getBehandling().getAktørId().getId())
            .payload(payloadJason)
            .type(meldingstype.getType())
            .kildeId(VEDTAK_PREFIX + String.valueOf(vedtak.getId()))
            .build();
        feedRepository.lagre(vedtakUtgåendeHendelse);
    }

    private boolean erInnvilgetFørstegangssøknad(BehandlingVedtak vedtak, BehandlingType behandlingType) {
        return VedtakResultatType.INNVILGET.equals(vedtak.getVedtakResultatType())
            && (erFørstegangsSøknad(behandlingType));
    }

    private boolean erEndringUtenEndretPeriode(BehandlingVedtak vedtak) {
        if (!erEndring(vedtak.getBehandlingsresultat().getBehandling().getType())) {
            return false;
        }
        Optional<Behandling> originalBehandling = vedtak.getBehandlingsresultat().getBehandling().getOriginalBehandling();
        if (!originalBehandling.isPresent()) {
            throw HendelsePublisererFeil.FACTORY.manglerOriginialBehandlingPåEndringsVedtak().toException();
        }
        return !uttakFomEllerTomErEndret(vedtak.getBehandlingsresultat(), originalBehandling.get().getBehandlingsresultat());
    }

    @Override
    public void lagreFagsakAvsluttet(FagsakStatusEvent event) {

        log.info("lagrer utgående hendelse for fagsak {}", event.getFagsakId());
        Innhold innhold = mapFagsakEventTilInnhold(event);

        String payloadJason = JsonMapper.toJson(innhold);

        VedtakUtgåendeHendelse vedtakUtgåendeHendelse = VedtakUtgåendeHendelse.builder()
            .aktørId(event.getAktørId().getId())
            .payload(payloadJason)
            .type(Meldingstype.FORELDREPENGER_OPPHOERT.getType())
            .kildeId(FAGSAK_PREFIX + String.valueOf(event.getFagsakId()))
            .build();
        feedRepository.lagre(vedtakUtgåendeHendelse);

    }

    private Innhold mapFagsakEventTilInnhold(FagsakStatusEvent event) {
        Fagsak fagsak = fagsakRepository.finnEksaktFagsak(event.getFagsakId());
        Innhold innhold = new ForeldrepengerOpphoert();
        innhold.setGsakId(fagsak.getSaksnummer().getVerdi());
        innhold.setAktoerId(event.getAktørId().getId());

        Optional<Behandling> behandling = behandlingRepository.hentSisteBehandlingForFagsakId(event.getFagsakId());
        if (!behandling.isPresent()) {
            throw HendelsePublisererFeil.FACTORY.finnerIkkeBehandlingForFagsak().toException();
        }
        Optional<UttakResultatEntitet> uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(behandling.get());
        if (!uttakResultat.isPresent()) {
            throw HendelsePublisererFeil.FACTORY.finnerIkkeRelevantUttaksplanForVedtak().toException();

        }
        UttakFomTom uttakFomTom = finnFørsteOgSisteStønadsdag(uttakResultat.get());
        innhold.setFoersteStoenadsdag(uttakFomTom.førsteStønadsdag);
        innhold.setSisteStoenadsdag(uttakFomTom.sisteStønadsdag);

        return innhold;
    }

    private UttakFomTom finnFørsteOgSisteStønadsdag(UttakResultatEntitet uttakResultat) {
        UttakFomTom resultat = new UttakFomTom();
        List<UttakResultatPeriodeEntitet> innvilgedePerioder = uttakResultat.getGjeldendePerioder().getPerioder().stream()
            .filter(periode -> PeriodeResultatType.INNVILGET.equals(periode.getPeriodeResultatType()))
            .collect(Collectors.toList());
        if (!innvilgedePerioder.isEmpty()) {
            resultat.førsteStønadsdag = innvilgedePerioder.stream().map(UttakResultatPeriodeEntitet::getFom).min(LocalDate::compareTo).get();
            resultat.sisteStønadsdag = innvilgedePerioder.stream().map(UttakResultatPeriodeEntitet::getTom).max(LocalDate::compareTo).get();
        } else {
            resultat.førsteStønadsdag = uttakResultat.getGjeldendePerioder().getPerioder().stream().map(UttakResultatPeriodeEntitet::getFom).min(LocalDate::compareTo).get();

            //Hvis det ikke finnes noen innvilgede perioder etter revurdering så vil hendelsen være at ytelsen opphører samme dag som den opprinnelig ble innvilget
            resultat.sisteStønadsdag = resultat.førsteStønadsdag;
        }
        return resultat;
    }

    private UttakFomTom finnOriginalStønadsOppstart(UttakResultatEntitet uttakResultat) {
        UttakFomTom resultat = new UttakFomTom();
        List<UttakResultatPeriodeEntitet> perioder = uttakResultat.getGjeldendePerioder().getPerioder().stream()
            .filter(periode -> PeriodeResultatType.INNVILGET.equals(periode.getPeriodeResultatType()))
            .collect(Collectors.toList());
        if (perioder.isEmpty()) {
            perioder = uttakResultat.getGjeldendePerioder().getPerioder();
        }
        resultat.førsteStønadsdag = perioder.stream().map(UttakResultatPeriodeEntitet::getFom).min(LocalDate::compareTo).get();
        resultat.sisteStønadsdag = resultat.førsteStønadsdag;

        return resultat;
    }

    private Innhold mapVedtakTilInnhold(BehandlingVedtak vedtak) {
        Innhold innhold;
        BehandlingType behandlingType = vedtak.getBehandlingsresultat().getBehandling().getType();
        Optional<Behandling> originalBehandling = Optional.empty();
        if (erFørstegangsSøknad(behandlingType) || erInnvilgetRevurdering(behandlingType, vedtak.getBehandlingsresultat().getBehandlingResultatType())) {
            innhold = new ForeldrepengerInnvilget();
        } else if (erOpphørtRevurdering(behandlingType, vedtak.getBehandlingsresultat().getBehandlingResultatType())) {
            innhold = new ForeldrepengerOpphoert();
            originalBehandling = vedtak.getBehandlingsresultat().getBehandling().getOriginalBehandling();
        } else {
            innhold = new ForeldrepengerEndret();
            originalBehandling = vedtak.getBehandlingsresultat().getBehandling().getOriginalBehandling();
        }

        if (originalBehandling.isPresent() && !uttakFomEllerTomErEndret(originalBehandling.get().getBehandlingsresultat(), vedtak.getBehandlingsresultat())) {//NOSONAR
            throw HendelsePublisererFeil.FACTORY.manglerOriginialBehandlingPåEndringsVedtak().toException();
        }

        Optional<UttakResultatEntitet> uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(vedtak.getBehandlingsresultat().getBehandling());
        UttakFomTom uttakFomTom;
        if (uttakResultat.isPresent()) {
            uttakFomTom = finnFørsteOgSisteStønadsdag(uttakResultat.get());
        } else if (originalBehandling.isPresent()) {

            Optional<UttakResultatEntitet> origUttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(originalBehandling.get());
            if (!origUttakResultat.isPresent()) {
                throw HendelsePublisererFeil.FACTORY.finnerIkkeRelevantUttaksplanForVedtak().toException();
            }
            uttakFomTom = finnOriginalStønadsOppstart(origUttakResultat.get());
        } else {
            throw HendelsePublisererFeil.FACTORY.finnerIkkeRelevantUttaksplanForVedtak().toException();
        }

        innhold.setFoersteStoenadsdag(uttakFomTom.førsteStønadsdag);
        innhold.setSisteStoenadsdag(uttakFomTom.sisteStønadsdag);

        innhold.setAktoerId(vedtak.getBehandlingsresultat().getBehandling().getAktørId().getId());
        innhold.setGsakId(vedtak.getBehandlingsresultat().getBehandling().getFagsak().getSaksnummer().getVerdi());

        return innhold;
    }

    private boolean uttakFomEllerTomErEndret(Behandlingsresultat gammeltResultat, Behandlingsresultat nyttResultat) {
        Optional<UttakResultatEntitet> gammeltUttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(gammeltResultat.getBehandling());
        Optional<UttakResultatEntitet> nyttUttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(nyttResultat.getBehandling());

        if (!gammeltUttakResultat.isPresent() || !nyttUttakResultat.isPresent()) {
            return true;
        }
        UttakFomTom gammelStønadsperiode = finnFørsteOgSisteStønadsdag(gammeltUttakResultat.get());
        UttakFomTom nyStønadsperionde = finnFørsteOgSisteStønadsdag(nyttUttakResultat.get());

        return !gammelStønadsperiode.førsteStønadsdag.equals(nyStønadsperionde.førsteStønadsdag) ||
            !gammelStønadsperiode.sisteStønadsdag.equals(nyStønadsperionde.sisteStønadsdag);
    }

    private boolean erInnvilgetRevurdering(BehandlingType behandlingType, BehandlingResultatType behandlingResultatType) {
        return BehandlingType.REVURDERING.equals(behandlingType) && BehandlingResultatType.INNVILGET.equals(behandlingResultatType);
    }


    private boolean erFørstegangsSøknad(BehandlingType behandlingType) {
        return BehandlingType.FØRSTEGANGSSØKNAD.equals(behandlingType);
    }

    private boolean erOpphørtRevurdering(BehandlingType behandlingType, BehandlingResultatType behandlingResultatType) {
        return BehandlingType.REVURDERING.equals(behandlingType) && BehandlingResultatType.OPPHØR.equals(behandlingResultatType);
    }

    private boolean erBeslutningsvedtak(BehandlingType behandlingType, BehandlingResultatType behandlingResultatType) {
        return BehandlingType.REVURDERING.equals(behandlingType) && BehandlingResultatType.INGEN_ENDRING.equals(behandlingResultatType);
    }

    private boolean erEndring(BehandlingType behandlingType) {
        return BehandlingType.REVURDERING.equals(behandlingType);
    }
}
