package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.avklarFritekst;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.getSvarFrist;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettFlettefelt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;

public class KlageYtelsesvedtakOpphevetDokument implements DokumentType {
    private BrevParametere brevParametere;
    private String fritekst;

    public KlageYtelsesvedtakOpphevetDokument(BrevParametere brevParametere, String fritekst) {
        super();
        this.brevParametere = brevParametere;
        this.fritekst = fritekst;
    }

    @Override
    public String getDokumentMalType() {
        return DokumentMalType.KLAGE_YTELSESVEDTAK_OPPHEVET_DOK;
    }

    @Override
    public List<Flettefelt> getFlettefelter(DokumentTypeDto dto) {
        List<Flettefelt> flettefelter = new ArrayList<>();
        flettefelter.add(opprettFlettefelt(Flettefelt.YTELSE_TYPE, dto.getYtelsesTypeKode()));
        flettefelter.add(opprettFlettefelt(Flettefelt.ANTALL_UKER, Integer.toString(dto.getBehandlingsfristIUker())));
        Optional<String> faktiskFritekst = avklarFritekst(fritekst, dto.getDokumentBehandlingsresultatDto().getFritekst());
        faktiskFritekst.ifPresent(s -> flettefelter.add(opprettFlettefelt(Flettefelt.FRITEKST, s)));
        flettefelter.add(opprettFlettefelt(Flettefelt.FRIST_DATO, getSvarFrist(brevParametere).toString()));
        return flettefelter;
    }
}
