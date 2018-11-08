package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.InfotrygdVedtakTestSett.lagInfotr;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.InfotrygdVedtakTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.meldinger.FinnSakListeRequest;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.meldinger.FinnSakListeResponse;
import no.nav.vedtak.felles.integrasjon.infotrygdsak.InfotrygdSakConsumer;

@Dependent
@Alternative
@Priority(1)
class InfotrygdSakConsumerMock implements InfotrygdSakConsumer {

    private List<String> blackList = Arrays.asList(TpsRepo.STD_KVINNE_FNR, TpsRepo.STD_MANN_FNR,
        TpsRepo.KVINNE_MED_INNTEKT_40k_OG_LØPENDE_ARBEIDSFORHOLD_FNR,TpsRepo.KVINNE_MEDL_EØSBORGER_BOSATT_NOR_FNR,
        TpsRepo.KVINNE_MEDL_FEIL_PERSONSTATUS_FNR);

    private RegisterKontekst registerKontekst;

    @Inject
    public InfotrygdSakConsumerMock(RegisterKontekst registerKontekst) {
        this.registerKontekst = registerKontekst;
    }

    @Override
    public FinnSakListeResponse finnSakListe(FinnSakListeRequest request) {
        String ident = Optional.ofNullable(request).map(FinnSakListeRequest::getPersonident).orElse(null);
        if (ident == null) {
            return new FinnSakListeResponse();
        }

        if (registerKontekst.erInitalisert()) {
            return InfotrygdVedtakTestSett.finnResponse(ident);
        }
        if(blackList.contains(ident)) {
            return new FinnSakListeResponse();
        }
        return lagInfotr(0, true);
    }

}
