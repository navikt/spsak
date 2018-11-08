package no.nav.foreldrepenger.behandlingslager.behandling.oppgave;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "OppgaveÅrsak")
@DiscriminatorValue(OppgaveÅrsak.DISCRIMINATOR)
public class OppgaveÅrsak extends Kodeliste {
    public static final String DISCRIMINATOR = "OPPGAVE_AARSAK";

    public static final OppgaveÅrsak BEHANDLE_SAK = new OppgaveÅrsak("BEH_SAK_VL"); //$NON-NLS-1$
    public static final OppgaveÅrsak BEHANDLE_SAK_INFOTRYGD = new OppgaveÅrsak("BEH_SAK_FOR"); //$NON-NLS-1$
    public static final OppgaveÅrsak SETT_ARENA_UTBET_VENT = new OppgaveÅrsak("SETTVENT_STO"); //$NON-NLS-1$
    public static final OppgaveÅrsak REGISTRER_SØKNAD = new OppgaveÅrsak("REG_SOK_VL"); //$NON-NLS-1$
    public static final OppgaveÅrsak GODKJENNE_VEDTAK = new OppgaveÅrsak("GOD_VED_VL"); //$NON-NLS-1$
    public static final OppgaveÅrsak REVURDER = new OppgaveÅrsak("RV_VL"); //$NON-NLS-1$
    public static final OppgaveÅrsak VURDER_DOKUMENT = new OppgaveÅrsak("VUR_VL"); //$NON-NLS-1$
    public static final OppgaveÅrsak VURDER_KONS_FOR_YTELSE = new OppgaveÅrsak("VUR_KONS_YTE_FOR"); //$NON-NLS-1$

    public static final OppgaveÅrsak UDEFINERT = new OppgaveÅrsak("-"); //$NON-NLS-1$

    OppgaveÅrsak() {
        // Hibernate trenger en
    }

    private OppgaveÅrsak(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
