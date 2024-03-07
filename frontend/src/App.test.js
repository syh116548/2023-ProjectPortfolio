import * as React from 'react';
import { render } from '@testing-library/react';
import App from './App.js';

test('renders app without crashing', () => {
  render(<App />);
});
