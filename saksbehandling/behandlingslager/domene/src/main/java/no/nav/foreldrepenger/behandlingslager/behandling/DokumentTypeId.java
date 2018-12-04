package no.nav.foreldrepenger.behandlingslager.behandling;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

/**
 * DokumentTypeId er et kodeverk som forvaltes av Kodeverkforvaltning. Det er et subsett av kodeverket DokumentType, mer spesifikt alle
 * inngående dokumenttyper.
 *
 * @see DokumentType
 */
@Entity(name = "DokumentTypeId")
@DiscriminatorValue(DokumentTypeId.DISCRIMINATOR)
public class DokumentTypeId extends Kodeliste {

    public static final String DISCRIMINATOR = "DOKUMENT_TYPE_ID";

    // FIXME SP: hva er koder for Sykepenger?
    public static final DokumentTypeId SØKNAD_FORELDREPENGER_FØDSEL = new DokumentTypeId("SØKNAD_FORELDREPENGER_FØDSEL"); //$NON-NLS-1$
    public static final DokumentTypeId FORELDREPENGER_ENDRING_SØKNAD = new DokumentTypeId("FORELDREPENGER_ENDRING_SØKNAD"); //$NON-NLS-1$

    // Støttedokumenter - Inntekt
    public static final DokumentTypeId INNTEKTSMELDING = new DokumentTypeId("INNTEKTSMELDING"); //$NON-NLS-1$
    public static final DokumentTypeId INNTEKTSOPPLYSNINGER = new DokumentTypeId("INNTEKTSOPPLYSNINGER"); //$NON-NLS-1$

    // FIXME SP: Hva er kode for sykemelding?  Det finnes mange ulike lege- og medisinske erklæring dokumenttype ider. Hvilke skal brukes?
    // Støttedokumenter - Sykdomsrelatert
    public static final DokumentTypeId LEGEERKLÆRING = new DokumentTypeId("LEGEERKLÆRING"); //$NON-NLS-1$

    // Klage
    public static final DokumentTypeId KLAGE_DOKUMENT = new DokumentTypeId("KLAGE_DOKUMENT"); //$NON-NLS-1$

    // Uspesifikke dokumenter
    public static final DokumentTypeId ANNET = new DokumentTypeId("ANNET"); //$NON-NLS-1$

    public static final DokumentTypeId UDEFINERT = new DokumentTypeId("-"); //$NON-NLS-1$

    private static final Set<DokumentTypeId> VEDLEGG_TYPER = Set.of(LEGEERKLÆRING);
    private static final Set<DokumentTypeId> SØKNAD_TYPER = Set.of(SØKNAD_FORELDREPENGER_FØDSEL);
    private static final Set<DokumentTypeId> ENDRING_SØKNAD_TYPER = Set.of(FORELDREPENGER_ENDRING_SØKNAD);
    private static final Set<DokumentTypeId> ANDRE_SPESIAL_TYPER = Set.of(INNTEKTSMELDING, KLAGE_DOKUMENT);

    DokumentTypeId() {
        // Hibernate trenger en
    }

    private DokumentTypeId(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static Set<DokumentTypeId> getVedleggTyper() {
        return VEDLEGG_TYPER;
    }

    public static Set<DokumentTypeId> getSpesialTyper() {
        Set<DokumentTypeId> typer = new LinkedHashSet<>(SØKNAD_TYPER);
        typer.addAll(ENDRING_SØKNAD_TYPER);
        typer.addAll(ANDRE_SPESIAL_TYPER);
        return Set.copyOf(typer);
    }

    public static Set<DokumentTypeId> getSøknadTyper() {
        return SØKNAD_TYPER;
    }

    public static Set<DokumentTypeId> getEndringSøknadTyper() {
        return ENDRING_SØKNAD_TYPER;
    }

    public boolean erSøknadType() {
        return SØKNAD_TYPER.contains(this);
    }
    
    
    public static DokumentKategori utledDokumentKategori(DokumentKategori dokumentKategori, DokumentTypeId dokumentTypeId) {
        if (DokumentTypeId.getSøknadTyper().contains(dokumentTypeId)) {
            return DokumentKategori.SØKNAD;
        }
        if (DokumentTypeId.KLAGE_DOKUMENT.equals(dokumentTypeId)) {
            return DokumentKategori.KLAGE_ELLER_ANKE;
        }
        return dokumentKategori;
    }
}
