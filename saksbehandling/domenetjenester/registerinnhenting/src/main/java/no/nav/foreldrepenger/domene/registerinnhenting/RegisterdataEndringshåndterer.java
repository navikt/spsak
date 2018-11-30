package no.nav.foreldrepenger.domene.registerinnhenting;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;

public interface RegisterdataEndringshåndterer {

    boolean skalInnhenteRegisteropplysningerPåNytt(Behandling behandling);

    void oppdaterRegisteropplysningerOgRestartBehandlingVedEndringer(Behandling behandling);

    /** Innhent nye registeropplysninger. Spol til {@link StartpunktType} dersom endringer*/
    EndringsresultatDiff oppdaterRegisteropplysninger(Behandling behandling, EndringsresultatSnapshot grunnlagSnapshot);

    /** Utfør {@link #oppdaterRegisteropplysningerOgRestartBehandlingVedEndringer(Behandling)} asynkront, inklusiv start prosesser behandling .
     * @param innhentRegisteropplysninger TODO
     * @param manuellGjenopptakelse om registerinnhenting skjer ifm at bruker fortsetter behandlingen manuelt  */
    ProsessTaskGruppe opprettProsessTaskOppdaterRegisteropplysningerOgRestartBehandlingVedEndringer(Behandling behandling, boolean innhentRegisteropplysninger, boolean manuellGjenopptakelse);
}
