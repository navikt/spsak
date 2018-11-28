package no.nav.foreldrepenger.behandlingslager.behandling.historikk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "HistorikkinnslagFeltType")
@DiscriminatorValue(HistorikkinnslagFeltType.DISCRIMINATOR)
public class HistorikkinnslagFeltType extends Kodeliste {
    static final String DISCRIMINATOR = "HISTORIKKINNSLAG_FELT_TYPE"; //$NON-NLS-1$

    public static final HistorikkinnslagFeltType UDEFINERT = new HistorikkinnslagFeltType("-");

    static final HistorikkinnslagFeltType AARSAK = new HistorikkinnslagFeltType("AARSAK");
    public static final HistorikkinnslagFeltType BEGRUNNELSE = new HistorikkinnslagFeltType("BEGRUNNELSE");
    static final HistorikkinnslagFeltType HENDELSE = new HistorikkinnslagFeltType("HENDELSE");
    static final HistorikkinnslagFeltType RESULTAT = new HistorikkinnslagFeltType("RESULTAT");
    static final HistorikkinnslagFeltType OPPLYSNINGER = new HistorikkinnslagFeltType("OPPLYSNINGER");
    static final HistorikkinnslagFeltType ENDRET_FELT = new HistorikkinnslagFeltType("ENDRET_FELT");
    static final HistorikkinnslagFeltType SKJERMLENKE = new HistorikkinnslagFeltType("SKJERMLENKE");
    static final HistorikkinnslagFeltType GJELDENDE_FRA = new HistorikkinnslagFeltType("GJELDENDE_FRA");
    static final HistorikkinnslagFeltType AKSJONSPUNKT_BEGRUNNELSE = new HistorikkinnslagFeltType("AKSJONSPUNKT_BEGRUNNELSE");
    static final HistorikkinnslagFeltType AKSJONSPUNKT_GODKJENT = new HistorikkinnslagFeltType("AKSJONSPUNKT_GODKJENT");
    static final HistorikkinnslagFeltType AKSJONSPUNKT_KODE = new HistorikkinnslagFeltType("AKSJONSPUNKT_KODE");
    static final HistorikkinnslagFeltType AVKLART_SOEKNADSPERIODE = new HistorikkinnslagFeltType("AVKLART_SOEKNADSPERIODE");
    static final HistorikkinnslagFeltType ANGÅR_TEMA = new HistorikkinnslagFeltType("ANGÅR_TEMA");

    public HistorikkinnslagFeltType() {
        //
    }

    private HistorikkinnslagFeltType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
