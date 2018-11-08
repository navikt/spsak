package no.nav.foreldrepenger.dokumentbestiller;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.uttak.InfoOmResterendeDagerTjeneste;
import no.nav.foreldrepenger.domene.uttak.OpphørFPTjeneste;
import no.nav.foreldrepenger.domene.uttak.beregnflerbarnsuker.BeregnEkstraFlerbarnsukerTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.BeregnUttaksaldoTjeneste;

/** Provider for å enklere å kunne hente ut ulike tjenester uten for mange injection points. */
@ApplicationScoped
public class DokumentMapperTjenesteProviderImpl implements DokumentMapperTjenesteProvider {

    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private BasisPersonopplysningTjeneste personopplysningTjeneste;
    private FamilieHendelseTjeneste familiehendelseTjeneste;
    private BeregnUttaksaldoTjeneste beregnUttaksaldoTjeneste;
    private HentGrunnlagsdataTjeneste hentGrunnlagsDataTjeneste;
    private BeregnEkstraFlerbarnsukerTjeneste beregnEkstraFlerbarnsukerTjeneste;
    private OpphørFPTjeneste opphørFPTjeneste;
    private InfoOmResterendeDagerTjeneste infoOmResterendeDagerTjeneste;

    public DokumentMapperTjenesteProviderImpl() {
        // for CDI proxy
    }

    @Inject
    public DokumentMapperTjenesteProviderImpl(SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                              BasisPersonopplysningTjeneste personopplysningTjeneste,
                                              FamilieHendelseTjeneste familiehendelseTjeneste,
                                              BeregnUttaksaldoTjeneste beregnUttaksaldoTjeneste,
                                              HentGrunnlagsdataTjeneste hentGrunnlagsDataTjeneste,
                                              BeregnEkstraFlerbarnsukerTjeneste beregnEkstraFlerbarnsukerTjeneste,
                                              OpphørFPTjeneste opphørFPTjeneste,
                                              InfoOmResterendeDagerTjeneste infoOmResterendeDagerTjeneste) {
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
        this.personopplysningTjeneste = personopplysningTjeneste;
        this.familiehendelseTjeneste = familiehendelseTjeneste;
        this.beregnUttaksaldoTjeneste = beregnUttaksaldoTjeneste;
        this.hentGrunnlagsDataTjeneste = hentGrunnlagsDataTjeneste;
        this.beregnEkstraFlerbarnsukerTjeneste = beregnEkstraFlerbarnsukerTjeneste;
        this.opphørFPTjeneste = opphørFPTjeneste;
        this.infoOmResterendeDagerTjeneste = infoOmResterendeDagerTjeneste;
    }

    @Override
    public SkjæringstidspunktTjeneste getSkjæringstidspunktTjeneste() {
        return skjæringstidspunktTjeneste;
    }

    public BasisPersonopplysningTjeneste getBasisPersonopplysningTjeneste() {
        return personopplysningTjeneste;
    }

    @Override
    public FamilieHendelseTjeneste getFamiliehendelseTjeneste() {
        return familiehendelseTjeneste;
    }

    @Override
    public BeregnUttaksaldoTjeneste getBeregnUttaksaldoTjeneste() {
        return beregnUttaksaldoTjeneste;
    }

    @Override
    public HentGrunnlagsdataTjeneste getHentGrunnlagsDataTjeneste() {
        return hentGrunnlagsDataTjeneste;
    }

    @Override
    public BeregnEkstraFlerbarnsukerTjeneste getBeregnEkstraFlerbarnsukerTjeneste() {
        return beregnEkstraFlerbarnsukerTjeneste;
    }

    @Override
    public OpphørFPTjeneste getOpphørFPTjeneste() {
        return opphørFPTjeneste;
    }

    @Override
    public InfoOmResterendeDagerTjeneste getInfoOmResterendeDagerTjeneste() {
        return infoOmResterendeDagerTjeneste;
    }

}
