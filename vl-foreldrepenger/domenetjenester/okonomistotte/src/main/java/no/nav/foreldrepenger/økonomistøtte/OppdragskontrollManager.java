package no.nav.foreldrepenger.økonomistøtte;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Attestant180;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Avstemming115;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragsenhet120;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeKomponent;
import no.nav.vedtak.util.FPDateUtil;

public abstract class OppdragskontrollManager {

    private static final String TYPE_ENHET = "BOS";
    private static final String ENHET = "8020";
    private static final LocalDate DATO_ENHET_FOM = LocalDate.of(1900, 1, 1);

    protected OppdragskontrollManager() {
    }

    protected abstract void opprettØkonomiOppdrag(Behandling behandling, Optional<Oppdragskontroll> forrigeOppdrag, Oppdragskontroll oppdragskontroll);

    protected void opprettOppdragsenhet120(Oppdrag110 oppdrag110) {
        Oppdragsenhet120.builder()
            .medTypeEnhet(TYPE_ENHET)
            .medEnhet(ENHET)
            .medDatoEnhetFom(DATO_ENHET_FOM)
            .medOppdrag110(oppdrag110)
            .build();
    }

    protected void opprettAttestant180(Oppdragslinje150 oppdragslinje150, String ansvarligSaksbehandler) {
        Attestant180.builder()
            .medAttestantId(ansvarligSaksbehandler)
            .medOppdragslinje150(oppdragslinje150)
            .build();
    }

    protected void opprettAttestant180(List<Oppdragslinje150> oppdragslinje150, String ansvarligSaksbehandler) {
        for (Oppdragslinje150 oppdrLinje150 : oppdragslinje150) {
            opprettAttestant180(oppdrLinje150, ansvarligSaksbehandler);
        }
    }

    protected Avstemming115 opprettAvstemming115() {
        return Avstemming115.builder()
            .medKodekomponent(ØkonomiKodeKomponent.VLFP.getKodeKomponent())
            .medNokkelAvstemming(LocalDateTime.now(FPDateUtil.getOffset()))
            .medTidspnktMelding(LocalDateTime.now(FPDateUtil.getOffset()))
            .build();
    }

    protected long incrementInitialValue(long initialValue) {
        AtomicLong teller = new AtomicLong(initialValue);
        return teller.incrementAndGet();
    }

    protected long genererFagsystemId(long saksnummer, long initialValue) {
        long verdi = incrementInitialValue(initialValue);
        return concatenateValues(saksnummer, verdi);
    }

    protected long concatenateValues(Number... values) {
        String result = Arrays.stream(values).map(Object::toString).collect(Collectors.joining());

        return Long.parseLong(result);
    }

    protected long settFagsystemId(Fagsak fagsak, long initialLøpenummer, boolean gjelderEndring) {
        if (gjelderEndring) {
            return incrementInitialValue(initialLøpenummer);
        }
        long saksnummer = Long.parseLong(fagsak.getSaksnummer().getVerdi());
        return genererFagsystemId(saksnummer, initialLøpenummer);
    }

    protected String endreTilElleveSiffer(String id) {
        if (id.length() == 11) {
            return id;
        } else {
            return "00" + id;
        }
    }

}
