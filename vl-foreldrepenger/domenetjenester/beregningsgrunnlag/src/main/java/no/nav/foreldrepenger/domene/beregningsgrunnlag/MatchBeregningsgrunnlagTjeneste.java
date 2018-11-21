package no.nav.foreldrepenger.domene.beregningsgrunnlag;


import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;

/**
 * Tjeneste som finner andeler basert på informasjon om andelen (arbeidsforholdId, andelsnr)
 */
public interface MatchBeregningsgrunnlagTjeneste {

    /**
     * Matcher andel fra periode først basert på andelsnr. Om dette gir eit funn returneres andelen. Om dette ikkje
     * gir eit funn matches det på arbeidsforholdId. Om dette ikkje gir eit funn kastes exception.
     *
     * @param behandling behandling som har beregningsgrunnlag med tilhørende beregningsgrunnlagperiode
     * @param periode beregningsgrunnlagperiode der man leter etter en andel basert på andelsnr og arbeidsforholdId
     * @param andelsnr andelsnr til andelen det letes etter
     * @param arbeidsforholdId arbeidsforholdId til arbeidsforholdet som andelen er knyttet til
     * @return andel som matcher oppgitt informasjon, ellers kastes exception
     */
    BeregningsgrunnlagPrStatusOgAndel matchMedAndelFraPeriode(Behandling behandling, BeregningsgrunnlagPeriode periode, Long andelsnr, String arbeidsforholdId);

    /**
     *  Matcher andel i siste beregningsgrunnlag med som ble lagret i steg KOFAKBER_UT, altså på vei ut av kontroller fakta for beregning
     *
     * @param behandling behandling som har beregningsgrunnlag med tilhørende beregningsgrunnlagperiode
     * @param periode beregningsgrunnlagperiode der man leter etter en andel basert på andelsnr og arbeidsforholdId
     * @param andelsnr andelsnr til andelen det letes etter
     * @param arbeidsforholdId arbeidsforholdId til arbeidsforholdet som andelen er knyttet til
     * @return andel som matcher oppgitt informasjon, ellers kastes exception
     */
    Optional<BeregningsgrunnlagPrStatusOgAndel>  matchMedAndelIForrigeBeregningsgrunnlag(Behandling behandling, BeregningsgrunnlagPeriode periode, Long andelsnr, String arbeidsforholdId);

    /**
     *  Matcher andel på arbeidsforholdId hvis denne er ulik null. Om ingen andel er funnet matches det på andelsnr.
     *
     *  Hvis ingen andel er funnet for arbeidsfohroldId eller andelsnr kastes exception.
     *
     * @param behandling behandling som har beregningsgrunnlag med tilhørende beregningsgrunnlagperiode
     * @param periode beregningsgrunnlagperiode der man leter etter en andel basert på andelsnr og arbeidsforholdId
     * @param andelsnr andelsnr til andelen det letes etter
     * @param arbeidsforholdId arbeidsforholdId til arbeidsforholdet som andelen er knyttet til
     * @return andel som matcher oppgitt informasjon, ellers kastes exception
     */
    BeregningsgrunnlagPrStatusOgAndel matchPåArbeidsforholdIdHvisTilgjengelig(Behandling behandling, BeregningsgrunnlagPeriode periode, String arbeidsforholdId, Long andelsnr);


    /**
     * Matcher andel på aktivitetstatus og inntektskategori.
     *
     * @param beregningsgrunnlagPeriode beregningsgrunnlagperiode der man leter etter en andel
     * @param aktivitetStatus aktivitetstatus til andelen det letes etter
     * @param inntektskategori inntektskategorien til andelen det letes etter
     * @return liste av andeler som matcher oppgitt informasjon, ellers empty
     */
    List<BeregningsgrunnlagPrStatusOgAndel> matchPåAktivitetstatusOgInntektskategori(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, AktivitetStatus aktivitetStatus, Inntektskategori inntektskategori);

    /**
     * Matcher andel på aktivitetstatus og inntektskategori, og orgnr og arbeidsforholdId hvis disse er ulik null.
     *
     * Kaster Exception om aktivitetstatus eller inntektskategori er lik null.
     *
     * Matcher først på aktivitetstatus og inntektskategori, om ingen match er funnet returneres Optional.empty(), om match er funnet videreføres resultatet.
     *
     * Om orgnr er null matches det på andeler med orgnr lik null fra resultatet ovenfor.
     *
     * Om orgnr er ulik null og arbeidsforholdId er null matches først på andeler med likt orgnr og arbeidsforholdRef ikke present eller arbeidsforholdRef present, men referanse lik null.
     * Om dette ikke gir resultat matches kun på orgnr, og første funn blant andelene som matcher returneres.
     *
     * Om orgnr er ulik null og arbeidsforholdId er ulik null matches det på andeler som har lik orgnr og arbeidsforholdRef er present og referanse er lik arbeidsforholdRef.
     *
     * @param beregningsgrunnlagPeriode beregningsgrunnlagperiode der man leter etter en andel
     * @param aktivitetStatus aktivitetstatus til andelen det letes etter
     * @param inntektskategori inntektskategorien til andelen det letes etter
     * @param orgnr orgnr til andelen det letes etter
     * @param arbeidsforholdId arbeidsforholdId til andelen det letes etter
     * @return andel som matcher oppgitt informasjon, ellers empty
     */
    Optional<BeregningsgrunnlagPrStatusOgAndel> matchPåTilgjengeligAndelsinformasjon(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, AktivitetStatus aktivitetStatus, Inntektskategori inntektskategori,
                                                                                     String orgnr, String arbeidsforholdId);
}
