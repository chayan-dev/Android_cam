import React from 'react';
import {View, Button, NativeModules, NativeEventEmitter} from 'react-native';
class App extends React.Component {
  componentDidMount() {
    this._addListenersForNativeCamSDK();
  }

  componentWillUnmount() {
    console.log('unmount');

    this.eventCaptureListener?.remove();
  }

  _addListenersForNativeCamSDK = () => {
    console.log(13);
    const eventEmitter = new NativeEventEmitter();
    this.eventCaptureListener = eventEmitter.addListener(
      "JS-Event",
      nativeData => {
        console.log({nativeData});
      },
    );
  };

  render() {
    return (
      <View>
        <Button
          title="launch camera"
          onPress={() => {
            NativeModules.CameraModule.openCamera();
          }}
        />
      </View>
    );
  }
}

export default App;