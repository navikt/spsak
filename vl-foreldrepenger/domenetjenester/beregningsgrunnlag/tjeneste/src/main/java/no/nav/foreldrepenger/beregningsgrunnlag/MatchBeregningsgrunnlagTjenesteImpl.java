package no.nav.foreldrepenger.beregningsgrunnlag;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@ApplicationScoped
public class MatchBeregningsgrunnlagTjenesteImpl implements MatchBeregningsgrunnlagTjeneste {


    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;


    MatchBeregningsgrunnlagTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public MatchBeregningsgrunnlagTjenesteImpl(BehandlingRepositoryProvider repositoryProvider) {
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
    }

    @Override
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


    @Override
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

    @Override
    public BeregningsgrunnlagPrStatusOgAndel matchPåArbeidsforholdIdHvisTilgjengelig(Behandling behandling, BeregningsgrunnlagPeriode periode, String arbeidsforholdId, Long andelsnr) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> arbeidsforholdId != null && a.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef).isPresent() ?
                ArbeidsforholdRef.ref(arbeidsforholdId).gjelderFor(a.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef).get())
                : a.getAndelsnr().equals(andelsnr))
            .findFirst()
            .orElseThrow(() -> MatchBeregningsgrunnlagTjenesteFeil.FACTORY.finnerIkkeAndelFeil(behandling.getId()).toException());
    }

    @Override
    public List<BeregningsgrunnlagPrStatusOgAndel> matchPåAktivitetstatusOgInntektskategori(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, AktivitetStatus aktivitetStatus, Inntektskategori inntektskategori) {
        return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> a.getInntektskategori().equals(inntektskategori)
                && a.getAktivitetStatus().equals(aktivitetStatus)).collect(Collectors.toList());
    }

    @Override
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

        MatchBeregningsgrunnlagTjenesteImpl.MatchBeregningsgrunnlagTjenesteFeil FACTORY = FeilFactory.create(MatchBeregningsgrunnlagTjenesteImpl.MatchBeregningsgrunnlagTjenesteFeil.class);

        @TekniskFeil(feilkode = "FP-401644", feilmelding = "Finner ikke andelen for eksisterende grunnlag. Behandling %s", logLevel = LogLevel.WARN)
        Feil finnerIkkeAndelFeil(long behandlingId);

        @TekniskFeil(feilkode = "FP-401692", feilmelding = "Fant flere enn 1 matchende periode i gjeldende grunnlag. Behandling %s", logLevel = LogLevel.WARN)
        Feil fantFlereEnn1Periode(long behandlingId);
    }

}
