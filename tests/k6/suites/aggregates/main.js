import cases from './cases/index.js';
import { defineSuite } from '../../lib/suite.js';
import { aggregatesTraffic } from './traffic.js';

const shape = JSON.parse(open('../../data/aggregates/shape.json'));
const suite = defineSuite({ name: 'aggregates', cases, traffic: aggregatesTraffic(shape) });

export const options = suite.options;
export const setup = suite.setup;
export const runCases = suite.runCases;
export const runTraffic = suite.runTraffic;
export const runReconfigure = suite.runReconfigure;
export const handleSummary = suite.handleSummary;
