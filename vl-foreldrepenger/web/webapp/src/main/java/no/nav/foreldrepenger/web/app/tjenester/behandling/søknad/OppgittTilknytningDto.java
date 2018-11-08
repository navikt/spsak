package no.nav.foreldrepenger.web.app.tjenester.behandling.søknad;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittLandOpphold;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittTilknytning;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;

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
        OppgittTilknytning oppgittTilknytning = søknad.getOppgittTilknytning();
        if (oppgittTilknytning != null) {
            return new OppgittTilknytningDto(
                oppgittTilknytning.isOppholdNå(),
                oppgittTilknytning.isOppholdINorgeSistePeriode(),
                oppgittTilknytning.isOppholdINorgeNestePeriode(),
                mapFør(oppgittTilknytning.getOpphold()),
                mapEtter(oppgittTilknytning.getOpphold()));
        }
        return null;
    }

    private static List<UtlandsoppholdDto> mapFør(Set<OppgittLandOpphold> opphold) {
        return UtlandsoppholdDto.mapFra(opphold.stream()
            .filter(o -> o.isTidligereOpphold())
            .filter(o -> !o.getLand().equals(Landkoder.NOR))
            .collect(Collectors.toList()));
    }

    private static List<UtlandsoppholdDto> mapEtter(Set<OppgittLandOpphold> utlandsopphold) {
        return UtlandsoppholdDto.mapFra(utlandsopphold.stream()
            .filter(o -> !o.isTidligereOpphold())
            .filter(o -> !o.getLand().equals(Landkoder.NOR))
            .collect(Collectors.toList()));
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
