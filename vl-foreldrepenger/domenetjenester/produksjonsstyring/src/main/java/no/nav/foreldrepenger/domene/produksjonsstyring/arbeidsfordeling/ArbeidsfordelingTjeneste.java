package no.nav.foreldrepenger.domene.produksjonsstyring.arbeidsfordeling;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;

public interface ArbeidsfordelingTjeneste {

    OrganisasjonsEnhet finnBehandlendeEnhet(String geografiskTilknytning, String diskresjonskode, BehandlingTema behandlingTema);

    List<OrganisasjonsEnhet> finnAlleBehandlendeEnhetListe(BehandlingTema behandlingTema);

    OrganisasjonsEnhet hentEnhetForDiskresjonskode(String kode, BehandlingTema behandlingTema);
}
