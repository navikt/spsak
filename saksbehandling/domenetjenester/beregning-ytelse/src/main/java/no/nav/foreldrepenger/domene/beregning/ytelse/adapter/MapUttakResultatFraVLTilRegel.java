package no.nav.foreldrepenger.domene.beregning.ytelse.adapter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.domene.beregning.regelmodell.UttakAktivitet;
import no.nav.foreldrepenger.domene.beregning.regelmodell.UttakResultat;
import no.nav.foreldrepenger.domene.beregning.regelmodell.UttakResultatPeriode;
import no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.Arbeidsforhold;

@ApplicationScoped
public class MapUttakResultatFraVLTilRegel {

    @Inject
    public MapUttakResultatFraVLTilRegel() {
    }

    public UttakResultat mapFra(UttakResultatEntitet vlUttakResultat, Behandling behandling) {
        Set<UttakResultatPeriode> uttakResultatPerioder = vlUttakResultat.getGjeldendePerioder().getPerioder().stream()
            .map(periode -> {
                List<UttakAktivitet> uttakAktiviteter = periode.getAktiviteter().stream()
                    .map(aktivitet -> mapAktivitet(aktivitet, behandling))
                    .collect(Collectors.toList());
                return new UttakResultatPeriode(periode.getFom(), periode.getTom(), uttakAktiviteter);
            })
            .collect(Collectors.toSet());

        return new UttakResultat(uttakResultatPerioder);
    }

    private UttakAktivitet mapAktivitet(UttakResultatPeriodeAktivitetEntitet uttakResultatPeriodeAktivitet, Behandling behandling) {
        BigDecimal utbetalingsgrad = uttakResultatPeriodeAktivitet.getUtbetalingsprosent();
        BigDecimal stillingsprosent = mapStillingsprosent(behandling);
        Arbeidsforhold arbeidsforhold = mapArbeidsforhold(uttakResultatPeriodeAktivitet.getUttakAktivitet());
        AktivitetStatus aktivitetStatus = mapAktivitetStatus(uttakResultatPeriodeAktivitet.getUttakArbeidType());

        return new UttakAktivitet(stillingsprosent, utbetalingsgrad, arbeidsforhold, aktivitetStatus, uttakResultatPeriodeAktivitet.isGraderingInnvilget());
    }

    private Arbeidsforhold mapArbeidsforhold(UttakAktivitetEntitet uttakAktivitet) {
        if (!uttakAktivitet.getUttakArbeidType().erArbeidstakerEllerFrilans()) {
            return null;
        }
       return Arbeidsforhold.builder()
           .medOrgnr(uttakAktivitet.getArbeidsforholdOrgnr())
           .medArbeidsforholdId(uttakAktivitet.getArbeidsforholdId())
           .medFrilanser(UttakArbeidType.FRILANS.equals(uttakAktivitet.getUttakArbeidType()))
           .build();
    }

    private AktivitetStatus mapAktivitetStatus(UttakArbeidType uttakArbeidType) {
        if (uttakArbeidType.erArbeidstakerEllerFrilans()) {
            return AktivitetStatus.ATFL;
        }
        if (uttakArbeidType.equals(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE)) {
            return AktivitetStatus.SN;
        }
        return AktivitetStatus.UDEFINERT;
    }

    private BigDecimal mapStillingsprosent(Behandling behandling) {
        /*
        // FIXME SP - hent ut stillingsprosent riktig
        if (UttakArbeidType.FRILANS.equals(uttakAktivitet.getUttakArbeidType())) {
            stillingsprosent = uttakStillingsprosentTjeneste.finnStillingsprosentFrilans(behandling, uttakAktivitet.getFom());
        } else {
            stillingsprosent = uttakStillingsprosentTjeneste.finnStillingsprosentOrdinærtArbeid(behandling, uttakAktivitet.getArbeidsforholdOrgnr(),
                uttakAktivitet.getArbeidsforholdId(), uttakAktivitet.getFom());
        }
        return stillingsprosent.orElse(BigDecimal.valueOf(100));
        */
        return BigDecimal.valueOf(100);
    }
}
