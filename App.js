import React from 'react';
import {View, Button, NativeModules, NativeEventEmitter} from 'react-native';
import {
  onCaptureHandler,
  onDismissPressHandler,
  onDirectionPressHandler,
  onDiscardImageHandler,
  onSubmitBarcodeImageHandler,
} from './sdkCam';
class App extends React.Component {
  componentDidMount() {
    this._addListenersForNativeCamSDK();
  }

  componentWillUnmount() {
    console.log('unmount');

    this.eventCaptureListener?.remove();
  }

  _addListenersForNativeCamSDK = () => {
    const eventEmitter = new NativeEventEmitter(NativeModules.Bridge);
    this.eventCaptureListener = eventEmitter.addListener(
      'CaptureEvent',
      ({state, image_base64}) => {
        console.log('683', state, image_base64);
        onCaptureHandler(state, image_base64);
      },
    );

    this.eventDirectionListener = eventEmitter.addListener(
      'DirectionEvent',
      ({state, direction}) => {
        onDirectionPressHandler(state, direction);
      },
    );

    this.eventDiscardListener = eventEmitter.addListener(
      'DiscardCaptureEvent',
      ({state}) => {
        console.log(
          '686: data received on discard',
          JSON.stringify(state, null, 1),
        );
        onDiscardImageHandler(state);
      },
    );

    this.eventSubmitListener = eventEmitter.addListener(
      'SubmitImageEvent',
      ({state, results, mode, upload_params}) => {
        return;
        // upload_params = JSON.parse(upload_params);
        // console.log(
        //   '711: data received on submit',
        //   JSON.stringify({state, mode, upload_params}, null, 1),
        // );
        // const callBack = this._navigateToReviewCallbackForDL;
        // onSubmitBarcodeImageHandler(state, mode, upload_params, callBack);
      },
    );

    this.eventDismissCameraListener = eventEmitter.addListener(
      'DismissCameraEvent',
      ({state}) => {
        onDismissPressHandler(state.previewList.length);
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
