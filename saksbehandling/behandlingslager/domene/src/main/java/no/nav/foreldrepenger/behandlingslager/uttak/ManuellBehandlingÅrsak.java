package no.nav.foreldrepenger.behandlingslager.uttak;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "ManuellBehandlingÅrsak")
@DiscriminatorValue(ManuellBehandlingÅrsak.DISCRIMINATOR)
public class ManuellBehandlingÅrsak extends Kodeliste {
    public static final String DISCRIMINATOR = "MANUELL_BEHANDLING_AARSAK";

    //TODO SOMMERFUGL rydde opp så navn matcher hva som er i databasen
    public static final ManuellBehandlingÅrsak UKJENT = new ManuellBehandlingÅrsak("-");
    public static final ManuellBehandlingÅrsak INGEN_DISPONIBLE_DAGER_IGJEN_PÅ_KVOTE = new ManuellBehandlingÅrsak("5002");
    public static final ManuellBehandlingÅrsak SØKER_HAR_IKKE_OMSORG = new ManuellBehandlingÅrsak("5003");
    public static final ManuellBehandlingÅrsak SØKER_HAR_IKKE_SØKT_OM_PERIODE_OG_PERIODE_ER_FØR_GYLDIG_DATO = new ManuellBehandlingÅrsak("5005");
    public static final ManuellBehandlingÅrsak AVKLAR_ARBEID = new ManuellBehandlingÅrsak("5006");
    public static final ManuellBehandlingÅrsak SØKNADSFRIST = new ManuellBehandlingÅrsak("5010");
    public static final ManuellBehandlingÅrsak IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE = new ManuellBehandlingÅrsak("5011");
    public static final ManuellBehandlingÅrsak PERIODE_UAVKLART = new ManuellBehandlingÅrsak("5012");
    public static final ManuellBehandlingÅrsak IKKE_SAMTYKKE = new ManuellBehandlingÅrsak("5013");
    public static final ManuellBehandlingÅrsak VURDER_SAMTIDIG_UTTAK = new ManuellBehandlingÅrsak("5014");
    public static final ManuellBehandlingÅrsak FPFF_FOR_TIDLIG_ELLER_SENT = new ManuellBehandlingÅrsak("5015");
    public static final ManuellBehandlingÅrsak VURDER_OVERFØRING = new ManuellBehandlingÅrsak("5016");
    public static final ManuellBehandlingÅrsak FAR_ELLER_MEDMOR_SØKT_FPFF = new ManuellBehandlingÅrsak("5017");
    public static final ManuellBehandlingÅrsak SØKT_FOR_SENT = new ManuellBehandlingÅrsak("5021");
    public static final ManuellBehandlingÅrsak ADOPSJON_IKKE_IMPLEMENTERT = new ManuellBehandlingÅrsak("5098");
    public static final ManuellBehandlingÅrsak FORELDREPENGER_IKKE_IMPLEMENTERT = new ManuellBehandlingÅrsak("5099");


    ManuellBehandlingÅrsak(String kode) {
        super(kode, DISCRIMINATOR);
    }

    ManuellBehandlingÅrsak() {
        // For hibernate
    }
}
