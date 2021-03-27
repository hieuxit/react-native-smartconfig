import { NativeModules } from 'react-native';

type SmartconfigType = {
  multiply(a: number, b: number): Promise<number>;
};

const { Smartconfig } = NativeModules;

export default Smartconfig as SmartconfigType;
