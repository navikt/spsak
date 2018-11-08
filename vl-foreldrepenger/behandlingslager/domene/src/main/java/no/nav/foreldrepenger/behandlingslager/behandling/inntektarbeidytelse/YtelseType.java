package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "YtelseType")
public class YtelseType extends Kodeliste {

    YtelseType(String kode, String discriminator) {
        super(kode, discriminator);
    }

    public YtelseType() {
        //hibernate
    }
}
