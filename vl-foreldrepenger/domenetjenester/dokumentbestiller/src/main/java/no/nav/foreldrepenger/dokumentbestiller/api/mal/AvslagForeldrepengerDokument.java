package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettFlettefelt;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettObligatoriskeFlettefelt;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettStrukturertFlettefeltListe;

import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeMedPerioderDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.PeriodeDto;
import no.nav.foreldrepenger.dokumentbestiller.doktype.sammenslåperioder.PeriodeMerger;

public class AvslagForeldrepengerDokument implements DokumentType {
    private BrevParametere brevParametere;

    public AvslagForeldrepengerDokument(BrevParametere brevParametere) {
        this.brevParametere = brevParametere;
    }

    @Override
    public String getDokumentMalType() {
        return DokumentMalType.AVSLAG_FORELDREPENGER_DOK;
    }

    @Override
    public boolean harPerioder() {
        return true;
    }

    @Override
    public List<Flettefelt> getFlettefelter(DokumentTypeDto dtoIn) {
        DokumentTypeMedPerioderDto dto = (DokumentTypeMedPerioderDto) dtoIn;
        List<Flettefelt> flettefelter = new ArrayList<>();
        leggTilObligatoriskeFelter(dto, flettefelter);
        leggTilIkkeObligatoriskeFelter(dto, flettefelter);
        leggtilStrukturerteFelter(dto, flettefelter);
        return flettefelter;
    }

    private void leggTilObligatoriskeFelter(DokumentTypeMedPerioderDto dto, List<Flettefelt> flettefelter) {
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.BEHANDLINGSTYPE, dto.getBehandlingsTypeAvslagVedtak()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.SOKERSNAVN, dto.getSøkersNavn()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.PERSON_STATUS, dto.getPersonstatus()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.RELASJONSKODE, dto.getRelasjonsKode()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.MOTTATT_DATO, dto.getMottattDato()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.GJELDER_FØDSEL, dto.getGjelderFødsel()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.ANTALL_BARN, dto.getAntallBarn()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.FØDSELSDATO_PASSERT, dto.getBarnErFødt()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.HALV_G, dto.getHalvG()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.AVSLAGSAARSAK, dto.getDokumentBehandlingsresultatDto().getAvslagsårsak()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.KLAGE_FRIST_UKER, brevParametere.getKlagefristUker()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.LOV_HJEMMEL_FOR_AVSLAG, DokumentMalFelles.formaterLovhjemlerForAvslag((dto))));
    }



    private void leggTilIkkeObligatoriskeFelter(DokumentTypeMedPerioderDto dto, List<Flettefelt> flettefelter) {
        if (dto.getSisteDagIFellesPeriode() != null) {
            flettefelter.add(opprettFlettefelt(Flettefelt.SISTE_DAG_I_FELLES_PERIODE, dto.getSisteDagIFellesPeriode().toString()));
        }
        if (dto.getUkerEtterFellesPeriode() != null) {
            flettefelter.add(opprettFlettefelt(Flettefelt.UKER_ETTER_FELLES_PERIODE, dto.getUkerEtterFellesPeriode().toString()));
        }
    }

    private void leggtilStrukturerteFelter(DokumentTypeMedPerioderDto dto, List<Flettefelt> flettefelter) {
        List<PeriodeDto> periodeDtos = new ArrayList<>(dto.getPeriode());
        periodeDtos = PeriodeMerger.mergePerioder(periodeDtos);
        flettefelter.addAll(opprettStrukturertFlettefeltListe(Flettefelt.PERIODE, periodeDtos));
    }
}
