import React from 'react';
import { expect } from 'chai';
import { shallowWithIntl } from 'testHelpers/intl-enzyme-test-helper';
import { reduxFormPropsMock } from 'testHelpers/redux-form-test-helper';
import {
  PeriodpickerField, SelectField, CheckboxField, DecimalField, TextAreaField,
} from 'form/Fields';

import { Knapp, Hovedknapp } from 'nav-frontend-knapper';
import navBrukerKjonn from 'kodeverk/navBrukerKjonn';
import sinon from 'sinon';
import { UttakNyPeriode } from './UttakNyPeriode';

const periodeTyper = [{
  0: { kode: 'FELLESPERIODE', navn: 'Fellesperioden', kodeverk: 'UTTAK_PERIODE_TYPE' },
  1: { kode: 'MØDREKVOTE', navn: 'Mødrekvoten', kodeverk: 'UTTAK_PERIODE_TYPE' },
  2: { kode: 'FEDREKVOTE', navn: 'Fedrekvoten', kodeverk: 'UTTAK_PERIODE_TYPE' },
  3: { kode: 'FORELDREPENGER', navn: 'Foreldrepenger', kodeverk: 'UTTAK_PERIODE_TYPE' },
  4: { kode: 'ANNET', navn: 'Andre typer som f.eks utsettelse', kodeverk: 'UTTAK_PERIODE_TYPE' },
  5: { kode: 'FORELDREPENGER_FØR_FØDSEL', navn: 'Foreldrepenger før fødsel', kodeverk: 'UTTAK_PERIODE_TYPE' },
}];

const utsettelseÅrsaker = [{
  0: { kode: 'LOVBESTEMT_FERIE', navn: 'Lovbestemt ferie', kodeverk: 'UTSETTELSE_AARSAK_TYPE' },
  1: { kode: 'SYKDOM', navn: 'Avhengig av hjelp grunnet sykdom', kodeverk: 'UTSETTELSE_AARSAK_TYPE' },
  2: { kode: 'ARBEID', navn: 'Arbeid', kodeverk: 'UTSETTELSE_AARSAK_TYPE' },
  3: { kode: 'INSTITUSJONSOPPHOLD_SØKER', navn: 'Søker er innlagt i helseinstitusjon', kodeverk: 'UTSETTELSE_AARSAK_TYPE' },
  4: { kode: 'INSTITUSJONSOPPHOLD_BARNET', navn: 'Barn er innlagt i helseinstitusjon', kodeverk: 'UTSETTELSE_AARSAK_TYPE' },
}];

const overføringÅrsaker = [{
  0: {
    kode: 'INSTITUSJONSOPPHOLD_ANNEN_FORELDER',
    navn: 'Den andre foreldren er innlagt i helseinstitusjon',
    kodeverk: 'OVERFOERING_AARSAK_TYPE',
  },
  1: {
    kode: 'SYKDOM_ANNEN_FORELDER',
    navn: 'Den andre foreldren er pga sykdom avhengig av hjelp for å ta seg av barnet/barna',
    kodeverk: 'OVERFOERING_AARSAK_TYPE',
  },
}];

const nyPeriode = {
  fom: '2018-02-01',
  tom: '2018-03-05',
};

const arbeidsforhold = [
  {
    arbeidsforholdId: '234234234-23423423-23423',
    virksomhetNavn: 'STATOIL ASAAVD STATOIL SOKKELVIRKSOMHET',
    arbeidsforholdType: {
      kode: 'ARBEIDSTAKER',
      navn: 'Arbeidstaker',
    },
    virksomhetId: '987654321',
  },
];

const sokerKjonn = navBrukerKjonn.MANN;

const newPeriodeCallback = sinon.spy();
const newPeriodeResetCallback = sinon.spy();

describe('<UttakNyPeriode>', () => {
  it('skal vise skjema for ny utakksperiode', () => {
    const wrapper = shallowWithIntl(<UttakNyPeriode
      newPeriodeCallback={newPeriodeCallback}
      newPeriodeResetCallback={newPeriodeResetCallback}
      nyPeriode={nyPeriode}
      periodeTyper={periodeTyper}
      utsettelseÅrsaker={utsettelseÅrsaker}
      overføringÅrsaker={overføringÅrsaker}
      arbeidsforhold={arbeidsforhold}
      sokerKjonn={sokerKjonn}
      nyPeriodeDisabledDaysFom="2018-05-20"
      {...reduxFormPropsMock}
    />);

    const title = wrapper.find('FormattedMessage').first();
    expect(title.prop('id')).to.equal('UttakInfoPanel.NyPeriode');

    const periodpicker = wrapper.find(PeriodpickerField);
    expect(periodpicker).to.have.length(1);

    const select = wrapper.find(SelectField);
    expect(select).to.have.length(1);

    const checkbox = wrapper.find(CheckboxField);
    expect(checkbox).to.have.length(2);

    const textAreaField = wrapper.find(TextAreaField);
    expect(textAreaField).to.have.length(1);

    const okKnapp = wrapper.find('Hovedknapp');
    expect(okKnapp).to.have.length(1);
    expect(okKnapp.prop('mini')).is.true;
    expect(okKnapp.prop('htmlType')).is.eql('button');
    expect(okKnapp.prop('onClick')).is.eql(reduxFormPropsMock.handleSubmit);
    expect(okKnapp.find('FormattedMessage').prop('id')).to.equal('UttakInfoPanel.Oppdater');

    const avbrytKnapp = wrapper.find(Knapp);
    expect(avbrytKnapp).to.have.length(1);
    expect(avbrytKnapp.prop('mini')).is.true;

    expect(avbrytKnapp.prop('onClick')).is.eql(newPeriodeResetCallback);
    expect(avbrytKnapp.find('FormattedMessage').prop('id')).to.equal('UttakInfoPanel.Avbryt');
  });

  it('Skal regne ut og vise antall dager i valgt periode', () => {
    const wrapper = shallowWithIntl(<UttakNyPeriode
      newPeriodeCallback={newPeriodeCallback}
      newPeriodeResetCallback={newPeriodeResetCallback}
      nyPeriode={nyPeriode}
      periodeTyper={periodeTyper}
      utsettelseÅrsaker={utsettelseÅrsaker}
      overføringÅrsaker={overføringÅrsaker}
      arbeidsforhold={arbeidsforhold}
      sokerKjonn={sokerKjonn}
      nyPeriodeDisabledDaysFom="2018-05-20"
      {...reduxFormPropsMock}
    />);

    const weeksAndDays = wrapper.find('FormattedMessage').at(1);
    expect(weeksAndDays.prop('id')).to.eql('UttakInfoPanel.AntallFlereDagerOgFlereUker');
    expect(weeksAndDays.prop('values')).to.eql({
      weeks: '4',
      days: '3',
    });
  });

  it('skal vise dropdown for årsak til overføring av kvote hvis søker er kvinne og stønadskonto er FEDREKVOTE', () => {
    const wrapper = shallowWithIntl(<UttakNyPeriode
      newPeriodeCallback={newPeriodeCallback}
      newPeriodeResetCallback={newPeriodeResetCallback}
      nyPeriode={nyPeriode}
      periodeTyper={periodeTyper}
      utsettelseÅrsaker={utsettelseÅrsaker}
      overføringÅrsaker={overføringÅrsaker}
      arbeidsforhold={arbeidsforhold}
      sokerKjonn={sokerKjonn}
      nyPeriodeDisabledDaysFom="2018-05-20"
      {...reduxFormPropsMock}
    />);
    wrapper.setProps({
      nyPeriode: {
        periodeType: 'MØDREKVOTE',
      },
    });
    const input = wrapper.find(SelectField);
    expect(input).to.have.length(2);
  });

  it('skal vise dropdown for årsak for utsettelsen hvis type uttak er satt til utsettelse', () => {
    const wrapper = shallowWithIntl(<UttakNyPeriode
      newPeriodeCallback={newPeriodeCallback}
      newPeriodeResetCallback={newPeriodeResetCallback}
      nyPeriode={nyPeriode}
      periodeTyper={periodeTyper}
      utsettelseÅrsaker={utsettelseÅrsaker}
      overføringÅrsaker={overføringÅrsaker}
      arbeidsforhold={arbeidsforhold}
      sokerKjonn={sokerKjonn}
      nyPeriodeDisabledDaysFom="2018-05-20"
      {...reduxFormPropsMock}
    />);
    wrapper.setProps({
      nyPeriode: {
        typeUttak: 'utsettelse',
      },
    });
    const select = wrapper.find(SelectField);
    expect(select).to.have.length(2);
  });

  it('skal vise graderingandel hvis type uttak er gradert for og validere at graderingen er et heltall', () => {
    const wrapper = shallowWithIntl(<UttakNyPeriode
      newPeriodeCallback={newPeriodeCallback}
      newPeriodeResetCallback={newPeriodeResetCallback}
      nyPeriode={nyPeriode}
      periodeTyper={periodeTyper}
      utsettelseÅrsaker={utsettelseÅrsaker}
      overføringÅrsaker={overføringÅrsaker}
      arbeidsforhold={arbeidsforhold}
      sokerKjonn={sokerKjonn}
      nyPeriodeDisabledDaysFom="2018-05-20"
      {...reduxFormPropsMock}
    />);
    wrapper.setProps({
      nyPeriode: {
        typeUttak: 'gradert',
      },
    });
    const input = wrapper.find(DecimalField);
    expect(input).to.have.length(1);
  });

  it('skal vise prosentandel for samtidig uttak hvis samtidig uttak er valgt og validere at det er et heltall', () => {
    const wrapper = shallowWithIntl(<UttakNyPeriode
      newPeriodeCallback={newPeriodeCallback}
      newPeriodeResetCallback={newPeriodeResetCallback}
      nyPeriode={nyPeriode}
      periodeTyper={periodeTyper}
      utsettelseÅrsaker={utsettelseÅrsaker}
      overføringÅrsaker={overføringÅrsaker}
      arbeidsforhold={arbeidsforhold}
      sokerKjonn={sokerKjonn}
      nyPeriodeDisabledDaysFom="2018-05-20"
      {...reduxFormPropsMock}
    />);
    wrapper.setProps({
      nyPeriode: {
        samtidigUttak: true,
      },
    });
    const input = wrapper.find(DecimalField);
    expect(input).to.have.length(1);
  });

  it('Skal sende inn ny periode når man klikker på Oppdater', () => {
    const wrapper = shallowWithIntl(<UttakNyPeriode
      newPeriodeCallback={newPeriodeCallback}
      newPeriodeResetCallback={newPeriodeResetCallback}
      nyPeriode={nyPeriode}
      periodeTyper={periodeTyper}
      utsettelseÅrsaker={utsettelseÅrsaker}
      overføringÅrsaker={overføringÅrsaker}
      arbeidsforhold={arbeidsforhold}
      sokerKjonn={sokerKjonn}
      nyPeriodeDisabledDaysFom="2018-05-20"
      {...reduxFormPropsMock}
    />);
    wrapper.setProps({
      nyPeriode: {
        periodeType: 'FEDREKVOTE',
      },
    });
    const okKnapp = wrapper.find(Hovedknapp);
    okKnapp.simulate('click');
    expect(reduxFormPropsMock.handleSubmit.called).is.true;
  });
});
