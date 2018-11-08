package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettFlettefelt;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;

public class ForlengetDokument implements DokumentType {
    private String dokumentMalType;
    private boolean variantForMedlemskap;
    private boolean variantForTidligSøknad;

    public ForlengetDokument(String dokumentMalType) {
        Objects.requireNonNull(dokumentMalType, "DokumentMalType kan ikke være null");
        this.dokumentMalType = dokumentMalType;
        this.variantForMedlemskap = DokumentMalType.FORLENGET_MEDL_DOK.equals(dokumentMalType);
        this.variantForTidligSøknad = DokumentMalType.FORLENGET_TIDLIG_SOK.equals(dokumentMalType);
    }

    @Override
    public String getDokumentMalType() {
        return dokumentMalType;
    }

    @Override
    public List<Flettefelt> getFlettefelter(DokumentTypeDto dto) {

        List<Flettefelt> flettefelter = new ArrayList<>();
        flettefelter.add(opprettFlettefelt(Flettefelt.YTELSE_TYPE, dto.getYtelsesTypeKode()));
        flettefelter.add(opprettFlettefelt(Flettefelt.SOKNAD_DATO, dto.getMottattDato().toString()));
        if (variantForMedlemskap) {
            flettefelter.add(opprettFlettefelt(Flettefelt.FORLENGET_BEHANDLINGSFRIST, "false"));
        } else {
            flettefelter.add(opprettFlettefelt(Flettefelt.BEHANDLINGSFRIST_UKER, Integer.toString(dto.getBehandlingsfristIUker())));
            flettefelter.add(opprettFlettefelt(Flettefelt.FORLENGET_BEHANDLINGSFRIST, "true"));
        }
        flettefelter.add(opprettFlettefelt(Flettefelt.ANTALL_BARN, Integer.toString(dto.getAntallBarn())));
        return flettefelter;
    }
}
