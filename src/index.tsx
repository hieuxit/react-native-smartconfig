import { NativeModules } from 'react-native';

type SmartConfigRequest = {
  ssid: string;
  password: string;
};

type SmartConfigResult = {
  bssid: string;
  ipv4: string;
};

type SmartconfigType = {
  start(request: SmartConfigRequest): Promise<SmartConfigResult>;
  stop(): void;
};

const { Smartconfig } = NativeModules;

export default Smartconfig as SmartconfigType;
