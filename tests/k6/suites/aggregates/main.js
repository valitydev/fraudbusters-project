import cases from './cases/index.js';
import { defineSuite } from '../../lib/suite.js';
import { aggregatesTraffic } from './traffic.js';

// Traffic only: without K6_PROFILE the suite runs baseline, since the default functional profile has no cases here.
if (!__ENV.K6_PROFILE) __ENV.K6_PROFILE = 'baseline';

const shape = JSON.parse(open('../../data/aggregates/shape.json'));
const suite = defineSuite({ name: 'aggregates', cases, traffic: aggregatesTraffic(shape) });

export const options = suite.options;
export const setup = suite.setup;
export const runCases = suite.runCases;
export const runTraffic = suite.runTraffic;
export const runReconfigure = suite.runReconfigure;
export const handleSummary = suite.handleSummary;
