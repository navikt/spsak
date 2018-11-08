package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.validering;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjon;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskontoberegning;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPerioder;
import no.nav.vedtak.feil.FeilFactory;

@ApplicationScoped
public class OverstyrUttakResultatValidatorImpl implements OverstyrUttakResultatValidator {

    private FagsakRelasjonRepository fagsakRelasjonRepository;


    OverstyrUttakResultatValidatorImpl() {
        // CDI
    }

    @Inject
    public OverstyrUttakResultatValidatorImpl(FagsakRelasjonRepository fagsakRelasjonRepository) {
        this.fagsakRelasjonRepository = fagsakRelasjonRepository;
    }

    @Override
    public void valider(Fagsak fagsak, UttakResultatPerioder opprinnelig, UttakResultatPerioder perioder) {
        new PerioderHarFastsattResultatValidering().utfør(perioder);
        new IkkeNegativKontoValidering(stønadskontoer(fagsak)).utfør(perioder);
        new BareSplittetPerioderValidering(opprinnelig).utfør(perioder);
        new EndringerHarBegrunnelseValidering(opprinnelig).utfør(perioder);
        new HarSattUtbetalingsprosentValidering(opprinnelig).utfør(perioder);
        new EndringerBareEtterEndringsdatoValidering().utfør(perioder);
    }

    private List<Stønadskonto> stønadskontoer(Fagsak fagsak) {
        FagsakRelasjon fagsakRelasjon = fagsakRelasjonRepository.finnRelasjonFor(fagsak);
        Optional<Stønadskontoberegning> stønadskontoberegning = fagsakRelasjon.getStønadskontoberegning();
        Set<Stønadskonto> stønadskontoer = stønadskontoberegning.orElseThrow(
            () -> FeilFactory.create(OverstyrUttakValideringFeil.class).fantIkkeStønadskontoer(fagsak.getId()).toException())
            .getStønadskontoer();
        return new ArrayList<>(stønadskontoer);
    }
}
