package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "RelatertYtelseStatus")
@DiscriminatorValue(RelatertYtelseStatus.DISCRIMINATOR)
public class RelatertYtelseStatus extends Kodeliste {

    public static final String DISCRIMINATOR = "RELATERT_YTELSE_STATUS"; //$NON-NLS-1$
    //Statuser fra Arena
    public static final RelatertYtelseStatus AVSLU = new RelatertYtelseStatus("AVSLU"); //$NON-NLS-1$
    public static final RelatertYtelseStatus GODKJ = new RelatertYtelseStatus("GODKJ"); //$NON-NLS-1$
    public static final RelatertYtelseStatus INNST = new RelatertYtelseStatus("INNST"); //$NON-NLS-1$
    public static final RelatertYtelseStatus IVERK = new RelatertYtelseStatus("IVERK"); //$NON-NLS-1$
    public static final RelatertYtelseStatus MOTAT = new RelatertYtelseStatus("MOTAT"); //$NON-NLS-1$
    public static final RelatertYtelseStatus OPPRE = new RelatertYtelseStatus("OPPRE"); //$NON-NLS-1$
    public static final RelatertYtelseStatus REGIS = new RelatertYtelseStatus("REGIS"); //$NON-NLS-1$

    //Statuser far Infotrygd
    public static final RelatertYtelseStatus IKKE_PÅBEGYNT = new RelatertYtelseStatus("IP"); //$NON-NLS-1$
    public static final RelatertYtelseStatus UNDER_BEHANDLING = new RelatertYtelseStatus("UB"); //$NON-NLS-1$
    public static final RelatertYtelseStatus SENDT_TIL_SAKSBEHANDLER = new RelatertYtelseStatus("SG"); //$NON-NLS-1$
    public static final RelatertYtelseStatus UNDERKJENT_AV_SAKSBEHANDLER = new RelatertYtelseStatus("UK"); //$NON-NLS-1$
    public static final RelatertYtelseStatus RETUNERT = new RelatertYtelseStatus("RT"); //$NON-NLS-1$
    public static final RelatertYtelseStatus SENDT = new RelatertYtelseStatus("ST"); //$NON-NLS-1$
    public static final RelatertYtelseStatus VIDERESENDT_DIREKTORATET = new RelatertYtelseStatus("VD"); //$NON-NLS-1$
    public static final RelatertYtelseStatus VENTER_IVERKSETTING = new RelatertYtelseStatus("VI"); //$NON-NLS-1$
    public static final RelatertYtelseStatus VIDERESENDT_TRYGDERETTEN = new RelatertYtelseStatus("VT"); //$NON-NLS-1$

    public static final RelatertYtelseStatus LØPENDE_VEDTAK = new RelatertYtelseStatus("L"); //$NON-NLS-1$
    public static final RelatertYtelseStatus IKKE_STARTET = new RelatertYtelseStatus("I"); //$NON-NLS-1$
    public static final RelatertYtelseStatus AVSLUTTET_IT = new RelatertYtelseStatus("A"); //$NON-NLS-1$

    private static final List<RelatertYtelseStatus> ÅPEN_SAK_STATUSER = Arrays.asList(IKKE_PÅBEGYNT,
        UNDER_BEHANDLING, SENDT_TIL_SAKSBEHANDLER, UNDERKJENT_AV_SAKSBEHANDLER, RETUNERT, SENDT, VIDERESENDT_DIREKTORATET,
        VENTER_IVERKSETTING, VIDERESENDT_TRYGDERETTEN);

    private static final Set<String> LØPENDE_VEDTAK_STATUS = Collections.singleton(LØPENDE_VEDTAK.getKode());
    private static final Set<String> ER_IKKE_STARTET_STATUS = Collections.singleton(IKKE_STARTET.getKode());

    RelatertYtelseStatus() {
        // Hibernate trenger den
    }

    private RelatertYtelseStatus(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static boolean erÅpenSakStatus(String statusKode) {
        return ÅPEN_SAK_STATUSER.stream().anyMatch(relatertYtelseStatus -> relatertYtelseStatus.getKode().equals(statusKode));
    }

    public static boolean erLøpendeVedtak(String status) {
        return LØPENDE_VEDTAK_STATUS.contains(status);
    }

    public static boolean erIkkeStartetStatus(String statusString) {
        return ER_IKKE_STARTET_STATUS.contains(statusString);
    }
}
