package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface Kompletthetsjekker {
    KompletthetResultat vurderSøknadMottatt(Behandling behandling);

    KompletthetResultat vurderSøknadMottattForTidlig(Behandling behandling);

    KompletthetResultat vurderForsendelseKomplett(Behandling behandling);

    List<ManglendeVedlegg> utledAlleManglendeVedleggForForsendelse(Behandling behandling);

    List<ManglendeVedlegg> utledAlleManglendeVedleggSomIkkeKommer(Behandling behandling);

    boolean erForsendelsesgrunnlagKomplett(Behandling behandling);
}
