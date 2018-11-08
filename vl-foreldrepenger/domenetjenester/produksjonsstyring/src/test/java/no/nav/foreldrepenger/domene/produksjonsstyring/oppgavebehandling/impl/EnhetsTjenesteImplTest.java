package no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.Familierelasjon;
import no.nav.foreldrepenger.behandlingslager.aktør.GeografiskTilknytning;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.arbeidsfordeling.ArbeidsfordelingTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.EnhetsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

public class EnhetsTjenesteImplTest {


    private static AktørId MOR_AKTØR_ID = new AktørId(444L);
    private static PersonIdent MOR_IDENT = new PersonIdent("12128965432");
    private static Personinfo MOR_PINFO;

    private static AktørId FAR_AKTØR_ID = new AktørId(555L);
    private static PersonIdent FAR_IDENT = new PersonIdent("11119164523");
    private static Personinfo FAR_PINFO;

    private static AktørId BARN_AKTØR_ID = new AktørId(333L);
    private static AktørId ELDRE_BARN_AKTØR_ID = new AktørId(222L);
    private static PersonIdent BARN_IDENT = new PersonIdent("03031855655");
    private static PersonIdent ELDRE_BARN_IDENT = new PersonIdent("06060633333");
    private static Personinfo BARN_PINFO;
    private static Personinfo ELDRE_BARN_PINFO;
    private static LocalDate ELDRE_BARN_FØDT = LocalDate.of(2006,6,6);
    private static LocalDate BARN_FØDT = LocalDate.of(2018,3,3);

    private static Familierelasjon relasjontilEldreBarn = new Familierelasjon(ELDRE_BARN_IDENT, RelasjonsRolleType.BARN, ELDRE_BARN_FØDT, "Vei", true);
    private static Familierelasjon relasjontilBarn = new Familierelasjon(BARN_IDENT, RelasjonsRolleType.BARN, BARN_FØDT, "Vei", true);
    private static Familierelasjon relasjonEkteFar = new Familierelasjon(FAR_IDENT, RelasjonsRolleType.EKTE, LocalDate.of(1991,11,11), "Vei", true);

    private static OrganisasjonsEnhet enhetNormal = new OrganisasjonsEnhet("4802", "NAV Bærum", "AKTIV");
    private static OrganisasjonsEnhet enhetKode6 = new OrganisasjonsEnhet("2103", "NAV Viken", "AKTIV");

    private static GeografiskTilknytning tilknytningNormal = new GeografiskTilknytning("0219", null);
    private static GeografiskTilknytning tilknytningKode6 = new GeografiskTilknytning("0219", "SPSF");
    private static GeografiskTilknytning relatertKode6 = new GeografiskTilknytning(null, "SPSF");

    private TpsTjeneste tpsTjeneste;
    private ArbeidsfordelingTjeneste arbeidsfordelingTjeneste;
    private EnhetsTjeneste enhetsTjeneste;


    @Before
    public void oppsett() {
        tpsTjeneste = mock(TpsTjeneste.class);
        arbeidsfordelingTjeneste = mock(ArbeidsfordelingTjeneste.class);
        enhetsTjeneste = new EnhetsTjenesteImpl(tpsTjeneste, arbeidsfordelingTjeneste);
    }

    @Test
    public void finn_enhet_utvidet_normal_fordeling() {
        // Oppsett
        settOppTpsStrukturer(false, false, false, true);

        OrganisasjonsEnhet enhet = enhetsTjeneste.hentEnhetSjekkRegistrerteRelasjoner(MOR_AKTØR_ID, BehandlingTema.ENGANGSSTØNAD);

        assertThat(enhet).isNotNull();
        assertThat(enhet).isEqualTo(enhetNormal);
    }

    @Test
    public void finn_enhet_utvidet_bruker_kode_fordeling() {
        // Oppsett
        settOppTpsStrukturer(true, false, false, true);

        OrganisasjonsEnhet enhet = enhetsTjeneste.hentEnhetSjekkRegistrerteRelasjoner(MOR_AKTØR_ID, BehandlingTema.ENGANGSSTØNAD);

        assertThat(enhet).isNotNull();
        assertThat(enhet).isEqualTo(enhetKode6);
    }

    @Test
    public void finn_enhet_utvidet_barn_kode_fordeling() {
        // Oppsett
        settOppTpsStrukturer(false, true, false, true);

        OrganisasjonsEnhet enhet = enhetsTjeneste.hentEnhetSjekkRegistrerteRelasjoner(MOR_AKTØR_ID, BehandlingTema.ENGANGSSTØNAD);

        assertThat(enhet).isNotNull();
        assertThat(enhet).isEqualTo(enhetKode6);
    }

    @Test
    public void finn_enhet_utvidet_ektefelle_kode_fordeling() {
        // Oppsett
        settOppTpsStrukturer(false, false, true, true);

        OrganisasjonsEnhet enhet = enhetsTjeneste.hentEnhetSjekkRegistrerteRelasjoner(MOR_AKTØR_ID, BehandlingTema.ENGANGSSTØNAD);

        assertThat(enhet).isNotNull();
        assertThat(enhet).isEqualTo(enhetKode6);
    }

    @Test
    public void oppdater_enhet_annenpart_kode_fordeling() {
        // Oppsett
        settOppTpsStrukturer(false, false, true, false);

        Optional<OrganisasjonsEnhet> enhet = enhetsTjeneste.oppdaterEnhetSjekkOppgitte(enhetNormal.getEnhetId(), BehandlingTema.ENGANGSSTØNAD, Arrays.asList(FAR_AKTØR_ID));

        assertThat(enhet).isPresent();
        assertThat(enhet).hasValueSatisfying(enhetObj -> assertThat(enhetObj).isEqualTo(enhetKode6));
    }

    @Test
    public void presendens_enhet() {
        // Oppsett
        settOppTpsStrukturer(false, false, false, false);

        OrganisasjonsEnhet enhet = enhetsTjeneste.enhetsPresedens(enhetNormal, enhetKode6, false);

        assertThat(enhet).isEqualTo(enhetKode6);
    }

    @Test
    public void oppdater_enhet_mor_annenpart_kode_fordeling() {
        // Oppsett
        settOppTpsStrukturer(true, false, true, false);

        Optional<OrganisasjonsEnhet> enhet = enhetsTjeneste.oppdaterEnhetSjekkOppgitte(enhetKode6.getEnhetId(), BehandlingTema.ENGANGSSTØNAD, Arrays.asList(FAR_AKTØR_ID));

        assertThat(enhet).isNotPresent();
    }

    @Test
    public void oppdater_etter_vent_barn_fått_kode6() {
        // Oppsett
        settOppTpsStrukturer(false, true, false, true);

        Optional<OrganisasjonsEnhet> enhet = enhetsTjeneste.oppdaterEnhetSjekkRegistrerteRelasjoner(enhetNormal.getEnhetId(), BehandlingTema.ENGANGSSTØNAD, MOR_AKTØR_ID, Optional.of(FAR_AKTØR_ID), Collections.emptyList());

        assertThat(enhet).isPresent();
        assertThat(enhet).hasValueSatisfying(enhetObj -> assertThat(enhetObj).isEqualTo(enhetKode6));
    }

    @Test
    public void oppdater_etter_vent_far_fått_kode6() {
        // Oppsett
        settOppTpsStrukturer(false, false, true, false);

        Optional<OrganisasjonsEnhet> enhet = enhetsTjeneste.oppdaterEnhetSjekkRegistrerteRelasjoner(enhetNormal.getEnhetId(), BehandlingTema.ENGANGSSTØNAD, MOR_AKTØR_ID, Optional.of(FAR_AKTØR_ID), Collections.emptyList());

        assertThat(enhet).isPresent();
        assertThat(enhet).hasValueSatisfying(enhetObj -> assertThat(enhetObj).isEqualTo(enhetKode6));
    }

    private void settOppTpsStrukturer(boolean morKode6, boolean barnKode6, boolean annenPartKode6, boolean foreldreRelatertTps) {
        HashSet<Familierelasjon> relasjoner = new HashSet<>(Arrays.asList(relasjontilEldreBarn, relasjontilBarn));
        if (foreldreRelatertTps) {
            relasjoner.add(relasjonEkteFar);
        }
        MOR_PINFO = new Personinfo.Builder().medAktørId(MOR_AKTØR_ID).medPersonIdent(MOR_IDENT).medNavn("Kari Dunk")
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE).medFødselsdato(LocalDate.of(1989,12,12)).medAdresse("Vei")
            .medFamilierelasjon(relasjoner).build();
        FAR_PINFO = new Personinfo.Builder().medAktørId(FAR_AKTØR_ID).medPersonIdent(FAR_IDENT).medNavn("Ola Dunk")
            .medNavBrukerKjønn(NavBrukerKjønn.MANN).medFødselsdato(LocalDate.of(1991,11,11)).medAdresse("Vei").build();
        ELDRE_BARN_PINFO = new Personinfo.Builder().medAktørId(ELDRE_BARN_AKTØR_ID).medPersonIdent(ELDRE_BARN_IDENT).medFødselsdato(ELDRE_BARN_FØDT)
            .medNavBrukerKjønn(NavBrukerKjønn.MANN).medNavn("Dunk junior d.e.").medAdresse("Vei").build();
        BARN_PINFO = new Personinfo.Builder().medAktørId(BARN_AKTØR_ID).medPersonIdent(BARN_IDENT).medFødselsdato(BARN_FØDT)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE).medNavn("Dunk junior d.y.").medAdresse("Vei").build();

        when(tpsTjeneste.hentFnrForAktør(MOR_AKTØR_ID)).thenReturn(MOR_IDENT);
        when(tpsTjeneste.hentFnrForAktør(FAR_AKTØR_ID)).thenReturn(FAR_IDENT);
        when(tpsTjeneste.hentFnrForAktør(BARN_AKTØR_ID)).thenReturn(BARN_IDENT);
        when(tpsTjeneste.hentFnrForAktør(ELDRE_BARN_AKTØR_ID)).thenReturn(ELDRE_BARN_IDENT);

        when(tpsTjeneste.hentBrukerForAktør(MOR_AKTØR_ID)).thenReturn(Optional.of(MOR_PINFO));
        when(tpsTjeneste.hentBrukerForAktør(FAR_AKTØR_ID)).thenReturn(Optional.of(FAR_PINFO));
        when(tpsTjeneste.hentBrukerForAktør(BARN_AKTØR_ID)).thenReturn(Optional.of(BARN_PINFO));
        when(tpsTjeneste.hentBrukerForAktør(ELDRE_BARN_AKTØR_ID)).thenReturn(Optional.of(ELDRE_BARN_PINFO));

        when(tpsTjeneste.hentGeografiskTilknytning(MOR_IDENT)).thenReturn(morKode6 ? tilknytningKode6 : tilknytningNormal);
        when(tpsTjeneste.hentGeografiskTilknytning(FAR_IDENT)).thenReturn(annenPartKode6 ? tilknytningKode6 : tilknytningNormal);
        when(tpsTjeneste.hentGeografiskTilknytning(ELDRE_BARN_IDENT)).thenReturn(barnKode6 ? tilknytningKode6 : tilknytningNormal);
        when(tpsTjeneste.hentGeografiskTilknytning(BARN_IDENT)).thenReturn(morKode6 ? tilknytningKode6 : tilknytningNormal);

        when(arbeidsfordelingTjeneste.finnBehandlendeEnhet(any(),isNull(), any())).thenReturn(enhetNormal);
        when(arbeidsfordelingTjeneste.finnBehandlendeEnhet(any(), matches("SPSF"), any())).thenReturn(enhetKode6);
        when(arbeidsfordelingTjeneste.hentEnhetForDiskresjonskode(matches("SPSF"), any())).thenReturn(enhetKode6);

        when(tpsTjeneste.hentDiskresjonskoderForFamilierelasjoner(MOR_IDENT))
            .thenReturn(barnKode6 || (annenPartKode6 && foreldreRelatertTps) ? Collections.singletonList(relatertKode6): Collections.emptyList());
        when(tpsTjeneste.hentDiskresjonskoderForFamilierelasjoner(FAR_IDENT)).thenReturn(annenPartKode6 ? Collections.singletonList(relatertKode6): Collections.emptyList());
    }
}
