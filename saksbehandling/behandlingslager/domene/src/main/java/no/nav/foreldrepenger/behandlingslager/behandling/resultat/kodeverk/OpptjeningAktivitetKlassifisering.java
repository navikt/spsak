package no.nav.foreldrepenger.behandlingslager.behandling.resultat.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "OpptjeningAktivitetKlassifisering")
@DiscriminatorValue(OpptjeningAktivitetKlassifisering.DISCRIMINATOR)
public class OpptjeningAktivitetKlassifisering extends Kodeliste {

    public static final String DISCRIMINATOR = "OPPTJENING_AKTIVITET_KLASSIFISERING";

    public static final OpptjeningAktivitetKlassifisering BEKREFTET_GODKJENT      = new OpptjeningAktivitetKlassifisering("BEKREFTET_GODKJENT"); //$NON-NLS-1$   Adopsjon
    public static final OpptjeningAktivitetKlassifisering BEKREFTET_AVVIST     = new OpptjeningAktivitetKlassifisering("BEKREFTET_AVVIST"); //$NON-NLS-1$  Omsorgoverdragelse
    public static final OpptjeningAktivitetKlassifisering ANTATT_GODKJENT = new OpptjeningAktivitetKlassifisering("ANTATT_GODKJENT"); //$NON-NLS-1$  fodsel
    public static final OpptjeningAktivitetKlassifisering MELLOMLIGGENDE_PERIODE = new OpptjeningAktivitetKlassifisering("MELLOMLIGGENDE_PERIODE"); //$NON-NLS-1$
    public static final OpptjeningAktivitetKlassifisering UDEFINERT = new OpptjeningAktivitetKlassifisering("-"); //$NON-NLS-1$

    public OpptjeningAktivitetKlassifisering() {
    }

    public OpptjeningAktivitetKlassifisering(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
