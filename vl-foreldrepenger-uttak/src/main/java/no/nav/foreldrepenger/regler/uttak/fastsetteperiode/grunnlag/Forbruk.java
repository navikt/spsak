package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class Forbruk {
    private Map<AktivitetIdentifikator, ForbrukForAktivitet> saldoer;

    public Forbruk(Map<AktivitetIdentifikator, ForbrukForAktivitet> saldoer) {
        this.saldoer = saldoer;
    }

    public static Forbruk zero(List<AktivitetIdentifikator> aktivitetIdentifikatorer) {
        Map<AktivitetIdentifikator, ForbrukForAktivitet> saldoer = new HashMap<>();
        for (AktivitetIdentifikator aktivitet : aktivitetIdentifikatorer) {
            saldoer.put(aktivitet, ForbrukForAktivitet.zero());
        }
        return new Forbruk(saldoer);
    }

    public void registrerForbruk(AktivitetIdentifikator aktivitetIdentifikator, Stønadskontotype konto, int forbruk) {
        ForbrukForAktivitet aktuelleSaldoer = this.saldoer.get(aktivitetIdentifikator);
        aktuelleSaldoer.registrerForbruk(konto, forbruk);
    }

    public int getForbruk(AktivitetIdentifikator aktivitetIdentifikator, Stønadskontotype konto) {
        ForbrukForAktivitet aktuelleSaldoer = this.saldoer.get(aktivitetIdentifikator);
        return aktuelleSaldoer.getForbruk(konto);
    }

    public int getMinsteForbruk(Stønadskontotype stønadskontotype) {
        return saldoer.values().stream()
                .map(s -> s.getForbruk(stønadskontotype))
                .min(Integer::compareTo)
                .orElse(0);
    }

    public int getHøyesteForbruk(Stønadskontotype stønadskontotype) {
        return saldoer.values().stream()
                .map(s -> s.getForbruk(stønadskontotype))
                .max(Integer::compareTo)
                .orElse(0);
    }


}
