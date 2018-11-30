package no.nav.foreldrepenger.domene.mottak.ytelse;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.hendelser.Forretningshendelse;
import no.nav.foreldrepenger.behandlingslager.hendelser.ForretningshendelseType;
import no.nav.foreldrepenger.domene.typer.AktørId;

public class YtelseForretningshendelse extends Forretningshendelse {
    private AktørId aktørId;
    private LocalDate fom;

    public YtelseForretningshendelse(ForretningshendelseType forretningshendelseType) {
        super(forretningshendelseType);
    }

    public YtelseForretningshendelse(ForretningshendelseType forretningshendelseType, String aktørId, LocalDate fom) {
        super(forretningshendelseType);
        this.aktørId = new AktørId(aktørId);
        this.fom = fom;

    }

    public AktørId getAktørId() {
        return aktørId;
    }

    public LocalDate getFom() {
        return fom;
    }
}
