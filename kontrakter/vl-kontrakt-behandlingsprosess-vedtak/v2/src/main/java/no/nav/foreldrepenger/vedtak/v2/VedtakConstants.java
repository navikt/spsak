package no.nav.foreldrepenger.vedtak.v2;

import no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.es.v2.BeregningsgrunnlagEngangsstoenad;
import no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.fp.v2.BeregningsgrunnlagForeldrepenger;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.es.v2.PersonopplysningerEngangsstoenad;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.PersonopplysningerForeldrepenger;
import no.nav.vedtak.felles.xml.vedtak.uttak.fp.v2.UttakForeldrepenger;
import no.nav.vedtak.felles.xml.vedtak.v2.Vedtak;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.es.v2.VilkaarsgrunnlagAdopsjon;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.es.v2.VilkaarsgrunnlagFoedsel;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.es.v2.VilkaarsgrunnlagMedlemskap;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.es.v2.VilkaarsgrunnlagSoekersopplysningsplikt;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.es.v2.VilkaarsgrunnlagSoeknadsfrist;
import no.nav.vedtak.felles.xml.vedtak.ytelse.es.v2.YtelseEngangsstoenad;
import no.nav.vedtak.felles.xml.vedtak.ytelse.fp.v2.YtelseForeldrepenger;

public final class VedtakConstants {
    public static final String NAMESPACE = "urn:no:nav:vedtak:felles:xml:vedtak:v2";
    public static final String XSD_LOCATION = "xsd/vedtak-v2.xsd";
    public static final Class<no.nav.vedtak.felles.xml.vedtak.v2.Vedtak> JAXB_CLASS = no.nav.vedtak.felles.xml.vedtak.v2.Vedtak.class;
    public static final Class<?>[] ADDITIONAL_CLASSES = {Vedtak.class,
            //ES klasser
            VilkaarsgrunnlagFoedsel.class,
            VilkaarsgrunnlagAdopsjon.class,
            VilkaarsgrunnlagMedlemskap.class,
            VilkaarsgrunnlagSoeknadsfrist.class,
            VilkaarsgrunnlagSoekersopplysningsplikt.class,
            BeregningsgrunnlagEngangsstoenad.class,
            YtelseEngangsstoenad.class,
            PersonopplysningerEngangsstoenad.class,
            //FP klasser:
            no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.fp.v2.VilkaarsgrunnlagFoedsel.class,
            no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.fp.v2.VilkaarsgrunnlagAdopsjon.class,
            no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.fp.v2.VilkaarsgrunnlagMedlemskap.class,
            no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.fp.v2.VilkaarsgrunnlagOpptjening.class,
            no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.fp.v2.VilkaarsgrunnlagSoeknadsfrist.class,
            no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.fp.v2.VilkaarsgrunnlagSoekersopplysningsplikt.class,
            BeregningsgrunnlagForeldrepenger.class,
            YtelseForeldrepenger.class,
            UttakForeldrepenger.class,
            PersonopplysningerForeldrepenger.class,
            no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.fp.v2.ObjectFactory.class,
            no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.es.v2.ObjectFactory.class,
            no.nav.vedtak.felles.xml.vedtak.oppdrag.v2.ObjectFactory.class,
            no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.ObjectFactory.class,
            no.nav.vedtak.felles.xml.vedtak.personopplysninger.es.v2.ObjectFactory.class,
            no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.es.v2.ObjectFactory.class,
            no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.ObjectFactory.class,
            no.nav.vedtak.felles.xml.vedtak.uttak.fp.v2.ObjectFactory.class,
            no.nav.vedtak.felles.xml.vedtak.ytelse.es.v2.ObjectFactory.class,
            no.nav.vedtak.felles.xml.vedtak.ytelse.fp.v2.ObjectFactory.class,
            no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.v2.ObjectFactory.class

    };
    public static final String[] ADDITIONAL_XSD_LOCATIONS = new String[] {
            "xsd/ytelse/ytelse-fp-v2.xsd", "xsd/ytelse/ytelse-es-v2.xsd",
            "xsd/vilkaarsgrunnlag/vilkaarsgrunnlag-fp-v2.xsd", "xsd/vilkaarsgrunnlag/vilkaarsgrunnlag-es-v2.xsd",
            "xsd/uttak/uttak-fp-v2.xsd",
            "xsd/personopplysninger/personopplysninger-fp-v2.xsd", "xsd/personopplysninger/personopplysninger-es-v2.xsd",
            "xsd/personopplysninger/personopplysninger-dvh-fp-v2.xsd", "xsd/personopplysninger/personopplysninger-dvh-es-v2.xsd",
            "xsd/beregningsgrunnlag/beregningsgrunnlag-fp-v2.xsd", "xsd/beregningsgrunnlag/beregningsgrunnlag-es-v2.xsd"
    };

    private VedtakConstants() {
    }
}
