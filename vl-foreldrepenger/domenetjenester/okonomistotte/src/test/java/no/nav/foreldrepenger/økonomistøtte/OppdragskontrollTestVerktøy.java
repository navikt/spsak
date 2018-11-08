package no.nav.foreldrepenger.økonomistøtte;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;

import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Avstemming115;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Grad170;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragsenhet120;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Refusjonsinfo156;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeEndringLinje;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeFagområde;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeKlassifik;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeKomponent;

public class OppdragskontrollTestVerktøy {

    public OppdragskontrollTestVerktøy() {
    }

    List<Oppdragslinje150> getOppdragslinje150MedKlassekode(Oppdragskontroll oppdrag, String klassekode) {
        List<Oppdragslinje150> alleOppdr150Liste = getOppdragslinje150Liste(oppdrag);
        return alleOppdr150Liste.stream().filter(opp150 -> opp150.getKodeKlassifik().equals(klassekode))
            .collect(Collectors.toList());
    }

    void verifiserKjedingForOppdragslinje150(List<Oppdragslinje150> originaltOppdr150ListeAT, List<Oppdragslinje150> originaltOppdr150ListeFL) {
        for (Oppdragslinje150 opp150FL : originaltOppdr150ListeFL) {
            Assertions.assertThat(originaltOppdr150ListeAT).allSatisfy(opp150AT -> {
                Assertions.assertThat(opp150AT.getDelytelseId()).isNotEqualTo(opp150FL.getDelytelseId());
                Assertions.assertThat(opp150AT.getRefDelytelseId()).isNotEqualTo(opp150FL.getDelytelseId());
                Assertions.assertThat(opp150FL.getRefDelytelseId()).isNotEqualTo(opp150AT.getDelytelseId());
            });
        }
    }

    void verifiserOppdr150SomAndelerSlåSammen(Oppdragskontroll originaltOppdrag, Oppdragskontroll revurderingOppdrag) {
        List<Oppdragslinje150> originaltoppdr150Liste = getOppdragslinje150Liste(originaltOppdrag);
        List<Oppdragslinje150> originaltOppdr150ListeBruker = originaltoppdr150Liste.stream().filter(opp150 -> opp150.getOppdrag110().getKodeFagomrade().equals(ØkonomiKodeFagområde.FP.name()))
            .filter(opp150 -> !opp150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik())).collect(Collectors.toList());
        List<Oppdragslinje150> revurderingOppdr150Liste = getOppdragslinje150Liste(revurderingOppdrag);
        List<Oppdragslinje150> revurderingOppdr150ListeBruker = revurderingOppdr150Liste.stream().filter(opp150 -> opp150.getOppdrag110().getKodeFagomrade().equals(ØkonomiKodeFagområde.FP.name()))
            .filter(opp150 -> !opp150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik())).filter(opp150 -> opp150.getKodeEndringLinje().equals(ØkonomiKodeEndringLinje.NY.name()))
            .filter(opp150 -> opp150.getDatoVedtakFom().equals(LocalDate.now().plusDays(8))).collect(Collectors.toList());

        Assertions.assertThat(originaltOppdr150ListeBruker).hasSize(1);
        Assertions.assertThat(revurderingOppdr150ListeBruker).hasSize(1);
        Assertions.assertThat(originaltOppdr150ListeBruker.get(0).getKodeKlassifik()).isEqualTo(ØkonomiKodeKlassifik.FPATORD.getKodeKlassifik());
        Assertions.assertThat(revurderingOppdr150ListeBruker.get(0).getKodeKlassifik()).isEqualTo(ØkonomiKodeKlassifik.FPATORD.getKodeKlassifik());
        Assertions.assertThat(originaltOppdr150ListeBruker.get(0).getSats()).isEqualTo(3000L);
        Assertions.assertThat(revurderingOppdr150ListeBruker.get(0).getSats()).isEqualTo(3100L);
    }

    void verifiserOppdr150MedNyKlassekode(List<Oppdragslinje150> opp150RevurdListe) {
        List<Oppdragslinje150> opp150Liste = opp150RevurdListe.stream()
            .filter(oppdr150 -> !oppdr150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik()) && !oppdr150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPREFAGFER_IOP.getKodeKlassifik()))
            .filter(oppdr150 -> oppdr150.getKodeEndringLinje().equals(ØkonomiKodeEndringLinje.NY.name())).collect(Collectors.toList());
        List<String> klasseKodeListe = opp150Liste.stream().map(Oppdragslinje150::getKodeKlassifik).distinct().collect(Collectors.toList());
        Assertions.assertThat(klasseKodeListe).containsOnly(ØkonomiKodeKlassifik.FPATAL.getKodeKlassifik(), ØkonomiKodeKlassifik.FPREFAG_IOP.getKodeKlassifik());
        Assertions.assertThat(opp150Liste).anySatisfy(opp150 -> Assertions.assertThat(opp150.getRefDelytelseId()).isNull());
    }

    void verifiserDelYtelseOgFagsystemIdForEnKlassekode(List<Oppdragslinje150> opp150RevurderingListe, List<Oppdragslinje150> opp150OriginalListe) {
        Oppdragslinje150 førsteOpp150IRevurderingKjede = opp150RevurderingListe.stream().min(Comparator.comparing(Oppdragslinje150::getDatoVedtakFom)).get();
        Assertions.assertThat(førsteOpp150IRevurderingKjede.getRefDelytelseId()).isNotNull();
        Assertions.assertThat(førsteOpp150IRevurderingKjede.getRefFagsystemId()).isNotNull();
        Assertions.assertThat(opp150OriginalListe).anySatisfy(opp150Original -> Assertions.assertThat(opp150Original.getDelytelseId()).isEqualTo(førsteOpp150IRevurderingKjede.getRefDelytelseId()));
    }

    void verifiserDelYtelseOgFagsystemIdForFlereKlassekode(List<Oppdragslinje150> opp150RevurderingListe, List<Oppdragslinje150> opp150OriginalListe) {
        opp150OriginalListe.removeIf(opp150 -> opp150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik())
            || opp150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPREFAGFER_IOP.getKodeKlassifik()));
        List<String> klassekodeListe = opp150OriginalListe.stream().map(Oppdragslinje150::getKodeKlassifik).distinct().collect(Collectors.toList());
        klassekodeListe.forEach(kode -> {
            if (opp150RevurderingListe.stream().anyMatch(opp150 -> opp150.getKodeKlassifik().equals(kode))) {
                Oppdragslinje150 førsteOpp150IKjede = opp150RevurderingListe.stream().filter(opp150 -> opp150.getKodeKlassifik().equals(kode))
                    .min(Comparator.comparing(Oppdragslinje150::getDatoVedtakFom)).get();
                Assertions.assertThat(opp150OriginalListe).anySatisfy(opp150 -> Assertions.assertThat(opp150.getDelytelseId()).isEqualTo(førsteOpp150IKjede.getRefDelytelseId()));
            }
        });
    }

    List<Oppdragslinje150> getOppdragslinje150Liste(Oppdragskontroll oppdragskontroll) {
        return oppdragskontroll.getOppdrag110Liste().stream()
            .flatMap(oppdrag110 -> oppdrag110.getOppdragslinje150Liste().stream())
            .collect(Collectors.toList());
    }

    boolean erOpp150ForFeriepenger(Oppdragslinje150 opp150) {
        return opp150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPREFAGFER_IOP.getKodeKlassifik()) ||
            opp150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik());
    }

    String endreTilElleveSiffer(String id) {
        if (id.length() == 11) {
            return id;
        } else {
            return "00" + id;
        }
    }

    void verifiserAvstemming115FraRepo(Oppdragskontroll oppdragRevurdering) {
        List<Avstemming115> avstemming115RevurdList = oppdragRevurdering.getOppdrag110Liste().stream()
            .map(Oppdrag110::getAvstemming115).collect(Collectors.toList());

        Assertions.assertThat(avstemming115RevurdList).isNotEmpty();
        Assertions.assertThat(avstemming115RevurdList).hasSameSizeAs(oppdragRevurdering.getOppdrag110Liste());
        for (Avstemming115 avstemming115Revurd : avstemming115RevurdList) {
            Assertions.assertThat(avstemming115Revurd.getKodekomponent()).isEqualTo(ØkonomiKodeKomponent.VLFP.getKodeKomponent());
        }
    }

    void verifiserOppdragsenhet120FraRepo(Oppdragskontroll oppdragRevurdering) {
        List<Oppdragsenhet120> oppdragsenhet120RevurdList = oppdragRevurdering.getOppdrag110Liste().stream()
            .flatMap(oppdrag110 -> oppdrag110.getOppdragsenhet120Liste().stream()).collect(Collectors.toList());

        Assertions.assertThat(oppdragsenhet120RevurdList).isNotEmpty();
        Assertions.assertThat(oppdragsenhet120RevurdList).isNotNull();
        Assertions.assertThat(oppdragsenhet120RevurdList).hasSameSizeAs(oppdragRevurdering.getOppdrag110Liste());
        for (Oppdragsenhet120 oppdragsenhet120Revurd : oppdragsenhet120RevurdList) {
            Assertions.assertThat(oppdragsenhet120Revurd.getTypeEnhet()).isEqualTo("BOS");
            Assertions.assertThat(oppdragsenhet120Revurd.getEnhet()).isEqualTo("8020");
            Assertions.assertThat(oppdragsenhet120Revurd.getDatoEnhetFom()).isEqualTo(LocalDate.of(1900, 1, 1));
        }
    }

    void verifiserGrad170FraRepo(List<Oppdragslinje150> opp150RevurderingList, Oppdragskontroll originaltOppdrag) {
        List<Oppdragslinje150> originaltOpp150Liste = getOppdragslinje150Liste(originaltOppdrag);

        for (Oppdragslinje150 opp150Revurdering : opp150RevurderingList) {
            if (!erOpp150ForFeriepenger(opp150Revurdering)) {
                Assertions.assertThat(opp150Revurdering.getGrad170Liste()).isNotNull();
                Assertions.assertThat(opp150Revurdering.getGrad170Liste()).isNotEmpty();
            } else {
                Assertions.assertThat(opp150Revurdering.getGrad170Liste()).isEmpty();
            }
            Oppdragslinje150 originaltOpp150 = originaltOpp150Liste.stream().
                filter(opp150 -> opp150.getDelytelseId().equals(opp150Revurdering.getDelytelseId())).findFirst().orElse(null);
            if (originaltOpp150 != null && !erOpp150ForFeriepenger(originaltOpp150)) {
                Grad170 grad170Revurdering = opp150Revurdering.getGrad170Liste().get(0);
                Grad170 grad170Originalt = originaltOpp150.getGrad170Liste().get(0);
                Assertions.assertThat(grad170Revurdering.getTypeGrad()).isEqualTo(grad170Originalt.getTypeGrad());
                Assertions.assertThat(grad170Revurdering.getGrad()).isEqualTo(grad170Originalt.getGrad());
            }
        }
    }

    void verifiserRefusjonInfo156FraRepo(List<Oppdrag110> opp110RevurderingList, Oppdragskontroll originaltOppdrag) {

        List<Oppdragslinje150> opp150RevurderingList = opp110RevurderingList.stream().filter(opp110 -> opp110.getKodeFagomrade().equals(ØkonomiKodeFagområde.FPREF.name()))
            .flatMap(opp110 -> opp110.getOppdragslinje150Liste().stream())
            .collect(Collectors.toList());

        List<Oppdragslinje150> originaltOpp150Liste = getOppdragslinje150Liste(originaltOppdrag);

        for (Oppdragslinje150 opp150Revurdering : opp150RevurderingList) {
            Oppdragslinje150 originaltOpp150 = originaltOpp150Liste.stream().
                filter(opp150 -> opp150.getDelytelseId().equals(opp150Revurdering.getRefDelytelseId())).findFirst().orElse(null);
            if (originaltOpp150 != null) {
                Refusjonsinfo156 refusjonsinfo156Originalt = originaltOpp150.getRefusjonsinfo156();
                Refusjonsinfo156 refusjonsinfo156Revurdering = opp150Revurdering.getRefusjonsinfo156();
                Assertions.assertThat(refusjonsinfo156Revurdering.getMaksDato()).isEqualTo(refusjonsinfo156Originalt.getMaksDato());
                Assertions.assertThat(refusjonsinfo156Revurdering.getRefunderesId()).isEqualTo(refusjonsinfo156Originalt.getRefunderesId());
                Assertions.assertThat(refusjonsinfo156Revurdering.getDatoFom()).isEqualTo(refusjonsinfo156Originalt.getDatoFom());
            }
        }
    }

    void verifiserOppdragslinje150ForHverKlassekode(Oppdragskontroll oppdragOriginalt, Oppdragskontroll oppdragRevurdering) {
        List<Oppdragslinje150> originaltOppdr150ListeAT = getOppdragslinje150MedKlassekode(oppdragOriginalt, ØkonomiKodeKlassifik.FPATORD.getKodeKlassifik());
        List<Oppdragslinje150> originaltOppdr150ListeFL = getOppdragslinje150MedKlassekode(oppdragOriginalt, ØkonomiKodeKlassifik.FPATAL.getKodeKlassifik());
        List<Oppdragslinje150> revurderingOppdr150ListeAT = getOppdragslinje150MedKlassekode(oppdragRevurdering, ØkonomiKodeKlassifik.FPATORD.getKodeKlassifik());
        List<Oppdragslinje150> revurderingOppdr150ListeFL = getOppdragslinje150MedKlassekode(oppdragRevurdering, ØkonomiKodeKlassifik.FPATAL.getKodeKlassifik());
        verifiserKjedingForOppdragslinje150(originaltOppdr150ListeAT, originaltOppdr150ListeFL);
        verifiserKjedingForOppdragslinje150(revurderingOppdr150ListeAT, revurderingOppdr150ListeFL);
    }

    void verifiserOpphørsdatoen(Oppdragskontroll originaltOppdrag, Oppdragskontroll oppdragRevurdering) {
        List<Oppdragslinje150> originaltOppdr150Liste = originaltOppdrag.getOppdrag110Liste().stream().flatMap(oppdrag110 -> oppdrag110.getOppdragslinje150Liste().stream()).collect(Collectors.toList());
        List<Oppdragslinje150> oppdr150OpphørtListe = oppdragRevurdering.getOppdrag110Liste().stream().flatMap(oppdrag110 -> oppdrag110.getOppdragslinje150Liste()
            .stream()).filter(opp150 -> opp150.gjelderOpphør() && !opp150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPREFAG_IOP.getKodeKlassifik())).collect(Collectors.toList());
        for (Oppdragslinje150 opp150Opphørt : oppdr150OpphørtListe) {
            String klassekode = opp150Opphørt.getKodeKlassifik();
            LocalDate førsteDatoVedtakFom = originaltOppdr150Liste.stream().filter(opp150 -> opp150.getKodeKlassifik().equals(klassekode)).min(Comparator.comparing(Oppdragslinje150::getDatoVedtakFom))
                .map(Oppdragslinje150::getDatoVedtakFom).get();
            assertThat(opp150Opphørt.getDatoStatusFom()).isEqualTo(førsteDatoVedtakFom);
        }
    }

    LocalDate finnFørsteDatoVedtakFom(List<Oppdragslinje150> originaltOpp150Liste, Oppdragslinje150 originaltOpp150) {
        if (originaltOpp150.getOppdrag110().getKodeFagomrade().equals(ØkonomiKodeFagområde.FP.name())) {
            return originaltOpp150Liste.stream().filter(opp150 -> !opp150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPREFAG_IOP.getKodeKlassifik())
                && !opp150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPREFAGFER_IOP.getKodeKlassifik())).min(Comparator.comparing(Oppdragslinje150::getDatoVedtakFom)).map(Oppdragslinje150::getDatoVedtakFom).get();
        } else {
            String refunderesId = originaltOpp150.getRefusjonsinfo156().getRefunderesId();
            return originaltOpp150Liste.stream().filter(opp150 -> opp150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPREFAG_IOP.getKodeKlassifik()))
                .filter(opp150 -> opp150.getRefusjonsinfo156().getRefunderesId().equals(refunderesId)).min(Comparator.comparing(Oppdragslinje150::getDatoVedtakFom)).map(Oppdragslinje150::getDatoVedtakFom).get();
        }
    }

    boolean opp150MedGradering(Oppdragslinje150 oppdragslinje150) {
        boolean erBrukerEllerVirksomhet = oppdragslinje150.getOppdrag110().getKodeFagomrade().equals(ØkonomiKodeFagområde.FP.name()) ||
            oppdragslinje150.getRefusjonsinfo156().getRefunderesId().equals("00789123456");
        boolean gjelderFeriepenger = oppdragslinje150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik()) ||
            oppdragslinje150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPREFAGFER_IOP.getKodeKlassifik());
        return erBrukerEllerVirksomhet && !gjelderFeriepenger;
    }
}
