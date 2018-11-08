package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

/**
 * <h3>Internt kodeverk</h3>
 * Definerer typer av handlinger en saksbehandler kan gjøre vedrørende et arbeidsforhold
 * <p>
 */
@Entity(name = "ArbeidsforholdHandlingType")
@DiscriminatorValue(ArbeidsforholdHandlingType.DISCRIMINATOR)
public class ArbeidsforholdHandlingType extends Kodeliste {

    public static final String DISCRIMINATOR = "ARBEIDSFORHOLD_HANDLING_TYPE";

    public static final ArbeidsforholdHandlingType BRUK = new ArbeidsforholdHandlingType("BRUK"); //$NON-NLS-1$
    public static final ArbeidsforholdHandlingType NYTT_ARBEIDSFORHOLD = new ArbeidsforholdHandlingType("NYTT_ARBEIDSFORHOLD"); //$NON-NLS-1$
    public static final ArbeidsforholdHandlingType BRUK_UTEN_INNTEKTSMELDING = new ArbeidsforholdHandlingType("BRUK_UTEN_INNTEKTSMELDING"); //$NON-NLS-1$
    public static final ArbeidsforholdHandlingType IKKE_BRUK = new ArbeidsforholdHandlingType("IKKE_BRUK"); //$NON-NLS-1$
    public static final ArbeidsforholdHandlingType SLÅTT_SAMMEN_MED_ANNET = new ArbeidsforholdHandlingType("SLÅTT_SAMMEN_MED_ANNET"); //$NON-NLS-1$

    public static final ArbeidsforholdHandlingType UDEFINERT = new ArbeidsforholdHandlingType("-"); //$NON-NLS-1$

    public ArbeidsforholdHandlingType() {
    }

    public ArbeidsforholdHandlingType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
