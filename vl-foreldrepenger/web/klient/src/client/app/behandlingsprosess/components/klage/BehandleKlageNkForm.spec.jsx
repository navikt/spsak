import { expect } from 'chai';
import klageVurderingType from 'kodeverk/klageVurdering';
import aksjonspunktCodes from 'kodeverk/aksjonspunktCodes';
import { buildInitialValues, transformValues } from './BehandleKlageNkForm';

describe('<BehandleKlageNkForm>', () => {
  it('skal initiere form med aktuelle verdier fra behandling', () => {
    const klageVurdering = 'AVVIS';
    const avvistArsak = 'AVVIST';
    const medholdArsak = 'MEDHOLD';
    const begrunnelse = 'BEGRUNNELSE';

    const klageVurderingResultatNK = {
      klageVurdering,
      klageMedholdArsak: medholdArsak,
      klageAvvistArsak: avvistArsak,
      begrunnelse,
    };

    const initialValues = buildInitialValues.resultFunc({}, klageVurderingResultatNK);

    expect(initialValues.klageVurdering).to.equal(klageVurdering);
    expect(initialValues.klageAvvistArsak).to.equal(avvistArsak);
    expect(initialValues.klageMedholdArsak).to.equal(medholdArsak);
    expect(initialValues.begrunnelse).to.equal(begrunnelse);
  });

  it('skal ikke sende medhold årsak når klagevurdering er avvis', () => {
    const klageVurdering = klageVurderingType.AVVIS_KLAGE;
    const avvistArsak = 'AVVIST';
    const medholdArsak = 'MEDHOLD';
    const begrunnelse = 'BEGRUNNELSE';
    const aksjonspunktCode = aksjonspunktCodes.BEHANDLE_KLAGE_NK;
    const kode = aksjonspunktCode;

    const values = {
      klageVurdering,
      klageMedholdArsak: medholdArsak,
      klageAvvistArsak: avvistArsak,
      begrunnelse,
      kode,
    };


    const transformedValues = transformValues(values, aksjonspunktCode);

    expect(transformedValues.klageMedholdArsak).to.be.null;
    expect(transformedValues.klageAvvistArsak).to.equal(avvistArsak);
  });

  it('skal ikke sende avvist årsak når klagevurdering er medhold', () => {
    const klageVurdering = klageVurderingType.MEDHOLD_I_KLAGE;
    const avvistArsak = 'AVVIST';
    const medholdArsak = 'MEDHOLD';
    const begrunnelse = 'BEGRUNNELSE';
    const aksjonspunktCode = aksjonspunktCodes.BEHANDLE_KLAGE_NK;
    const kode = aksjonspunktCode;

    const values = {
      klageVurdering,
      klageMedholdArsak: medholdArsak,
      klageAvvistArsak: avvistArsak,
      begrunnelse,
      kode,
    };


    const transformedValues = transformValues(values, aksjonspunktCode);

    expect(transformedValues.klageAvvistArsak).to.be.null;
    expect(transformedValues.klageMedholdArsak).to.equal(medholdArsak);
  });
});
