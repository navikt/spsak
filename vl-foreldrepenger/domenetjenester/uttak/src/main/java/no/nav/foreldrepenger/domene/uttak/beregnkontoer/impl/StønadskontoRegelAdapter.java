package no.nav.foreldrepenger.domene.uttak.beregnkontoer.impl;

import static no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType.FEDREKVOTE;
import static no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType.FELLESPERIODE;
import static no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType.FLERBARNSDAGER;
import static no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType.FORELDREPENGER;
import static no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType.MØDREKVOTE;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjon;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskontoberegning;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.uttaksvilkår.StønadskontoRegelOrkestrering;
import no.nav.foreldrepenger.uttaksvilkår.StønadskontoResultat;

@ApplicationScoped
public class StønadskontoRegelAdapter {

    private StønadskontoRegelOrkestrering stønadskontoRegel = new StønadskontoRegelOrkestrering();
    private StønadskontoRegelOversetter stønadskontoRegelOversetter = new StønadskontoRegelOversetter();
    private FagsakRelasjonRepository fagsakRelasjonRepository;

    @Inject
    public StønadskontoRegelAdapter(FagsakRelasjonRepository fagsakRelasjonRepository) {
        this.fagsakRelasjonRepository = fagsakRelasjonRepository;
    }

    StønadskontoRegelAdapter() {
        // For CDI
    }

    public Stønadskontoberegning beregnKontoer(Behandling behandling, FamilieHendelseGrunnlag familieHendelseGrunnlag, YtelseFordelingAggregat ytelseFordelingAggregat) {
        boolean harSøkerRett = !behandling.getBehandlingsresultat().isVilkårAvslått();

        FagsakRelasjon fagsakRelasjon = fagsakRelasjonRepository.finnRelasjonFor(behandling.getFagsak());

        BeregnKontoerGrunnlag grunnlag = stønadskontoRegelOversetter.tilRegelmodell(behandling.getRelasjonsRolleType(), familieHendelseGrunnlag, ytelseFordelingAggregat, fagsakRelasjon, harSøkerRett);
        StønadskontoResultat stønadskontoResultat = stønadskontoRegel.beregnKontoer(grunnlag);

        return konverterTilStønadskontoberegning(stønadskontoResultat);
    }

    private Stønadskontoberegning konverterTilStønadskontoberegning(StønadskontoResultat stønadskontoResultat) {
        Stønadskontoberegning.Builder stønadskontoberegningBuilder = Stønadskontoberegning.builder()
            .medRegelEvaluering(stønadskontoResultat.getEvalueringResultat())
            .medRegelInput(stønadskontoResultat.getInnsendtGrunnlag());

        Map<no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype, Integer> maksDagerStønadskonto = stønadskontoResultat.getStønadskontoer();
        for (Map.Entry<no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype, Integer> entry : maksDagerStønadskonto.entrySet()) {
            Stønadskonto stønadskonto = Stønadskonto.builder()
                .medMaxDager(entry.getValue())
                .medStønadskontoType(mapFra(entry.getKey()))
                .build();
            stønadskontoberegningBuilder.medStønadskonto(stønadskonto);
        }
        return stønadskontoberegningBuilder.build();
    }

    private StønadskontoType mapFra(no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype stønadskontotype) {
        switch (stønadskontotype) {
            case FEDREKVOTE:
                return FEDREKVOTE;
            case MØDREKVOTE:
                return MØDREKVOTE;
            case FELLESPERIODE:
                return FELLESPERIODE;
            case FORELDREPENGER:
                return FORELDREPENGER;
            case FLERBARNSDAGER:
                return FLERBARNSDAGER;
            case FORELDREPENGER_FØR_FØDSEL:
                return FORELDREPENGER_FØR_FØDSEL;
            default:
                throw new IllegalArgumentException(String.format("Ukjent stønadskontotype: %s", stønadskontotype));
        }
    }

}
