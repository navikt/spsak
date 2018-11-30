package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;

interface StartpunktUtleder {
    StartpunktType utledStartpunkt(Behandling behandling, Long grunnlagId1, Long grunnlagId2);
}
