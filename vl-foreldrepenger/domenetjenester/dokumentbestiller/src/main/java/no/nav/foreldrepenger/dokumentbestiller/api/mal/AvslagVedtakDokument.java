package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.avklarFritekst;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettFlettefelt;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettObligatoriskeFlettefelt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;

public class AvslagVedtakDokument implements DokumentType {
    private BrevParametere brevParametere;
    private String fritekst;

    public AvslagVedtakDokument(BrevParametere brevParametere, String fritekst) {
        this.brevParametere = brevParametere;
        this.fritekst = fritekst;
    }

    @Override
    public String getDokumentMalType() {
        return DokumentMalType.AVSLAGSVEDTAK_DOK;
    }

    @Override
    public List<Flettefelt> getFlettefelter(DokumentTypeDto dto) {
        List<Flettefelt> flettefelter = new ArrayList<>();

        flettefelter.add(opprettFlettefelt(Flettefelt.BEHANDLINGSTYPE, dto.getBehandlingsTypeAvslagVedtak()));
        flettefelter.add(opprettFlettefelt(Flettefelt.RELASJONSKODE, dto.getRelasjonsKode()));
        flettefelter.add(opprettFlettefelt(Flettefelt.GJELDER_FØDSEL, Boolean.toString(dto.getGjelderFødsel())));
        flettefelter.add(opprettFlettefelt(Flettefelt.ANTALL_BARN, Integer.toString(dto.getAntallBarn())));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.FØDSELSDATO_PASSERT, dto.getBarnErFødt()));
        flettefelter.add(opprettFlettefelt(Flettefelt.AVSLAGSAARSAK, dto.getDokumentBehandlingsresultatDto().getAvslagsårsak()));
        Optional<String> faktiskFritekst = avklarFritekst(fritekst, dto.getDokumentBehandlingsresultatDto().getFritekst());
        faktiskFritekst.ifPresent(fritekstEn -> flettefelter.add(opprettFlettefelt(Flettefelt.FRITEKST, fritekstEn)));
        flettefelter.add(opprettFlettefelt(Flettefelt.KLAGE_FRIST_UKER, brevParametere.getKlagefristUker().toString()));
        flettefelter.add(opprettFlettefelt(Flettefelt.VILKÅR_TYPE, dto.getDokumentBehandlingsresultatDto().getVilkårTypeKode()));
        flettefelter.add(opprettFlettefelt(Flettefelt.KJØNN, dto.getBrukerKjønnKode()));

        return flettefelter;
    }
}
