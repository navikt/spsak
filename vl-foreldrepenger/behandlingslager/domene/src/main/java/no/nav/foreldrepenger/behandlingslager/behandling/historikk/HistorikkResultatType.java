package no.nav.foreldrepenger.behandlingslager.behandling.historikk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "HistorikkResultatType")
@DiscriminatorValue(HistorikkResultatType.DISCRIMINATOR)
public class HistorikkResultatType extends Kodeliste {
    public static final String DISCRIMINATOR = "HISTORIKK_RESULTAT_TYPE"; //$NON-NLS-1$

    public static final HistorikkResultatType UDEFINIERT = new HistorikkResultatType("-");

    public static final HistorikkResultatType AVVIS_KLAGE = new HistorikkResultatType("AVVIS_KLAGE");
    public static final HistorikkResultatType MEDHOLD_I_KLAGE = new HistorikkResultatType("MEDHOLD_I_KLAGE");
    public static final HistorikkResultatType OPPHEVE_VEDTAK = new HistorikkResultatType("OPPHEVE_VEDTAK");
    public static final HistorikkResultatType OPPRETTHOLDT_VEDTAK = new HistorikkResultatType("OPPRETTHOLDT_VEDTAK");
    public static final HistorikkResultatType STADFESTET_VEDTAK = new HistorikkResultatType("STADFESTET_VEDTAK");
    public static final HistorikkResultatType BEREGNET_AARSINNTEKT = new HistorikkResultatType("BEREGNET_AARSINNTEKT");
    public static final HistorikkResultatType UTFALL_UENDRET = new HistorikkResultatType("UTFALL_UENDRET");

    public HistorikkResultatType() {
        //
    }

    private HistorikkResultatType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
