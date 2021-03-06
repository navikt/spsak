package no.nav.foreldrepenger.behandlingslager.behandling;

import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "BehandlingÅrsakType")
@DiscriminatorValue(BehandlingÅrsakType.DISCRIMINATOR)
public class BehandlingÅrsakType extends Kodeliste {
    
    public static final String DISCRIMINATOR = "BEHANDLING_AARSAK"; //$NON-NLS-1$
    public static final BehandlingÅrsakType RE_FEIL_ELLER_ENDRET_FAKTA = new BehandlingÅrsakType("RE-FEFAKTA"); //$NON-NLS-1$
    public static final BehandlingÅrsakType RE_ENDRING_FRA_BRUKER = new BehandlingÅrsakType("RE-END-FRA-BRUKER"); //$NON-NLS-1$
    public static final BehandlingÅrsakType RE_ENDRET_INNTEKTSMELDING = new BehandlingÅrsakType("RE-END-INNTEKTSMELD"); //$NON-NLS-1$
    public static final BehandlingÅrsakType KØET_BEHANDLING = new BehandlingÅrsakType("KØET-BEHANDLING"); //$NON-NLS-1$
    public static final BehandlingÅrsakType RE_ANNET = new BehandlingÅrsakType("RE-ANNET"); //$NON-NLS-1$

    // Manuelt opprettet revurdering (obs: årsakene kan også bli satt på en automatisk opprettet revurdering)
    public static final BehandlingÅrsakType RE_OPPLYSNINGER_OM_MEDLEMSKAP = new BehandlingÅrsakType("RE-MDL");
    public static final BehandlingÅrsakType RE_OPPLYSNINGER_OM_OPPTJENING = new BehandlingÅrsakType("RE-OPTJ");
    public static final BehandlingÅrsakType RE_OPPLYSNINGER_OM_INNTEKT = new BehandlingÅrsakType("RE-INNTK");
    public static final BehandlingÅrsakType RE_OPPLYSNINGER_OM_DØD = new BehandlingÅrsakType("RE-DØD");
    public static final BehandlingÅrsakType RE_OPPLYSNINGER_OM_SØKNAD_FRIST = new BehandlingÅrsakType("RE-FRIST");
    public static final BehandlingÅrsakType RE_OPPLYSNINGER_OM_BEREGNINGSGRUNNLAG = new BehandlingÅrsakType("RE-BER-GRUN");

    public static final BehandlingÅrsakType RE_REGISTEROPPLYSNING = new BehandlingÅrsakType("RE-REGISTEROPPL"); //$NON-NLS-1$
    public static final BehandlingÅrsakType RE_OPPLYSNINGER_OM_YTELSER = new BehandlingÅrsakType("RE-YTELSE"); //$NON-NLS-1$
    public static final BehandlingÅrsakType RE_TILSTØTENDE_YTELSE_INNVILGET = new BehandlingÅrsakType("RE-TILST-YT-INNVIL"); //$NON-NLS-1$
    public static final BehandlingÅrsakType RE_ENDRING_BEREGNINGSGRUNNLAG = new BehandlingÅrsakType("RE-ENDR-BER-GRUN"); //$NON-NLS-1$
    public static final BehandlingÅrsakType RE_TILSTØTENDE_YTELSE_OPPHØRT = new BehandlingÅrsakType("RE-TILST-YT-OPPH"); //$NON-NLS-1$

    public static final BehandlingÅrsakType UDEFINERT = new BehandlingÅrsakType("-"); //$NON-NLS-1$

    BehandlingÅrsakType() {
        //for Hibernate
    }

    private BehandlingÅrsakType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static Set<BehandlingÅrsakType> årsakerForAutomatiskRevurdering() {
        return Set.of(RE_TILSTØTENDE_YTELSE_INNVILGET, RE_ENDRING_BEREGNINGSGRUNNLAG, RE_TILSTØTENDE_YTELSE_OPPHØRT);
    }
}
