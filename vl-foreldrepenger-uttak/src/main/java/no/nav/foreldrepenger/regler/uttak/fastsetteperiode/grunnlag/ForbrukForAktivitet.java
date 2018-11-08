package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.EnumMap;
import java.util.Map;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class ForbrukForAktivitet {
    private Map<Stønadskontotype, Integer> saldoer = new EnumMap<>(Stønadskontotype.class);

    public static ForbrukForAktivitet zero() {
        return new ForbrukForAktivitet();
    }

    public void registrerForbruk(Stønadskontotype konto, int forbruk) {
        Integer forrige = saldoer.getOrDefault(konto, 0);
        saldoer.put(konto, forrige + forbruk);
    }

    public int getForbruk(Stønadskontotype konto) {
        return saldoer.getOrDefault(konto, 0);
    }

}
