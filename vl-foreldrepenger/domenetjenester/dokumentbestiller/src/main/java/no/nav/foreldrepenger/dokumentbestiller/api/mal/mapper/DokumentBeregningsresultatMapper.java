package no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper;

import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DuplikatVerktøy.slåSammenArbeidsforholdDto;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper.DokutmentAktivitetMapper.mapAnnenAktivitet;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper.DokutmentAktivitetMapper.mapArbeidsforhold;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper.DokutmentAktivitetMapper.mapNæring;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.InnvilgetÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.PeriodeBeregner;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeMedPerioderDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.PeriodeDto;
import no.nav.foreldrepenger.dokumentbestiller.doktype.InnvilgelseForeldrepengerMapper;

class DokumentBeregningsresultatMapper {
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private UttakRepository uttakRepository;
    private FagsakRelasjonRepository fagsakRelasjonRepository;

    DokumentBeregningsresultatMapper(BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                     UttakRepository uttakRepository,
                                     FagsakRelasjonRepository fagsakRelasjonRepository) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.uttakRepository = uttakRepository;
        this.fagsakRelasjonRepository = fagsakRelasjonRepository;
    }

    void mapDataRelatertTilBeregningsresultat(Behandling behandling, BeregningsresultatFP beregningsresultat, DokumentTypeMedPerioderDto dto) {
        //TODO: Tor - venter avklaring
        for (BeregningsresultatPeriode periode : beregningsresultat.getBeregningsresultatPerioder()) {
            dto.getDokumentBeregningsresultatDto().setAntallArbeidsgivere(((int) periode.getBeregningsresultatAndelList().stream().filter(andel -> AktivitetStatus.ARBEIDSTAKER.equals(andel.getAktivitetStatus())).map(BeregningsresultatAndel::getVirksomhet).distinct().count()));
        }

        mapDataRelatertTilPerioder(behandling, beregningsresultat, dto);
    }

    private void mapDataRelatertTilPerioder(Behandling behandling, BeregningsresultatFP beregningsresultat, DokumentTypeMedPerioderDto dto) {
        List<UttakResultatPeriodeEntitet> uttakPerioder = finnUttaksPerioder(behandling);
        List<BeregningsgrunnlagPeriode> beregningsgrunnlagPerioder = finnBeregninsgrunnlagperioder(behandling);
        for (BeregningsresultatPeriode periode : beregningsresultat.getBeregningsresultatPerioder()) {
            dto.addPeriode(mapBeregningsresultatPeriode(periode, uttakPerioder, behandling, dto, beregningsgrunnlagPerioder));
        }

        if (uttakPerioder.size() != beregningsresultat.getBeregningsresultatPerioder().size()) {
            uttakPerioder.stream()
                .filter(up -> !PeriodeBeregner.erPeriodeDekket(up, beregningsresultat.getBeregningsresultatPerioder()))
                .forEach(up -> {
                    dto.addPeriode(mapUttaksPeriode(behandling, up, dto));
                });
        }
        //PFP-485
        if (dto.isInnvilgetGraderingFinnes()) {
            leggTilLovhjemmelIDto(dto, "14-16");
        }
    }

    private PeriodeDto mapUttaksPeriode(Behandling behandling, UttakResultatPeriodeEntitet up, DokumentTypeMedPerioderDto medPerioderDto) {
        PeriodeDto dto = new PeriodeDto();

        dto.setPeriodeFom(up.getFom().toString());
        dto.setPeriodeTom(up.getTom().toString());

        setÅrsak(behandling, dto, up);
        if (IkkeOppfyltÅrsak.UKJENT.getKode().equals(dto.getÅrsak()) || "".equals(dto.getÅrsak())) {
            dto.setÅrsak(InnvilgetÅrsak.UTTAK_OPPFYLT.getKode());
            leggTilLovhjemmelIDto(medPerioderDto, "14-6");
        } else {
            String lovhjemmel = up.getPeriodeResultatÅrsak().getLovReferanse(behandling.getFagsakYtelseType()).orElse("");
            leggTilLovhjemmelIDto(medPerioderDto, lovhjemmel);
        }
        dto.setInnvilget(PeriodeResultatType.INNVILGET.equals(up.getPeriodeResultatType()));
        List<UttakResultatPeriodeAktivitetEntitet> uttakAktiviteter = up.getAktiviteter();
        dto.setAntallTapteDager(PeriodeBeregner.alleAktiviteterHarNullUtbetaling(uttakAktiviteter) ? uttakAktiviteter.stream().mapToInt(UttakResultatPeriodeAktivitetEntitet::getTrekkdager).max().orElse(0) : 0);

        return dto;
    }

    private PeriodeDto mapBeregningsresultatPeriode(BeregningsresultatPeriode periode,
                                                    List<UttakResultatPeriodeEntitet> uttakPerioder,
                                                    Behandling behandling,
                                                    DokumentTypeMedPerioderDto dtoMedPerioder,
                                                    List<BeregningsgrunnlagPeriode> beregninsgrunnlagPerioder) {
        PeriodeDto dto = new PeriodeDto();

        dto.setPeriodeFom(periode.getBeregningsresultatPeriodeFom().toString());
        dto.setPeriodeTom(periode.getBeregningsresultatPeriodeTom().toString());
        UttakResultatPeriodeEntitet uttakPeriode = PeriodeBeregner.finnUttaksPeriode(periode, uttakPerioder);
        setÅrsak(behandling, dto, uttakPeriode);
        if (IkkeOppfyltÅrsak.UKJENT.getKode().equals(dto.getÅrsak()) || "".equals(dto.getÅrsak())) {
            dto.setÅrsak(InnvilgetÅrsak.UTTAK_OPPFYLT.getKode());
            leggTilLovhjemmelIDto(dtoMedPerioder, "14-6"); //TODO PK-45610 trenger innvilgesesårsaker
        } else {
            String lovhjemmel = uttakPeriode.getPeriodeResultatÅrsak().getLovReferanse(behandling.getFagsakYtelseType()).orElse("");
            leggTilLovhjemmelIDto(dtoMedPerioder, lovhjemmel);
        }
        dto.setInnvilget(PeriodeResultatType.INNVILGET.equals(uttakPeriode.getPeriodeResultatType()));
        List<UttakResultatPeriodeAktivitetEntitet> uttakAktiviteter = uttakPeriode.getAktiviteter();
        dto.setAntallTapteDager(PeriodeBeregner.alleAktiviteterHarNullUtbetaling(uttakAktiviteter) ? uttakAktiviteter.stream().mapToInt(UttakResultatPeriodeAktivitetEntitet::getTrekkdager).max().orElse(0) : 0);
        BeregningsgrunnlagPeriode bgPeriode = PeriodeBeregner.finnBeregninsgrunnlagperiode(periode, beregninsgrunnlagPerioder);
        List<BeregningsgrunnlagPrStatusOgAndel> bgPerStatusOgAndelListe = bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList();
            for (BeregningsresultatAndel andel : periode.getBeregningsresultatAndelList()) {
                AktivitetStatus status = andel.getAktivitetStatus();
                Optional<UttakResultatPeriodeAktivitetEntitet> uttakAktivitet = PeriodeBeregner.finnAktivitetMedStatusHvisFinnes(status, uttakAktiviteter, andel.getVirksomhet(), andel.getArbeidsforholdRef());
                Optional<BeregningsgrunnlagPrStatusOgAndel> bgPrStatusOgAndel = PeriodeBeregner.finnBgPerStatusOgAndelHvisFinnes(status, bgPerStatusOgAndelListe, andel.getVirksomhet(), andel.getArbeidsforholdRef());
                if (AktivitetStatus.ARBEIDSTAKER.equals(status)) {
                    dto.leggTilArbeidsforhold(mapArbeidsforhold(uttakAktivitet, andel));
                } else if (AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE.equals(status)) {
                    dto.setNæring(mapNæring(andel, uttakAktivitet, bgPrStatusOgAndel));
                } else {
                    dto.leggTilAnnenAktivitet(mapAnnenAktivitet(andel, uttakAktivitet));
                }
            }
        dto.setArbeidsforhold(slåSammenArbeidsforholdDto(dto.getArbeidsforhold()));
        return dto;
    }


    private void setÅrsak(Behandling behandling, PeriodeDto dto, UttakResultatPeriodeEntitet uttakPeriode) {
        String årsaksKode = uttakPeriode.getPeriodeResultatÅrsak() != null ? uttakPeriode.getPeriodeResultatÅrsak().getKode() : "";
        if (IkkeOppfyltÅrsak.DEN_ANDRE_PART_SYK_SKADET_IKKE_OPPFYLT.getKode().equals(årsaksKode)) {
            dto.setÅrsak(fagsakRelasjonRepository.finnRelasjonFor(behandling.getFagsak()).getFagsakNrTo().filter(fagsak2 -> RelasjonsRolleType.FARA.equals(fagsak2.getRelasjonsRolleType()))
                .map(årsak -> årsaksKode + InnvilgelseForeldrepengerMapper.DISCRIMINATOR_4007).orElse(årsaksKode));
        } else if (IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER.getKode().equals(årsaksKode) && RelasjonsRolleType.MEDMOR.equals(behandling.getFagsak().getRelasjonsRolleType())) {
            dto.setÅrsak(årsaksKode + InnvilgelseForeldrepengerMapper.DISCRIMINATOR_4005);
        } else {
            dto.setÅrsak(årsaksKode);
        }
    }

    private void leggTilLovhjemmelIDto(DokumentTypeMedPerioderDto dto, String lovhjemmel) {
        String[] splittetHjemler = lovhjemmel.split(",");
        for (String hjemmel : splittetHjemler) {
            dto.leggTilLovhjemmelVurdering(hjemmel);
        }
    }

    private List<UttakResultatPeriodeEntitet> finnUttaksPerioder(Behandling behandling) {
        Optional<UttakResultatEntitet> uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        if (uttakResultat.isPresent()) {
            return uttakResultat.get().getGjeldendePerioder().getPerioder();
        }
        return Collections.emptyList();
    }

    private List<BeregningsgrunnlagPeriode> finnBeregninsgrunnlagperioder(Behandling behandling) {
        Optional<Beregningsgrunnlag> bg = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        return (bg.map(Beregningsgrunnlag::getBeregningsgrunnlagPerioder).orElse(Collections.emptyList()));
    }
}
