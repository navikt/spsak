package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettFlettefelt;

import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.InnsynResultatType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;

public class InnsynskravSvarDokument implements DokumentType {
    private InnsynResultatType innsynResultatType;
    private String fritekst;
    private BrevParametere brevParametere;

    public InnsynskravSvarDokument(BrevParametere brevParametere, String fritekst, InnsynResultatType innsynResultatType) {
        this.innsynResultatType = innsynResultatType;
        this.fritekst = fritekst;
        this.brevParametere = brevParametere;
    }

    @Override
    public String getDokumentMalType() {
        return DokumentMalType.INNSYNSKRAV_SVAR;
    }

    @Override
    public List<Flettefelt> getFlettefelter(DokumentTypeDto dto) {
        List<Flettefelt> flettefelter = new ArrayList<>();

        flettefelter.add(opprettFlettefelt(Flettefelt.YTELSE_TYPE, dto.getYtelsesTypeKode()));
        flettefelter.add(opprettFlettefelt(Flettefelt.INNSYN_RESULTAT_TYPE, innsynResultatType.getKode()));
        flettefelter.add(opprettFlettefelt(Flettefelt.FRITEKST, fritekst));
        flettefelter.add(opprettFlettefelt(Flettefelt.KLAGE_FRIST_UKER, String.valueOf(brevParametere.getKlagefristUkerInnsyn())));
        return flettefelter;
    }
}
