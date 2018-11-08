package no.nav.foreldrepenger.domene.familiehendelse.omsorg;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.domene.personopplysning.AvklarForeldreansvarAksjonspunktData;
import no.nav.foreldrepenger.domene.personopplysning.AvklarOmsorgOgForeldreansvarAksjonspunktData;

public interface OmsorghendelseTjeneste {

    void aksjonspunktAvklarOmsorgOgForeldreansvar(Behandling behandling, AvklarOmsorgOgForeldreansvarAksjonspunktData adapter);

    void aksjonspunktAvklarForeldreansvar(Behandling behandling, AvklarForeldreansvarAksjonspunktData adapter);

    void aksjonspunktOmsorgsvilkår(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon);

}
