package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag;

import java.time.LocalDate;
import java.time.Month;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.sak.impl.InfotrygdTjenesteFeil;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.binding.FinnGrunnlagListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.binding.FinnGrunnlagListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.binding.FinnGrunnlagListeUgyldigInput;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.meldinger.FinnGrunnlagListeRequest;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.meldinger.FinnGrunnlagListeResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.infotrygdberegningsgrunnlag.InfotrygdBeregningsgrunnlagConsumer;


@ApplicationScoped
public class InfotrygdBeregningsgrunnlagTjenesteImpl implements  InfotrygdBeregningsgrunnlagTjeneste{
    private static final String TJENESTE = "InfotrygdBeregningsgrunnlag";
    private static final Logger log = LoggerFactory.getLogger(InfotrygdBeregningsgrunnlagTjenesteImpl.class);

    private KodeverkRepository kodeverkRepository;
    private InfotrygdBeregningsgrunnlagConsumer infotrygdBeregningsgrunnlagConsumer;

    @Inject
    public InfotrygdBeregningsgrunnlagTjenesteImpl(InfotrygdBeregningsgrunnlagConsumer infotrygdBeregningsgrunnlagConsumer, KodeverkRepository kodeverkRepository) {
        this.infotrygdBeregningsgrunnlagConsumer = infotrygdBeregningsgrunnlagConsumer;
        this.kodeverkRepository = kodeverkRepository;
    }

    InfotrygdBeregningsgrunnlagTjenesteImpl() {
        // CDI
    }

    @Override
    public YtelsesBeregningsgrunnlag hentGrunnlagListeFull(Behandling behandling, String fnr, LocalDate fom){
        FinnGrunnlagListeResponse finnGrunnlagListeResponse = finnGrunnlagListeFull(fnr, fom);
        return convert(finnGrunnlagListeResponse);
    }

    private YtelsesBeregningsgrunnlag convert(FinnGrunnlagListeResponse finnGrunnlagListeResponse) {
        return new YtelsesBeregningsgrunnlag(finnGrunnlagListeResponse, kodeverkRepository);
    }

    private FinnGrunnlagListeResponse finnGrunnlagListeFull(String fnr, LocalDate fom){
        FinnGrunnlagListeRequest finnGrunnlagListeRequest = new FinnGrunnlagListeRequest();
        try {
            finnGrunnlagListeRequest.setFom(DateUtil.convertToXMLGregorianCalendar(fom));
            finnGrunnlagListeRequest.setTom(DateUtil.convertToXMLGregorianCalendar(LocalDate.of(9999, Month.DECEMBER, 31)));
            finnGrunnlagListeRequest.setPersonident(fnr);
            return infotrygdBeregningsgrunnlagConsumer.finnBeregningsgrunnlagListe(finnGrunnlagListeRequest);

        } catch (FinnGrunnlagListeSikkerhetsbegrensning e) {
            throw InfotrygdTjenesteFeil.FACTORY.tjenesteUtilgjengeligSikkerhetsbegrensning(TJENESTE, e).toException();
        } catch (FinnGrunnlagListeUgyldigInput e) {
            throw InfotrygdTjenesteFeil.FACTORY.ugyldigInput(TJENESTE, e).toException();
        } catch (FinnGrunnlagListePersonIkkeFunnet e) {//$NON-NLS-1$ //NOSONAR
            // Skal ut ifra erfaringer fra fundamentet ikke gjøres noe med, fordi dette er normalt.
            InfotrygdTjenesteFeil.FACTORY.personIkkeFunnet(e).log(log);
        } catch (DatatypeConfigurationException e) {
            throw InfotrygdTjenesteFeil.FACTORY.tekniskFeil(TJENESTE, e).toException();
        }
        return new FinnGrunnlagListeResponse();
    }

}
