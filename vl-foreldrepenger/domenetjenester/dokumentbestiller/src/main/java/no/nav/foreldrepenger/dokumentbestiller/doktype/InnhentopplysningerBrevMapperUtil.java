package no.nav.foreldrepenger.dokumentbestiller.doktype;

import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.dokumentbestiller.BrevFeil;
import no.nav.foreldrepenger.integrasjon.dokument.innhentopplysninger.BehandlingsTypeKode;

class InnhentopplysningerBrevMapperUtil {

    private InnhentopplysningerBrevMapperUtil() {
    }

    static BehandlingsTypeKode mapToXmlBehandlingsType(String vlKode) {
        if (Objects.equals(vlKode, BehandlingType.FØRSTEGANGSSØKNAD.getKode())) {
            return BehandlingsTypeKode.FOERSTEGANGSBEHANDLING;
        } else if (Objects.equals(vlKode, BehandlingType.KLAGE.getKode())) {
            return BehandlingsTypeKode.KLAGE;
        } else if (Objects.equals(vlKode, BehandlingType.REVURDERING.getKode())) {
            return BehandlingsTypeKode.REVURDERING;
        }
        throw BrevFeil.FACTORY.innhentDokumentasjonKreverGyldigBehandlingstype(vlKode).toException();
    }
}
