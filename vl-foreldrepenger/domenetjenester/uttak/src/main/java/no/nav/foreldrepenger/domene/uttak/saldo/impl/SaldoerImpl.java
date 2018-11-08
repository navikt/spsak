package no.nav.foreldrepenger.domene.uttak.saldo.impl;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.domene.uttak.saldo.Aktivitet;
import no.nav.foreldrepenger.domene.uttak.saldo.Saldoer;

class SaldoerImpl implements Saldoer {

    private Map<StønadskontoType, Map<Aktivitet, Integer>> saldoPerAktivitetForSøker = new HashMap<>();
    private Map<StønadskontoType, Map<Aktivitet, Integer>> saldoPerAktivitetForMotpart = new HashMap<>();
    private Map<StønadskontoType, Integer> maxDagerPerStønadskontotype = new HashMap<>();
    private Optional<LocalDate> maksDatoUttak = Optional.empty();

    @Override
    public int saldo(StønadskontoType stønadskonto, Aktivitet aktivitet) {
        return getMaxDager(stønadskonto) - trekkdager(stønadskonto, aktivitet);
    }

    @Override
    public int saldo(StønadskontoType stønadskonto) {
        Integer saldo = null;
        for (Aktivitet aktivitet : aktiviteterForSøker()) {
            int saldoForAktivitet = saldo(stønadskonto, aktivitet);
            if (saldo == null || saldoForAktivitet > saldo.intValue()) {
                saldo = saldoForAktivitet;
            }
        }
        if (saldo == null) {
            return 0;
        }
        return saldo;
    }

    @Override
    public Set<Aktivitet> aktiviteterForSøker() {
        Set<Aktivitet> aktivitetSet = new HashSet<>();
        saldoPerAktivitetForSøker.values().stream().forEach(map -> aktivitetSet.addAll(map.keySet()));
        return aktivitetSet;
    }

    @Override
    public Set<StønadskontoType> stønadskontoer() {
        return maxDagerPerStønadskontotype.keySet();
    }

    @Override
    public int getMaxDager(StønadskontoType stønadskonto) {
        Integer maxDager = maxDagerPerStønadskontotype.get(stønadskonto);
        return maxDager == null ? 0 : maxDager;
    }

    @Override
    public Optional<LocalDate> getMaksDatoUttak() {
        return maksDatoUttak;
    }

    private int trekkdager(StønadskontoType stønadskonto, Aktivitet aktivitet) {
        Map<Aktivitet, Integer> aktiviteter = saldoPerAktivitetForSøker.get(stønadskonto);
        int trekkdager = 0;
        if (aktiviteter != null) {
            Integer trekkdagerForAktivitet = aktiviteter.get(aktivitet);
            if (trekkdagerForAktivitet != null) {
                trekkdager = trekkdagerForAktivitet;
            }
        }
        return trekkdager + trekkdagerForMotpart(stønadskonto);
    }


    private int trekkdagerForMotpart(StønadskontoType stønadskonto) {
        Map<Aktivitet, Integer> trekkPerAktivitet = saldoPerAktivitetForMotpart.get(stønadskonto);
        if (trekkPerAktivitet != null) {
            return trekkPerAktivitet.values().stream().min(Integer::compare).orElse(0);
        }
        return 0;
    }

    void setMaxDager(StønadskontoType stønadskonto, int maxDager) {
        maxDagerPerStønadskontotype.put(stønadskonto, maxDager);
    }

    void trekkForSøker(StønadskontoType stønadskonto, Aktivitet aktivitet, int trekkdager) {
        trekk(saldoPerAktivitetForSøker, stønadskonto, aktivitet, trekkdager);
    }


    void trekkForAnnenPart(StønadskontoType stønadskonto, Aktivitet aktivitet, int trekkdager) {
        trekk(saldoPerAktivitetForMotpart, stønadskonto, aktivitet, trekkdager);
    }


    void setMaksDatoUttak(Optional<LocalDate> maksDatoUttak) {
        this.maksDatoUttak = maksDatoUttak;
    }

    private static void trekk(Map<StønadskontoType, Map<Aktivitet, Integer>> saldoPerAktivitet,
                              StønadskontoType stønadskonto, Aktivitet aktivitet, int trekkdager) {
        Map<Aktivitet, Integer> aktiviteter = saldoPerAktivitet.get(stønadskonto);
        if (aktiviteter == null) {
            aktiviteter = new HashMap<>();
            saldoPerAktivitet.put(stønadskonto, aktiviteter);
        }
        Integer total = aktiviteter.get(aktivitet);
        if (total == null) {
            aktiviteter.put(aktivitet, trekkdager);
        } else {
            aktiviteter.put(aktivitet, total + trekkdager);
        }
    }

}
