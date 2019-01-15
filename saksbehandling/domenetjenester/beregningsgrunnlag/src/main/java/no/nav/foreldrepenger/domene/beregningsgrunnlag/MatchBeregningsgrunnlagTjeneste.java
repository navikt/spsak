package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@ApplicationScoped
public class MatchBeregningsgrunnlagTjeneste {


    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;


    MatchBeregningsgrunnlagTjeneste() {
        // for CDI proxy
    }

    @Inject
    public MatchBeregningsgrunnlagTjeneste(ResultatRepositoryProvider repositoryProvider) {
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
    }

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
    public BeregningsgrunnlagPrStatusOgAndel matchMedAndelFraPeriode(Behandling behandling, BeregningsgrunnlagPeriode periode, Long andelsnr, String arbeidsforholdId) {
        Optional<BeregningsgrunnlagPrStatusOgAndel> matchetAndel = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> a.getAndelsnr().equals(andelsnr))
            .findFirst();
        return matchetAndel.orElseGet(() -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> a.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef).isPresent())
            .filter(a -> ArbeidsforholdRef.ref(arbeidsforholdId).gjelderFor(a.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef).get()))
            .findFirst()
            .orElseThrow(() -> MatchBeregningsgrunnlagTjenesteFeil.FACTORY.finnerIkkeAndelFeil(behandling.getId()).toException()));
    }


    /**
         *  Matcher andel i siste beregningsgrunnlag med som ble lagret i steg KOFAKBER_UT, altså på vei ut av kontroller fakta for beregning
         *
         * @param behandling behandling som har beregningsgrunnlag med tilhørende beregningsgrunnlagperiode
         * @param periode beregningsgrunnlagperiode der man leter etter en andel basert på andelsnr og arbeidsforholdId
         * @param andelsnr andelsnr til andelen det letes etter
         * @param arbeidsforholdId arbeidsforholdId til arbeidsforholdet som andelen er knyttet til
         * @return andel som matcher oppgitt informasjon, ellers kastes exception
         */
    public Optional<BeregningsgrunnlagPrStatusOgAndel> matchMedAndelIForrigeBeregningsgrunnlag(Behandling behandling, BeregningsgrunnlagPeriode periode, Long andelsnr, String arbeidsforholdId) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> forrigeGrunnlag = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitet(behandling, BeregningsgrunnlagTilstand.KOFAKBER_UT);
        if (!forrigeGrunnlag.isPresent()) {
            return Optional.empty();
        }
        List<BeregningsgrunnlagPeriode> matchendePerioder = forrigeGrunnlag.get().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()
            .stream().filter(periodeIGjeldendeGrunnlag -> periodeIGjeldendeGrunnlag.getPeriode().overlapper(periode.getPeriode())).collect(Collectors.toList());
        if (matchendePerioder.size() != 1) {
            throw MatchBeregningsgrunnlagTjenesteFeil.FACTORY.fantFlereEnn1Periode(behandling.getId()).toException();
        }
        return Optional.of(matchMedAndelFraPeriode(behandling, matchendePerioder.get(0), andelsnr, arbeidsforholdId));
    }

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
    public BeregningsgrunnlagPrStatusOgAndel matchPåArbeidsforholdIdHvisTilgjengelig(Behandling behandling, BeregningsgrunnlagPeriode periode, String arbeidsforholdId, Long andelsnr) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> arbeidsforholdId != null && a.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef).isPresent() ?
                ArbeidsforholdRef.ref(arbeidsforholdId).gjelderFor(a.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef).get())
                : a.getAndelsnr().equals(andelsnr))
            .findFirst()
            .orElseThrow(() -> MatchBeregningsgrunnlagTjenesteFeil.FACTORY.finnerIkkeAndelFeil(behandling.getId()).toException());
    }

    /**
         * Matcher andel på aktivitetstatus og inntektskategori.
         *
         * @param beregningsgrunnlagPeriode beregningsgrunnlagperiode der man leter etter en andel
         * @param aktivitetStatus aktivitetstatus til andelen det letes etter
         * @param inntektskategori inntektskategorien til andelen det letes etter
         * @return liste av andeler som matcher oppgitt informasjon, ellers empty
         */
    public List<BeregningsgrunnlagPrStatusOgAndel> matchPåAktivitetstatusOgInntektskategori(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, AktivitetStatus aktivitetStatus, Inntektskategori inntektskategori) {
        return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> a.getInntektskategori().equals(inntektskategori)
                && a.getAktivitetStatus().equals(aktivitetStatus)).collect(Collectors.toList());
    }

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
    public Optional<BeregningsgrunnlagPrStatusOgAndel> matchPåTilgjengeligAndelsinformasjon(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, AktivitetStatus aktivitetStatus, Inntektskategori inntektskategori,
                                                                                            String arbeidsgiverId, String arbeidsforholdId) {
        if (aktivitetStatus == null || inntektskategori == null) {
            throw new IllegalArgumentException("Aktivietstatus eller inntektskategori kan ikke være null");
        }
        List<BeregningsgrunnlagPrStatusOgAndel> matchAktivitetStatusOgInntektskategori = matchPåAktivitetstatusOgInntektskategori(beregningsgrunnlagPeriode, aktivitetStatus, inntektskategori);
        if (matchAktivitetStatusOgInntektskategori.isEmpty()) {
            return Optional.empty();
        }
        if (arbeidsgiverId == null) {
            return matchAktivitetStatusOgInntektskategori.stream().filter(andel -> andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsgiver).map(Arbeidsgiver::getIdentifikator).orElse(null) == null).findFirst();
        }
        if (arbeidsforholdId == null) {
            Optional<BeregningsgrunnlagPrStatusOgAndel> andelUtanArbeidsforholdId = matchPåArbeidsforholdUtanArbeidsforholdId(beregningsgrunnlagPeriode, arbeidsgiverId);
            return andelUtanArbeidsforholdId.isPresent() ? andelUtanArbeidsforholdId : matchKunPåArbeidsgiverId(beregningsgrunnlagPeriode, arbeidsgiverId);
        }
        return matchPåArbeidsgiverIdOgArbeidsforholdId(beregningsgrunnlagPeriode, arbeidsgiverId, arbeidsforholdId);
    }

    private Optional<BeregningsgrunnlagPrStatusOgAndel> matchPåArbeidsgiverIdOgArbeidsforholdId(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, String arbeidsgiverId, String arbeidsforholdId) {
        return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> Objects.equals(a.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsgiver).map(Arbeidsgiver::getIdentifikator).orElse(null), arbeidsgiverId))
            .filter(a -> a.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef).isPresent() && a.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef).get().gjelderFor(ArbeidsforholdRef.ref(arbeidsforholdId)))
            .findFirst();
    }

    private Optional<BeregningsgrunnlagPrStatusOgAndel> matchPåArbeidsforholdUtanArbeidsforholdId(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, String arbeidsgiverId) {
        return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> Objects.equals(a.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsgiver).map(Arbeidsgiver::getIdentifikator).orElse(null), arbeidsgiverId))
            .filter(a -> !a.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef).isPresent())
            .findFirst();
    }

    private Optional<BeregningsgrunnlagPrStatusOgAndel> matchKunPåArbeidsgiverId(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, String arbeidsgiverId) {
        return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
             .filter(a -> Objects.equals(a.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsgiver).map(Arbeidsgiver::getIdentifikator).orElse(null), arbeidsgiverId))
             .findFirst();
    }

    interface MatchBeregningsgrunnlagTjenesteFeil extends DeklarerteFeil {

        MatchBeregningsgrunnlagTjeneste.MatchBeregningsgrunnlagTjenesteFeil FACTORY = FeilFactory.create(MatchBeregningsgrunnlagTjeneste.MatchBeregningsgrunnlagTjenesteFeil.class);

        @TekniskFeil(feilkode = "FP-401644", feilmelding = "Finner ikke andelen for eksisterende grunnlag. Behandling %s", logLevel = LogLevel.WARN)
        Feil finnerIkkeAndelFeil(long behandlingId);

        @TekniskFeil(feilkode = "FP-401692", feilmelding = "Fant flere enn 1 matchende periode i gjeldende grunnlag. Behandling %s", logLevel = LogLevel.WARN)
        Feil fantFlereEnn1Periode(long behandlingId);
    }

}
