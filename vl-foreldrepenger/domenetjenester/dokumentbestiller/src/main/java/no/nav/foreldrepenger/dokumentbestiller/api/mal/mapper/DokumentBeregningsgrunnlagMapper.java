package no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BeregningsgrunnlagAndelDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BeregningsgrunnlagRegelDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeMedPerioderDto;

class DokumentBeregningsgrunnlagMapper {

    void mapDataRelatertTilBeregningsgrunnlag(final Beregningsgrunnlag beregningsgrunnlag, final DokumentTypeMedPerioderDto dto) {
        BigDecimal seksG = beregningsgrunnlag.getGrunnbeløp().getVerdi().multiply(BigDecimal.valueOf(6));
        dto.getDokumentBeregningsgrunnlagDto().setSeksG(seksG.longValue());
        List<BeregningsgrunnlagPeriode> perioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        BeregningsgrunnlagPeriode førstePeriode = perioder.get(0);
        dto.getDokumentBeregningsgrunnlagDto().setDagsats(førstePeriode.getDagsats() != null ? førstePeriode.getDagsats() : 0);
        dto.getDokumentBeregningsgrunnlagDto().setMånedsbeløp(dto.getDokumentBeregningsgrunnlagDto().getDagsats() * 260 / 12);
        dto.getDokumentBeregningsgrunnlagDto().setLovhjemmelBeregning(beregningsgrunnlag.getHjemmel().getNavn());
        dto.getDokumentBeregningsgrunnlagDto().setInntektOverSeksG(førstePeriode.getBruttoPrÅr().compareTo(seksG) > 0);
        int totalBrukerAndel = finnesBrukerAndel(perioder);
        int totalArbeidsgiverAndel = finnesArbeidsgiverAndel(perioder);
        dto.getDokumentBeregningsgrunnlagDto().setTotalBrukerAndel((long) totalBrukerAndel);
        dto.getDokumentBeregningsgrunnlagDto().setTotalArbeidsgiverAndel((long) totalArbeidsgiverAndel);

        for (BeregningsgrunnlagAktivitetStatus aktivitetStatus : beregningsgrunnlag.getAktivitetStatuser()) {
            dto.getDokumentBeregningsgrunnlagDto().addBeregningsgrunnlagRegel(mapRegel(aktivitetStatus.getAktivitetStatus(), førstePeriode, dto));
        }
        dto.getDokumentBeregningsgrunnlagDto().setOverbetaling(false);
    }

    private int finnesArbeidsgiverAndel(List<BeregningsgrunnlagPeriode> perioder) {
        for (BeregningsgrunnlagPeriode periode : perioder) {
            for (BeregningsgrunnlagPrStatusOgAndel bgpsa : periode.getBeregningsgrunnlagPrStatusOgAndelList()) {
                if (bgpsa.getDagsatsArbeidsgiver() != null && bgpsa.getDagsatsArbeidsgiver() > 0) {
                    return 1;
                }
            }
        }
        return 0;
    }

    private int finnesBrukerAndel(List<BeregningsgrunnlagPeriode> perioder) {
        for (BeregningsgrunnlagPeriode periode : perioder) {
            for (BeregningsgrunnlagPrStatusOgAndel bgpsa : periode.getBeregningsgrunnlagPrStatusOgAndelList()) {
                if (bgpsa.getDagsatsBruker() != null && bgpsa.getDagsatsBruker() > 0) {
                    return 1;
                }
            }
        }
        return 0;
    }

    void mapDataRelatertTilBeregningsgrunnlag(final Beregningsgrunnlag beregningsgrunnlag, final Beregningsgrunnlag gammeltBeregningsgrunnlag,
                                              final DokumentTypeMedPerioderDto dto) {
        mapDataRelatertTilBeregningsgrunnlag(beregningsgrunnlag, dto);

        BeregningsgrunnlagPeriode periode = gammeltBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        final Long nyDagsats = dto.getDokumentBeregningsgrunnlagDto().getDagsats();
        Long gammelDagsats = periode.getDagsats() != null ? periode.getDagsats() : 0;

        if (nyDagsats < gammelDagsats) {
            dto.getDokumentBeregningsgrunnlagDto().setOverbetaling(true);
        }
    }

    private BeregningsgrunnlagRegelDto mapRegel(AktivitetStatus aktivitetStatus, BeregningsgrunnlagPeriode periode, DokumentTypeMedPerioderDto dokumentDto) {
        BeregningsgrunnlagRegelDto dto = new BeregningsgrunnlagRegelDto();
        dto.setStatus(aktivitetStatus.getKode());
        Set<AktivitetStatus> aktivitetStatuserMedArbeidstaker = new HashSet<>();
        aktivitetStatuserMedArbeidstaker.add(AktivitetStatus.ARBEIDSTAKER);
        aktivitetStatuserMedArbeidstaker.add(AktivitetStatus.KOMBINERT_AT_FL_SN);
        aktivitetStatuserMedArbeidstaker.add(AktivitetStatus.KOMBINERT_AT_FL);
        aktivitetStatuserMedArbeidstaker.add(AktivitetStatus.KOMBINERT_AT_SN);

        if (aktivitetStatuserMedArbeidstaker.contains(aktivitetStatus)) {
            //Feltet skal være antall arbeidsforhold - ikke antall arbeidsgivere. Burde få nytt navn i ny XSD
            dto.setAntallArbeidsgivereIBeregning((int) periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bgPrStatusOgAndel -> bgPrStatusOgAndel.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER))
                .count());
        } else {
            dto.setAntallArbeidsgivereIBeregning(0);
        }
        dto.setNavBetalerRestbeløp(false);
        // Håndter ev enkeltstatus
        if (mapEnkeltstatus(aktivitetStatus, periode, dto, dokumentDto)) {
            return dto;
        }
        // Håndter sammensatt status
        if (AktivitetStatus.KOMBINERT_AT_FL.equals(aktivitetStatus)) {
            mapEnkeltstatus(AktivitetStatus.FRILANSER, periode, dto, dokumentDto);
            mapEnkeltstatus(AktivitetStatus.ARBEIDSTAKER, periode, dto, dokumentDto);
        } else if (AktivitetStatus.KOMBINERT_AT_FL_SN.equals(aktivitetStatus)) {
            mapEnkeltstatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, periode, dto, dokumentDto);
            mapEnkeltstatus(AktivitetStatus.FRILANSER, periode, dto, dokumentDto);
            mapEnkeltstatus(AktivitetStatus.ARBEIDSTAKER, periode, dto, dokumentDto);
        } else if (AktivitetStatus.KOMBINERT_AT_SN.equals(aktivitetStatus)) {
            mapEnkeltstatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, periode, dto, dokumentDto);
            mapEnkeltstatus(AktivitetStatus.ARBEIDSTAKER, periode, dto, dokumentDto);
        } else if (AktivitetStatus.KOMBINERT_FL_SN.equals(aktivitetStatus)) {
            mapEnkeltstatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, periode, dto, dokumentDto);
            mapEnkeltstatus(AktivitetStatus.FRILANSER, periode, dto, dokumentDto);
        }
        return dto;
    }

    private boolean mapEnkeltstatus(AktivitetStatus aktivitetStatus, BeregningsgrunnlagPeriode periode, BeregningsgrunnlagRegelDto dto, DokumentTypeMedPerioderDto dokumentDto) {
        boolean funnet = false;
        for (BeregningsgrunnlagPrStatusOgAndel andel : periode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            if (aktivitetStatus.equals(andel.getAktivitetStatus())) {
                funnet = true;
                mapMatchetStatus(dto, andel, dokumentDto);
            } else if (aktivitetStatus.equals(AktivitetStatus.TILSTØTENDE_YTELSE)) {
                if (!RelatertYtelseType.UDEFINERT.equals(andel.getYtelse())) {
                    dto.setTilstøtendeYtelse(andel.getYtelse().getKode());
                    funnet = true;
                }
                mapMatchetStatus(dto, andel, dokumentDto);
            }
        }
        return funnet;
    }

    private void mapMatchetStatus(BeregningsgrunnlagRegelDto dto, BeregningsgrunnlagPrStatusOgAndel andel, DokumentTypeMedPerioderDto dokumentDto) {
        if (andel.getDagsatsArbeidsgiver() != null && andel.getDagsatsArbeidsgiver() > 0
            && andel.getDagsatsBruker() != null && andel.getDagsatsBruker() > 0) {
            dto.setNavBetalerRestbeløp(true);
        }
        dto.addBeregningsgrunnlagAndelDto(mapAndel(andel, dokumentDto, dto));
    }

    private BeregningsgrunnlagAndelDto mapAndel(BeregningsgrunnlagPrStatusOgAndel andel, DokumentTypeMedPerioderDto dokumentDto, BeregningsgrunnlagRegelDto regelDto) {
        BeregningsgrunnlagAndelDto dto = new BeregningsgrunnlagAndelDto();
        dto.setStatus(andel.getAktivitetStatus().getKode());
        if (AktivitetStatus.ARBEIDSTAKER.equals(andel.getAktivitetStatus()) && andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet).isPresent()) {
            dto.setArbeidsgiverNavn(andel.getBgAndelArbeidsforhold().get().getVirksomhet().get().getNavn());
        }
        dto.setDagsats(String.valueOf(andel.getDagsats()));
        dto.setMånedsinntekt(String.valueOf(andel.getBruttoPrÅr().divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP).longValue()));
        dto.setÅrsinntekt(String.valueOf(andel.getBruttoPrÅr().longValue()));
        if (andel.getBesteberegningPrÅr() != null) {
            regelDto.setBesteBeregning(true);
        }
        oppdaterRegelRedusert(andel, regelDto);
        if (andel.getOverstyrtPrÅr() != null) {
            dokumentDto.setOverstyrtBeløpBeregning(true);
        }
        if (AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE.equals(andel.getAktivitetStatus())) {
            dto.setPensjonsgivendeInntekt(andel.getPgiSnitt() == null ? null : String.valueOf(andel.getPgiSnitt().longValue()));
            dto.setSisteLignedeÅr(andel.getBeregningsperiodeTom() == null ? null : String.valueOf(andel.getBeregningsperiodeTom().getYear()));
        }
        return dto;
    }

    private void oppdaterRegelRedusert(BeregningsgrunnlagPrStatusOgAndel andel, BeregningsgrunnlagRegelDto regelDto) {
        regelDto.setTotaltRedusertBeregningsgrunnlag(regelDto.getTotaltRedusertBeregningsgrunnlag() + finnTotaltRedusert(andel));
    }

    private long finnTotaltRedusert(BeregningsgrunnlagPrStatusOgAndel andel) {
        return andel.getRedusertPrÅr() == null ? 0 : andel.getRedusertPrÅr().longValue();
    }
}
