package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "MedlemskapDekningType")
@DiscriminatorValue(MedlemskapDekningType.DISCRIMINATOR)
public class MedlemskapDekningType extends Kodeliste {
    public static final String DISCRIMINATOR = "MEDLEMSKAP_DEKNING";
    public static final MedlemskapDekningType FTL_2_6 = new MedlemskapDekningType("FTL_2_6");          // Folketrygdloven § 2-6
    public static final MedlemskapDekningType FTL_2_7_a = new MedlemskapDekningType("FTL_2_7_a");      // Folketrygdloven § 2-7, 3.ledd bokstav a
    public static final MedlemskapDekningType FTL_2_7_b = new MedlemskapDekningType("FTL_2_7_b");      // Folketrygdloven § 2-7, 3. ledd bokstav b
    public static final MedlemskapDekningType FTL_2_9_1_a = new MedlemskapDekningType("FTL_2_9_1_a");  // Folketrygdloven § 2-9, 1.ledd bokstav a
    public static final MedlemskapDekningType FTL_2_9_1_b = new MedlemskapDekningType("FTL_2_9_1_b");  // Folketrygdloven § 2-9, 1.ledd bokstav b
    public static final MedlemskapDekningType FTL_2_9_1_c = new MedlemskapDekningType("FTL_2_9_1_c");  // Folketrygdloven § 2-9, 1.ledd bokstav c
    public static final MedlemskapDekningType FTL_2_9_2_a = new MedlemskapDekningType("FTL_2_9_2_a");  // Folketrygdloven § 2-9, annet ledd, jfr. 1.ledd bokstav a
    public static final MedlemskapDekningType FTL_2_9_2_c = new MedlemskapDekningType("FTL_2_9_2_c");  // Folketrygdloven § 2-9, annet ledd, jf. 1. ledd bokstav c
    public static final MedlemskapDekningType FULL = new MedlemskapDekningType("FULL");                // Full
    public static final MedlemskapDekningType IHT_AVTALE = new MedlemskapDekningType("IHT_AVTALE");    // I henhold til avtale
    public static final MedlemskapDekningType OPPHOR = new MedlemskapDekningType("OPPHOR");            // Opphør
    public static final MedlemskapDekningType UNNTATT = new MedlemskapDekningType("UNNTATT");          // Unntatt

    public static final List<MedlemskapDekningType> DEKNINGSTYPER = unmodifiableList(asList(
        FTL_2_6,
        FTL_2_7_a,
        FTL_2_7_b,
        FTL_2_9_1_a,
        FTL_2_9_1_b,
        FTL_2_9_1_c,
        FTL_2_9_2_a,
        FTL_2_9_2_c,
        FULL,
        UNNTATT));

    public static final List<MedlemskapDekningType> DEKNINGSTYPE_ER_FRIVILLIG_MEDLEM = unmodifiableList(asList(
        FTL_2_7_a,
        FTL_2_7_b,
        FTL_2_9_1_a,
        FTL_2_9_1_c,
        FTL_2_9_2_a,
        FTL_2_9_2_c,
        FULL
    ));

    public static final List<MedlemskapDekningType> DEKNINGSTYPE_ER_MEDLEM_UNNTATT = unmodifiableList(singletonList(
        UNNTATT));

    public static final List<MedlemskapDekningType> DEKNINGSTYPE_ER_IKKE_MEDLEM = unmodifiableList(asList(
        FTL_2_6,
        FTL_2_9_1_b
    ));

    public static final List<MedlemskapDekningType> DEKNINGSTYPE_ER_UAVKLART = unmodifiableList(asList(
        IHT_AVTALE,
        OPPHOR));

    /* Legger inn udefinert kode.  Må gjerne erstattes av noe annet dersom starttilstand er kjent. */
    public static final MedlemskapDekningType UDEFINERT = new MedlemskapDekningType("-"); //$NON-NLS-1$

    MedlemskapDekningType() {
        // Hibernate trenger en
    }

    private MedlemskapDekningType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
