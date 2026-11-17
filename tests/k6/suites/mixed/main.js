import cases from './cases/index.js';
import { defineSuite } from '../../lib/suite.js';
import { trafficOf } from '../traffic.js';

const suite = defineSuite({ name: 'mixed', cases, traffic: trafficOf('mixed') });

export const options = suite.options;
export const setup = suite.setup;
export const runCases = suite.runCases;
export const runTraffic = suite.runTraffic;
export const runReconfigure = suite.runReconfigure;
export const handleSummary = suite.handleSummary;
