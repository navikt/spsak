package no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.GeografiskTilknytning;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.arbeidsfordeling.ArbeidsfordelingTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.EnhetsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

public class EnhetsTjenesteImplTest {


    private static AktørId MOR_AKTØR_ID = new AktørId(444L);
    private static PersonIdent MOR_IDENT = new PersonIdent("12128965432");
    private static Personinfo søkerPersonInfo;

    private static OrganisasjonsEnhet enhetNormal = new OrganisasjonsEnhet("4802", "NAV Bærum", "AKTIV");
    private static OrganisasjonsEnhet enhetKode6 = new OrganisasjonsEnhet("2103", "NAV Viken", "AKTIV");

    private static GeografiskTilknytning tilknytningNormal = new GeografiskTilknytning("0219", null);
    private static GeografiskTilknytning tilknytningKode6 = new GeografiskTilknytning("0219", "SPSF");

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
        settOppTpsStrukturer(false);

        OrganisasjonsEnhet enhet = enhetsTjeneste.hentEnhetSjekkRegistrerteRelasjoner(MOR_AKTØR_ID, BehandlingTema.SYKEPENGER);

        assertThat(enhet).isNotNull();
        assertThat(enhet).isEqualTo(enhetNormal);
    }

    @Test
    public void finn_enhet_utvidet_bruker_kode_fordeling() {
        // Oppsett
        settOppTpsStrukturer(true);

        OrganisasjonsEnhet enhet = enhetsTjeneste.hentEnhetSjekkRegistrerteRelasjoner(MOR_AKTØR_ID, BehandlingTema.SYKEPENGER);

        assertThat(enhet).isNotNull();
        assertThat(enhet).isEqualTo(enhetKode6);
    }

    @Test
    public void presendens_enhet() {
        // Oppsett
        settOppTpsStrukturer(false);

        OrganisasjonsEnhet enhet = enhetsTjeneste.enhetsPresedens(enhetNormal, enhetKode6, false);

        assertThat(enhet).isEqualTo(enhetKode6);
    }

    private void settOppTpsStrukturer(boolean søkerKode6) {
        søkerPersonInfo = new Personinfo.Builder().medAktørId(MOR_AKTØR_ID).medPersonIdent(MOR_IDENT).medNavn("Kari Dunk")
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE).medFødselsdato(LocalDate.of(1989,12,12)).medAdresse("Vei").
            build();

        when(tpsTjeneste.hentFnrForAktør(MOR_AKTØR_ID)).thenReturn(MOR_IDENT);

        when(tpsTjeneste.hentBrukerForAktør(MOR_AKTØR_ID)).thenReturn(Optional.of(søkerPersonInfo));
        when(tpsTjeneste.hentGeografiskTilknytning(MOR_IDENT)).thenReturn(søkerKode6 ? tilknytningKode6 : tilknytningNormal);

        when(arbeidsfordelingTjeneste.finnBehandlendeEnhet(any(),isNull(), any())).thenReturn(enhetNormal);
        when(arbeidsfordelingTjeneste.finnBehandlendeEnhet(any(), matches("SPSF"), any())).thenReturn(enhetKode6);
        when(arbeidsfordelingTjeneste.hentEnhetForDiskresjonskode(matches("SPSF"), any())).thenReturn(enhetKode6);

    }
}
