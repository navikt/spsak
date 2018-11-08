package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto;

import java.util.ArrayList;
import java.util.List;

public class StønadskontoDto {

    private final List<AktivitetFordeligDto> aktivitetFordeligDtoList;
    private List<AktivitetFordeligDto> aktivitetFordelingAnnenPart = new ArrayList<>();

    private final int maxDager;


    public StønadskontoDto(List<AktivitetFordeligDto> aktivitetFordeligDtoList, int maxDager) {
        this.aktivitetFordeligDtoList = aktivitetFordeligDtoList;
        this.maxDager = maxDager;
    }

    public int getMaxDager() {
        return maxDager;
    }

    public List<AktivitetFordeligDto> getAktivitetFordeligDtoList() {
        return aktivitetFordeligDtoList;
    }

    public List<AktivitetFordeligDto> getAktivitetFordelingAnnenPart() {
        return aktivitetFordelingAnnenPart;
    }
}
