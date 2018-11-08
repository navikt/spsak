package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import java.util.Optional;
import java.util.Set;

/**
 * Immutable aggregat som samler informasjon fra ulike kilder for Medlemskap informasjon fra registere, søknad, og slik
 * det er vurdert av Saksbehandler (evt. automatisk vurdert).
 */
public class MedlemskapAggregat {

    private final VurdertMedlemskap vurdertMedlemskap;
    private final Set<RegistrertMedlemskapPerioder> registrertMedlemskapPeridoer;
    private final OppgittTilknytning oppgittTilknytning;
    private final VurdertMedlemskapPeriode vurderingLøpendeMedlemskap;

    public MedlemskapAggregat(VurdertMedlemskap medlemskap, Set<RegistrertMedlemskapPerioder> medlemskapPerioder,
                              OppgittTilknytning oppgittTilknytning, VurdertMedlemskapPeriode vurderingLøpendeMedlemskap) {
        this.vurdertMedlemskap = medlemskap;
        this.registrertMedlemskapPeridoer = medlemskapPerioder;
        this.oppgittTilknytning = oppgittTilknytning;
        this.vurderingLøpendeMedlemskap = vurderingLøpendeMedlemskap;
    }

    /** Hent Medlemskap slik det er vurdert (hvis eksisterer). */
    public Optional<VurdertMedlemskap> getVurdertMedlemskap() {
        return Optional.ofNullable(vurdertMedlemskap);
    }

    /** Hent Registrert medlemskapinformasjon (MEDL) slik det er innhentet. */
    public Set<RegistrertMedlemskapPerioder> getRegistrertMedlemskapPerioder() {
        return registrertMedlemskapPeridoer;
    }

    /** Hent Oppgitt tilknytning slik det er oppgitt av Søker. */
    public Optional<OppgittTilknytning> getOppgittTilknytning() {
        return Optional.ofNullable(oppgittTilknytning);
    }

    /** Hent Løpende medlemskap (hvis eksisterer)*/
    public Optional<VurdertMedlemskapPeriode> getVurderingLøpendeMedlemskap() {
        return Optional.ofNullable(vurderingLøpendeMedlemskap);
    }
}
