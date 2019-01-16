package no.nav.foreldrepenger.behandlingskontroll;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;

public interface BehandlingModellRepository extends BehandlingslagerRepository {

    BehandlingModell getModell(BehandlingType behandlingType, FagsakYtelseType fagsakYtelseType);

    BehandlingStegKonfigurasjon getBehandlingStegKonfigurasjon();

    KodeverkRepository getKodeverkRepository();


}
