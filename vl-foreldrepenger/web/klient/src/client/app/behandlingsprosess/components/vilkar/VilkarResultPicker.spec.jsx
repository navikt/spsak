import React from 'react';
import { shallowWithIntl, intlMock } from 'testHelpers/intl-enzyme-test-helper';
import { expect } from 'chai';

import vilkarType from 'kodeverk/vilkarType';
import vilkarUtfallType from 'kodeverk/vilkarUtfallType';
import aksjonspunktStatus from 'kodeverk/aksjonspunktStatus';
import { isRequiredMessage } from 'utils/validation/messages';
import VilkarResultPicker from './VilkarResultPicker';

describe('<VilkarResultPicker>', () => {
  const avslagsarsaker = [{ kode: 'TEST', navn: 'test' }];

  it('skal vise komponent med radioknapper', () => {
    const wrapper = shallowWithIntl(<VilkarResultPicker.WrappedComponent
      intl={intlMock}
      avslagsarsaker={avslagsarsaker}
      erVilkarOk
      readOnly={false}
      hasAksjonspunkt
    />);
    expect(wrapper.find('RadioOption')).to.have.length(2);
  });

  it('skal kunne overstyre vilkårtekster', () => {
    const textId = 'Test';
    const wrapper = shallowWithIntl(<VilkarResultPicker.WrappedComponent
      intl={intlMock}
      avslagsarsaker={avslagsarsaker}
      erVilkarOk={false}
      customVilkarIkkeOppfyltText={{ id: textId, values: { test: 'testvalue' } }}
      readOnly={false}
      hasAksjonspunkt
    />);

    expect(wrapper.find('RadioOption').at(1).prop('label').props.id).to.equal(textId);
  });

  it('skal ikke vise nedtrekksliste når vilkårsresultat ikke er valgt', () => {
    const wrapper = shallowWithIntl(<VilkarResultPicker.WrappedComponent
      intl={intlMock}
      avslagsarsaker={avslagsarsaker}
      erVilkarOk
      readOnly={false}
      hasAksjonspunkt
    />);

    expect(wrapper.find('SelectField')).to.have.length(0);
  });

  it('skal ikke vise nedtrekksliste når vilkårsresultat er OK', () => {
    const wrapper = shallowWithIntl(<VilkarResultPicker.WrappedComponent
      intl={intlMock}
      avslagsarsaker={avslagsarsaker}
      erVilkarOk
      readOnly={false}
      hasAksjonspunkt
    />);

    expect(wrapper.find('SelectField')).to.have.length(0);
  });

  it('skal vise nedtrekksliste når vilkårsresultat er valgt', () => {
    const wrapper = shallowWithIntl(<VilkarResultPicker.WrappedComponent
      intl={intlMock}
      avslagsarsaker={avslagsarsaker}
      erVilkarOk={false}
      readOnly={false}
      hasAksjonspunkt
    />);

    const select = wrapper.find('SelectField');
    expect(select).to.have.length(1);
    expect(select.prop('label')).to.eql('Angi avslagsårsak');
    expect(select.prop('placeholder')).to.eql('Velg årsak');
    expect(select.prop('selectValues')).to.have.length(1);
  });

  it('skal feile validering når en har valgt å avvise vilkår men ikke valgt avslagsårsak', () => {
    const erVilkarOk = false;
    const avslagCode = undefined;
    const errors = VilkarResultPicker.validate(erVilkarOk, avslagCode);

    expect(errors.avslagCode).to.eql(isRequiredMessage());
  });

  it('skal ikke feile validering når en har valgt både å avvise vilkår og avslagsårsak', () => {
    const erVilkarOk = false;
    const avslagCode = 'VALGT_KODE';
    const errors = VilkarResultPicker.validate(erVilkarOk, avslagCode);

    expect(errors).to.eql({});
  });

  it('skal sette opp initielle verdier', () => {
    const behandlingsresultat = {
      avslagsarsak: {
        kode: 'Avslagskoden',
      },
    };
    const aksjonspunkter = [{
      status: {
        kode: aksjonspunktStatus.UTFORT,
      },
      vilkarType: {
        kode: vilkarType.FODSELSVILKARET_MOR,
      },
    }];
    const intielleVerdier = VilkarResultPicker.buildInitialValues(behandlingsresultat, aksjonspunkter, vilkarUtfallType.IKKE_OPPFYLT);

    expect(intielleVerdier).to.eql({
      avslagCode: 'Avslagskoden',
      erVilkarOk: false,
    });
  });
});
