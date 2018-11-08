package no.nav.foreldrepenger.vedtak.xml.behandlingsresultat;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageAvvistÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageMedholdÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurderingResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallMerknad;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.vedtak.xml.VedtakXmlUtil;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat.BeregningsresultatXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.vilkår.VilkårsgrunnlagXmlTjeneste;
import no.nav.vedtak.felles.xml.felles.v2.KodeverksOpplysning;
import no.nav.vedtak.felles.xml.vedtak.v2.Behandlingsresultat;
import no.nav.vedtak.felles.xml.vedtak.v2.Behandlingstype;
import no.nav.vedtak.felles.xml.vedtak.v2.KlageAvvistAarsak;
import no.nav.vedtak.felles.xml.vedtak.v2.KlageMedholdAarsak;
import no.nav.vedtak.felles.xml.vedtak.v2.KlageVurdertAv;
import no.nav.vedtak.felles.xml.vedtak.v2.Klagevurdering;
import no.nav.vedtak.felles.xml.vedtak.v2.Klagevurderingresultat;
import no.nav.vedtak.felles.xml.vedtak.v2.ManuellVurderingsResultat;
import no.nav.vedtak.felles.xml.vedtak.v2.ObjectFactory;
import no.nav.vedtak.felles.xml.vedtak.v2.Vedtak;
import no.nav.vedtak.felles.xml.vedtak.v2.Vilkaar;
import no.nav.vedtak.felles.xml.vedtak.v2.Vilkaarsutfall;
import no.nav.vedtak.felles.xml.vedtak.v2.Vurderingsvariant;
import no.nav.vedtak.felles.xml.vedtak.v2.VurderteVilkaar;

public abstract class BehandlingsresultatXmlTjeneste {

    private ObjectFactory v2ObjectFactory = new ObjectFactory();
    private BeregningsresultatXmlTjeneste beregningsresultatXmlTjeneste;
    private VilkårsgrunnlagXmlTjeneste vilkårsgrunnlagXmlTjeneste;

    public BehandlingsresultatXmlTjeneste() {
        // For CDI
    }

    public BehandlingsresultatXmlTjeneste(BeregningsresultatXmlTjeneste beregningsresultatXmlTjeneste,
                                          VilkårsgrunnlagXmlTjeneste vilkårsgrunnlagXmlTjeneste) {
        this.beregningsresultatXmlTjeneste = beregningsresultatXmlTjeneste;
        this.vilkårsgrunnlagXmlTjeneste = vilkårsgrunnlagXmlTjeneste;
    }

    public void setBehandlingresultat(Vedtak vedtak, Behandling behandling) {
        Behandlingsresultat behandlingsresultat;
        if (behandling.getType().equals(BehandlingType.KLAGE)) {
            behandlingsresultat = v2ObjectFactory.createKlagevurderingresultat();
            leggTilKlageVerdier((Klagevurderingresultat) behandlingsresultat, behandling);
        } else {
            behandlingsresultat = v2ObjectFactory.createBehandlingsresultat();
        }
        setBehandlingsresultatType(behandlingsresultat, behandling);
        setBehandlingstype(behandlingsresultat, behandling);
        behandlingsresultat.setBehandlingsId(behandling.getId().toString());
        setVurderteVilkaar(behandlingsresultat, behandling);
        setManuelleVurderinger(behandlingsresultat, behandling);
        beregningsresultatXmlTjeneste.setBeregningsresultat(behandlingsresultat, behandling);

        vedtak.setBehandlingsresultat(behandlingsresultat);
    }

    private void leggTilKlageVerdier(Klagevurderingresultat klagevurderingresultat, Behandling behandling) {
        Optional<KlageVurderingResultat> optionalGjeldendeKlagevurderingresultat = behandling.hentGjeldendeKlageVurderingResultat();
        if (optionalGjeldendeKlagevurderingresultat.isPresent()) {
            KlageVurderingResultat gjeldendeKlagevurderingresultat = optionalGjeldendeKlagevurderingresultat.get();
            klagevurderingresultat.setKlageAvvistAarsak(hentKlageAvvistårsak(gjeldendeKlagevurderingresultat));
            klagevurderingresultat.setKlageMedholdAarsak(hentKlageMedholdårsak(gjeldendeKlagevurderingresultat));
            klagevurderingresultat.setKlageVurdering(hentKlagevurdering(gjeldendeKlagevurderingresultat));
            klagevurderingresultat.setKlageVurdertAv(hentKlageVurdertAv(gjeldendeKlagevurderingresultat));
        }
    }

    private KlageVurdertAv hentKlageVurdertAv(KlageVurderingResultat vurderingsresultat) { no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdertAv klageVurdertAv = vurderingsresultat.getKlageVurdertAv();
        if (no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdertAv.NFP.equals(klageVurdertAv)) {
            return KlageVurdertAv.NFP;
        } else if (no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdertAv.NK.equals(klageVurdertAv)) {
            return KlageVurdertAv.NK;
        }
        return null;
    }

    private Klagevurdering hentKlagevurdering(KlageVurderingResultat vurderingsresultat) {
        KlageVurdering klageVurdering = vurderingsresultat.getKlageVurdering();
        if (KlageVurdering.STADFESTE_YTELSESVEDTAK.equals(klageVurdering)) {
            return Klagevurdering.STADFESTE_YTELSESVEDTAK;
        } else if (KlageVurdering.OPPHEVE_YTELSESVEDTAK.equals(klageVurdering)) {
            return Klagevurdering.OPPHEVE_YTELSESVEDTAK;
        } else if (KlageVurdering.MEDHOLD_I_KLAGE.equals(klageVurdering)) {
            return Klagevurdering.MEDHOLD_I_KLAGE;
        } else if (KlageVurdering.AVVIS_KLAGE.equals(klageVurdering)) {
            return Klagevurdering.AVVIS_KLAGE;
        }
        return null;
    }

    private KlageMedholdAarsak hentKlageMedholdårsak(KlageVurderingResultat vurderingsresultat) {
        KlageMedholdÅrsak klageMedholdÅrsak = vurderingsresultat.getKlageMedholdÅrsak();
        if (KlageMedholdÅrsak.NYE_OPPLYSNINGER.equals(klageMedholdÅrsak)) {
            return KlageMedholdAarsak.NYE_OPPLYSNINGER;
        } else if (KlageMedholdÅrsak.ULIK_VURDERING.equals(klageMedholdÅrsak)) {
            return KlageMedholdAarsak.ULIK_VURDERING;
        } else if (KlageMedholdÅrsak.ULIK_REGELVERKSTOLKNING.equals(klageMedholdÅrsak)) {
            return KlageMedholdAarsak.ULIK_REGELVERKSTOLKNING;
        } else if (KlageMedholdÅrsak.PROSESSUELL_FEIL.equals(klageMedholdÅrsak)) {
            return KlageMedholdAarsak.PROSESSUELL_FEIL;
        }
        return null;
    }

    private KlageAvvistAarsak hentKlageAvvistårsak(KlageVurderingResultat vurderingsresultat) {
        KlageAvvistÅrsak klageAvvistÅrsak = vurderingsresultat.getKlageAvvistÅrsak();
        if (KlageAvvistÅrsak.KLAGET_FOR_SENT.equals(klageAvvistÅrsak)) {
            return KlageAvvistAarsak.KLAGET_FOR_SENT;
        } else if (KlageAvvistÅrsak.KLAGE_UGYLDIG.equals(klageAvvistÅrsak)) {
            return KlageAvvistAarsak.KLAGE_UGYLDIG;
        }
        return null;
    }

    private void setBehandlingsresultatType(Behandlingsresultat behandlingsresultat, Behandling behandling) {
        BehandlingResultatType behandlingResultatType = behandling.getBehandlingsresultat().getBehandlingResultatType();
        if (BehandlingResultatType.INNVILGET.equals(behandlingResultatType)) {
            behandlingsresultat.setBehandlingsresultat(VedtakXmlUtil.lagKodeverkOpplysning(BehandlingResultatType.INNVILGET));
        } else if (BehandlingResultatType.getKlageKoder().contains(behandlingResultatType)) {
            behandlingsresultat.setBehandlingsresultat(VedtakXmlUtil.lagKodeverkOpplysning(behandlingResultatType));
        } else {
            behandlingsresultat.setBehandlingsresultat(VedtakXmlUtil.lagKodeverkOpplysning(BehandlingResultatType.AVSLÅTT));
        }
    }

    private void setManuelleVurderinger(Behandlingsresultat behandlingsresultat, Behandling behandling) {
        Set<Aksjonspunkt> alleAksjonspunkter = behandling.getAksjonspunkter();
        if (!alleAksjonspunkter.isEmpty()) {

            Behandlingsresultat.ManuelleVurderinger manuelleVurderinger = v2ObjectFactory.createBehandlingsresultatManuelleVurderinger();
            alleAksjonspunkter
                .forEach(aksjonspunkt -> leggTilManuellVurdering(manuelleVurderinger, aksjonspunkt));
            behandlingsresultat.setManuelleVurderinger(manuelleVurderinger);
        }
    }

    private void leggTilManuellVurdering(Behandlingsresultat.ManuelleVurderinger manuelleVurderinger, Aksjonspunkt aksjonspunkt) {
        ManuellVurderingsResultat manuellVurderingsResultat = v2ObjectFactory.createManuellVurderingsResultat();
        manuellVurderingsResultat.setAksjonspunkt(VedtakXmlUtil.lagKodeverksOpplysningForAksjonspunkt(aksjonspunkt.getAksjonspunktDefinisjon()));
        if (aksjonspunkt.getAksjonspunktDefinisjon().getVilkårType() != null) {
            manuellVurderingsResultat.setGjelderVilkaar(VedtakXmlUtil.lagKodeverksOpplysning(aksjonspunkt.getAksjonspunktDefinisjon().getVilkårType()));
        }
        if (aksjonspunkt.getBegrunnelse() != null && !aksjonspunkt.getBegrunnelse().isEmpty()) {
            manuellVurderingsResultat.setSaksbehandlersBegrunnelse(aksjonspunkt.getBegrunnelse());
        }
        manuelleVurderinger.getManuellVurdering().add(manuellVurderingsResultat);
    }

    private void setVurderteVilkaar(Behandlingsresultat behandlingsresultat, Behandling behandling) {
        VilkårResultat vilkårResultat = behandling.getBehandlingsresultat().getVilkårResultat();
        if (vilkårResultat != null) {
            VurderteVilkaar vurderteVilkaar = v2ObjectFactory.createVurderteVilkaar();
            List<Vilkaar> vilkår = vurderteVilkaar.getVilkaar();
            vilkårResultat.getVilkårene()
                .forEach(vk -> vilkår.add(lagVilkår(vk, behandling)));
            behandlingsresultat.setVurderteVilkaar(vurderteVilkaar);

        }
    }

    private Vilkaar lagVilkår(Vilkår vilkårFraBehandling, Behandling behandling) {
        Vilkaar vilkår = v2ObjectFactory.createVilkaar();
        vilkår.setType(VedtakXmlUtil.lagKodeverksOpplysning(vilkårFraBehandling.getVilkårType()));

        // Sett utfall
        if (VilkårUtfallType.OPPFYLT.equals(vilkårFraBehandling.getGjeldendeVilkårUtfall())) {
            vilkår.setUtfall(Vilkaarsutfall.OPPFYLT);
        } else if (VilkårUtfallType.IKKE_OPPFYLT.equals(vilkårFraBehandling.getGjeldendeVilkårUtfall())) {
            vilkår.setUtfall(Vilkaarsutfall.IKKE_OPPFYLT);
        } else if (VilkårUtfallType.IKKE_VURDERT.equals(vilkårFraBehandling.getGjeldendeVilkårUtfall())) {
            vilkår.setUtfall(Vilkaarsutfall.IKKE_VURDERT);
        }

        if (vilkårFraBehandling.getVilkårUtfallMerknad() != null) {
            //Kan ikke bruke hjelpefunksjonen fra vedtakXmlUtikl for kodeverdier her fordi
            // VILKAR_UTFALL_MERKNAD kodeverket ikke har skikeklige navn. Kan byttes tilbake når dette er fikset.
            KodeverksOpplysning kodeverksOpplysning = new KodeverksOpplysning();
            VilkårUtfallMerknad vilkårUtfallMerknad = vilkårFraBehandling.getVilkårUtfallMerknad();
            kodeverksOpplysning.setKode(vilkårUtfallMerknad.getKode());
            kodeverksOpplysning.setValue(vilkårUtfallMerknad.getBeskrivelse());
            kodeverksOpplysning.setKodeverk(vilkårUtfallMerknad.getKodeverk());
            vilkår.setUtfallMerknad(kodeverksOpplysning);
        }
        vilkår.setVurdert(vilkårFraBehandling.erManueltVurdert() ? Vurderingsvariant.MANUELT : Vurderingsvariant.AUTOMATISK);
        vilkårsgrunnlagXmlTjeneste.setVilkårsgrunnlag(behandling, vilkårFraBehandling, vilkår);
        return vilkår;
    }

    private void setBehandlingstype(Behandlingsresultat behandlingsresultat, Behandling behandling) {
        if (BehandlingType.KLAGE.equals(behandling.getType())) {
            behandlingsresultat.setBehandlingstype(Behandlingstype.KLAGE);
        } else if (BehandlingType.FØRSTEGANGSSØKNAD.equals(behandling.getType())) {
            behandlingsresultat.setBehandlingstype(Behandlingstype.FOERSTEGANGSSOEKNAD);
        } else if (BehandlingType.REVURDERING.equals(behandling.getType())) {
            behandlingsresultat.setBehandlingstype(Behandlingstype.REVURDERING);
        }
    }
}
