package no.nav.foreldrepenger.web.app.tjenester.behandling.inntektarbeidytelse;

import java.util.ArrayList;
import java.util.List;

public class RelaterteYtelserDto {
    private String relatertYtelseType;
    private List<TilgrensendeYtelserDto> tilgrensendeYtelserListe = new ArrayList<>();

    public RelaterteYtelserDto(String relatertYtelseType, List<TilgrensendeYtelserDto> tilgrensendeYtelserListe) {
        this.relatertYtelseType = relatertYtelseType;
        if (tilgrensendeYtelserListe != null) {
            this.tilgrensendeYtelserListe.addAll(tilgrensendeYtelserListe);
        }
    }

    public String getRelatertYtelseType() {
        return relatertYtelseType;
    }

    public List<TilgrensendeYtelserDto> getTilgrensendeYtelserListe() {
        return tilgrensendeYtelserListe;
    }
}
