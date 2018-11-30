package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;



@Entity(name = "InntektsmeldingInnsendingsårsak")
@DiscriminatorValue(InntektsmeldingInnsendingsårsak.DISCRIMINATOR)
public class InntektsmeldingInnsendingsårsak extends Kodeliste {

    public static final String DISCRIMINATOR = "INNTEKTSMELDING_INNSENDINGSAARSAK"; //$NON-NLS-1$

    public static final InntektsmeldingInnsendingsårsak NY = new InntektsmeldingInnsendingsårsak("NY");
    public static final InntektsmeldingInnsendingsårsak ENDRING = new InntektsmeldingInnsendingsårsak("ENDRING");
    public static final InntektsmeldingInnsendingsårsak UDEFINERT = new InntektsmeldingInnsendingsårsak("-");

    InntektsmeldingInnsendingsårsak() {
        //for Hibernate
    }

    private InntektsmeldingInnsendingsårsak(String kode) {
        super(kode, DISCRIMINATOR);
    }


}
