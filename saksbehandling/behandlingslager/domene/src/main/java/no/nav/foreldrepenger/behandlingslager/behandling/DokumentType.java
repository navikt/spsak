package no.nav.foreldrepenger.behandlingslager.behandling;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

/**
 * DokumentType er et kodeverk som forvaltes av dokkat. Et subsett av kodeverket, mer spesifikt alle inngående
 * dokumenttyper, er forvaltet av Kodeverkforvaltning og er eksponert som DokumentTypeId.
 *
 * Benytt HentDokumentType for å hente opp dette kodeverket.
 *
 * @see DokumentTypeId
 * @see HentDokumentType
 */
@Entity(name = "DokumentType")
@DiscriminatorValue(DokumentType.DISCRIMINATOR)
public class DokumentType extends Kodeliste {

    public static final String DISCRIMINATOR = "DOKUMENT_TYPE";

    public static final DokumentType UDEFINERT = new DokumentType("-"); //$NON-NLS-1$

    DokumentType() {
        // Hibernate trenger en
    }

    private DokumentType(String kode) {
        this(kode, DISCRIMINATOR);
    }

    protected DokumentType(String kode, String discriminator) {
        super(kode, discriminator);
    }
}
