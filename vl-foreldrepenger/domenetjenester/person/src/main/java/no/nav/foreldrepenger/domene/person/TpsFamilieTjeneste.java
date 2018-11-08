package no.nav.foreldrepenger.domene.person;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.aktør.FødtBarnInfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;

public interface TpsFamilieTjeneste {
    List<FødtBarnInfo> getFødslerRelatertTilBehandling(Behandling behandling, FamilieHendelseGrunnlag familieHendelseGrunnlag);
}
