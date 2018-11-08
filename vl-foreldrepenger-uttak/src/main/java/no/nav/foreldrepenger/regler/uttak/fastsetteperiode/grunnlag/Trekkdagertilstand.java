package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class Trekkdagertilstand {

    public enum Part {
        SØKER,
        ANNEN_PART
    }

    private final Map<AktivitetIdentifikator, Map<Stønadskontotype, Integer>> kvoter;

    private final Map<Part, List<AktivitetIdentifikator>> aktiviteter = new EnumMap<>(Part.class);

    private final Map<Part, Forbruk> forbrukteDager = new EnumMap<>(Part.class);

    private List<FastsattPeriodeAnnenPart> uttakPerioderAnnenPart = new ArrayList<>();

    private int annenPartaktuellPeriodeIndex = 0;

    Trekkdagertilstand(Map<AktivitetIdentifikator, Map<Stønadskontotype, Integer>> kvoter,
                       List<AktivitetIdentifikator> søkersAktiviteter,
                       List<AktivitetIdentifikator> annenPartsAktiviteter,
                       List<FastsattPeriodeAnnenPart> uttakPerioderAnnenPart) {
        this.kvoter = kvoter;
        this.uttakPerioderAnnenPart = uttakPerioderAnnenPart;
        aktiviteter.put(Part.SØKER, søkersAktiviteter);
        aktiviteter.put(Part.ANNEN_PART, annenPartsAktiviteter);
        forbrukteDager.put(Part.SØKER, Forbruk.zero(søkersAktiviteter));
        forbrukteDager.put(Part.ANNEN_PART, Forbruk.zero(annenPartsAktiviteter));
    }

    public void registrerForbruk(Part part, UttakPeriode periode) {
        Forbruk forbruk = forbrukteDager.get(part);
        for (AktivitetIdentifikator aktivitet : aktiviteter.get(part)) {
            forbruk.registrerForbruk(aktivitet, periode.getTrekkKonto(), periode.getTrekkdager(aktivitet));
            if (periode.isFlerbarnsdager()) {
                forbruk.registrerForbruk(aktivitet, Stønadskontotype.FLERBARNSDAGER, periode.getTrekkdager(aktivitet));
            }
        }
    }

    public List<FastsattPeriodeAnnenPart> getUttakPerioderAnnenPart() {
        return uttakPerioderAnnenPart;
    }

    public void registrerForbrukAnnenPart(FastsattPeriodeAnnenPart periode, LocalDate knekk) {
        if (!periode.overlapper(knekk)) {
            throw new IllegalArgumentException("knekk " + knekk + " + er utenfor periode " + periode.getFom() + " " + periode.getFom());
        }

        Forbruk forbruk = forbrukteDager.get(Part.ANNEN_PART);
        int virkedagerInnenfor = Virkedager.beregnAntallVirkedager(periode.getFom(), knekk.minusDays(1));
        int virkedagerHele = periode.virkedager();

        for (UttakPeriodeAktivitet uttakPeriodeAktivitet : periode.getUttakPeriodeAktiviteter()) {
            int opprinneligeTrekkdager = uttakPeriodeAktivitet.getTrekkdager();
            if (opprinneligeTrekkdager > 0 && virkedagerInnenfor > 0) {
                int vektetTrekkdager = BigDecimal.valueOf((long) opprinneligeTrekkdager * virkedagerInnenfor)
                        .divide(BigDecimal.valueOf(virkedagerHele), 0, RoundingMode.DOWN)
                        .intValue();
                forbruk.registrerForbruk(uttakPeriodeAktivitet.getAktivitetIdentifikator(), uttakPeriodeAktivitet.getStønadskontotype(), vektetTrekkdager);
                if (periode.isFlerbarnsdager()) {
                    forbruk.registrerForbruk(uttakPeriodeAktivitet.getAktivitetIdentifikator(), Stønadskontotype.FLERBARNSDAGER, vektetTrekkdager);
                }
            }
        }
    }

    public void registrerForbrukAnnenPart(FastsattPeriodeAnnenPart periode) {
        Forbruk forbruk = forbrukteDager.get(Part.ANNEN_PART);
        for (UttakPeriodeAktivitet uttakPeriodeAktivitet : periode.getUttakPeriodeAktiviteter()) {
            if (uttakPeriodeAktivitet.getTrekkdager() > 0) {
                forbruk.registrerForbruk(uttakPeriodeAktivitet.getAktivitetIdentifikator(), uttakPeriodeAktivitet.getStønadskontotype(), uttakPeriodeAktivitet.getTrekkdager());
                if (periode.isFlerbarnsdager()) {
                    forbruk.registrerForbruk(uttakPeriodeAktivitet.getAktivitetIdentifikator(), Stønadskontotype.FLERBARNSDAGER, uttakPeriodeAktivitet.getTrekkdager());
                }
            }
        }
    }

    public boolean harNegativSaldo() {
        for (Stønadskontotype stønadskontotype : Stønadskontotype.values()) {
            for (Map.Entry<AktivitetIdentifikator, Map<Stønadskontotype, Integer>> entry : kvoter.entrySet()) {
                Integer kvote = entry.getValue().getOrDefault(stønadskontotype, 0);

                //ja, det er riktig at vi henter høyeste forbruk for søker, og laveste for annen part .. det er slik regelen er
                int forbrukSøker = forbrukteDager.get(Part.SØKER).getHøyesteForbruk(stønadskontotype);
                int forbrukAnnenPart = forbrukteDager.get(Part.ANNEN_PART).getMinsteForbruk(stønadskontotype);
                if (kvote < forbrukSøker + forbrukAnnenPart) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getSamletForbruk(AktivitetIdentifikator aktivitetIdentifikator, Stønadskontotype stønadskontotype) {
        return forbrukteDager.get(Part.SØKER).getForbruk(aktivitetIdentifikator, stønadskontotype)
                + forbrukteDager.get(Part.ANNEN_PART).getMinsteForbruk(stønadskontotype);
    }

    Set<Stønadskontotype> getStønadskontotyper() {
        Optional<Map<Stønadskontotype, Integer>> first = kvoter.values().stream().findFirst();
        return first.map(Map::keySet).orElseGet(Collections::emptySet);
    }


    public void reduserSaldo(UttakPeriode uttakPeriode) {
        registrerForbruk(Trekkdagertilstand.Part.SØKER, uttakPeriode);
    }



    public void trekkSaldoForAnnenPartsPerioder(UttakPeriode periodeUnderBehandling) {
        LocalDate dato = periodeUnderBehandling.getFom();
        while (annenPartaktuellPeriodeIndex < uttakPerioderAnnenPart.size() && uttakPerioderAnnenPart.get(annenPartaktuellPeriodeIndex).getFom().isBefore(dato)) {
            FastsattPeriodeAnnenPart uttakPeriode = uttakPerioderAnnenPart.get(annenPartaktuellPeriodeIndex);
            if (!periodeUnderBehandling.isSamtidigUttak() && uttakPeriode.overlapper(dato)) {
                registrerForbrukAnnenPart(uttakPeriode, dato);
            } else {
                registrerForbrukAnnenPart(uttakPeriode);
            }
            annenPartaktuellPeriodeIndex++;
        }
    }

    /**
     * Finn saldo for gitt stønadskontotype.
     *
     * @param stønadskontotype stønadskontotypen det skal finnes saldo for.
     * @return saldo for gitt stønadskontotype.
     */
    public int saldo(AktivitetIdentifikator aktivitetIdentifikator, Stønadskontotype stønadskontotype) {
        return getGjenstående(aktivitetIdentifikator, stønadskontotype);
    }

    private int getGjenstående(AktivitetIdentifikator aktivitetIdentifikator, Stønadskontotype stønadskontotype) {
        return kvoter.get(aktivitetIdentifikator).getOrDefault(stønadskontotype, 0) - getSamletForbruk(aktivitetIdentifikator, stønadskontotype);
    }


}
