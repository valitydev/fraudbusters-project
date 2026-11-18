import cases from './cases/index.js';
import { defineSuite } from '../../lib/suite.js';

const suite = defineSuite({ name: 'payouts', since: 'payouts', cases });

export const options = suite.options;
export const setup = suite.setup;
export const runCases = suite.runCases;
export const handleSummary = suite.handleSummary;
