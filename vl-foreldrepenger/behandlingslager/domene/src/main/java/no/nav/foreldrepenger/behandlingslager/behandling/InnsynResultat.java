package no.nav.foreldrepenger.behandlingslager.behandling;

import java.time.LocalDate;
import java.util.Collection;

public interface InnsynResultat<T extends InnsynDokument> {
    LocalDate getMottattDato();

    InnsynResultatType getInnsynResultatType();

    String getBegrunnelse();

    Collection<T> getInnsynDokumenter();
}
