package no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util;

import static no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseStørrelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseStørrelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@ApplicationScoped
public class BeregningYtelseTestUtil {
    @Inject
    private InntektArbeidYtelseTjeneste iayTjeneste;
    @Inject
    private BeregningArbeidsgiverTestUtil beregningArbeidsgiverTestUtil;
    @Inject
    private BeregningOpptjeningTestUtil opptjeningTestUtil;

    public BeregningYtelseTestUtil() {
        // CDI
    }

    /**
     * Opprett en foreldrepenger ytelse, lagre et InntektArbeidYtelseAggregat og legg en OpptjeningAktivitet til opptjening
     *
     * @param behandling
     * @param periode
     * @param inntektPerMnd
     * @param dekningsgrad
     * @param skjaæringsTidspunktOpptjening
     */
    public void opprettYtelseForeldrepenger(Behandling behandling,
                                            Periode periode,
                                            BigDecimal inntektPerMnd,
                                            BigDecimal dekningsgrad,
                                            LocalDate skjaæringsTidspunktOpptjening,
                                            YtelseArbeidsforhold ytelseArbeidsforhold) {
        AktørYtelseBuilder aktørYtelseBuilder = hentAktørYtelseBuilder(behandling);

        leggTilInfotrygdYtelse(aktørYtelseBuilder, periode, ytelseArbeidsforhold.getSaksnummer(),
            inntektPerMnd, dekningsgrad, RelatertYtelseType.FORELDREPENGER, ytelseArbeidsforhold.getOrgnr(),
            ytelseArbeidsforhold.getArbeidskategori());
        lagreYtelse(behandling, aktørYtelseBuilder, skjaæringsTidspunktOpptjening, ytelseArbeidsforhold.getOrgnr(),
            periode, OpptjeningAktivitetType.FORELDREPENGER);
    }

    /**
     * Opprett en sykepenger ytelse, lagre et InntektArbeidYtelseAggregat og legg en OpptjeningAktivitet til opptjening
     * Hvis relatert ytelse er sykepenger og dekningsgrad er 65% settes arbeidskategori til inaktiv.
     *
     * @param behandling
     * @param periode
     * @param inntektPerMnd
     * @param dekningsgrad
     * @param skjaæringsTidspunktOpptjening
     */
    public void opprettYtelseSykepenger(Behandling behandling,
                                        Periode periode,
                                        BigDecimal inntektPerMnd,
                                        BigDecimal dekningsgrad,
                                        LocalDate skjaæringsTidspunktOpptjening,
                                        YtelseArbeidsforhold ytelseArbeidsforhold) {
        AktørYtelseBuilder aktørYtelseBuilder = hentAktørYtelseBuilder(behandling);

        Arbeidskategori arbeidskategori = ytelseArbeidsforhold.getArbeidskategori();
        if (dekningsgrad.compareTo(Dekningsgrad.DEKNINGSGRAD_65.tilProsentVerdi()) == 0) {
            arbeidskategori = Arbeidskategori.INAKTIV;
        }

        leggTilInfotrygdYtelse(aktørYtelseBuilder, periode, ytelseArbeidsforhold.getSaksnummer(),
            inntektPerMnd, dekningsgrad, RelatertYtelseType.SYKEPENGER, ytelseArbeidsforhold.getOrgnr(), arbeidskategori);
        lagreYtelse(behandling, aktørYtelseBuilder, skjaæringsTidspunktOpptjening, ytelseArbeidsforhold.getOrgnr(), periode,
            OpptjeningAktivitetType.SYKEPENGER);
    }

    /**
     * Legg en ytelse til Infotrygd. Inntekt tilsvarer månedlig inntekt for ytelsen.
     *
     * @param aktørYtelseBuilder
     * @param periode
     * @param saksnummer
     * @param inntektPerMnd
     * @param dekningsgrad
     * @param ytelseType
     * @param orgnr
     * @param arbeidskategori
     */
    public void leggTilInfotrygdYtelse(AktørYtelseBuilder aktørYtelseBuilder,
                                       Periode periode, String saksnummer,
                                       BigDecimal inntektPerMnd, BigDecimal dekningsgrad,
                                       RelatertYtelseType ytelseType, String orgnr,
                                       Arbeidskategori arbeidskategori) {

        leggTilYtelse(aktørYtelseBuilder, periode.getFom(), periode.getTom(), Saksnummer.infotrygd(saksnummer),
            inntektPerMnd, InntektPeriodeType.MÅNEDLIG, dekningsgrad, Fagsystem.INFOTRYGD, ytelseType,
            orgnr, arbeidskategori);
    }

    /**
     * Legg en ytelse til Arena. Inntekt tilsvarer månedlig inntekt for ytelsen.
     *
     * @param aktørYtelseBuilder
     * @param periode
     * @param saksnummer
     * @param inntektPerMnd
     * @param dekningsgrad
     * @param ytelseType
     * @param orgnr
     * @param arbeidskategori
     */
    public void leggTilArenaYtelse(AktørYtelseBuilder aktørYtelseBuilder,
                                   Periode periode, String saksnummer,
                                   BigDecimal inntektPerMnd, BigDecimal dekningsgrad,
                                   RelatertYtelseType ytelseType, String orgnr,
                                   Arbeidskategori arbeidskategori){
        leggTilYtelse(aktørYtelseBuilder, periode.getFom(), periode.getTom(), Saksnummer.arena(saksnummer),
            inntektPerMnd, InntektPeriodeType.MÅNEDLIG, dekningsgrad, Fagsystem.ARENA, ytelseType,
            orgnr, arbeidskategori);
    }

    /**
     * Legg en ytelse til aktør ytelse.
     *
     * @param aktørYtelseBuilder
     * @param fom
     * @param tom
     * @param saksnummer
     * @param inntekt
     * @param dekningsgradProsent
     * @param fagsystem
     * @param ytelseType
     * @param orgnr
     * @param arbeidskategori
     */
    public void leggTilYtelse(AktørYtelseBuilder aktørYtelseBuilder,
                              LocalDate fom, LocalDate tom,
                              Saksnummer saksnummer, BigDecimal inntekt,
                              InntektPeriodeType inntektPeriodeType,
                              BigDecimal dekningsgradProsent,
                              Fagsystem fagsystem, RelatertYtelseType ytelseType,
                              String orgnr, Arbeidskategori arbeidskategori) {
        YtelseBuilder ytelseBuilder = hentYtelesBuilder(aktørYtelseBuilder, saksnummer,
            DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom), fagsystem, ytelseType, RelatertYtelseTilstand.LØPENDE);

        YtelseStørrelse ytelseStørrelse = hentYtelseStørrelse(inntekt, inntektPeriodeType, orgnr);

        YtelseGrunnlag ytelseGrunnlag = hentYtelseGrunnlag(ytelseBuilder, arbeidskategori, ytelseStørrelse,
            dekningsgradProsent, dekningsgradProsent);

        ytelseBuilder.medYtelseGrunnlag(ytelseGrunnlag);
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
    }

    /**
     * Lager en instance av YtelseArbeidsforhold som innheolder arbeid og sak parameter
     *
     * @param saksnummer
     * @param orgnr
     * @param arbeidskategori
     * @return a ny instance av YtelseArbeidsforhold
     */
    public static YtelseArbeidsforhold lagYtelseArbeidsforhold(String saksnummer,
                                                               String orgnr,
                                                               Arbeidskategori arbeidskategori) {
        return new YtelseArbeidsforhold(saksnummer, orgnr, arbeidskategori);
    }

    private AktørYtelseBuilder hentAktørYtelseBuilder(Behandling behandling) {
        InntektArbeidYtelseAggregatBuilder iayAggregatBuilder = iayTjeneste.opprettBuilderForRegister(behandling);
        return iayAggregatBuilder.getAktørYtelseBuilder(behandling.getAktørId());
    }

    private void lagreYtelse(Behandling behandling,
                             AktørYtelseBuilder aktørYtelseBuilder,
                             LocalDate skjaæringsTidspunktOpptjening,
                             String orgnr,
                             Periode periode,
                             OpptjeningAktivitetType aktivitetType) {
        InntektArbeidYtelseAggregatBuilder iayAggregatBuilder = iayTjeneste.opprettBuilderForRegister(behandling);
        iayTjeneste.lagre(behandling, iayAggregatBuilder.leggTilAktørYtelse(aktørYtelseBuilder));
        opptjeningTestUtil.leggOpptjeningAktivitetTilOpptjening(behandling, skjaæringsTidspunktOpptjening, orgnr,
            periode, aktivitetType);
    }

    private YtelseBuilder hentYtelesBuilder(InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder,
                                            Saksnummer saksnummer,
                                            DatoIntervallEntitet periode,
                                            Fagsystem fagsystem,
                                            RelatertYtelseType ytelseType,
                                            RelatertYtelseTilstand ytelseTilstand) {

        YtelseBuilder ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(fagsystem, ytelseType, saksnummer);
        ytelseBuilder.medPeriode(periode);
        ytelseBuilder.medStatus(ytelseTilstand);
        return ytelseBuilder;
    }

    private YtelseStørrelse hentYtelseStørrelse(BigDecimal beløp,
                                                InntektPeriodeType periodeType,
                                                String orgnr) {
        return YtelseStørrelseBuilder.ny()
            .medBeløp(beløp)
            .medHyppighet(periodeType)
            .medVirksomhet(beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr).getVirksomhet())
            .build();
    }

    private YtelseGrunnlag hentYtelseGrunnlag(YtelseBuilder ytelseBuilder,
                                              Arbeidskategori arbeidskategori,
                                              YtelseStørrelse ytelseStørrelse,
                                              BigDecimal dekningsgradProsent,
                                              BigDecimal inntektsgrunnlagProsent) {
        return ytelseBuilder.getGrunnlagBuilder()
            .medArbeidskategori(arbeidskategori)
            .medYtelseStørrelse(ytelseStørrelse)
            .medDekningsgradProsent(dekningsgradProsent)
            .medInntektsgrunnlagProsent(inntektsgrunnlagProsent)
            .build();
    }

    public static class YtelseArbeidsforhold {
        private final String saksnummer;
        private final String orgnr;
        private final Arbeidskategori arbeidskategori;

        public YtelseArbeidsforhold(String saksnummer, String orgnr, Arbeidskategori arbeidskategori) {
            this.saksnummer = saksnummer;
            this.orgnr = orgnr;
            this.arbeidskategori = arbeidskategori;
        }

        public Arbeidskategori getArbeidskategori() {
            return arbeidskategori;
        }

        public String getOrgnr() {
            return orgnr;
        }

        public String getSaksnummer() {
            return saksnummer;
        }

    }
}
