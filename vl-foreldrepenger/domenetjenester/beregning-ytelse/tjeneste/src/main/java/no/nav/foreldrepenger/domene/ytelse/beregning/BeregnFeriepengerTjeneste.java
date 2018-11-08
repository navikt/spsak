package no.nav.foreldrepenger.domene.ytelse.beregning;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFeriepenger;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjon;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository;
import no.nav.foreldrepenger.beregning.regelmodell.feriepenger.BeregningsresultatFeriepengerRegelModell;
import no.nav.foreldrepenger.beregning.regler.feriepenger.RegelBeregnFeriepenger;
import no.nav.foreldrepenger.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.foreldrepenger.beregningsgrunnlag.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.foreldrepenger.domene.ytelse.beregning.adapter.MapBeregningsresultatFeriepengerFraRegelTilVL;
import no.nav.foreldrepenger.domene.ytelse.beregning.adapter.MapBeregningsresultatFeriepengerFraVLTilRegel;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@ApplicationScoped
public class BeregnFeriepengerTjeneste {

    private JacksonJsonConfig jacksonJsonConfig = new JacksonJsonConfig();
    private FagsakRelasjonRepository fagsakRelasjonRepository;
    private BehandlingRepository behandlingRepository;
    private BeregningsresultatFPRepository beregningsresultatRepository;

    BeregnFeriepengerTjeneste() {
        //NOSONAR
    }

    @Inject
    public BeregnFeriepengerTjeneste(BehandlingRepositoryProvider behandlingRepositoryProvider) {
        this.fagsakRelasjonRepository = behandlingRepositoryProvider.getFagsakRelasjonRepository();
        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
        this.beregningsresultatRepository = behandlingRepositoryProvider.getBeregningsresultatFPRepository();
    }

    public void beregnFeriepenger(Behandling behandling, BeregningsresultatFP beregningsresultatFP, Beregningsgrunnlag beregningsgrunnlag) {

        Optional<Behandling> annenPartsBehandling = finnAnnenPartsBehandling(behandling);
        Optional<BeregningsresultatFP> annenPartsBeregningsresultat = annenPartsBehandling.flatMap(beh -> {
            if (BehandlingResultatType.INNVILGET.equals(beh.getBehandlingsresultat().getBehandlingResultatType())) {
                return beregningsresultatRepository.hentBeregningsresultatFP(beh);
            }
            return Optional.empty();
        });

        BeregningsresultatFeriepengerRegelModell regelModell = MapBeregningsresultatFeriepengerFraVLTilRegel.mapFra(beregningsgrunnlag, behandling, beregningsresultatFP, annenPartsBeregningsresultat);
        String regelInput = toJson(regelModell);

        RegelBeregnFeriepenger regelBeregnFeriepenger = new RegelBeregnFeriepenger();
        Evaluation evaluation = regelBeregnFeriepenger.evaluer(regelModell);
        RegelResultat regelResultat = RegelmodellOversetter.getRegelResultat(evaluation);

        BeregningsresultatFeriepenger beregningsresultatFeriepenger = BeregningsresultatFeriepenger.builder()
            .medFeriepengerRegelInput(regelInput)
            .medFeriepengerRegelSporing(regelResultat.getRegelSporing())
            .build(beregningsresultatFP);

        MapBeregningsresultatFeriepengerFraRegelTilVL.mapFra(beregningsresultatFP, regelModell, beregningsresultatFeriepenger);
    }

    private Optional<Behandling> finnAnnenPartsBehandling(Behandling behandling) {
        Optional<Fagsak> annenFagsakOpt = finnAnnenPartsFagsak(behandling.getFagsak());
        return annenFagsakOpt.flatMap(fagsak -> behandlingRepository.finnSisteAvsluttedeIkkeHenlagteBehandling(fagsak.getId()));
    }

    private Optional<Fagsak> finnAnnenPartsFagsak(Fagsak fagsak) {
        Optional<FagsakRelasjon> optionalFagsakRelasjon = fagsakRelasjonRepository.finnRelasjonForHvisEksisterer(fagsak);
        return optionalFagsakRelasjon.flatMap(fagsakRelasjon -> fagsakRelasjon.getFagsakNrEn().equals(fagsak) ? fagsakRelasjon.getFagsakNrTo() : Optional.of(fagsakRelasjon.getFagsakNrEn()));
    }

    private String toJson(BeregningsresultatFeriepengerRegelModell grunnlag) {
        JacksonJsonConfig var10000 = this.jacksonJsonConfig;
        BeregnFeriepengerFeil var10002 = BeregnFeriepengerFeil.FACTORY;
        return var10000.toJson(grunnlag, var10002::jsonMappingFeilet);
    }

    interface BeregnFeriepengerFeil extends DeklarerteFeil {
        BeregnFeriepengerFeil FACTORY = FeilFactory.create(BeregnFeriepengerFeil.class); // NOSONAR ok med konstant

        @TekniskFeil(feilkode = "FP-985762", feilmelding = "JSON mapping feilet", logLevel = LogLevel.ERROR)
        Feil jsonMappingFeilet(JsonProcessingException var1);
    }
}
