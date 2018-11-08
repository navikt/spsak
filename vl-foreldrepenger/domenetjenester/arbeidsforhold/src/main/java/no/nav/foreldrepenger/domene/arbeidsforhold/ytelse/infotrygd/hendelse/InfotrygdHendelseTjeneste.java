package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.hendelse;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface InfotrygdHendelseTjeneste {

    List<InfotrygdHendelse> hentHendelsesListFraInfotrygdFeed(Behandling behandling);

}
