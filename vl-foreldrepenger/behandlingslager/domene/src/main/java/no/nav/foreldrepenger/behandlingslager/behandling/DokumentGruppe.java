package no.nav.foreldrepenger.behandlingslager.behandling;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;


@Entity(name = "DokumentGruppe")
@DiscriminatorValue(DokumentGruppe.DISCRIMINATOR)
public class DokumentGruppe extends Kodeliste {

    public static final String DISCRIMINATOR = "DOKUMENT_GRUPPE";

    public static final DokumentGruppe SØKNAD = new DokumentGruppe("SØKNAD"); //$NON-NLS-1$
    public static final DokumentGruppe INNTEKTSMELDING = new DokumentGruppe("INNTEKTSMELDING"); //$NON-NLS-1$
    public static final DokumentGruppe ENDRINGSSØKNAD = new DokumentGruppe("ENDRINGSSØKNAD"); //$NON-NLS-1$
    public static final DokumentGruppe KLAGE = new DokumentGruppe("KLAGE"); //$NON-NLS-1$
    public static final DokumentGruppe VEDLEGG = new DokumentGruppe("VEDLEGG"); //$NON-NLS-1$

    public static final DokumentGruppe UDEFINERT = new DokumentGruppe("-"); //$NON-NLS-1$

    DokumentGruppe() {
        // Hibernate trenger en
    }

    DokumentGruppe(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
