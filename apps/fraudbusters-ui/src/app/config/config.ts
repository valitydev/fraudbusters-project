import { getBaseClass } from './get-base-class';
import type appConfig from '../../assets/appConfig.json';

export type Config = typeof appConfig;
export const BASE_CONFIG = getBaseClass<Config>();
