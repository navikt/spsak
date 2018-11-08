package no.nav.foreldrepenger.dokumentbestiller.brev;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentData;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalRestriksjon;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentDataTjeneste;

public class SjekkDokumentTilgjengelig {

    private DokumentDataTjeneste dokumentDataTjeneste;

    public SjekkDokumentTilgjengelig(DokumentDataTjeneste dokumentDataTjeneste) {
        this.dokumentDataTjeneste = dokumentDataTjeneste;
    }
    
    public boolean sjekkOmTilgjengelig(Behandling behandling, DokumentMalType mal) {
        DokumentMalRestriksjon restriksjon = mal.getDokumentMalRestriksjon();
        if (DokumentMalRestriksjon.ÅPEN_BEHANDLING.equals(restriksjon)) {
            return !behandling.erSaksbehandlingAvsluttet() && !behandling.erAvsluttet();
        }
        if (DokumentMalRestriksjon.ÅPEN_BEHANDLING_IKKE_SENDT.equals(restriksjon)) {
            return !(behandling.erSaksbehandlingAvsluttet() || behandling.erAvsluttet() || erDokumentProdusert(behandling.getId(), mal.getKode()));
        }
        return true;
    }

    public boolean erDokumentProdusert(Long behandlingId, String dokumentMalTypeKode) {
        DokumentMalType dokumentMalType = dokumentDataTjeneste.hentDokumentMalType(dokumentMalTypeKode);
        List<DokumentData> dokumentDatas = dokumentDataTjeneste.hentDokumentDataListe(behandlingId, dokumentMalType.getKode());
        Optional<DokumentData> dokumentData = dokumentDatas.stream().filter(dd -> dd.getBestiltTid() != null).findFirst();
        return dokumentData.isPresent();
    }
}
