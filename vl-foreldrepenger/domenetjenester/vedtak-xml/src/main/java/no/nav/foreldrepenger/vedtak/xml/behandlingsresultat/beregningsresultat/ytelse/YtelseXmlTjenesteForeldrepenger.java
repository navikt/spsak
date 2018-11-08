package no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat.ytelse;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.vedtak.xml.VedtakXmlUtil;
import no.nav.vedtak.felles.xml.vedtak.v2.Beregningsresultat;
import no.nav.vedtak.felles.xml.vedtak.ytelse.fp.v2.ObjectFactory;
import no.nav.vedtak.felles.xml.vedtak.ytelse.fp.v2.YtelseForeldrepenger;

@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class YtelseXmlTjenesteForeldrepenger implements YtelseXmlTjeneste {
    private ObjectFactory ytelseObjectFactory;

    BeregningsresultatFPRepository beregningsresultatFPRepository;

    public YtelseXmlTjenesteForeldrepenger() {
        //For CDI
    }

    @Inject
    public YtelseXmlTjenesteForeldrepenger(BeregningsresultatFPRepository beregningsresultatFPRepository) {
        this.ytelseObjectFactory = new ObjectFactory();
        this.beregningsresultatFPRepository = beregningsresultatFPRepository;
    }

    @Override
    public void setYtelse(Beregningsresultat beregningsresultat, Behandling behandling) {
        YtelseForeldrepenger ytelseForeldrepenger = ytelseObjectFactory.createYtelseForeldrepenger();
        Optional<BeregningsresultatFP> beregningsresultatOptional = beregningsresultatFPRepository.hentBeregningsresultatFP(behandling);
        if (beregningsresultatOptional.isPresent()) {
            BeregningsresultatFP beregningsresultatFP = beregningsresultatOptional.get();
            setBeregningsresultat(ytelseForeldrepenger, beregningsresultatFP.getBeregningsresultatPerioder());
        }
    }

    private void setBeregningsresultat(YtelseForeldrepenger ytelseForeldrepenger, List<BeregningsresultatPeriode> beregningsresultatPerioder) {
        List<no.nav.vedtak.felles.xml.vedtak.ytelse.fp.v2.Beregningsresultat> resultat = beregningsresultatPerioder
            .stream()
            .map(periode -> periode.getBeregningsresultatAndelList()).flatMap(andeler -> andeler.stream()).map(andel -> konverterFraDomene(andel)).collect(Collectors.toList());

        ytelseForeldrepenger.getBeregningsresultat().addAll(resultat);
    }

    private no.nav.vedtak.felles.xml.vedtak.ytelse.fp.v2.Beregningsresultat konverterFraDomene(BeregningsresultatAndel andelDomene) {
        no.nav.vedtak.felles.xml.vedtak.ytelse.fp.v2.Beregningsresultat kontrakt = new no.nav.vedtak.felles.xml.vedtak.ytelse.fp.v2.Beregningsresultat();
        kontrakt.setPeriode(VedtakXmlUtil.lagPeriodeOpplysning(andelDomene.getBeregningsresultatPeriode().getBeregningsresultatPeriodeFom(), andelDomene.getBeregningsresultatPeriode().getBeregningsresultatPeriodeTom()));
        kontrakt.setBrukerErMottaker(VedtakXmlUtil.lagBooleanOpplysning(andelDomene.erBrukerMottaker()));
        kontrakt.setVirksomhet(konverterVirksomhetFraDomene(andelDomene));
        kontrakt.setAktivitetstatus(VedtakXmlUtil.lagKodeverkOpplysning(andelDomene.getAktivitetStatus()));
        kontrakt.setInntektskategori(VedtakXmlUtil.lagKodeverkOpplysning(andelDomene.getInntektskategori()));
        kontrakt.setDagsats(VedtakXmlUtil.lagIntOpplysning(andelDomene.getDagsats()));
        kontrakt.setStillingsprosent(VedtakXmlUtil.lagDecimalOpplysning(andelDomene.getStillingsprosent()));
        kontrakt.setUtbetalingsgrad(VedtakXmlUtil.lagDecimalOpplysning(andelDomene.getUtbetalingsgrad()));
        return kontrakt;
    }

    private no.nav.vedtak.felles.xml.vedtak.ytelse.fp.v2.Virksomhet konverterVirksomhetFraDomene(BeregningsresultatAndel andelDomene) {
        no.nav.vedtak.felles.xml.vedtak.ytelse.fp.v2.Virksomhet kontrakt = new no.nav.vedtak.felles.xml.vedtak.ytelse.fp.v2.Virksomhet();
        Optional.ofNullable(andelDomene.getVirksomhet()).ifPresent(virksomhet -> {
            kontrakt.setOrgnr(VedtakXmlUtil.lagStringOpplysning(virksomhet.getOrgnr()));
            kontrakt.setNavn(VedtakXmlUtil.lagStringOpplysning(virksomhet.getNavn()));
        });

        Optional.ofNullable(andelDomene.getArbeidsforholdRef()).ifPresent(ref -> kontrakt.setArbeidsforholdid(VedtakXmlUtil.lagStringOpplysning(ref.getReferanse())));
        return kontrakt;
    }
}
