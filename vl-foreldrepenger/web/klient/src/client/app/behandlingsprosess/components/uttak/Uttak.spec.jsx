import React from 'react';
import { expect } from 'chai';
import { shallow } from 'enzyme';
import sinon from 'sinon';
import aksjonspunktCodes from 'kodeverk/aksjonspunktCodes';
import { intlMock } from 'testHelpers/intl-enzyme-test-helper';
import { UttakImpl as Uttak } from './Uttak';

describe('<Uttak>', () => {
  const uttakActivities = [{
    id: 1,
    hovedsoker: true,
    group: 1,
    fom: '',
    tom: '',
    periodeResultatType: {
      kode: '',
      navn: '',
      kodeverk: '',
    },
    aktiviteter: [{
      stønadskontoType: {
        kode: '',
        navn: '',
        kodeverk: '',
      },
    }],
  }, {
    id: 2,
    group: 1,
    hovedsoker: true,
    fom: '',
    tom: '',
    periodeResultatType: {
      kode: '',
      navn: '',
      kodeverk: '',
    },
    aktiviteter: [{
      stønadskontoType: {
        kode: '',
        navn: '',
        kodeverk: '',
      },
    }],
  }];

  const stonadskonto = {
    stonadskontoer: {},
  };


  it('skal rendre uttak, uten selected timeline', () => {
    const wrapper = shallow(<Uttak
      readOnly={false}
      formName="UttakForm"
      behandlingFormPrefix=""
      soknadDate="2018-03-02"
      familiehendelseDate="2018-02-02"
      endringsDate="2018-01-12"
      hovedsokerKjonnKode=""
      periodeTyper={[]}
      uttaksresultatActivity={[]}
      reduxFormChange={sinon.spy()}
      reduxFormInitialize={sinon.spy()}
      submitting={false}
      isDirty={false}
      manuellOverstyring={false}
      kanOverstyre
      isApOpen={false}
      stonadskonto={stonadskonto}
      allAksjonspunkter={[]}
      intl={intlMock}
      soknadsType="ST-001"
      uttakPerioder={[]}
      harSoktOmFlerbarnsdager={false}
      annenForelderSoktOmFlerbarnsdager={false}
    />);
    wrapper.setState({ selectedItem: null });
    const rows = wrapper.find('Row');
    expect(rows).has.length(3);
    const checkBox = wrapper.find('CheckboxField');
    expect(checkBox).has.length(1);
    expect(checkBox.first().prop('name')).to.eql('manuellOverstyring');
    const uttakTimeLine = wrapper.find('UttakTimeLine');
    expect(uttakTimeLine).has.length(1);
    const uttakTimeLineData = wrapper.find('UttakTimeLineData');
    expect(uttakTimeLineData).has.length(0);
    const confirmKnapp = wrapper.find('Hovedknapp');
    expect(confirmKnapp).has.length(0);
  });

  it('skal rendre uttak, med manuell overstyring', () => {
    const wrapper = shallow(<Uttak
      readOnly={false}
      formName="UttakForm"
      behandlingFormPrefix=""
      soknadDate="2018-03-02"
      familiehendelseDate="2018-02-02"
      endringsDate="2018-01-12"
      hovedsokerKjonnKode=""
      periodeTyper={[]}
      uttaksresultatActivity={uttakActivities}
      uttakPerioder={uttakActivities}
      reduxFormChange={sinon.spy()}
      reduxFormInitialize={sinon.spy()}
      submitting={false}
      isDirty={false}
      manuellOverstyring
      kanOverstyre
      isApOpen={false}
      stonadskonto={stonadskonto}
      allAksjonspunkter={[]}
      intl={intlMock}
      soknadsType="ST-001"
      harSoktOmFlerbarnsdager={false}
      annenForelderSoktOmFlerbarnsdager={false}
    />);
    wrapper.setState({ selectedItem: uttakActivities[0] });
    const checkBox = wrapper.find('CheckboxField');
    expect(checkBox).to.have.length(1);
    const formattedMessage = wrapper.find('FormattedMessage');
    expect(formattedMessage).to.have.length(1);
    expect(formattedMessage.first().prop('id')).to.eql('Uttak.Confirm');
    expect(checkBox.first().prop('name')).to.eql('manuellOverstyring');
    const uttakTimeLine = wrapper.find('UttakTimeLine');
    expect(uttakTimeLine).to.have.length(1);
    const uttakTimeLineData = wrapper.find('UttakTimeLineData');
    expect(uttakTimeLineData).to.have.length(1);
    const confirmKnapp = wrapper.find('Hovedknapp');
    expect(confirmKnapp).to.have.length(1);
    expect(confirmKnapp.first().prop('disabled')).to.eql(true);
  });

  it('skal rendre uttak, uten overstyrerrolle', () => {
    const wrapper = shallow(<Uttak
      readOnly={false}
      formName="UttakForm"
      behandlingFormPrefix=""
      soknadDate="2018-03-02"
      familiehendelseDate="2018-02-02"
      endringsDate="2018-01-12"
      hovedsokerKjonnKode=""
      periodeTyper={[]}
      uttaksresultatActivity={uttakActivities}
      uttakPerioder={[]}
      reduxFormChange={sinon.spy()}
      reduxFormInitialize={sinon.spy()}
      submitting={false}
      isDirty={false}
      manuellOverstyring={false}
      kanOverstyre={false}
      stonadskonto={stonadskonto}
      allAksjonspunkter={[]}
      intl={intlMock}
      soknadsType="ST-001"
      harSoktOmFlerbarnsdager={false}
      annenForelderSoktOmFlerbarnsdager={false}
    />);
    wrapper.setState({ selectedItem: uttakActivities[0] });
    const checkBox = wrapper.find('CheckboxField');
    expect(checkBox).to.have.length(0);
    const uttakTimeLine = wrapper.find('UttakTimeLine');
    expect(uttakTimeLine).to.have.length(1);
    const uttakTimeLineData = wrapper.find('UttakTimeLineData');
    expect(uttakTimeLineData).to.have.length(1);
    const confirmKnapp = wrapper.find('Hovedknapp');
    expect(confirmKnapp).to.have.length(0);
  });

  it('skal rendre uttak, med aksjonspunkt', () => {
    const aksjonspunkter = [{
      id: 1,
      definisjon: {
        kode: aksjonspunktCodes.FASTSETT_UTTAKPERIODER,
        navn: 'ap1',
      },
      status: {
        kode: 'UTFO',
        navn: 's1',
      },
      toTrinnsBehandling: true,
      toTrinnsBehandlingGodkjent: false,
      kanLoses: true,
      erAktivt: true,
    }];
    const wrapper = shallow(<Uttak
      readOnly={false}
      aksjonspunkter={aksjonspunkter}
      formName="UttakForm"
      behandlingFormPrefix=""
      soknadDate="2018-03-02"
      familiehendelseDate="2018-02-02"
      endringsDate="2018-01-12"
      hovedsokerKjonnKode=""
      periodeTyper={[]}
      uttaksresultatActivity={uttakActivities}
      uttakPerioder={[]}
      reduxFormChange={sinon.spy()}
      reduxFormInitialize={sinon.spy()}
      submitting={false}
      isDirty={false}
      manuellOverstyring={false}
      kanOverstyre={false}
      isApOpen
      stonadskonto={stonadskonto}
      allAksjonspunkter={[]}
      intl={intlMock}
      soknadsType="ST-001"
      harSoktOmFlerbarnsdager={false}
      annenForelderSoktOmFlerbarnsdager={false}
    />);
    wrapper.setState({ selectedItem: uttakActivities[0] });
    const checkBox = wrapper.find('CheckboxField');
    expect(checkBox).to.have.length(0);
    const formattedMessage = wrapper.find('FormattedMessage');
    expect(formattedMessage).to.have.length(1);
    expect(formattedMessage.first().prop('id')).to.eql('Uttak.Overstyrt');
    const uttakTimeLine = wrapper.find('UttakTimeLine');
    expect(uttakTimeLine).to.have.length(1);
    const uttakTimeLineData = wrapper.find('UttakTimeLineData');
    expect(uttakTimeLineData).to.have.length(1);
    const confirmKnapp = wrapper.find('Hovedknapp');
    expect(confirmKnapp).to.have.length(0);
  });

  it('skal rendre uttak, med uttakTimeLineData', () => {
    const aksjonspunkter = [{
      id: 1,
      definisjon: {
        kode: aksjonspunktCodes.FASTSETT_UTTAKPERIODER,
        navn: 'ap1',
      },
      status: {
        kode: 'UTFO',
        navn: 's1',
      },
      toTrinnsBehandling: true,
      toTrinnsBehandlingGodkjent: false,
      kanLoses: true,
      erAktivt: true,
    }];
    const wrapper = shallow(<Uttak
      readOnly={false}
      aksjonspunkter={aksjonspunkter}
      formName="UttakForm"
      behandlingFormPrefix=""
      soknadDate="2018-03-02"
      familiehendelseDate="2018-02-02"
      endringsDate="2018-01-12"
      hovedsokerKjonnKode=""
      periodeTyper={[]}
      uttaksresultatActivity={uttakActivities}
      uttakPerioder={uttakActivities}
      reduxFormChange={sinon.spy()}
      reduxFormInitialize={sinon.spy()}
      submitting={false}
      isDirty={false}
      manuellOverstyring={false}
      kanOverstyre={false}
      isApOpen
      stonadskonto={stonadskonto}
      allAksjonspunkter={[]}
      intl={intlMock}
      soknadsType="ST-001"
      harSoktOmFlerbarnsdager={false}
      annenForelderSoktOmFlerbarnsdager={false}
    />);
    wrapper.setState({ selectedItem: uttakActivities[0] });
    expect(wrapper.state('selectedItem')).to.eql(uttakActivities[0]);

    const uttakTimeLineData = wrapper.find('UttakTimeLineData');
    expect(uttakTimeLineData).to.have.length(1);

    const uttakTimeLine = wrapper.find('UttakTimeLine');
    uttakTimeLine.prop('openPeriodInfo', { preventDefault: sinon.spy() });
    wrapper.update();
    expect(wrapper.state('selectedItem')).to.eql(uttakActivities[0]);

    uttakTimeLineData.prop('callbackForward')({ preventDefault() {} });
    wrapper.update();
    expect(wrapper.state('selectedItem')).to.eql(uttakActivities[1]);

    uttakTimeLineData.prop('callbackBackward')({ preventDefault() {} });
    wrapper.update();
    expect(wrapper.state('selectedItem')).to.eql(uttakActivities[0]);

    uttakTimeLineData.prop('callbackCancelSelectedActivity')();
    wrapper.update();
    expect(wrapper.state('selectedItem')).is.undefined;
  });
});
