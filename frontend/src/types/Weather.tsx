import {Current} from './Current';
import {Daily} from './Daily';
import {Hourly} from './Hourly';

export interface Weather {
  latitude: number;
  longitude: number;
  timezone: string;
  elevation: number;
  current: Current;
  daily: Daily[];
  hourly: Hourly[];
}

