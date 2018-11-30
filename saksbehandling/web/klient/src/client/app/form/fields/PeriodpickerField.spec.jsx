import React from 'react';
import { createStore, combineReducers } from 'redux';
import { Provider } from 'react-redux';
import { IntlProvider } from 'react-intl';
import { reduxForm, reducer as formReducer } from 'redux-form';
import { mount } from 'enzyme';
import { expect } from 'chai';
import { messages } from 'testHelpers/intl-enzyme-test-helper';

import PeriodpickerField from './PeriodpickerField';

const MockForm = reduxForm({ form: 'mock' })(({ handleSubmit, children }) => <form onSubmit={handleSubmit}>{children}</form>);
const mountFieldInForm = (field, initialValues) => mount(
  <Provider store={createStore(combineReducers({ form: formReducer }))}>
    <IntlProvider locale="nb-NO" messages={messages}>
      <MockForm initialValues={initialValues}>
        {field}
      </MockForm>
    </IntlProvider>
  </Provider>,
);

describe('<PeriodpickerField>', () => {
  it('skal formatere fra ISO-format til norsk datoformat', () => {
    const wrapper = mountFieldInForm(<PeriodpickerField names={['fomDato', 'tomDato']} />, { fomDato: '2017-02-01', tomDato: '2017-05-11' });
    const input = wrapper.find('Input');
    expect(input).to.have.length(1);
    expect(input.props().value).to.eql('01.02.2017 - 11.05.2017');
  });
});
