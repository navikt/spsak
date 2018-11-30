package no.nav.foreldrepenger.domene.personopplysning;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;

public interface PersonopplysningTjeneste extends StandardPersonopplysningTjeneste {

    void aksjonspunktVergeOppdaterer(Behandling behandling, VergeAksjonpunktDto adapter);

    void aksjonspunktAvklarSaksopplysninger(Behandling behandling, PersonopplysningAksjonspunktDto adapter);

    EndringsresultatSnapshot finnAktivGrunnlagId(Behandling behandling);

    DiffResult diffResultat(EndringsresultatDiff grunnlagIdDiff, FagsakYtelseType ytelseType, boolean kunSporedeEndringer);

}
