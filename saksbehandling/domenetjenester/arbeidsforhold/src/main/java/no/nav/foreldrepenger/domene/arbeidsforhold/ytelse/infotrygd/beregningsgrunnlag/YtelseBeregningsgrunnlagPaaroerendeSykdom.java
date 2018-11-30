package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.PaaroerendeSykdom;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

public class YtelseBeregningsgrunnlagPaaroerendeSykdom extends YtelseBeregningsgrunnlagPeriodeYtelse{

    private final LocalDate foedselsdatoPleietrengende;

    YtelseBeregningsgrunnlagPaaroerendeSykdom(PaaroerendeSykdom paaroerendeSykdom, KodeverkRepository kodeverkRepository) {
        super(RelatertYtelseType.PÅRØRENDESYKDOM, paaroerendeSykdom, kodeverkRepository);
        foedselsdatoPleietrengende = DateUtil.convertToLocalDate(paaroerendeSykdom.getFoedselsdatoPleietrengende());
    }

    public LocalDate getFoedselsdatoPleietrengende() {
        return foedselsdatoPleietrengende;
    }

    @Override
    public void mapSpesialverdier(YtelseGrunnlagBuilder builder) {
        //Ingen spesialhåndtering
    }
}
