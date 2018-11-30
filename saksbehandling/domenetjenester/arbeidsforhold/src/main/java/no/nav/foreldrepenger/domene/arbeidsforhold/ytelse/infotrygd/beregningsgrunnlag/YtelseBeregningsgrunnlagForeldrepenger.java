package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Foreldrepenger;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

public class YtelseBeregningsgrunnlagForeldrepenger extends YtelseBeregningsgrunnlagPeriodeYtelse{

    private BigDecimal dekningsgrad;
    private BigDecimal gradering;
    private final LocalDate foedselsdatoBarn;
    private final LocalDate opprinneligIdentdato;


    YtelseBeregningsgrunnlagForeldrepenger(Foreldrepenger foreldrepenger, KodeverkRepository kodeverkRepository) {
        super(RelatertYtelseType.FORELDREPENGER, foreldrepenger,kodeverkRepository);
        if(foreldrepenger.getDekningsgrad() != null) {
            dekningsgrad = new BigDecimal(foreldrepenger.getDekningsgrad());
        }
        if(foreldrepenger.getGradering() != null) {
            gradering = new BigDecimal(foreldrepenger.getGradering());
        }
        foedselsdatoBarn = DateUtil.convertToLocalDate(foreldrepenger.getFoedselsdatoBarn());
        opprinneligIdentdato = DateUtil.convertToLocalDate(foreldrepenger.getOpprinneligIdentdato());
    }

    public BigDecimal getDekningsgrad() {
        return dekningsgrad;
    }

    public BigDecimal getGradering() {
        return gradering;
    }

    public LocalDate getFoedselsdatoBarn() {
        return foedselsdatoBarn;
    }

    public LocalDate getOpprinneligIdentdato() {
        return opprinneligIdentdato;
    }

    @Override
    public void mapSpesialverdier(YtelseGrunnlagBuilder builder) {
        builder.medDekningsgradProsent(dekningsgrad);
        builder.medGraderingProsent(gradering);
        builder.medOpprinneligIdentdato(opprinneligIdentdato);
    }
}
