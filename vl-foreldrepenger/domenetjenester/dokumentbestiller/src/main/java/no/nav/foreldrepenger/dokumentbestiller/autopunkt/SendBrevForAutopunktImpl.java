package no.nav.foreldrepenger.dokumentbestiller.autopunkt;

import java.time.LocalDate;
import java.time.Period;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.finn.unleash.Unleash;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Terminbekreftelse;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerApplikasjonTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillBrevDto;
import no.nav.vedtak.felles.integrasjon.unleash.FeatureToggle;

@ApplicationScoped
public class SendBrevForAutopunktImpl implements SendBrevForAutopunkt {

    static final String SOKT_FOR_TIDLIG_BREV_FEATURE_TOGGLE_NAVN = "fpsak.sokt-for-tidlig-brev";

    private DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste;
    private AksjonspunktRepository aksjonspunktRepository;
    private BehandlingRepository behandlingRepository;
    private SøknadRepository søknadRepository;
    private FamilieHendelseRepository familieGrunnlagRepository;
    private Unleash unleash;

    public SendBrevForAutopunktImpl() {
        //CDI
    }

    @Inject
    public SendBrevForAutopunktImpl(DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste,
                                    BehandlingRepositoryProvider provider,
                                    @FeatureToggle("fpsak") Unleash unleash) {
        this.dokumentBestillerApplikasjonTjeneste = dokumentBestillerApplikasjonTjeneste;
        this.aksjonspunktRepository = provider.getAksjonspunktRepository();
        this.behandlingRepository = provider.getBehandlingRepository();
        this.søknadRepository = provider.getSøknadRepository();
        this.familieGrunnlagRepository = provider.getFamilieGrunnlagRepository();
        this.unleash = unleash;
    }

    public void sendBrevForSøknadIkkeMottatt(Behandling behandling) {
        String dokumentMalType = DokumentMalType.INNTEKTSMELDING_FOR_TIDLIG_DOK;
        if (!harSendtBrevForMal(behandling.getId(), dokumentMalType)) {
            BestillBrevDto bestillBrevDto = new BestillBrevDto(behandling.getId(), dokumentMalType);
            dokumentBestillerApplikasjonTjeneste.bestillDokument(bestillBrevDto, HistorikkAktør.VEDTAKSLØSNINGEN);
        }
    }

    public void sendBrevForTidligSøknad(Behandling behandling, Aksjonspunkt ap) {
        if (!unleash.isEnabled(SOKT_FOR_TIDLIG_BREV_FEATURE_TOGGLE_NAVN)) {
            return;
        }
        String dokumentMalType = DokumentMalType.FORLENGET_TIDLIG_SOK;
        if (!harSendtBrevForMal(behandling.getId(), dokumentMalType) && erSøktPåPapir(behandling)) {
            BestillBrevDto bestillBrevDto = new BestillBrevDto(behandling.getId(), dokumentMalType);
            dokumentBestillerApplikasjonTjeneste.bestillDokument(bestillBrevDto, HistorikkAktør.VEDTAKSLØSNINGEN);
        }
        behandling.setBehandlingstidFrist(beregnBehandlingstidsfrist(ap, behandling));
    }

    public void sendBrevForVenterPåFødsel(Behandling behandling, Aksjonspunkt ap) {
        String dokumentMalType = DokumentMalType.FORLENGET_MEDL_DOK;
        final Terminbekreftelse gjeldendeTerminBekreftelse = familieGrunnlagRepository.hentAggregat(behandling)
            .getGjeldendeTerminbekreftelse()
            .orElseThrow(IllegalStateException::new);
        LocalDate frist = beregnFristTid(ap, gjeldendeTerminBekreftelse);
        if (!harSendtBrevForMal(behandling.getId(), dokumentMalType) && frist.isAfter(LocalDate.now().plusDays(1))) {
            BestillBrevDto bestillBrevDto = new BestillBrevDto(behandling.getId(), dokumentMalType);
            dokumentBestillerApplikasjonTjeneste.bestillDokument(bestillBrevDto, HistorikkAktør.VEDTAKSLØSNINGEN);
        }
        if (behandling.getBehandlingstidFrist().isBefore(frist)) {
            behandling.setBehandlingstidFrist(frist);
        }
        aksjonspunktRepository.setFrist(ap, frist.atStartOfDay(), Venteårsak.UDEFINERT);
    }

    private LocalDate beregnFristTid(Aksjonspunkt ap, Terminbekreftelse gjeldendeTerminBekreftelse) {
        LocalDate oppgittTermindato = gjeldendeTerminBekreftelse.getTermindato();
        return oppgittTermindato.plus(Period.parse(ap.getAksjonspunktDefinisjon().getFristPeriode()));
    }

    private boolean erSøktPåPapir(Behandling behandling) {
        return søknadRepository.hentSøknadHvisEksisterer(behandling.getId())
            .filter(søknad -> !søknad.getElektroniskRegistrert()).isPresent();
    }

    private boolean harSendtBrevForMal(Long behandlingId, String malType) {
        return dokumentBestillerApplikasjonTjeneste.erDokumentProdusert(behandlingId, malType);
    }

    private LocalDate beregnBehandlingstidsfrist(Aksjonspunkt ap, Behandling behandling) {
        return LocalDate.from(ap.getFristTid().plusWeeks(behandling.getType().getBehandlingstidFristUker()));
    }

}
