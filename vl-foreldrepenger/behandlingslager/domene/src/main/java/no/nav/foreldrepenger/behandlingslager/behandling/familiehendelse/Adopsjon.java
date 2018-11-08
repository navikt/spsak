package no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse;

import java.time.LocalDate;

public interface Adopsjon {

    LocalDate getOmsorgsovertakelseDato();

    LocalDate getAnkomstNorgeDato();

    LocalDate getForeldreansvarDato();

    Boolean getErEktefellesBarn();

    Boolean getAdoptererAlene();

    OmsorgsovertakelseVilkårType getOmsorgovertakelseVilkår();

}
