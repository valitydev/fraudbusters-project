import cases from './cases/index.js';
import { text } from '../../lib/env.js';
import { featuresOf } from '../../lib/features.js';
import { defineSuite } from '../../lib/suite.js';

const PHASES = ['prepare', 'verify'];
const phase = text('K6_PHASE', 'prepare');
if (!PHASES.includes(phase)) throw new Error(`K6_PHASE must be ${PHASES.join(' or ')}, got ${phase}`);
if (!__ENV.RUN_ID) throw new Error('RUN_ID is required: both phases use the identifiers of one run');

const suite = defineSuite({
  name: `lifecycle-${phase}`,
  profile: phase,
  runId: () => __ENV.RUN_ID,
  features: featuresOf(__ENV.FROM_FEATURES === undefined ? 'FEATURES' : 'FROM_FEATURES'),
  cases: cases.map((item) => ({ ...item, profiles: ['functional', 'smoke'], run: item[phase] })),
});

export const options = suite.options;
export const setup = suite.setup;
export const runCases = suite.runCases;
export const handleSummary = suite.handleSummary;
