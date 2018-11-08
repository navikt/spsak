package no.nav.foreldrepenger.migrering;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.migrering.api.HistorikkMigreringRepository;

class HistorikkMigreringTestdataBuilder {
    private HistorikkMigreringRepository historikkMigreringRepository;
    private AksjonspunktRepository aksjonspunktRepository;

    HistorikkMigreringTestdataBuilder(HistorikkMigreringRepository historikkMigreringRepository, AksjonspunktRepository aksjonspunktRepository) {
        this.historikkMigreringRepository = historikkMigreringRepository;
        this.aksjonspunktRepository = aksjonspunktRepository;
    }

    @SuppressWarnings("deprecation")
    public void opprettHistorikkinnslag(Behandling behandling)  {
        Historikkinnslag historikkinnslag1 = new Historikkinnslag();
        historikkinnslag1.setBehandling(behandling);
        historikkinnslag1.setAktør(HistorikkAktør.VEDTAKSLØSNINGEN);
        historikkinnslag1.setType(HistorikkinnslagType.BEH_STARTET);
        historikkinnslag1.setTekst("{\"endredeFelter\":[],\"opplysninger\":[],\"hendelse\":\"Søknad mottatt\"}");
        historikkMigreringRepository.lagre(historikkinnslag1);

        Historikkinnslag historikkinnslag2 = new Historikkinnslag();
        historikkinnslag2.setBehandling(behandling);
        historikkinnslag2.setAktør(HistorikkAktør.VEDTAKSLØSNINGEN);
        historikkinnslag2.setType(HistorikkinnslagType.VEDTAK_FATTET);
        historikkinnslag2.setTekst("{\"endredeFelter\":[],\"skjermlinke\":{\"faktaNavn\":\"default\",\"punktNavn\":\"vedtak\",\"linkTekst\":\"Vedtak\"},\"opplysninger\":[],\"hendelse\":\"Vedtak fattet og iverksatt\",\"resultat\":\"Innvilget\"}");
        historikkMigreringRepository.lagre(historikkinnslag2);

        Historikkinnslag historikkinnslag3 = new Historikkinnslag();
        historikkinnslag3.setBehandling(behandling);
        historikkinnslag3.setAktør(HistorikkAktør.VEDTAKSLØSNINGEN);
        historikkinnslag3.setType(HistorikkinnslagType.SAK_RETUR);
        historikkinnslag3.setTekst("{\"totrinnsvurdering\":[{\"skjermlinke\":{\"faktaNavn\":\"adopsjon\",\"punktNavn\":\"adopsjon\",\"linkTekst\":\"Adopsjon\"},\"aksjonspunkter\":[{\"begrunnelse\":\"ddas\",\"godkjent\":false,\"kode\":\"6004\"},{\"begrunnelse\":\"asddas\",\"godkjent\":false,\"kode\":\"5005\"}]}],\"endredeFelter\":[],\"opplysninger\":[],\"hendelse\":\"Vedtak returnert\"}");
        historikkMigreringRepository.lagre(historikkinnslag3);


        aksjonspunktRepository.leggTilAksjonspunkt(behandling, aksjonspunktRepository.finnAksjonspunktDefinisjon("5005"));
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, aksjonspunktRepository.finnAksjonspunktDefinisjon("6004"));

        Historikkinnslag historikkinnslag4 = new Historikkinnslag();
        historikkinnslag4.setBehandling(behandling);
        historikkinnslag4.setAktør(HistorikkAktør.VEDTAKSLØSNINGEN);
        historikkinnslag4.setType(HistorikkinnslagType.BEH_VENT);
        historikkinnslag4.setTekst("{\"endredeFelter\":[],\"opplysninger\":[],\"hendelse\":\"Behandlingen er satt på vent med frist 18.12.2017\"}");
        historikkMigreringRepository.lagre(historikkinnslag4);

        Historikkinnslag historikkinnslag5 = new Historikkinnslag();
        historikkinnslag5.setBehandling(behandling);
        historikkinnslag5.setAktør(HistorikkAktør.SAKSBEHANDLER);
        historikkinnslag5.setType(HistorikkinnslagType.FAKTA_ENDRET);
        historikkinnslag5.setTekst("{\"begrunnelse\":\"sdf sdf sdf sdf sdf \",\"endredeFelter\":[{\"navn\":\"SjekkFodselDokForm.DokumentasjonForeligger\",\"fraVerdi\":false,\"tilVerdi\":true},{\"navn\":\"OmsorgOgForeldreansvarFaktaForm.NrOfChildren\",\"fraVerdi\":0,\"tilVerdi\":1}],\"skjermlinke\":{\"faktaNavn\":\"foedsel\",\"punktNavn\":\"foedsel\",\"linkTekst\":\"Fødsel\"},\"opplysninger\":[{\"verdi\":1,\"navn\":\"Historikk.Template.5.AntallBarn\"}]}");
        historikkMigreringRepository.lagre(historikkinnslag5);

        Historikkinnslag historikkinnslag7 = new Historikkinnslag();
        historikkinnslag7.setBehandling(behandling);
        historikkinnslag7.setAktør(HistorikkAktør.VEDTAKSLØSNINGEN);
        historikkinnslag7.setType(HistorikkinnslagType.OVERSTYRT);
        historikkinnslag7.setTekst("{\"begrunnelse\":\"ojioki\",\"endredeFelter\":[{\"navn\":\"Historikk.Template.7.OverstyrtVilkar\",\"fraVerdi\":\"Vilkåret er ikke oppfylt\",\"tilVerdi\":\"Vilkåret er oppfylt\"}],\"skjermlinke\":{\"faktaNavn\":\"adopsjon\",\"punktNavn\":\"adopsjon\",\"linkTekst\":\"Adopsjon\"},\"opplysninger\":[]}");
        historikkMigreringRepository.lagre(historikkinnslag7);
    }
}
