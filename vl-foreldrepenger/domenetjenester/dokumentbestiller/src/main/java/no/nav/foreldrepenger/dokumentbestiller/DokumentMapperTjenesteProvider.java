package no.nav.foreldrepenger.dokumentbestiller;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.uttak.InfoOmResterendeDagerTjeneste;
import no.nav.foreldrepenger.domene.uttak.OpphørFPTjeneste;
import no.nav.foreldrepenger.domene.uttak.beregnflerbarnsuker.BeregnEkstraFlerbarnsukerTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.BeregnUttaksaldoTjeneste;

public interface DokumentMapperTjenesteProvider {

    SkjæringstidspunktTjeneste getSkjæringstidspunktTjeneste();

    BasisPersonopplysningTjeneste getBasisPersonopplysningTjeneste();

    FamilieHendelseTjeneste getFamiliehendelseTjeneste();

    BeregnUttaksaldoTjeneste getBeregnUttaksaldoTjeneste();

    HentGrunnlagsdataTjeneste getHentGrunnlagsDataTjeneste();

    BeregnEkstraFlerbarnsukerTjeneste getBeregnEkstraFlerbarnsukerTjeneste();

    OpphørFPTjeneste getOpphørFPTjeneste();

    InfoOmResterendeDagerTjeneste getInfoOmResterendeDagerTjeneste();
}
