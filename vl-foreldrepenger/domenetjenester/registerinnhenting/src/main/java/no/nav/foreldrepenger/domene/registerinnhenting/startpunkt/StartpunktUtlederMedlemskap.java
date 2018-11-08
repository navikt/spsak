package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;
import no.nav.foreldrepenger.domene.medlem.identifiserer.MedlemEndringIdentifiserer;

@ApplicationScoped
@GrunnlagRef("MedlemskapAggregat")
class StartpunktUtlederMedlemskap implements StartpunktUtleder {

    private MedlemskapRepository medlemskapRepository;
    private MedlemEndringIdentifiserer endringIdentifiserer;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;

    @Inject
    StartpunktUtlederMedlemskap(MedlemskapRepository medlemskapRepository, MedlemEndringIdentifiserer endringIdentifiserer, SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        this.medlemskapRepository = medlemskapRepository;
        this.endringIdentifiserer = endringIdentifiserer;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
    }

    @Override
    public StartpunktType utledStartpunkt(Behandling behandling, Long grunnlagId1, Long grunnlagId2) {
        final MedlemskapAggregat grunnlag1 = medlemskapRepository.hentMedlemskapPåId(grunnlagId1);
        final MedlemskapAggregat grunnlag2 = medlemskapRepository.hentMedlemskapPåId(grunnlagId2);

        final boolean erEndretFørSkjæringstidspunkt = endringIdentifiserer.erEndretFørSkjæringstidspunkt(grunnlag1, grunnlag2, skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling));
        if (erEndretFørSkjæringstidspunkt) {
            FellesStartpunktUtlederLogger.loggEndringSomFørteTilStartpunkt(this.getClass().getSimpleName(), StartpunktType.INNGANGSVILKÅR_MEDLEMSKAP, "medlemskap", grunnlagId1, grunnlagId2);
            return StartpunktType.INNGANGSVILKÅR_MEDLEMSKAP;
        }
        FellesStartpunktUtlederLogger.loggEndringSomFørteTilStartpunkt(this.getClass().getSimpleName(), StartpunktType.UTTAKSVILKÅR, "medlemskap", grunnlagId1, grunnlagId2);
        return StartpunktType.UTTAKSVILKÅR;
    }
}
