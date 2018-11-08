package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk;

import java.util.Arrays;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "TemaUnderkategori")
@DiscriminatorValue(TemaUnderkategori.DISCRIMINATOR)
public class TemaUnderkategori extends Kodeliste {
    public static final String DISCRIMINATOR = "TEMA_UNDERKATEGORI"; //$NON-NLS-1$

    public static final TemaUnderkategori UDEFINERT = new TemaUnderkategori("-"); //$NON-NLS-1$

    public static final TemaUnderkategori FORELDREPENGER_FODSEL = new TemaUnderkategori("FØ"); //$NON-NLS-1$
    public static final TemaUnderkategori FORELDREPENGER_ADOPSJON = new TemaUnderkategori("AP"); //$NON-NLS-1$
    public static final TemaUnderkategori FORELDREPENGER_SVANGERSKAPSPENGER = new TemaUnderkategori("SV"); //$NON-NLS-1$

    public static final TemaUnderkategori SYKEPENGER_SYKEPENGER = new TemaUnderkategori("SP"); //$NON-NLS-1$

    public static final TemaUnderkategori PÅRØRENDE_OMSORGSPENGER = new TemaUnderkategori("OM"); //$NON-NLS-1$
    public static final TemaUnderkategori PÅRØRENDE_OPPLÆRINGSPENGER = new TemaUnderkategori("OP"); //$NON-NLS-1$
    public static final TemaUnderkategori PÅRØRENDE_PLEIETRENGENDE_SYKT_BARN = new TemaUnderkategori("PB"); //$NON-NLS-1$
    public static final TemaUnderkategori PÅRØRENDE_PLEIETRENGENDE = new TemaUnderkategori("PI"); //$NON-NLS-1$
    public static final TemaUnderkategori PÅRØRENDE_PLEIETRENGENDE_PÅRØRENDE = new TemaUnderkategori("PP"); //$NON-NLS-1$
    public static final TemaUnderkategori PÅRØRENDE_PLEIEPENGER = new TemaUnderkategori("PN"); //$NON-NLS-1$


    public static final TemaUnderkategori SYKEPENGER_FORSIKRINGSRISIKO = new TemaUnderkategori("SU"); //$NON-NLS-1$
    public static final TemaUnderkategori SYKEPENGER_REISETILSKUDD = new TemaUnderkategori("RT"); //$NON-NLS-1$
    public static final TemaUnderkategori SYKEPENGER_UTENLANDSOPPHOLD = new TemaUnderkategori("RS"); //$NON-NLS-1$

    public static final TemaUnderkategori OVERGANGSSTØNAD = new TemaUnderkategori("OG"); //$NON-NLS-1$

    public static final TemaUnderkategori FORELDREPENGER_FODSEL_UTLAND = new TemaUnderkategori("FU"); //$NON-NLS-1$
    public static final TemaUnderkategori ENGANGSSTONAD_ADOPSJON = new TemaUnderkategori("AE"); //$NON-NLS-1$
    public static final TemaUnderkategori ENGANGSSTONAD_FODSEL = new TemaUnderkategori("FE"); //$NON-NLS-1$

    private static final List<TemaUnderkategori> FORELDREPENGER_BEHANDLINGSTEMAER = Arrays.asList(
        FORELDREPENGER_FODSEL, FORELDREPENGER_ADOPSJON, FORELDREPENGER_FODSEL_UTLAND);

    private static final List<TemaUnderkategori> ENGANGSSTONAD_BEHANDLINGSTEMAER = Arrays.asList(ENGANGSSTONAD_ADOPSJON,
        ENGANGSSTONAD_FODSEL);

    public TemaUnderkategori() {
        // Hibernate trenger den
    }

    private TemaUnderkategori(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static boolean erGjelderEngangsstonad(String underkategori) {
        return ENGANGSSTONAD_BEHANDLINGSTEMAER.stream().anyMatch(temaUnderkategori ->
            temaUnderkategori.getKode().equals(underkategori));
    }

    public static boolean erGjelderSvangerskapspenger(String underkategori) {
        return FORELDREPENGER_SVANGERSKAPSPENGER.getKode().equals(underkategori);
    }

    public static boolean erGjelderForeldrepenger(String underkategori) {
        return FORELDREPENGER_BEHANDLINGSTEMAER.stream().anyMatch(temaUnderkategori ->
            temaUnderkategori.getKode().equals(underkategori));
    }
}
