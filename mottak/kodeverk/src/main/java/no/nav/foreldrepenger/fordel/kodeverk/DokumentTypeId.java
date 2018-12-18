package no.nav.foreldrepenger.fordel.kodeverk;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


/**
 * DokumentTypeId er et kodeverk som forvaltes av Kodeverkforvaltning. Det er et subsett av kodeverket DokumentType,  mer spesifikt alle inngående dokumenttyper.
 */
@Entity(name = "DokumentTypeId")
@DiscriminatorValue(DokumentTypeId.DISCRIMINATOR)
public class DokumentTypeId extends Kodeliste {

    public static final String DISCRIMINATOR = "DOKUMENT_TYPE_ID";

    // Engangsstønad
    public static final DokumentTypeId SØKNAD_ENGANGSSTØNAD_FØDSEL = new DokumentTypeId("SØKNAD_ENGANGSSTØNAD_FØDSEL"); //$NON-NLS-1$
    public static final DokumentTypeId SØKNAD_ENGANGSSTØNAD_ADOPSJON = new DokumentTypeId("SØKNAD_ENGANGSSTØNAD_ADOPSJON"); //$NON-NLS-1$
    public static final DokumentTypeId ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL = new DokumentTypeId("ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL"); //$NON-NLS-1$
    public static final DokumentTypeId ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON = new DokumentTypeId("ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON"); //$NON-NLS-1$

    // Foreldrepenger
    public static final DokumentTypeId SØKNAD_FORELDREPENGER_FØDSEL = new DokumentTypeId("SØKNAD_FORELDREPENGER_FØDSEL"); //$NON-NLS-1$
    public static final DokumentTypeId SØKNAD_FORELDREPENGER_ADOPSJON = new DokumentTypeId("SØKNAD_FORELDREPENGER_ADOPSJON"); //$NON-NLS-1$
    public static final DokumentTypeId FORELDREPENGER_ENDRING_SØKNAD = new DokumentTypeId("FORELDREPENGER_ENDRING_SØKNAD"); //$NON-NLS-1$
    public static final DokumentTypeId FLEKSIBELT_UTTAK_FORELDREPENGER = new DokumentTypeId("FLEKSIBELT_UTTAK_FORELDREPENGER"); //$NON-NLS-1$
    public static final DokumentTypeId ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON = new DokumentTypeId("ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON"); //$NON-NLS-1$
    public static final DokumentTypeId ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL = new DokumentTypeId("ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL"); //$NON-NLS-1$
    public static final DokumentTypeId ETTERSENDT_FLEKSIBELT_UTTAK_FORELDREPENGER = new DokumentTypeId("ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL"); //$NON-NLS-1$
    public static final DokumentTypeId ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD = new DokumentTypeId("ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD"); //$NON-NLS-1$

    // Støttedokumenter FP
    public static final DokumentTypeId INNTEKTSMELDING = new DokumentTypeId("INNTEKTSMELDING"); //$NON-NLS-1$
    public static final DokumentTypeId INNTEKTSOPPLYSNINGER = new DokumentTypeId("INNTEKTSOPPLYSNINGER"); //$NON-NLS-1$

    // Klage
    public static final DokumentTypeId KLAGE_DOKUMENT = new DokumentTypeId("KLAGE_DOKUMENT"); //$NON-NLS-1$

    // Uspesifikke dokumenter
    public static final DokumentTypeId DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL = new DokumentTypeId("DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL"); //$NON-NLS-1$
    public static final DokumentTypeId DOKUMENTASJON_AV_OMSORGSOVERTAKELSE = new DokumentTypeId("DOKUMENTASJON_AV_OMSORGSOVERTAKELSE"); //$NON-NLS-1$
    public static final DokumentTypeId ANNET = new DokumentTypeId("ANNET"); //$NON-NLS-1$

    public static final DokumentTypeId UDEFINERT = new DokumentTypeId("-"); //$NON-NLS-1$


    DokumentTypeId() {
        // Hibernate trenger en
    }

    private static final Set<DokumentTypeId> ENGANGSSTØNAD_TYPER = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            SØKNAD_ENGANGSSTØNAD_FØDSEL, SØKNAD_ENGANGSSTØNAD_ADOPSJON, ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL, ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON)));
    private static final Set<DokumentTypeId> FORELDREPENGER_TYPER = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            SØKNAD_FORELDREPENGER_FØDSEL, SØKNAD_FORELDREPENGER_ADOPSJON, FORELDREPENGER_ENDRING_SØKNAD, FLEKSIBELT_UTTAK_FORELDREPENGER,
            ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON, ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL, ETTERSENDT_FLEKSIBELT_UTTAK_FORELDREPENGER,
            ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD, INNTEKTSOPPLYSNINGER, INNTEKTSMELDING)));

    private DokumentTypeId(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public boolean erForeldrepengerRelatert() {
        return FORELDREPENGER_TYPER.contains(this);
    }

    public boolean erEngangsstønadRelatert() {
        return ENGANGSSTØNAD_TYPER.contains(this);
    }
}
