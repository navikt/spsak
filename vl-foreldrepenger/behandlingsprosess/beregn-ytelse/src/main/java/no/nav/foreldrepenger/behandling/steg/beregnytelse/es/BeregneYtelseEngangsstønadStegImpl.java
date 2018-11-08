package no.nav.foreldrepenger.behandling.steg.beregnytelse.es;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.steg.beregnytelse.api.BeregneYtelseSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Sats;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.SatsType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepository;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.FPDateUtil;

/** Steg for å beregne tilkjent ytelse (for Engangsstønad). */
@BehandlingStegRef(kode = "BERYT")
@BehandlingTypeRef
@FagsakYtelseTypeRef("ES")
@ApplicationScoped
public class BeregneYtelseEngangsstønadStegImpl implements BeregneYtelseSteg {

    private int maksStønadsalderAdopsjon;

    private BehandlingRepositoryProvider repositoryProvider;
    private BeregningRepository beregningRepository;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;

    BeregneYtelseEngangsstønadStegImpl() {
        // for CDI proxy
    }

    @Inject
    BeregneYtelseEngangsstønadStegImpl(BehandlingRepositoryProvider repositoryProvider, BeregningRepository beregningRepository,
                                       @KonfigVerdi(value = "maks.stønadsalder.adopsjon") int maksStønadsalder,
                                       SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        this.repositoryProvider = repositoryProvider;
        this.beregningRepository = beregningRepository;
        this.maksStønadsalderAdopsjon = maksStønadsalder;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = repositoryProvider.getBehandlingRepository().hentBehandling(kontekst.getBehandlingId());

        Beregning sisteBeregning = finnSisteBeregning(behandling);
        if (sisteBeregning == null || !sisteBeregning.isOverstyrt()) {
            long antallBarn = new BarnFinner(repositoryProvider).finnAntallBarn(behandling, maksStønadsalderAdopsjon);
            LocalDate satsDato = getSatsDato(behandling);
            Sats sats = beregningRepository.finnEksaktSats(SatsType.ENGANG, satsDato);
            long beregnetYtelse = sats.getVerdi() * antallBarn;
            Beregning beregning = new Beregning(sats.getVerdi(), antallBarn, beregnetYtelse, LocalDateTime.now(FPDateUtil.getOffset()));

            BeregningResultat beregningResultat = BeregningResultat.builder()
                .medBeregning(beregning)
                .buildFor(behandling);
            beregningRepository.lagre(beregningResultat, kontekst.getSkriveLås());
        }
        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }

    private LocalDate getSatsDato(Behandling behandling) {
        return skjæringstidspunktTjeneste.utledSkjæringstidspunktForEngangsstønadFraBekreftedeData(behandling)
            // Må godta skjæringstidspunkt fra oppgitte data, ettersom tillatt å godkj. SRB-vilkår uten å bekrefte data
            .orElseGet(() -> skjæringstidspunktTjeneste.utledSkjæringstidspunktForEngangsstønadFraOppgitteData(behandling));
    }

    private Beregning finnSisteBeregning(Behandling behandling) {
        if (behandling.getBehandlingsresultat() != null && behandling.getBehandlingsresultat().getBeregningResultat() != null) {
            Optional<Beregning> sisteBeregningOptional = behandling.getBehandlingsresultat().getBeregningResultat().getSisteBeregning();
            if (sisteBeregningOptional.isPresent()) {
                return sisteBeregningOptional.get();
            }
        }
        return null;
    }

    @Override
    public void vedTransisjon(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell,
                              TransisjonType transisjonType, BehandlingStegType førsteSteg, BehandlingStegType sisteSteg, TransisjonType inngangUtgang) {

        RyddBeregninger ryddBeregninger = new RyddBeregninger(repositoryProvider.getBehandlingRepository(), behandling, kontekst);
        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
        if (behandlingsresultat != null && behandlingsresultat.getBeregningResultat() != null
            && erFremoverhoppEllerErTilbakehoppOgBeregningIkkeErOverstyrt(transisjonType, behandlingsresultat)) {
            ryddBeregninger.ryddBeregninger();
        }
    }

    private boolean erFremoverhoppEllerErTilbakehoppOgBeregningIkkeErOverstyrt(TransisjonType transisjonType, Behandlingsresultat behandlingsresultat) {
        return Objects.equals(TransisjonType.HOPP_OVER_FRAMOVER, transisjonType)
            || (Objects.equals(TransisjonType.HOPP_OVER_BAKOVER, transisjonType) && !behandlingsresultat.getBeregningResultat().isOverstyrt());
    }
}
