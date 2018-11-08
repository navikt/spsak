package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaData;

public class KontrollerFaktaDataDto {
    private final List<KontrollerFaktaPeriodeDto> perioder = new ArrayList<>();

    public KontrollerFaktaDataDto(KontrollerFaktaData perioder) {
        this.perioder.addAll(perioder.getPerioder().stream()
            .map(KontrollerFaktaPeriodeDto::new)
            .sorted(comparing(KontrollerFaktaPeriodeDto::getFom))
            .collect(toList()));
    }

    public List<KontrollerFaktaPeriodeDto> getPerioder() {
        return perioder;
    }
}
