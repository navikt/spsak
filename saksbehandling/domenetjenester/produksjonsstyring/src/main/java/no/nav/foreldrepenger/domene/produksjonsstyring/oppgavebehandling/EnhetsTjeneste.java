package no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.domene.typer.AktørId;

public interface EnhetsTjeneste {

    List<OrganisasjonsEnhet> hentEnhetListe(BehandlingTema behandlingTema);
    
    List<OrganisasjonsEnhet> hentEnhetListe();

    // Vil undersøke Kode6 for aktør og dennes relasjoner registrert i TPS (ikke foreldre)
    OrganisasjonsEnhet hentEnhetSjekkRegistrerteRelasjoner(AktørId aktørId, BehandlingTema behandlingTema);

    // Returnerer empty hvis ingen endring. Sjekker om oppgitt aktør og relaterte aktører utover TPS har Kode6
    Optional<OrganisasjonsEnhet> oppdaterEnhetSjekkOppgitte(String enhetId, BehandlingTema behandlingTema, List<AktørId> relaterteAktører);

    // Returnerer empty hvis ingen endring. Sjekker registrerte relasjoner i TPS for oppgitt aktør og eventuelt koblet sak
    Optional<OrganisasjonsEnhet> oppdaterEnhetSjekkRegistrerteRelasjoner(String enhetId, BehandlingTema behandlingTema, AktørId aktørId, Optional<AktørId> kobletAktørId, List<AktørId> relaterteAktører);

    OrganisasjonsEnhet enhetsPresedens(OrganisasjonsEnhet enhetSak1, OrganisasjonsEnhet enhetSak2, boolean arverKlage);
}
