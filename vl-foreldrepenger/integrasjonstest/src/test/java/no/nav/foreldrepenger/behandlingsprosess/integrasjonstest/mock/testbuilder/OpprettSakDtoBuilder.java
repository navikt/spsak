package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder;

import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.kontrakter.fordel.OpprettSakDto;

public class OpprettSakDtoBuilder {

    private final KodeverkRepository kodeverkRepository;

    private String journalpostId = "123";
    private String behandlingstemaOffisiellKode;
    private String aktørId;

    private OpprettSakDtoBuilder(BehandlingRepositoryProvider provider) {
        kodeverkRepository = provider.getKodeverkRepository();
    }

    public static OpprettSakDtoBuilder builder(BehandlingRepositoryProvider provider) {
        return new OpprettSakDtoBuilder(provider);
    }

    public OpprettSakDtoBuilder medBehandlingstema(BehandlingTema tema) {
        BehandlingTema behandlingTema = kodeverkRepository.finn(BehandlingTema.class, tema.getKode());
        Objects.requireNonNull(behandlingTema);
        this.behandlingstemaOffisiellKode = behandlingTema.getOffisiellKode();
        return this;
    }

    public OpprettSakDtoBuilder medAktørId(AktørId aktørId) {
        this.aktørId = aktørId.getId();
        return this;
    }

    public OpprettSakDto build() {
        return new OpprettSakDto(journalpostId, behandlingstemaOffisiellKode, aktørId);
    }

}
