package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto;

import static java.util.Arrays.asList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Virkedager;

public class StønadskontoerDto {
    private final Map<String, StønadskontoDto> stonadskontoer;

    private LocalDate maksDato;

    private StønadskontoerDto(Map<String, StønadskontoDto> stonadskontoer) {
        this.stonadskontoer = stonadskontoer;
    }

    public Map<String, StønadskontoDto> getStonadskontoer() {
        return stonadskontoer;
    }

    public LocalDate getMaksDato() {
        return maksDato;
    }

    public static Builder build() {
        return new Builder();
    }

    public static class Builder {

        private StønadskontoerDto kladd = new StønadskontoerDto(new HashMap<>());

        public Builder leggTil(AktivitetIdentifikatorDto aktivitetIdentifikator,
                               int fordelteDager,
                               Stønadskonto stønadskonto) {

            StønadskontoDto dto = kladd.stonadskontoer.computeIfAbsent(stønadskonto.getStønadskontoType().getKode(),
                s -> new StønadskontoDto(new ArrayList<>(), stønadskonto.getMaxDager()));
            Optional<AktivitetFordeligDto> aktivitetDto = hentForAktivitet(dto.getAktivitetFordeligDtoList(), aktivitetIdentifikator);
            int nyeFordelteDager = aktivitetDto.map(stønadskontoDto -> stønadskontoDto.getFordelteDager() + fordelteDager)
                .orElse(fordelteDager);
            dto.getAktivitetFordeligDtoList().remove(aktivitetDto.orElse(null));
            dto.getAktivitetFordeligDtoList().add(new AktivitetFordeligDto(aktivitetIdentifikator, nyeFordelteDager));
            return this;
        }

        public Builder leggTilForAnnenPart(AktivitetIdentifikatorDto aktivitetIdentifikator,
                                           int fordelteDager,
                                           Stønadskonto stønadskonto) {
            StønadskontoDto dto = kladd.stonadskontoer.computeIfAbsent(stønadskonto.getStønadskontoType().getKode(),
                s -> new StønadskontoDto(new ArrayList<>(), stønadskonto.getMaxDager()));
            List<AktivitetFordeligDto> aktivitetFordelingAnnenPart = dto.getAktivitetFordelingAnnenPart();

            Optional<AktivitetFordeligDto> aktivitetDto = hentForAktivitet(aktivitetFordelingAnnenPart, aktivitetIdentifikator);
            int nyeFordelteDager = fordelteDager;
            if (aktivitetDto.isPresent()) {
                AktivitetFordeligDto aktivitetFordeligDto = aktivitetDto.get();
                nyeFordelteDager += aktivitetFordeligDto.getFordelteDager();
                aktivitetFordelingAnnenPart.remove(aktivitetFordeligDto);
            }
            aktivitetFordelingAnnenPart.add(new AktivitetFordeligDto(aktivitetIdentifikator, nyeFordelteDager));
            return this;
        }

        private Optional<AktivitetFordeligDto> hentForAktivitet(List<AktivitetFordeligDto> list, AktivitetIdentifikatorDto aktivitetIdentifikator) {
            return list.stream()
                .filter(stønadskontoDto -> Objects.equals(aktivitetIdentifikator, stønadskontoDto.getAktivitetIdentifikator()))
                .findFirst();
        }

        public StønadskontoerDto create(Fagsak fagsak, Optional<LocalDate> sisteUttaksdato) {
            if (sisteUttaksdato.isPresent()) {
                if (fagsak.getRelasjonsRolleType().equals(RelasjonsRolleType.MORA)) {
                    kladd.maksDato = beregnMaksDato(asList(StønadskontoType.MØDREKVOTE.getKode(),
                        StønadskontoType.FELLESPERIODE.getKode(),
                        StønadskontoType.FORELDREPENGER.getKode()), sisteUttaksdato.get());
                } else {
                    kladd.maksDato = beregnMaksDato(asList(StønadskontoType.FEDREKVOTE.getKode(),
                        StønadskontoType.FELLESPERIODE.getKode(),
                        StønadskontoType.FORELDREPENGER.getKode()), sisteUttaksdato.get());
                }
            }
            return kladd;
        }

        private LocalDate beregnMaksDato(List<String> gyldigeStønadskontoer, LocalDate sisteUttaksdato) {
            int tilgjengeligeDager = 0;
            for (Map.Entry<String, StønadskontoDto> entry : kladd.stonadskontoer.entrySet()) {
                String stønadskonto = entry.getKey();
                if (gyldigeStønadskontoer.contains(stønadskonto)) {
                    StønadskontoDto stønadskontoDto = entry.getValue();
                    Integer fordelteDager = finnMinimumFordelteDager(stønadskontoDto.getAktivitetFordeligDtoList());
                    Integer fordelteDagerAnnenPart = finnMinimumFordelteDager(stønadskontoDto.getAktivitetFordelingAnnenPart());

                    tilgjengeligeDager += stønadskontoDto.getMaxDager() - fordelteDager - fordelteDagerAnnenPart;
                }
            }

            if (tilgjengeligeDager > 0) {
                return Virkedager.plusVirkedager(sisteUttaksdato, tilgjengeligeDager);
            }
            return sisteUttaksdato;
        }

        private Integer finnMinimumFordelteDager(List<AktivitetFordeligDto> aktiviteter) {
            return aktiviteter.stream()
                .min(Comparator.comparing(AktivitetFordeligDto::getFordelteDager))
                .map(AktivitetFordeligDto::getFordelteDager).orElse(0);
        }
    }
}
