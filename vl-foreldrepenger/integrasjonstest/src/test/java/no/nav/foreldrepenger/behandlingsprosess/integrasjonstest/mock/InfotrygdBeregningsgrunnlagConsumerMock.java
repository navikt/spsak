package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.InfotrygdVedtakTestSett.lagInfotrBG;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.InfotrygdVedtakTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.meldinger.FinnGrunnlagListeRequest;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.meldinger.FinnGrunnlagListeResponse;
import no.nav.vedtak.felles.integrasjon.infotrygdberegningsgrunnlag.InfotrygdBeregningsgrunnlagConsumer;

@Dependent
@Alternative
@Priority(1)
class InfotrygdBeregningsgrunnlagConsumerMock implements InfotrygdBeregningsgrunnlagConsumer {

    private List<String> blackList = Arrays.asList(TpsRepo.STD_KVINNE_FNR, TpsRepo.STD_MANN_FNR,
        TpsRepo.KVINNE_MED_INNTEKT_40k_OG_LØPENDE_ARBEIDSFORHOLD_FNR, TpsRepo.KVINNE_MEDL_EØSBORGER_BOSATT_NOR_FNR,
        TpsRepo.KVINNE_MEDL_FEIL_PERSONSTATUS_FNR);

    private RegisterKontekst registerKontekst;

    @Inject
    public InfotrygdBeregningsgrunnlagConsumerMock(RegisterKontekst registerKontekst) {
        this.registerKontekst = registerKontekst;
    }

    @Override
    public FinnGrunnlagListeResponse finnBeregningsgrunnlagListe(FinnGrunnlagListeRequest finnGrunnlagListeRequest) {

        String ident = Optional.ofNullable(finnGrunnlagListeRequest).map(FinnGrunnlagListeRequest::getPersonident).orElse(null);
        if (ident == null) {
            return new FinnGrunnlagListeResponse();
        }

        if (registerKontekst.erInitalisert()) {
            return InfotrygdVedtakTestSett.finnIBGResponse(ident);
        }
        if (blackList.contains(ident)) {
            return new FinnGrunnlagListeResponse();
        }
        return lagInfotrBG(0, true);
    }
}
