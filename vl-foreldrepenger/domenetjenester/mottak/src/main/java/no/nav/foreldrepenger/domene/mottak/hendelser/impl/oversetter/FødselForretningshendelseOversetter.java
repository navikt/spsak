package no.nav.foreldrepenger.domene.mottak.hendelser.impl.oversetter;

import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.domene.familiehendelse.fødsel.FødselForretningshendelse;
import no.nav.foreldrepenger.domene.mottak.ForretningshendelsestypeRef;
import no.nav.foreldrepenger.domene.mottak.hendelser.ForretningshendelseOversetter;
import no.nav.foreldrepenger.domene.mottak.hendelser.kontrakt.ForretningshendelseDto;
import no.nav.foreldrepenger.domene.mottak.hendelser.kontrakt.FødselHendelse;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.integrasjon.rest.JsonMapper;

@ApplicationScoped
@ForretningshendelsestypeRef("FØDSEL")
public class FødselForretningshendelseOversetter implements ForretningshendelseOversetter<FødselForretningshendelse> {

    @Override
    public FødselForretningshendelse oversett(ForretningshendelseDto forretningshendelse) {
        FødselHendelse fødselHendelse = JsonMapper.fromJson(forretningshendelse.getPayloadJson(), FødselHendelse.class);
        return new FødselForretningshendelse(fødselHendelse.getAktørIdListe().stream().map(AktørId::new).collect(Collectors.toList()), fødselHendelse.getFødselsdato());
    }
}
