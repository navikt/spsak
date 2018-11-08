package no.nav.foreldrepenger.behandling.impl;

import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.behandling.BehandlendeFagsystem.BehandlendeSystem.INFOTRYGD;
import static no.nav.foreldrepenger.behandling.BehandlendeFagsystem.BehandlendeSystem.MANUELL_VURDERING;
import static no.nav.foreldrepenger.behandling.BehandlendeFagsystem.BehandlendeSystem.PRØV_IGJEN;
import static no.nav.foreldrepenger.behandling.BehandlendeFagsystem.BehandlendeSystem.VEDTAKSLØSNING;
import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType.INNSYN;
import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType.KLAGE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandling.BehandlendeFagsystem;
import no.nav.foreldrepenger.behandling.FagsakTjeneste;
import no.nav.foreldrepenger.behandling.VurderFagsystem;
import no.nav.foreldrepenger.behandling.VurderFagsystemTjeneste;
import no.nav.foreldrepenger.behandlingslager.IntervallUtil;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Adopsjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Terminbekreftelse;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Journalpost;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Arbeidsforhold;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsforholdTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.FPDateUtil;


@ApplicationScoped
public class VurderFagsystemTjenesteImpl implements VurderFagsystemTjeneste {
    private static final TemporalAmount fireUker = Period.parse("P4W");
    private static final TemporalAmount sekstenUker = Period.parse("P16W");
    private static final TemporalAmount treMåneder = Period.parse("P3M");
    private static final TemporalAmount tiMåneder = Period.parse("P10M");

    private Period venteFristAareg;

    private FagsakTjeneste fagsakTjeneste;
    private ArbeidsforholdTjeneste arbeidsforholdTjeneste;
    private TpsTjeneste tpsTjeneste;
    private FagsakRepository fagsakRepository;
    private BehandlingRepository behandlingRepository;
    private BehandlingRepositoryProvider repositoryProvider;
    private FamilieHendelseRepository familieHendelseRepository;
    private MottatteDokumentTjeneste mottatteDokumentTjeneste;

    public VurderFagsystemTjenesteImpl() {
        //For CDI
    }

    @Inject
    public VurderFagsystemTjenesteImpl(@KonfigVerdi("fordeling.venter.intervall") Period venteFristAareg,
                                       FagsakTjeneste fagsakTjeneste,
                                       ArbeidsforholdTjeneste arbeidsforholdTjeneste,
                                       TpsTjeneste tpsTjeneste,
                                       BehandlingRepositoryProvider repositoryProvider, MottatteDokumentTjeneste mottatteDokumentTjeneste) {
        this.venteFristAareg = venteFristAareg;
        this.fagsakTjeneste = fagsakTjeneste;
        this.arbeidsforholdTjeneste = arbeidsforholdTjeneste;
        this.tpsTjeneste = tpsTjeneste;
        this.repositoryProvider = repositoryProvider;
        this.fagsakRepository = repositoryProvider.getFagsakRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.familieHendelseRepository = repositoryProvider.getFamilieGrunnlagRepository();
        this.mottatteDokumentTjeneste = mottatteDokumentTjeneste;
    }

    @Override
    public BehandlendeFagsystem vurderFagsystem(VurderFagsystem vurderFagsystem) {
        Optional<Journalpost> journalpost = Optional.empty();
        if (vurderFagsystem.getJournalpostId().isPresent()) {
            journalpost = fagsakTjeneste.hentJournalpost(vurderFagsystem.getJournalpostId().get());
        }
        if (journalpost.isPresent()) {
            return new BehandlendeFagsystem(VEDTAKSLØSNING).medSaksnummer(journalpost.get().getFagsak().getSaksnummer());
        }

        if (vurderFagsystem.getSaksnummer().isPresent()) {
            return vurderSøknadMedSaksnummer(vurderFagsystem, vurderFagsystem.getSaksnummer().get());
        }

        if (BehandlingTema.gjelderForeldrepenger(vurderFagsystem.getBehandlingTema())) {
            return vurderForeldrepengerDokument(vurderFagsystem);
        }

        if (BehandlingTema.gjelderEngangsstønad(vurderFagsystem.getBehandlingTema()) && vurderFagsystem.isStrukturertSøknad()) {
            return vurderStrukturertEngangsstønadSøknad(vurderFagsystem);
        }

        return vurderUstrukturertDokument(vurderFagsystem);
    }

    private boolean vurderOmSakGjelderSammeBarn(VurderFagsystem vurderFagsystem, Fagsak fagsak) {

        Optional<Behandling> behandling = hentSisteYtelsesBehandling(fagsak.getId());
        if (!behandling.isPresent()) {
            return false;
        }
        Optional<FamilieHendelseGrunnlag> familieHendelseAggregat = familieHendelseRepository.hentAggregatHvisEksisterer(behandling.get());
        if (!familieHendelseAggregat.isPresent()) {
            return false;
        }
        Optional<LocalDate> fødselsDatoSøknad = vurderFagsystem.getBarnFodselsdato();
        if (fødselsDatoSøknad.isPresent()) {
            Optional<LocalDate> fødselsDatoSak = familieHendelseAggregat.get().getGjeldendeVersjon().getFødselsdato();
            if (fødselsDatoSak.isPresent()) {
                return fødselsDatoSak.equals(fødselsDatoSøknad);
            }
        }
        if (vurderFagsystem.getBarnTermindato().isPresent()) {

            Optional<Terminbekreftelse> terminbekreftelse = familieHendelseAggregat.get().getGjeldendeTerminbekreftelse();
            if (terminbekreftelse.isPresent()) {
                return terminbekreftelse.get().getTermindato().equals(vurderFagsystem.getBarnTermindato().get()); //NOSONAR
            }
        }
        if (vurderFagsystem.getOmsorgsovertakelsedato().isPresent()) {
            Optional<Adopsjon> adopsjon = familieHendelseAggregat.get().getGjeldendeAdopsjon();
            if (adopsjon.isPresent()) {
                return gjelderSammeAdopsjonskull(vurderFagsystem, familieHendelseAggregat.get(), adopsjon.get());
            }
        }

        return false;
    }

    private boolean gjelderSammeAdopsjonskull(VurderFagsystem vurderFagsystem, FamilieHendelseGrunnlag familieHendelseAggregat, Adopsjon adopsjon) {
        List<LocalDate> fødselsdatoerFraGrunnlag = familieHendelseAggregat.getGjeldendeBarna().stream().map(UidentifisertBarn::getFødselsdato).collect(Collectors.toList());
        return adopsjon.getOmsorgsovertakelseDato().equals(vurderFagsystem.getOmsorgsovertakelsedato().get()) && //NOSONAR
            erAdopsjonsBarnFødselsdatoerLike(fødselsdatoerFraGrunnlag,
                vurderFagsystem.getAdopsjonsbarnFodselsdatoer());
    }

    private BehandlendeFagsystem vurderSøknadMedSaksnummer(VurderFagsystem vurderFagsystem, Saksnummer saksnummer) {
        Optional<Fagsak> sak = filterUtFagsakSomBehandlesAvInfotrygd(fagsakRepository.hentSakGittSaksnummer(saksnummer));
        if (sak.isPresent()) {
            if (erSakenAvsluttet(sak.get())) {
                return vurderTilfelleMedAvsluttetFagsak(saksnummer, sak.get());
            }
            return new BehandlendeFagsystem(VEDTAKSLØSNING).medSaksnummer(saksnummer);
        }

        if (harAnnenPartSakForSammeBarn(vurderFagsystem)) {
            throw SkjæringstidspunktFeil.FACTORY.brukersSaknummerIkkeFunnetIVLSelvOmAnnenPartsSakErDer(saksnummer).toException();
        }

        return new BehandlendeFagsystem(MANUELL_VURDERING);
    }

    private BehandlendeFagsystem vurderTilfelleMedAvsluttetFagsak(Saksnummer saksnummer, Fagsak sak) {
        if (mottatteDokumentTjeneste.erSisteYtelsesbehandlingAvslåttPgaManglendeDokumentasjon(sak)) {
            if (!mottatteDokumentTjeneste.harFristForInnsendingAvDokGåttUt(sak)) {
                return new BehandlendeFagsystem(VEDTAKSLØSNING).medSaksnummer(saksnummer);
            }
        }
        return new BehandlendeFagsystem(MANUELL_VURDERING);
    }

    private boolean erSakenAvsluttet(Fagsak sak) {
        return !sak.erÅpen();
    }


    private boolean harAnnenPartSakForSammeBarn(VurderFagsystem vurderFagsystem) {
        Optional<AktørId> annenPart = vurderFagsystem.getAnnenPart();
        if (annenPart.isPresent()) {
            return filterUtFagsakSomBehandlesAvInfotrygd(fagsakRepository.hentForBruker(annenPart.get())).stream().anyMatch(annenPartSak -> vurderOmSakGjelderSammeBarn(vurderFagsystem, annenPartSak));
        }
        return false;
    }

    private BehandlendeFagsystem vurderStrukturertEngangsstønadSøknad(VurderFagsystem vurderFagsystem) {
        List<Fagsak> fagsaker = hentFagsaker(vurderFagsystem.getAktørId(), vurderFagsystem.getBehandlingTema());

        List<Fagsak> passendeFagsaker = fagsaker
            .stream()
            .filter(sak -> erSakPassendeForES(sak, vurderFagsystem))
            .collect(Collectors.toList());


        if (passendeFagsaker.isEmpty()) {
            if (fagsaker.stream().anyMatch(f -> getBehandlingsTemaForFagsak(f).equals(BehandlingTema.ENGANGSSTØNAD))) {
                return new BehandlendeFagsystem(MANUELL_VURDERING);
            }
            if (harAnnenPartSakForSammeBarn(vurderFagsystem)) {
                return new BehandlendeFagsystem(VEDTAKSLØSNING);
            } else
                return new BehandlendeFagsystem(INFOTRYGD);
        }

        if (passendeFagsaker.size() != 1) {
            return new BehandlendeFagsystem(MANUELL_VURDERING);
        }
        if (harLukketBehandling(fagsaker.get(0))) {
            return new BehandlendeFagsystem(MANUELL_VURDERING);
        }
        return new BehandlendeFagsystem(VEDTAKSLØSNING).medSaksnummer(fagsaker.get(0).getSaksnummer());
    }

    private BehandlendeFagsystem vurderUstrukturertDokument(VurderFagsystem vurderFagsystem) {
        List<Fagsak> saker = hentFagsaker(vurderFagsystem.getAktørId(), vurderFagsystem.getBehandlingTema());

        boolean erSøknad = DokumentTypeId.getSøknadTyper().contains(vurderFagsystem.getDokumentTypeId()) ||
            DokumentKategori.SØKNAD.equals(vurderFagsystem.getDokumentKategori());

        if (saker.isEmpty()) {
            // Vil unngå saker uten behandling som man ikke kan gjøre noe med i fpsak ....
            return erSøknad ? new BehandlendeFagsystem(INFOTRYGD) : new BehandlendeFagsystem(MANUELL_VURDERING);
        }

        // Finnes åpen sak?
        Optional<Fagsak> åpenSak = saker.stream().filter(Fagsak::erÅpen).max(finnSisteBehandlingsDato());
        if (åpenSak.isPresent()) {
            return new BehandlendeFagsystem(VEDTAKSLØSNING).medSaksnummer(åpenSak.get().getSaksnummer());
        }

        // Finnes avsluttet sak nyere enn grensen?
        Optional<Fagsak> avsluttetSakNyereEnn3mnd = finnSisteAvsluttedeSakNyereEnn3mnd(saker);
        if (avsluttetSakNyereEnn3mnd.isPresent()) {
            return new BehandlendeFagsystem(MANUELL_VURDERING);
        }

        Optional<Fagsak> avsluttetSakEldreEnn3mndMenNyereEnn10mnd = finnSisteAvsluttetSakEldreEnn3mndMenNyereEnn10mnd(saker);
        if (erSøknad && !avsluttetSakEldreEnn3mndMenNyereEnn10mnd.isPresent()) {
            return new BehandlendeFagsystem(VEDTAKSLØSNING);
        } else {
            return new BehandlendeFagsystem(MANUELL_VURDERING);
        }
    }

    private BehandlingTema getBehandlingsTemaForFagsak(Fagsak s) {
        Optional<Behandling> behandling = hentSisteYtelsesBehandling(s.getId());
        if (!behandling.isPresent()) {
            return BehandlingTema.fraFagsak(s, null);
        }

        Behandling sisteBehandling = behandling.get();
        final Optional<FamilieHendelseGrunnlag> grunnlag = repositoryProvider.getFamilieGrunnlagRepository().hentAggregatHvisEksisterer(sisteBehandling);
        return BehandlingTema.fraFagsak(s, grunnlag.map(FamilieHendelseGrunnlag::getSøknadVersjon).orElse(null));
    }

    private BehandlendeFagsystem vurderForeldrepengerDokument(VurderFagsystem vurderFagsystem) {

        if (vurderFagsystem.erInntektsmelding() && !arbeidsforholdErIAareg(vurderFagsystem)) {
            Optional<LocalDateTime> mottattTidspunktOptional = vurderFagsystem.getForsendelseMottattTidspunkt();
            // TODO PFP-57 Opprydding - Skal fjernes når kontrakt er oppdatert
            Optional<LocalDate> mottattDagOptional = vurderFagsystem.getForsendelseMottatt();
            if (mottattTidspunktOptional.isPresent()) {
                return finnBehandlendeFagsystemBasertPåFrist(mottattTidspunktOptional.get());
            } else if (mottattDagOptional.isPresent()){
                return finnBehandlendeFagsystemBasertPåFristDag(mottattDagOptional.get());
            } else {
                throw new IllegalStateException("Utviklerfeil - mangler mottattidspunkt for inntektsmelding, aktørId " + vurderFagsystem.getAktørId());
            }
        }

        List<Fagsak> alleFagsaker = hentFagsaker(vurderFagsystem.getAktørId(), vurderFagsystem.getBehandlingTema());

        List<Fagsak> åpneFagsaker;

        if (vurderFagsystem.erInntektsmelding()) {
            åpneFagsaker = alleFagsaker
                .stream()
                .filter(Fagsak::erÅpen)
                .collect(Collectors.toList());
        } else {
            åpneFagsaker = alleFagsaker
                .stream()
                .filter(this::erÅpenOgHarIkkeLukketBehandling)
                .collect(Collectors.toList());
        }

        if (åpneFagsaker.size() == 1) {
            return new BehandlendeFagsystem(VEDTAKSLØSNING).medSaksnummer(åpneFagsaker.get(0).getSaksnummer());
        } else if (åpneFagsaker.isEmpty()) {
            if (!alleFagsaker.isEmpty()) {
                return new BehandlendeFagsystem(MANUELL_VURDERING);
            } else if (skalVurdereInfotrygdForAnnenPart(vurderFagsystem)) {
                return new BehandlendeFagsystem(INFOTRYGD);
            } else
                return new BehandlendeFagsystem(VEDTAKSLØSNING);
        }
        return new BehandlendeFagsystem(MANUELL_VURDERING);
    }

    private BehandlendeFagsystem finnBehandlendeFagsystemBasertPåFrist(LocalDateTime mottattTidspunkt) {
        LocalDateTime grenseForVenting = mottattTidspunkt.plus(this.venteFristAareg);
        if (grenseForVenting.isAfter(FPDateUtil.nå())) {
            return new BehandlendeFagsystem(PRØV_IGJEN).medPrøvIgjenTidspunkt(grenseForVenting);
        } else {
            return new BehandlendeFagsystem(MANUELL_VURDERING);
        }
    }

    // TODO PFP-57 Opprydding - Skal fjernes når kontrakt er oppdatert
    private BehandlendeFagsystem finnBehandlendeFagsystemBasertPåFristDag(LocalDate mottattTidspunkt) {
        LocalDate grenseForVenting = mottattTidspunkt.plus(this.venteFristAareg);
        if (grenseForVenting.isAfter(FPDateUtil.iDag())) {
            LocalDateTime prøvIgjenTidspunkt = FPDateUtil.nå().plus(this.venteFristAareg);
            return new BehandlendeFagsystem(PRØV_IGJEN).medPrøvIgjenTidspunkt(prøvIgjenTidspunkt);
        } else {
            return new BehandlendeFagsystem(MANUELL_VURDERING);
        }
    }

    private boolean erÅpenOgHarIkkeLukketBehandling(Fagsak fagsak) {
        return fagsak.erÅpen() && !harLukketBehandling(fagsak);
    }

    private boolean harLukketBehandling(Fagsak fagsak) {
        Optional<Behandling> sisteBehandling = hentSisteYtelsesBehandling(fagsak.getId());
        if (sisteBehandling.isPresent()) {
            return sisteBehandling.get().getStatus().equals(BehandlingStatus.AVSLUTTET);
        }
        return false;
    }

    private boolean arbeidsforholdErIAareg(VurderFagsystem vurderFagsystem) {

        AktørId aktørId = vurderFagsystem.getAktørId();
        PersonIdent fnr = tpsTjeneste.hentFnrForAktør(aktørId);

        Optional<LocalDate> startDatoForeldrepengerInntektsmelding = vurderFagsystem.getStartDatoForeldrepengerInntektsmelding();
        LocalDate permisjonsstart = startDatoForeldrepengerInntektsmelding.orElse(LocalDate.now(FPDateUtil.getOffset()));
        Interval intervall = IntervallUtil.byggIntervall(permisjonsstart.minusMonths(4), permisjonsstart.plusMonths(1));

        Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforholdIdentifikatorListMap =
            arbeidsforholdTjeneste.finnArbeidsforholdForIdentIPerioden(fnr, intervall);

        for (ArbeidsforholdIdentifikator arbeidsforhold : arbeidsforholdIdentifikatorListMap.keySet()) {
            Optional<String> arbeidsforholdsid = vurderFagsystem.getArbeidsforholdsid();
            boolean arbeidsforholdIdMatcher = !arbeidsforholdsid.isPresent()
                || arbeidsforholdsid.get().equals(arbeidsforhold.getArbeidsforholdId().getReferanse());

            String virksomhetsnummer = vurderFagsystem.getVirksomhetsnummer().orElse("N/A");
            boolean matcherVirksomhet = arbeidsforhold.getArbeidsgiver() != null
                && arbeidsforhold.getArbeidsgiver().getIdentifikator().equals(virksomhetsnummer);

            if (arbeidsforholdIdMatcher && matcherVirksomhet) {
                return true;
            }
        }
        return false;
    }

    private boolean skalVurdereInfotrygdForAnnenPart(VurderFagsystem vurderFagsystem) {
        if (!vurderFagsystem.getAnnenPart().isPresent()) {
            return false;
        }
        return !harAnnenPartSakForSammeBarn(vurderFagsystem);
    }

    private List<Fagsak> hentFagsaker(AktørId aktørId, BehandlingTema behandlingTema) {
        return fagsakRepository.hentForBruker(aktørId).stream()
            .filter(f -> !f.getSkalTilInfotrygd())
            .filter(s -> kompatibeltBehandlingstema(getBehandlingsTemaForFagsak(s), behandlingTema))
            .collect(Collectors.toList());
    }

    private boolean kompatibeltBehandlingstema(BehandlingTema sak, BehandlingTema request) {
        if (BehandlingTema.gjelderSammeYtelse(sak, request)) {
            return sak.equals(request) || BehandlingTema.ikkeSpesifikkHendelse(sak) || BehandlingTema.ikkeSpesifikkHendelse(request);
        }
        return false;
    }

    private List<Fagsak> filterUtFagsakSomBehandlesAvInfotrygd(List<Fagsak> fagsak) {
        return fagsak.stream().filter(f -> !f.getSkalTilInfotrygd()).collect(Collectors.toList());
    }

    private Optional<Fagsak> filterUtFagsakSomBehandlesAvInfotrygd(Optional<Fagsak> fagsak) {
        if (fagsak.isPresent()) {
            return fagsak.filter(f -> !f.getSkalTilInfotrygd());
        }
        return Optional.empty();
    }

    private boolean erSakPassendeForES(Fagsak fagsak, VurderFagsystem vurderFagsystem) {
        Optional<Behandling> behandling = hentSisteYtelsesBehandling(fagsak.getId());
        if (!behandling.isPresent()) {
            return true;
        }

        Behandling sisteBehandling = behandling.get();
        final BehandlingTema vurderTema = vurderFagsystem.getBehandlingTema();
        final Optional<FamilieHendelseGrunnlag> grunnlag = repositoryProvider.getFamilieGrunnlagRepository().hentAggregatHvisEksisterer(sisteBehandling);
        if (grunnlag.isPresent()) {
            final FamilieHendelse søknadVersjon = grunnlag.get().getSøknadVersjon();
            if (søknadVersjon.getGjelderFødsel() && (BehandlingTema.ENGANGSSTØNAD_FØDSEL.equals(vurderTema) || BehandlingTema.ENGANGSSTØNAD.equals(vurderTema))) {
                return erPassendeFødselsSak(vurderFagsystem, grunnlag.get());
            } else if (søknadVersjon.getGjelderAdopsjon() && (BehandlingTema.ENGANGSSTØNAD_ADOPSJON.equals(vurderTema) || BehandlingTema.ENGANGSSTØNAD.equals(vurderTema))) {
                return erPassendeAdopsjonsSak(vurderFagsystem, grunnlag.get());
            }
        }
        return false;
    }

    private Comparator<Fagsak> finnSisteBehandlingsDato() {
        return Comparator.comparing(fagsak -> hentSisteYtelsesBehandling(fagsak.getId())
            .map(Behandling::getOpprettetDato).orElse(LocalDateTime.MIN));
    }

    // TODO (ØysteinHardeng): Er det ikke bedre å angi cut-off som parameter enn å hardkode i metode?
    private Optional<Fagsak> finnSisteAvsluttedeSakNyereEnn3mnd(List<Fagsak> saker) {
        return saker.stream()
            .filter(fagsak -> !fagsak.erÅpen())
            .filter(fagsak -> hentSisteYtelsesBehandling(fagsak.getId())
                .map(behandling -> behandling.getOpprettetDato().isAfter(ChronoLocalDateTime.from(FPDateUtil.nå().minus(treMåneder))))
                .orElse(false))
            .max(finnSisteBehandlingsDato());
    }

    // TODO (ØysteinHardeng): Er det ikke bedre å angi cut-off som parameter enn å hardkode i metode?
    private Optional<Fagsak> finnSisteAvsluttetSakEldreEnn3mndMenNyereEnn10mnd(List<Fagsak> saker) {
        return saker.stream()
            .filter(fagsak -> !fagsak.erÅpen())
            .filter(fagsak -> hentSisteYtelsesBehandling(fagsak.getId())
                .map(behandling ->
                    behandling.getOpprettetDato().isBefore(ChronoLocalDateTime.from(FPDateUtil.nå().minus(treMåneder)))
                        && behandling.getOpprettetDato().isAfter(ChronoLocalDateTime.from(FPDateUtil.nå().minus(tiMåneder))))
                .orElse(false))
            .max(finnSisteBehandlingsDato());
    }

    private boolean erPassendeFødselsSak(VurderFagsystem vurderFagsystem, FamilieHendelseGrunnlag grunnlag) {
        Optional<LocalDate> termindatoPåSak = grunnlag.getGjeldendeTerminbekreftelse().map(Terminbekreftelse::getTermindato);
        Optional<LocalDate> fødselsdatoPåSak = grunnlag.getGjeldendeBarna().stream().map(UidentifisertBarn::getFødselsdato).findFirst();
        Optional<LocalDate> termindatoPåSøknad = vurderFagsystem.getBarnTermindato();
        Optional<LocalDate> fødselsdatoPåSøknad = vurderFagsystem.getBarnFodselsdato();

        return erDatoIPeriodeHvisBeggeErTilstede(termindatoPåSøknad, termindatoPåSak, fireUker, fireUker) ||
            erDatoIPeriodeHvisBeggeErTilstede(fødselsdatoPåSøknad, termindatoPåSak, sekstenUker, fireUker) ||
            erDatoIPeriodeHvisBeggeErTilstede(termindatoPåSøknad, fødselsdatoPåSak, fireUker, sekstenUker) ||
            erDatoIPeriodeHvisBeggeErTilstede(fødselsdatoPåSøknad, fødselsdatoPåSak, fireUker, fireUker);
    }

    private boolean erPassendeAdopsjonsSak(VurderFagsystem vurderFagsystem, FamilieHendelseGrunnlag grunnlag) {
        Optional<LocalDate> overtagelsesDatoPåSak = grunnlag.getGjeldendeAdopsjon().map(Adopsjon::getOmsorgsovertakelseDato);
        Optional<LocalDate> overtagelsesDatoPåSøknad = vurderFagsystem.getOmsorgsovertakelsedato();

        if (!erDatoIPeriodeHvisBeggeErTilstede(overtagelsesDatoPåSøknad, overtagelsesDatoPåSak, fireUker, fireUker)) {
            return false;
        }

        List<LocalDate> fødselsDatoerPåSak = grunnlag.getGjeldendeBarna().stream().map(UidentifisertBarn::getFødselsdato)
            .collect(Collectors.toList());
        List<LocalDate> fødselsDatoerPåSøknad = vurderFagsystem.getAdopsjonsbarnFodselsdatoer();

        return erAdopsjonsBarnFødselsdatoerLike(fødselsDatoerPåSak, fødselsDatoerPåSøknad);
    }

    private boolean erAdopsjonsBarnFødselsdatoerLike(List<LocalDate> datoer1, List<LocalDate> datoer2) {
        if (datoer1.size() != datoer2.size()) {
            return false;
        }
        List<LocalDate> d1 = new ArrayList<>(datoer1);
        List<LocalDate> d2 = new ArrayList<>(datoer2);
        Collections.sort(d1);
        Collections.sort(d2);

        for (int i = 0; i < d1.size(); i++) {
            if (!d1.get(i).equals(d2.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean erDatoIPeriodeHvisBeggeErTilstede(Optional<LocalDate> nyDato, Optional<LocalDate> periodeDato, TemporalAmount førPeriode, TemporalAmount etterPeriode) {
        return (periodeDato.isPresent() && nyDato.isPresent())
            && !(nyDato.get().isBefore(periodeDato.get().minus(førPeriode)) || nyDato.get().isAfter(periodeDato.get().plus(etterPeriode)));
    }

    private Optional<Behandling> hentSisteYtelsesBehandling(Long fagsakId) {
        return behandlingRepository.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(fagsakId, asList(KLAGE, INNSYN));
    }
}
