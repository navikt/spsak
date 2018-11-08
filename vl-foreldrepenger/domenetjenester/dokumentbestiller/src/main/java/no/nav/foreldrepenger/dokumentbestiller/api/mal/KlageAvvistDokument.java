package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettFlettefelt;

import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerFeil;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;

public class KlageAvvistDokument implements DokumentType {
    private BrevParametere brevParametere;

    public KlageAvvistDokument(BrevParametere brevParametere) {
        super();
        this.brevParametere = brevParametere;
    }

    @Override
    public String getDokumentMalType() {
        return DokumentMalType.KLAGE_AVVIST_DOK;
    }

    @Override
    public List<Flettefelt> getFlettefelter(DokumentTypeDto dto) {
        List<Flettefelt> flettefelter = new ArrayList<>();
        flettefelter.add(opprettFlettefelt(Flettefelt.YTELSE_TYPE, dto.getYtelsesTypeKode()));
        if (!dto.getKlageAvvistÅrsakKode().isPresent()) {
            throw DokumentBestillerFeil.FACTORY.behandlingManglerKlageVurderingResultat(dto.getBehandlingId()).toException();
        }
        flettefelter.add(opprettFlettefelt(Flettefelt.AVVIST_GRUNN, dto.getKlageAvvistÅrsakKode().get()));
        flettefelter.add(opprettFlettefelt(Flettefelt.KLAGE_FRIST_UKER, brevParametere.getKlagefristUker().toString()));
        return flettefelter;
    }
}
