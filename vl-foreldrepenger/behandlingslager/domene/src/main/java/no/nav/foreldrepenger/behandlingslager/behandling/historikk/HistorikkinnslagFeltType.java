package no.nav.foreldrepenger.behandlingslager.behandling.historikk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "HistorikkinnslagFeltType")
@DiscriminatorValue(HistorikkinnslagFeltType.DISCRIMINATOR)
public class HistorikkinnslagFeltType extends Kodeliste {
    public static final String DISCRIMINATOR = "HISTORIKKINNSLAG_FELT_TYPE"; //$NON-NLS-1$

    public static final HistorikkinnslagFeltType UDEFINIERT = new HistorikkinnslagFeltType("-");

    public static final HistorikkinnslagFeltType AARSAK = new HistorikkinnslagFeltType("AARSAK");
    public static final HistorikkinnslagFeltType BEGRUNNELSE = new HistorikkinnslagFeltType("BEGRUNNELSE");
    public static final HistorikkinnslagFeltType HENDELSE = new HistorikkinnslagFeltType("HENDELSE");
    public static final HistorikkinnslagFeltType RESULTAT = new HistorikkinnslagFeltType("RESULTAT");
    public static final HistorikkinnslagFeltType OPPLYSNINGER = new HistorikkinnslagFeltType("OPPLYSNINGER");
    public static final HistorikkinnslagFeltType ENDRET_FELT = new HistorikkinnslagFeltType("ENDRET_FELT");
    public static final HistorikkinnslagFeltType SKJERMLENKE = new HistorikkinnslagFeltType("SKJERMLENKE");
    public static final HistorikkinnslagFeltType GJELDENDE_FRA = new HistorikkinnslagFeltType("GJELDENDE_FRA");
    public static final HistorikkinnslagFeltType AKSJONSPUNKT_BEGRUNNELSE = new HistorikkinnslagFeltType("AKSJONSPUNKT_BEGRUNNELSE");
    public static final HistorikkinnslagFeltType AKSJONSPUNKT_GODKJENT = new HistorikkinnslagFeltType("AKSJONSPUNKT_GODKJENT");
    public static final HistorikkinnslagFeltType AKSJONSPUNKT_KODE = new HistorikkinnslagFeltType("AKSJONSPUNKT_KODE");
    public static final HistorikkinnslagFeltType AVKLART_SOEKNADSPERIODE = new HistorikkinnslagFeltType("AVKLART_SOEKNADSPERIODE");
    public static final HistorikkinnslagFeltType ANGÅR_TEMA = new HistorikkinnslagFeltType("ANGÅR_TEMA");


    public HistorikkinnslagFeltType() {
        //
    }

    private HistorikkinnslagFeltType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
