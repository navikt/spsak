package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettObligatoriskeFlettefelt;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettObligatoriskeStrukturertFlettefelt;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt.BRØDTEKST;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt.HOVED_OVERSKRIFT;

import java.util.ArrayList;
import java.util.List;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;
import no.nav.vedtak.util.StringUtils;

public class FritekstVedtakDokument implements DokumentType {
    private String overskrift;
    private String brødtekst;

    public FritekstVedtakDokument() {

    }

    public FritekstVedtakDokument(String overskrift, String brødtekst) {
        this.overskrift = overskrift;
        this.brødtekst = brødtekst;
    }

    @Override
    public String getDokumentMalType() {
        return DokumentMalType.FRITEKST_DOK;
    }

    @Override
    public List<Flettefelt> getFlettefelter(DokumentTypeDto dto) {
        List<Flettefelt> flettefelter = new ArrayList<>();
        flettefelter.add(opprettObligatoriskeFlettefelt(HOVED_OVERSKRIFT, !StringUtils.nullOrEmpty(overskrift) ? overskrift : dto.getDokumentBehandlingsresultatDto().getOverskrift()));
        flettefelter.add(opprettObligatoriskeStrukturertFlettefelt(BRØDTEKST, !StringUtils.nullOrEmpty(brødtekst) ? brødtekst : dto.getDokumentBehandlingsresultatDto().getBrødtekst()));
        return flettefelter;
    }
}
