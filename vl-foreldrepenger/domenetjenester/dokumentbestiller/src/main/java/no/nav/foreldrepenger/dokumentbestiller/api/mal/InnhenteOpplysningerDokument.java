package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.getSvarFrist;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettFlettefelt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.BrevFeil;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;
import no.nav.foreldrepenger.integrasjon.dokument.innhentopplysninger.PersonstatusKode;

public class InnhenteOpplysningerDokument implements DokumentType {
    public static final String FLETTEFELT_SOKERS_NAVN = "søkersNavn";

    private static final Set<BehandlingType> gyldigeKoder = new HashSet<>(Arrays.asList(
        BehandlingType.REVURDERING,
        BehandlingType.KLAGE,
        BehandlingType.FØRSTEGANGSSØKNAD
    ));

    private String fritekst;
    private BehandlingType behandlingType;
    private BrevParametere brevParametere;

    public InnhenteOpplysningerDokument(BrevParametere brevParametere, String fritekst, BehandlingType behandlingType) {
        this.brevParametere = brevParametere;
        this.fritekst = fritekst;
        this.behandlingType = behandlingType;
        if (!gyldigeKoder.contains(behandlingType)) {
            throw BrevFeil.FACTORY.innhentDokumentasjonKreverGyldigBehandlingstype(behandlingType.toString()).toException();
        }
    }

    @Override
    public String getDokumentMalType() {
        return DokumentMalType.INNHENT_DOK;
    }

    @Override
    public List<Flettefelt> getFlettefelter(DokumentTypeDto dto) {
        List<Flettefelt> flettefelter = new ArrayList<>();

        flettefelter.add(opprettFlettefelt(Flettefelt.FRITEKST, fritekst));
        flettefelter.add(opprettFlettefelt(Flettefelt.SØKNAD_DATO, dto.getMottattDato().toString()));
        flettefelter.add(opprettFlettefelt(Flettefelt.FRIST_DATO, getSvarFrist(brevParametere).toString()));
        flettefelter.add(opprettFlettefelt(Flettefelt.YTELSE_TYPE, dto.getYtelsesTypeKode()));
        flettefelter.add(opprettFlettefelt(Flettefelt.BEHANDLINGSTYPE, behandlingType.getKode()));
        flettefelter.add(opprettFlettefelt(Flettefelt.PERSON_STATUS, dto.getPersonstatus()));
        flettefelter.add(opprettFlettefelt(FLETTEFELT_SOKERS_NAVN, dto.getSøkersNavn()));
        return flettefelter;
    }

    @Override
    public String getPersonstatusVerdi(PersonstatusType personstatus) {
        return Objects.equals(personstatus, PersonstatusType.DØD) ? PersonstatusKode.DOD.value() : PersonstatusKode.ANNET.value();
    }

}
