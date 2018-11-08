package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.UttakDokumentasjonType;

public class ManueltDokumentertPeriode extends Periode {
    private UttakDokumentasjonType type;

    public ManueltDokumentertPeriode(LocalDate fom, LocalDate tom, UttakDokumentasjonType type) {
        super(fom, tom);
        this.type = type;
    }

    public UttakDokumentasjonType getType() {
        return type;
    }
}
