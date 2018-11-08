package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "InntektsKilde")
@DiscriminatorValue(InntektsKilde.DISCRIMINATOR)
public class InntektsKilde extends Kodeliste {

    public static final String DISCRIMINATOR = "INNTEKTS_KILDE"; //$NON-NLS-1$

    public static final InntektsKilde UDEFINERT = new InntektsKilde("-"); //$NON-NLS-1$
    public static final InntektsKilde INNTEKT_OPPTJENING = new InntektsKilde("INNTEKT_OPPTJENING"); //$NON-NLS-1$
    public static final InntektsKilde INNTEKT_BEREGNING = new InntektsKilde("INNTEKT_BEREGNING"); //$NON-NLS-1$
    public static final InntektsKilde INNTEKT_SAMMENLIGNING = new InntektsKilde("INNTEKT_SAMMENLIGNING"); //$NON-NLS-1$
    public static final InntektsKilde SIGRUN = new InntektsKilde("SIGRUN"); //$NON-NLS-1$

    private InntektsKilde(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public InntektsKilde() {
        //hibernate
    }
}
