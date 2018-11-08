package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;

public interface DokumentType {

    String DEFAULT_PERSON_STATUS = "ANNET";

    String getDokumentMalType();

    List<Flettefelt> getFlettefelter(DokumentTypeDto dokumentTypeDto);

    default String getPersonstatusVerdi(@SuppressWarnings("unused") PersonstatusType personstatus) {
        return DEFAULT_PERSON_STATUS;
    }

    default boolean harPerioder() {
        return false;
    }
}
