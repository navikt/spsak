package no.nav.foreldrepenger.domene.mottak.hendelser;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.revurdering.RevurderingTjeneste;
import no.nav.foreldrepenger.behandling.revurdering.impl.RevurderingTjenesteProvider;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.task.StartBehandlingTask;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRevurderingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.mottak.Behandlingsoppretter;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.DokumentPersistererTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.BehandlendeEnhetTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.util.FPDateUtil;

@Dependent
public class BehandlingsoppretterImpl implements Behandlingsoppretter {

    private BehandlingVedtakRepository behandlingVedtakRepository;
    private BehandlingRepository behandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private RevurderingTjenesteProvider revurderingTjenesteProvider;
    private DokumentPersistererTjeneste dokumentPersistererTjeneste;
    private ProsessTaskRepository prosessTaskRepository;
    private MottatteDokumentTjeneste mottatteDokumentTjeneste;
    private SøknadRepository søknadRepository;
    private BehandlendeEnhetTjeneste behandlendeEnhetTjeneste;
    private KodeverkRepository kodeverkRepository;
    private AksjonspunktRepository aksjonspunktRepository;
    private BehandlingRevurderingRepository revurderingRepository;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;

    public BehandlingsoppretterImpl() {
        // For CDI
    }

    @Inject
    public BehandlingsoppretterImpl(BehandlingRepositoryProvider behandlingRepositoryProvider,
                                    BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                    RevurderingTjenesteProvider revurderingTjenesteProvider,
                                    DokumentPersistererTjeneste dokumentPersistererTjeneste,
                                    ProsessTaskRepository prosessTaskRepository,
                                    MottatteDokumentTjeneste mottatteDokumentTjeneste,
                                    BehandlendeEnhetTjeneste behandlendeEnhetTjeneste,
                                    HistorikkinnslagTjeneste historikkinnslagTjeneste,
                                    InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste) {
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.revurderingTjenesteProvider = revurderingTjenesteProvider;
        this.dokumentPersistererTjeneste = dokumentPersistererTjeneste;
        this.prosessTaskRepository = prosessTaskRepository;
        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
        this.mottatteDokumentTjeneste = mottatteDokumentTjeneste;
        this.aksjonspunktRepository = behandlingRepositoryProvider.getAksjonspunktRepository();
        this.søknadRepository = behandlingRepositoryProvider.getSøknadRepository();
        this.kodeverkRepository = behandlingRepositoryProvider.getKodeverkRepository();
        this.behandlendeEnhetTjeneste = behandlendeEnhetTjeneste;
        this.revurderingRepository = behandlingRepositoryProvider.getBehandlingRevurderingRepository();
        this.behandlingVedtakRepository = behandlingRepositoryProvider.getBehandlingVedtakRepository();
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
    }

    @Override
    public boolean erKompletthetssjekkPassert(Behandling behandling) {
        return behandlingskontrollTjeneste.erStegPassert(behandling, BehandlingStegType.VURDER_KOMPLETTHET);
    }

    @Override
    public Behandling opprettFørstegangsbehandling(Fagsak fagsak, BehandlingÅrsakType behandlingÅrsakType) {
        BehandlingType behandlingType = kodeverkRepository.finn(BehandlingType.class, BehandlingType.FØRSTEGANGSSØKNAD);
        return behandlingskontrollTjeneste.opprettNyBehandling(fagsak, behandlingType, (beh) -> {
            if (!behandlingÅrsakType.equals(BehandlingÅrsakType.UDEFINERT)) {
                BehandlingÅrsak.builder(behandlingÅrsakType).buildFor(beh);
            }
            beh.setBehandlingstidFrist(LocalDate.now(FPDateUtil.getOffset()).plusWeeks(behandlingType.getBehandlingstidFristUker()));
            setBehandlendeEnhet(beh);
        }); // NOSONAR
    }

    @Override
    public Behandling henleggOgOpprettNyFørstegangsbehandling(Fagsak fagsak, Behandling behandling, BehandlingÅrsakType behandlingÅrsakType) {
        henleggBehandling(behandling);
        Behandling nyFørstegangsbehandling = opprettNyFørstegangsbehandling(behandlingÅrsakType, fagsak);
        kopierInntektsmeldinger(behandling, nyFørstegangsbehandling);
        return nyFørstegangsbehandling;
    }

    @Override
    public Behandling opprettNyFørstegangsbehandling(BehandlingÅrsakType behandlingÅrsakType, Fagsak fagsak) {
        BehandlingType behandlingType = kodeverkRepository.finn(BehandlingType.class, BehandlingType.FØRSTEGANGSSØKNAD);
        Behandling forrigeBehandling = behandlingRepository.hentSisteBehandlingForFagsakId(fagsak.getId(), behandlingType)
            .orElseThrow(() -> new IllegalStateException("Fant ingen behandling som passet for saksnummer: " + fagsak.getSaksnummer()));

        return behandlingskontrollTjeneste.opprettNyBehandling(fagsak, behandlingType, (beh) -> {
                beh.setBehandlingstidFrist(LocalDate.now(FPDateUtil.getOffset()).plusWeeks(behandlingType.getBehandlingstidFristUker()));
                beh.setBehandlendeEnhet(forrigeBehandling.getBehandlendeOrganisasjonsEnhet());
                if (!behandlingÅrsakType.equals(BehandlingÅrsakType.UDEFINERT)) {
                    BehandlingÅrsak.builder(behandlingÅrsakType).buildFor(beh);
                }
            }
        );
    }

    @Override
    public Behandling opprettRevurdering(Fagsak fagsak, BehandlingÅrsakType revurderingsÅrsak) {
        RevurderingTjeneste revurderingTjeneste = revurderingTjenesteProvider.finnRevurderingTjenesteFor(fagsak);
        Behandling revurdering = revurderingTjeneste.opprettAutomatiskRevurdering(fagsak, revurderingsÅrsak);
        opprettTaskForÅStarteBehandling(revurdering);
        return revurdering;
    }

    @Override
    public Behandling oppdaterBehandlingViaHenleggelse(Behandling sisteYtelseBehandling, BehandlingÅrsakType revurderingsÅrsak) {
        henleggBehandling(sisteYtelseBehandling);
        Behandling revurdering = opprettRevurdering(sisteYtelseBehandling.getFagsak(), revurderingsÅrsak);

        kopierInntektsmeldinger(sisteYtelseBehandling, revurdering);

        // Kopier behandlingsårsaker fra forrige behandling
        new BehandlingÅrsak.Builder(sisteYtelseBehandling.getBehandlingÅrsaker().stream()
            .map(BehandlingÅrsak::getBehandlingÅrsakType)
            .collect(toList()))
            .buildFor(revurdering);

        BehandlingskontrollKontekst nyKontekst = behandlingskontrollTjeneste.initBehandlingskontroll(revurdering);
        behandlingRepository.lagre(revurdering, nyKontekst.getSkriveLås());

        return revurdering;
    }

    @Override
    public void henleggBehandling(Behandling behandling) {
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling.getId());
        behandlingskontrollTjeneste.settAutopunkterTilUtført(kontekst, true);
        behandlingskontrollTjeneste.henleggBehandling(kontekst, BehandlingResultatType.MERGET_OG_HENLAGT);
    }

    private void kopierInntektsmeldinger(Behandling origBehandling, Behandling nyBehandling) {
        List<Inntektsmelding> inntektsmeldinger = inntektArbeidYtelseTjeneste.hentAlleInntektsmeldingerForFagsak(origBehandling.getFagsakId());
        inntektsmeldinger.sort(Comparator.comparing(Inntektsmelding::getInnsendingstidspunkt));
        inntektsmeldinger.stream()
            .map(InntektsmeldingBuilder::kopi)
            .forEach(imb -> inntektArbeidYtelseTjeneste.lagre(nyBehandling, imb.build()));
    }

    @Override
    public boolean harMottattFørstegangssøknad(Behandling behandling) {
        return søknadRepository.hentSøknadHvisEksisterer(behandling).isPresent();
    }

    @Override
    public Behandling opprettKøetBehandling(Fagsak fagsak, BehandlingÅrsakType eksternÅrsak) {
        Optional<Behandling> sisteYtelsesbehandling = revurderingRepository.hentSisteYtelsesbehandling(fagsak.getId());
        Behandling behandling;
        if (sisteYtelsesbehandling.isPresent()) {
            behandling = opprettRevurdering(fagsak, eksternÅrsak);
        } else {
            behandling = opprettFørstegangsbehandling(fagsak, eksternÅrsak);
        }
        settSomKøet(behandling);
        return behandling;
    }

    @Override
    public Behandling opprettBerørtBehandling(Fagsak fagsak) {
        return opprettRevurdering(fagsak, BehandlingÅrsakType.BERØRT_BEHANDLING);
    }

    private void opprettTaskForÅStarteBehandling(Behandling behandling) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(StartBehandlingTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setCallIdFraEksisterende();
        prosessTaskRepository.lagre(prosessTaskData);
    }

    @Override
    public Behandling finnEllerOpprettFørstegangsbehandling(Fagsak fagsak) {
        BehandlingType behandlingType = kodeverkRepository.finn(BehandlingType.class, BehandlingType.FØRSTEGANGSSØKNAD);
        return behandlingskontrollTjeneste.opprettNyEllerOppdaterEksisterendeBehandling(fagsak, behandlingType,
            (beh) -> {
                LocalDate behandlingstidFrist = LocalDate.now(FPDateUtil.getOffset()).plusWeeks(behandlingType.getBehandlingstidFristUker());
                beh.setBehandlingstidFrist(behandlingstidFrist);
                if (beh.getBehandlendeEnhet() == null) {
                    setBehandlendeEnhet(beh);
                }
            });
    }

    private void setBehandlendeEnhet(Behandling behandling) {
        OrganisasjonsEnhet enhet = behandlendeEnhetTjeneste.finnBehandlendeEnhetFraSøker(behandling);
        behandling.setBehandlendeEnhet(enhet);
    }

    @Override
    public void settSomKøet(Behandling nyKøetBehandling) {
        aksjonspunktRepository.leggTilAksjonspunkt(nyKøetBehandling, AksjonspunktDefinisjon.AUTO_KØET_BEHANDLING);
    }

    @Override
    public boolean erAvslåttFørstegangsbehandling(Behandling behandling) {
        Boolean erVedtakAvslag = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandling.getId())
            .map(BehandlingVedtak::getVedtakResultatType)
            .map(vrt -> VedtakResultatType.AVSLAG.equals(vrt)).orElse(Boolean.FALSE);
        return erVedtakAvslag && behandling.getType().equals(BehandlingType.FØRSTEGANGSSØKNAD);
    }

    @Override
    public void opprettNyFørstegangsbehandling(InngåendeSaksdokument mottattDokument, Fagsak fagsak, Behandling avsluttetBehandling) {
        Behandling behandling = finnEllerOpprettFørstegangsbehandling(fagsak);

        // Ny førstegangssøknad med payload
        if (DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL.getKode().equals(mottattDokument.getDokumentTypeId().getKode()) && mottattDokument.harPayload()) {
            mottatteDokumentTjeneste.persisterDokumentinnhold(behandling, mottattDokument, Optional.empty());
        }

        opprettTaskForÅStarteBehandling(behandling);
        historikkinnslagTjeneste.opprettHistorikkinnslag(behandling, mottattDokument.getJournalpostId());
    }

}
