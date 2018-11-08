package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import javax.persistence.DiscriminatorValue;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@DiscriminatorValue(UttakDokumentasjonType.DISCRIMINATOR)
public class UttakDokumentasjonType extends Kodeliste {

    public static final String DISCRIMINATOR = "UTTAK_DOKUMENTASJON_TYPE";
    public static final UttakDokumentasjonType UTEN_OMSORG = new UttakDokumentasjonType("UTEN_OMSORG"); //$NON-NLS-1$
    public static final UttakDokumentasjonType ALENEOMSORG = new UttakDokumentasjonType("ALENEOMSORG"); //$NON-NLS-1$

    public static final UttakDokumentasjonType SYK_SØKER = new UttakDokumentasjonType("SYK_SOKER"); //$NON-NLS-1$
    public static final UttakDokumentasjonType INNLAGT_SØKER = new UttakDokumentasjonType("INNLAGT_SOKER"); //$NON-NLS-1$
    public static final UttakDokumentasjonType INNLAGT_BARN = new UttakDokumentasjonType("INNLAGT_BARN"); //$NON-NLS-1$
    public static final UttakDokumentasjonType UTEN_DOKUMENTASJON = new UttakDokumentasjonType("UTEN_DOKUMENTASJON"); //$NON-NLS-1$

    public static final UttakDokumentasjonType INSTITUSJONSOPPHOLD_ANNEN_FORELDRE = new UttakDokumentasjonType("INSTITUSJONSOPPHOLD_ANNEN_FORELDRE"); //$NON-NLS-1$
    public static final UttakDokumentasjonType SYKDOM_ANNEN_FORELDER = new UttakDokumentasjonType("SYKDOM_ANNEN_FORELDER"); //$NON-NLS-1$

    private UttakDokumentasjonType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    UttakDokumentasjonType() {
        // For Hibernate
    }

}
