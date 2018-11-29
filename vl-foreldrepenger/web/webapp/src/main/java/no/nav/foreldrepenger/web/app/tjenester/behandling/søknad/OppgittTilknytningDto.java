package no.nav.foreldrepenger.web.app.tjenester.behandling.søknad;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;

public class OppgittTilknytningDto {

    private boolean oppholdNorgeNa;
    private boolean oppholdSistePeriode;
    private boolean oppholdNestePeriode;
    private List<UtlandsoppholdDto> utlandsoppholdFor;
    private List<UtlandsoppholdDto> utlandsoppholdEtter;

    public OppgittTilknytningDto() {
        // trengs for deserialisering av JSON
    }

    private OppgittTilknytningDto(boolean oppholdNorgeNa,
                                  boolean oppholdSistePeriode,
                                  boolean oppholdNestePeriode,
                                  List<UtlandsoppholdDto> utlandsoppholdFor,
                                  List<UtlandsoppholdDto> utlandsoppholdEtter) {

        this.oppholdNorgeNa = oppholdNorgeNa;
        this.oppholdSistePeriode = oppholdSistePeriode;
        this.oppholdNestePeriode = oppholdNestePeriode;
        this.utlandsoppholdFor = utlandsoppholdFor;
        this.utlandsoppholdEtter = utlandsoppholdEtter;
    }

    public static OppgittTilknytningDto mapFra(Søknad søknad) {
        return null;
    }

    public boolean isOppholdNorgeNa() {
        return oppholdNorgeNa;
    }

    public boolean isOppholdSistePeriode() {
        return oppholdSistePeriode;
    }

    public boolean isOppholdNestePeriode() {
        return oppholdNestePeriode;
    }

    public List<UtlandsoppholdDto> getUtlandsoppholdFor() {
        return utlandsoppholdFor;
    }

    public List<UtlandsoppholdDto> getUtlandsoppholdEtter() {
        return utlandsoppholdEtter;
    }
}
