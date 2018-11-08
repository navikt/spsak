package no.nav.foreldrepenger.økonomistøtte.fp;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeFagområde;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeKlassifik;

class Oppdragslinje150Verktøy {

    private Oppdragslinje150Verktøy() {
    }

    static List<Oppdragslinje150> hentOppdr150ForFeriepengerMedKlassekode(List<Oppdragslinje150> tidligereOpp150List, String kodeKlassifik) {
        return tidligereOpp150List.stream().filter(oppdragslinje150 -> oppdragslinje150.getKodeKlassifik().equals(kodeKlassifik)).collect(Collectors.toList());
    }

    static List<Oppdragslinje150> hentTidligereOppdragslinje150(Oppdragskontroll forrigeOppdrag, boolean erBrukerMottaker, boolean medFeriepenger) {
        String økonomiKodeFagområde = erBrukerMottaker ? ØkonomiKodeFagområde.FP.name() : ØkonomiKodeFagområde.FPREF.name();
        String økonomiKodeKlassifik = erBrukerMottaker ? ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik() : ØkonomiKodeKlassifik.FPREFAGFER_IOP.getKodeKlassifik();
        List<Oppdragslinje150> alleOppdr150Liste = forrigeOppdrag.getOppdrag110Liste().stream().filter(oppdrag110 -> oppdrag110.getKodeFagomrade().equals(økonomiKodeFagområde))
            .flatMap(oppdrag110 -> oppdrag110.getOppdragslinje150Liste().stream()).collect(Collectors.toList());
        if (medFeriepenger) {
            return alleOppdr150Liste;
        }
        return alleOppdr150Liste.stream().filter(oppdragslinje150 -> !oppdragslinje150.getKodeKlassifik().equals(økonomiKodeKlassifik)).collect(Collectors.toList());
    }

    static long finnMaxDelytelseIdForOpp110(Oppdrag110 oppdrag110, Oppdragslinje150 sisteOppdr150) {
        List<Oppdragslinje150> opp150UtenFeriepengerListe = getOppdragslinje150UtenFeriepenger(oppdrag110);
        Optional<Oppdragslinje150> sisteOpprettetOpp150ForDenneOpp110 = opp150UtenFeriepengerListe.stream().max(Comparator.comparing(Oppdragslinje150::getDelytelseId));
        return sisteOpprettetOpp150ForDenneOpp110.map(Oppdragslinje150::getDelytelseId).orElseGet(sisteOppdr150::getDelytelseId);
    }

    static List<Oppdragslinje150> getOppdragslinje150UtenFeriepenger(Oppdrag110 oppdrag110) {
        String kodeKlassifikFeriepenger = oppdrag110.getKodeFagomrade().equals(ØkonomiKodeFagområde.FP.name()) ? ØkonomiKodeKlassifik.FPATFER.getKodeKlassifik() : ØkonomiKodeKlassifik.FPREFAGFER_IOP.getKodeKlassifik();
        return oppdrag110.getOppdragslinje150Liste().stream().filter(o150 -> !o150.getKodeKlassifik().equals(kodeKlassifikFeriepenger)).collect(Collectors.toList());
    }

    static Optional<Oppdragslinje150> finnSisteLinjeIKjedeForBruker(Oppdragskontroll forrigeOppdrag) {
        List<Oppdragslinje150> tidligereOpp150linjerListe = Oppdragslinje150Verktøy.hentTidligereOppdragslinje150(forrigeOppdrag, true, false);
        return tidligereOpp150linjerListe.stream()
            .filter(oppdragslinje150 -> oppdragslinje150.getKodeStatusLinje() == null)
            .max(Comparator.comparing(Oppdragslinje150::getDatoVedtakFom));
    }

    static List<Oppdragslinje150> finnSisteLinjeIKjedeForArbeidsgivere(Oppdragskontroll forrigeOppdrag) {
        List<Oppdragslinje150> oppdr150ForArbeidsgivereList = forrigeOppdrag.getOppdrag110Liste().stream()
            .filter(oppdrag110 -> oppdrag110.getKodeFagomrade().equals(ØkonomiKodeFagområde.FPREF.name()))
            .map(oppdrag110 -> {
                Oppdragslinje150 oppdr150 = oppdrag110.getOppdragslinje150Liste()
                    .stream()
                    .filter(oppdragslinje150 -> !oppdragslinje150.getKodeKlassifik().equals(ØkonomiKodeKlassifik.FPREFAGFER_IOP.getKodeKlassifik()))
                    .filter(oppdragslinje150 -> oppdragslinje150.getKodeStatusLinje() == null)
                    .max(Comparator.comparing(Oppdragslinje150::getDatoVedtakFom))
                    .orElseThrow(() -> new IllegalStateException("Utvikler feil: Mangler delytelseId"));

                return oppdr150;
            })
            .collect(Collectors.toList());
        return oppdr150ForArbeidsgivereList;
    }
}

