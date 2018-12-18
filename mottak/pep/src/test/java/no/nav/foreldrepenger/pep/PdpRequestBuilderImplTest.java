package no.nav.foreldrepenger.pep;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import no.nav.foreldrepenger.pip.PipRepository;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;

public class PdpRequestBuilderImplTest {
    private static final String DUMMY_ID_TOKEN = "dummyheader.dymmypayload.dummysignaturee";
    private static final String AKTØR = "AktørID_1";
    private static final UUID DOKUMENTFORSENDELSE = UUID.randomUUID();


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PipRepository pipRepository = Mockito.mock(PipRepository.class);
    //private AktørConsumerMedCache aktørConsumer = Mockito.mock(AktørConsumerMedCache.class);

    private PdpRequestBuilderImpl requestBuilder = new PdpRequestBuilderImpl(pipRepository);

    @Test
    public void skal_legge_aktør_id_og_ikke_fnr_på_request() throws Exception {
        AbacAttributtSamling attributter = byggAbacAttributtSamling();
        attributter.leggTil(AbacDataAttributter.opprett().leggTilAktørId(AKTØR));

        PdpRequest request = requestBuilder.lagPdpRequest(attributter);
        assertThat(request.getAktørId()).containsOnly(AKTØR);
        assertThat(request.getFnr()).isNullOrEmpty();
    }

    @Test
    public void skal_hente_aktør_id_gitt_forsendelse_id_som_input() {
        AbacAttributtSamling attributter = byggAbacAttributtSamling();
        attributter.leggTil(AbacDataAttributter.opprett().leggTilDokumentforsendelseId(DOKUMENTFORSENDELSE));

        when(pipRepository.hentAktørIdForForsendelser(Collections.singleton(DOKUMENTFORSENDELSE))).thenReturn(Collections.singleton(AKTØR));

        PdpRequest request = requestBuilder.lagPdpRequest(attributter);
        assertThat(request.getAktørId()).containsOnly(AKTØR);
    }

    private AbacAttributtSamling byggAbacAttributtSamling() {
        AbacAttributtSamling attributtSamling = AbacAttributtSamling.medJwtToken(DUMMY_ID_TOKEN);
        attributtSamling.setActionType(BeskyttetRessursActionAttributt.READ);
        attributtSamling.setResource(BeskyttetRessursResourceAttributt.FAGSAK);
        return attributtSamling;
    }
}