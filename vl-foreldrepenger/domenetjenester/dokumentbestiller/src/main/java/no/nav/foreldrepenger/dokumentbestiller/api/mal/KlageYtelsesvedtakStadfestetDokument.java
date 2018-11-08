package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.avklarFritekst;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettFlettefelt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;

public class KlageYtelsesvedtakStadfestetDokument implements DokumentType {
    private BrevParametere brevParametere;
    private String fritekst;

    public KlageYtelsesvedtakStadfestetDokument(BrevParametere brevParametere, String fritekst) {
        super();
        this.brevParametere = brevParametere;
        this.fritekst = fritekst;
    }

    @Override
    public String getDokumentMalType() {
        return DokumentMalType.KLAGE_YTELSESVEDTAK_STADFESTET_DOK;
    }

    @Override
    public List<Flettefelt> getFlettefelter(DokumentTypeDto dto) {
        List<Flettefelt> flettefelter = new ArrayList<>();

        flettefelter.add(opprettFlettefelt(Flettefelt.YTELSE_TYPE, dto.getYtelsesTypeKode()));
        Optional<String> faktiskFritekst = avklarFritekst(fritekst, dto.getDokumentBehandlingsresultatDto().getFritekst());
        faktiskFritekst.ifPresent(s -> flettefelter.add(opprettFlettefelt(Flettefelt.FRITEKST, s)));
        flettefelter.add(opprettFlettefelt(Flettefelt.KLAGE_FRIST_UKER, brevParametere.getKlagefristUker().toString()));
        return flettefelter;
    }
}
