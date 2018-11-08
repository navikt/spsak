package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.RegisterKontekst;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.TpsTestSett;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.AktoerIder;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;

@Alternative
@Priority(1)
public class AktørConsumerMock implements AktørConsumer {

    private static final TpsRepo TPS_REPO = TpsRepo.init();

    private RegisterKontekst registerKontekst;

    @Inject
    public AktørConsumerMock(RegisterKontekst registerKontekst) {
        this.registerKontekst = registerKontekst;
    }

    @Override
    public Optional<String > hentAktørIdForPersonIdent(String personIdent) {
        if (registerKontekst.erInitalisert()) {
            return Optional.ofNullable(TpsTestSett.finnAktoerId(personIdent).getId());
        }
        return Optional.ofNullable(TPS_REPO.finnAktoerId(personIdent).getId());
    }

    @Override
    public Optional<String> hentPersonIdentForAktørId(String aktørId) {
        if (registerKontekst.erInitalisert()) {
            return Optional.ofNullable(TpsTestSett.finnIdent(new AktørId(aktørId)));
        }
        return Optional.ofNullable(TPS_REPO.finnIdent(new AktørId(aktørId)));
    }

    @Override
    public List<AktoerIder> hentAktørIdForPersonIdentSet(Set<String> set) {
        return set.stream()
            .map(fnr -> {
                if (registerKontekst.erInitalisert()) {
                    return TpsTestSett.finnAktoerId(fnr);
                }
                return TPS_REPO.finnAktoerId(fnr);
            })
            .filter(Objects::nonNull)
            .map(n -> new AktoerIder(){{setAktoerId(n.toString());}})
            .collect(Collectors.toList());
    }
}
