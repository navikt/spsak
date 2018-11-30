package no.nav.foreldrepenger.behandling.steg.beregnytelse.fp;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.steg.beregnytelse.api.BeregneYtelseSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.domene.beregning.ytelse.BeregnFeriepengerTjeneste;
import no.nav.foreldrepenger.domene.beregning.ytelse.FastsettBeregningsresultatTjeneste;
import no.nav.foreldrepenger.domene.beregning.ytelse.FinnEndringsdatoBeregningsresultatFPTjeneste;

/**
 * Steg for å beregne tilkjent ytelse (for Foreldrepenger).
 */
@BehandlingStegRef(kode = "BERYT")
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class BeregneYtelseForeldrepengerStegImpl implements BeregneYtelseSteg {

    private BehandlingRepository behandlingRepository;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private BeregningsresultatFPRepository beregningsresultatFPRepository;
    private FastsettBeregningsresultatTjeneste fastsettBeregningsresultatTjeneste;
    private UttakRepository uttakRepository;
    private BeregnFeriepengerTjeneste beregnFeriepengerTjeneste;
    private FinnEndringsdatoBeregningsresultatFPTjeneste finnEndringsdatoBeregningsresultatFPTjeneste;

    BeregneYtelseForeldrepengerStegImpl() {
        // for CDI proxy
    }

    @Inject
    BeregneYtelseForeldrepengerStegImpl(BehandlingRepositoryProvider repositoryProvider,
                                        FastsettBeregningsresultatTjeneste fastsettBeregningsresultatTjeneste,
                                        BeregnFeriepengerTjeneste beregnFeriepengerTjeneste,
                                        FinnEndringsdatoBeregningsresultatFPTjeneste finnEndringsdatoBeregningsresultatFPTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        this.beregningsresultatFPRepository = repositoryProvider.getBeregningsresultatFPRepository();
        this.fastsettBeregningsresultatTjeneste = fastsettBeregningsresultatTjeneste;
        this.finnEndringsdatoBeregningsresultatFPTjeneste = finnEndringsdatoBeregningsresultatFPTjeneste;
        this.uttakRepository = repositoryProvider.getUttakRepository();
        this.beregnFeriepengerTjeneste = beregnFeriepengerTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {

        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling);
        UttakResultatEntitet uttakResultat = uttakRepository.hentUttakResultat(behandling);

        // Kalle regeltjeneste
        BeregningsresultatFP beregningsresultat = fastsettBeregningsresultatTjeneste.fastsettBeregningsresultat(beregningsgrunnlag, uttakResultat, behandling);

        // Beregn feriepenger
        beregnFeriepengerTjeneste.beregnFeriepenger(behandling, beregningsresultat, beregningsgrunnlag);

        // Sett endringsdato
        if (behandling.erRevurdering()) {
            Optional<LocalDate> endringsDato = finnEndringsdatoBeregningsresultatFPTjeneste.finnEndringsdato(behandling, beregningsresultat);
            endringsDato.ifPresent(endringsdato -> BeregningsresultatFP.builder(beregningsresultat).medEndringsdato(endringsdato));
        }

        // Lagre beregningsresultat
        beregningsresultatFPRepository.lagre(behandling, beregningsresultat);

        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }

    @Override
    public void vedHoppOverBakover(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell, BehandlingStegType tilSteg, BehandlingStegType fraSteg) {
        beregningsresultatFPRepository.deaktiverBeregningsresultatFP(behandling, kontekst.getSkriveLås());
    }


}
