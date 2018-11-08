import React from 'react';
import { createStore, combineReducers } from 'redux';
import { Provider } from 'react-redux';
import { IntlProvider } from 'react-intl';
import { reduxForm, reducer as formReducer } from 'redux-form';
import { mount } from 'enzyme';
import { expect } from 'chai';
import { messages } from 'testHelpers/intl-enzyme-test-helper';
import RadioGroupField from './RadioGroupField';
import RadioOption from './RadioOption';

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

describe('<RadioGroupField>', () => {
  it('Skal rendre radio inputs', () => {
    const wrapper = mountFieldInForm(
      <RadioGroupField label="label" columns={4} name="name">
        <RadioOption label="label" value />
        <RadioOption label="label" value={false} />
      </RadioGroupField>,
    );
    expect(wrapper.find('input')).to.have.length(2);
    expect(wrapper.find('input[type="radio"]')).to.have.length(2);
  });

  it('Skal rendre med fullbredde', () => {
    const wrapper = mountFieldInForm(
      <RadioGroupField label="label" bredde="fullbredde" name="name">
        <RadioOption label="label" value />
        <RadioOption label="label" value={false} />
      </RadioGroupField>,
    );
    expect(wrapper.find('div.input--fullbredde')).to.have.length(1);
  });
});
