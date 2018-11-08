package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.FastsettePerioderEndringTjeneste;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.UttakPeriodeEndringDto;

@ApplicationScoped
public class FastsettePerioderEndringTjenesteImpl implements FastsettePerioderEndringTjeneste {

    private UttakRepository uttakRepository;

    FastsettePerioderEndringTjenesteImpl() {
        //CDI
    }

    @Inject
    public FastsettePerioderEndringTjenesteImpl(UttakRepository uttakRepository) {
        this.uttakRepository = uttakRepository;
    }

    @Override
    public List<UttakPeriodeEndringDto> finnEndringerMellomOpprinneligOgOverstyrt(Behandling behandling, Long uttakResultatId) {
        UttakResultatEntitet uttakResultat = uttakRepository.hentUttakResultatPåId(uttakResultatId)
            .orElseThrow(() -> new IllegalStateException("Fant ingen uttakresultat med id " + uttakResultatId.toString()));
        return lagEndringDto(uttakResultat);
    }

    @Override
    public List<UttakPeriodeEndringDto> finnEndringerMellomOpprinneligOgOverstyrt(Behandling behandling) {
        UttakResultatEntitet uttakResultat = uttakRepository.hentUttakResultat(behandling);
        return lagEndringDto(uttakResultat);
    }

    private List<UttakPeriodeEndringDto> lagEndringDto(UttakResultatEntitet uttakResultat) {
        if (uttakResultat.getOverstyrtPerioder() == null || uttakResultat.getOverstyrtPerioder().getPerioder().isEmpty()) {
            return Collections.emptyList();
        }

        List<UttakPeriodeEndringDto> perioderMedEndringer = new ArrayList<>();
        perioderMedEndringer.addAll(finnEndringerAvOpprinnelig(uttakResultat));
        perioderMedEndringer.addAll(finnEndringerAvOverstyrt(uttakResultat));

        return perioderMedEndringer;
    }

    private List<UttakPeriodeEndringDto> finnEndringerAvOpprinnelig(UttakResultatEntitet uttakResultat) {
        List<UttakPeriodeEndringDto> perioderMedEndringer = new ArrayList<>();
        for (UttakResultatPeriodeEntitet opprinneligPeriode : uttakResultat.getOpprinneligPerioder().getPerioder()) {
            if (erSlettet(opprinneligPeriode, uttakResultat.getOverstyrtPerioder())) {
                perioderMedEndringer.add(lagEndretDto(opprinneligPeriode.getFom(), opprinneligPeriode.getTom(), UttakPeriodeEndringDto.TypeEndring.SLETTET));
            }
        }
        return perioderMedEndringer;
    }

    private List<UttakPeriodeEndringDto> finnEndringerAvOverstyrt(UttakResultatEntitet uttakResultat) {
        List<UttakPeriodeEndringDto> perioderMedEndringer = new ArrayList<>();
        for (UttakResultatPeriodeEntitet overstyrtPeriode : uttakResultat.getOverstyrtPerioder().getPerioder()) {
            if (erLagtTil(overstyrtPeriode, uttakResultat.getOpprinneligPerioder())) {
                perioderMedEndringer.add(lagEndretDto(overstyrtPeriode.getFom(), overstyrtPeriode.getTom(), UttakPeriodeEndringDto.TypeEndring.LAGT_TIL));
            } else if (erEndret(overstyrtPeriode, uttakResultat.getOpprinneligPerioder())) {
                perioderMedEndringer.add(lagEndretDto(overstyrtPeriode.getFom(), overstyrtPeriode.getTom(), UttakPeriodeEndringDto.TypeEndring.ENDRET));
            }
        }
        return perioderMedEndringer;
    }

    private boolean erSlettet(UttakResultatPeriodeEntitet opprinneligPeriode, UttakResultatPerioderEntitet overstyrtPerioder) {
        for (UttakResultatPeriodeEntitet overstyrtPeriode : overstyrtPerioder.getPerioder()) {
            if (overstyrtPeriode.getTom().isBefore(opprinneligPeriode.getTom()) && overstyrtPeriode.getFom().isEqual(opprinneligPeriode.getFom())) {
                return true;
            }
        }
        return false;
    }

    private boolean erEndret(UttakResultatPeriodeEntitet overstyrtPeriode, UttakResultatPerioderEntitet opprinneligePerioder) {
        for (UttakResultatPeriodeEntitet opprinnelig : opprinneligePerioder.getPerioder()) {
            if (erLik(opprinnelig, overstyrtPeriode)) {
                return false;
            }
        }
        return true;
    }

    private boolean erLagtTil(UttakResultatPeriodeEntitet overstyrtPeriode, UttakResultatPerioderEntitet opprinneligePerioder) {
        for (UttakResultatPeriodeEntitet opprinnligPeriode : opprinneligePerioder.getPerioder()) {
            if (opprinnligPeriode.getFom().isEqual(overstyrtPeriode.getFom()) && opprinnligPeriode.getTom().isEqual(overstyrtPeriode.getTom())) {
                return false;
            }
        }
        return true;
    }

    private boolean erLik(UttakResultatPeriodeEntitet periode1, UttakResultatPeriodeEntitet periode2) {
        if (!Objects.equals(periode1.getFom(), periode2.getFom()) ||
            !Objects.equals(periode1.getTom(), periode2.getTom()) ||
            !Objects.equals(periode1.getPeriodeResultatType(), periode2.getPeriodeResultatType()) ||
            !Objects.equals(periode1.getBegrunnelse(), periode2.getBegrunnelse())) {
            return false;
        }

        if (periode1.getAktiviteter().size() != periode2.getAktiviteter().size()) {
            return false;
        }

        for (UttakResultatPeriodeAktivitetEntitet aktivitet1 : periode1.getAktiviteter()) {
            boolean fantLikAktivetet = false;
            for (UttakResultatPeriodeAktivitetEntitet aktivitet2 : periode2.getAktiviteter()) {
                if (erLik(aktivitet1, aktivitet2)) {
                    fantLikAktivetet = true;
                }
            }
            if (!fantLikAktivetet) {
                return false;
            }
        }
        return true;
    }

    private boolean erLik(UttakResultatPeriodeAktivitetEntitet aktivitet1, UttakResultatPeriodeAktivitetEntitet aktivitet2) {
        return Objects.equals(aktivitet1.getFom(), aktivitet2.getFom()) &&
            Objects.equals(aktivitet1.getTom(), aktivitet2.getTom()) &&
            Objects.equals(aktivitet1.getTrekkonto(), aktivitet2.getTrekkonto()) &&
            Objects.equals(aktivitet1.getTrekkdager(), aktivitet2.getTrekkdager()) &&
            Objects.equals(aktivitet1.getArbeidsprosent(), aktivitet2.getArbeidsprosent()) &&
            Objects.equals(aktivitet1.getUtbetalingsprosent(), aktivitet2.getUtbetalingsprosent());
    }


    private UttakPeriodeEndringDto lagEndretDto(LocalDate fom, LocalDate tom, UttakPeriodeEndringDto.TypeEndring endringType) {
        return new UttakPeriodeEndringDto.Builder()
            .medTypeEndring(endringType)
            .medPeriode(fom, tom)
            .build();
    }
}
