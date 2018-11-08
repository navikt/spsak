package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettFlettefelt;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettObligatoriskeFlettefelt;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;
import no.nav.foreldrepenger.integrasjon.dokument.opphor.PersonstatusKode;

public class OpphørDokument implements DokumentType {
    private BrevParametere brevParametere;

    public OpphørDokument(BrevParametere brevParametere) {
        this.brevParametere = brevParametere;
    }

    @Override
    public String getDokumentMalType() {
        return DokumentMalType.OPPHØR_DOK;
    }

    @Override
    public List<Flettefelt> getFlettefelter(DokumentTypeDto dto) {
        List<Flettefelt> flettefelter = new ArrayList<>();
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.BEHANDLINGSTYPE, dto.getBehandlingsTypeAvslagVedtak()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.SOKERSNAVN, dto.getSøkersNavn()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.PERSON_STATUS, dto.getPersonstatus()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.RELASJONSKODE, dto.getRelasjonsKode()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.GJELDER_FØDSEL, dto.getGjelderFødsel()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.ANTALL_BARN, dto.getAntallBarn()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.FØDSELSDATO_PASSERT, dto.getBarnErFødt()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.HALV_G, dto.getHalvG()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.AVSLAGSAARSAK, dto.getDokumentBehandlingsresultatDto().getAvslagsårsak()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.KLAGE_FRIST_UKER, brevParametere.getKlagefristUker()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.LOV_HJEMMEL_FOR_AVSLAG, DokumentMalFelles.formaterLovhjemler(dto.getDokumentBehandlingsresultatDto().getLovhjemmelForAvslag())));

        //Ikke obligatoriske felter
        dto.getDokumentBeregningsresultatDto().getFørsteStønadsDato().ifPresent(localdate -> flettefelter.add(opprettFlettefelt(Flettefelt.STONADSDATO_FOM, localdate.toString())));
        dto.getDokumentBeregningsresultatDto().getSisteStønadsDato().ifPresent(localdate -> flettefelter.add(opprettFlettefelt(Flettefelt.STONADSDATO_TOM, localdate.toString())));
        dto.getDokumentBeregningsresultatDto().getOpphorDato().ifPresent(localdate -> flettefelter.add(opprettFlettefelt(Flettefelt.OPPHORDATO, localdate.toString())));
        dto.getDokumentBehandlingsresultatDto().getDodsdato().ifPresent(localDate -> flettefelter.add(opprettFlettefelt(Flettefelt.DODSDATO, localDate.toString())));

        return flettefelter;
    }

    @Override
    public String getPersonstatusVerdi(PersonstatusType personstatus) {
        return Objects.equals(personstatus, PersonstatusType.DØD) ? PersonstatusKode.DOD.value() : PersonstatusKode.ANNET.value();
    }
}
