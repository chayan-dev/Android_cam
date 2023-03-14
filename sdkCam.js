import RNFS from 'react-native-fs';
import ImageResizer from 'react-native-image-resizer';
import {NativeModules} from 'react-native'


export const onCaptureHandler = async (state, image_base64) => {
    console.log(
      '****  Receiving capture event data',
      JSON.stringify(state, null, 1),
    );
    let {direction, nextStep, previewList} = state;
    let {isAutomatic} = state;
    if (nextStep == 'right' || nextStep == 'left') {
      isAutomatic = true;
      direction = nextStep;
    }
    let stepsTaken = [...state.stepsTaken, state.nextStep];
    let rowCount = stepsTaken.reduce(
      (rowCount, step) => (step == 'down' ? (rowCount += 1) : rowCount),
      1,
    );
  
    const {filepath, thumbnail} = await base64ToJpg(image_base64);
    const lastIndex = previewList.length - 1;
    const index = previewList.length ? String(lastIndex + 1) : '0';
    let imageObj = {index, filepath, thumbnail};
    const modifiedPreviewList = [...previewList, imageObj];
    let updatedState = {
      ...state,
      isAutomatic,
      row: rowCount,
      stepsTaken,
      direction,
      nextStep: '',
      previewList: modifiedPreviewList,
    };
    const {top, left, right} = getOverlaps(updatedState);
    updatedState = {...updatedState, top, left, right};
    console.log(
      '742: dispatching capture data',
      JSON.stringify(updatedState, null, 1),
    );
  
    dispatchToNativeSDK(updatedState);
  };
  
  export const onDirectionPressHandler = (state, step) => {
    console.log(
      '751: received direction data: ',
      JSON.stringify({step, state}, null, 1),
    );
    let updatedState = {...state, nextStep: step};
    const {top, left, right} = getOverlaps(updatedState);
    updatedState = {...updatedState, top, left, right};
    console.log(
      '751: dispatching direction data',
      JSON.stringify(updatedState, null, 1),
    );
  
    dispatchToNativeSDK(updatedState);
  };
  
  export const onDiscardImageHandler = state => {
    console.log('*** Discarding image', JSON.stringify(state, null, 1));
  
    const {row, isAutomatic, direction, previewList} = state;
    let data = [...previewList];
  
    const removedImage = data.pop();
    console.log({removedImage});
    RNFS.unlink(removedImage.filepath);
    RNFS.unlink(removedImage.thumbnail);
  
    let sT = [...state.stepsTaken];
    sT.pop();
    let auto = isAutomatic;
    let dir = direction;
    if (data.length <= row) {
      auto = false;
      dir = null;
    }
    let rowCount = sT.reduce((rC, step) => (step == 'down' ? (rC += 1) : rC), 1);
  
    const payload = {
      ...state,
      previewList: data,
      isAutomatic: auto,
      stepsTaken: sT,
      row: rowCount,
      nextStep: null,
      direction: dir,
    };
    console.log(
      '** 726: dispatching data after discard',
      JSON.stringify(payload, null, 1),
    );
    dispatchToNativeSDK(payload);
  
    /** NO NEED TO DELETE FROM FS */
    //   const removedImage = data.pop();
    //   RNFS.unlink(removedImage.uri);
    //   RNFS.unlink(removedImage.thumbnail);
    /*** */
  };
  
  export const onSubmitBarcodeImageHandler = (state, mode, uploadParam, cb) => {
    // const gridOffset = 0; // Hardcoding because, we are not handling invalid case (unlike non-barcode cam case)
    // console.log('108', {uploadParam});
    // showToastonUpload();
    // const batch_uuid = Date.now() + uuid.v1();
  
    // const {previewList, row, direction} = state;
    // let {metadata, planogram_id, asset_id} = store.getState().beatPlan;
  
    // metadata = Object.keys(metadata.upload).length
    //   ? {metadata: JSON.stringify(metadata.upload)}
    //   : {};
  
    // previewList.map((data, i) => {
    //   let actualDirection = direction;
    //   let actualRow = row;
    //   let sign = actualDirection == 'left' ? -1 : 1;
    //   let x = parseInt((i + gridOffset) / actualRow) * sign;
    //   if (x == -0) {
    //     x = 0;
    //   }
    //   let y = (i + gridOffset) % actualRow;
  
    //   const dimension = [actualRow, Math.ceil(previewList.length / actualRow)];
    //   const position = [x, y];
  
    //   const base64Image = data.image_base64; // your base64 image string
    //   const filename = `${batch_uuid}_${i + 1}`;
    //   //const path = RNFS.DocumentDirectoryPath + `/${filename}.jpg`;
    //   //const position = JSON.stringify([i, size]);
    //   //const dimension = JSON.stringify([1, size]);
  
    //   const imgData = {
    //     ...metadata,
    //     uri: data.filepath,
    //     thumbnail: {
    //       uri: data.thumbnail,
    //       name: `${batch_uuid}_${i + 1}_thumbnail.jpg`,
    //     },
    //     batch_uuid,
    //     position: JSON.stringify(position),
    //     dimension: JSON.stringify(dimension),
    //     filename: `${filename}.jpg`,
    //     image_type: previewList.length > 1 ? 'multiple' : 'single',
    //     app_timestamp: getTimeStamp(),
    //     last_image_flag: i == previewList.length - 1 ? 1 : 0,
    //     total_image_captured: previewList.length,
    //     orientation: mode,
    //   };
  
    //   uploadImageToServer(imgData, uploadParam);
    // });
  
    // const timestamp = getTimeStamp();
    // const {activeStoreId, activeCategoryId} = uploadParam;
    // const factors = {
    //   store_id: activeStoreId,
    //   category_id: activeCategoryId,
    //   planogram_id,
    //   asset_id,
    // };
  
    // const callBackData = {
    //   timestamp,
    //   no_of_images: previewList.length,
    //   session_id: batch_uuid,
    // };
  
    // const lastDLData = {factors, callBackData};
    // console.log(JSON.stringify(lastDLData, null, 1));
    // setLastDLDataInLocalStorage(lastDLData);
    // store.dispatch(pushCallbackData(callBackData));
    // cb(callBackData); // Navigative to review screen
  };
  
  const getOverlaps = state => {
    const {row, previewList, nextStep, direction} = state;
  
    //console.log('765: getOverlap state:', JSON.stringify(state, null, 1));
  
    let top = '';
    let left = '';
    let right = '';
    let tempRow = row;
    // let overlayPosition = {opacity: 0};
  
    switch (nextStep) {
      case 'right':
        // overlayPosition = {top: 0, left: '-80%', opacity: 0.6};
        break;
      case 'left':
        // overlayPosition = {top: 0, right: '-80%', opacity: 0.6};
        break;
      case 'down':
        tempRow += 1;
        // overlayPosition = {top: '-80%', left: 0, opacity: 0.6};
        break;
    }
  
    top = previewList[previewList.length - 1];
    left = previewList[previewList.length - tempRow];
    right = previewList[previewList.length - tempRow];
    const x = {
      previewListLenght: previewList.length,
      top,
      left,
      right,
      nextStep,
      direction,
      tempRow,
    };
  
    console.log('767', JSON.stringify(x, null, 1));
    if (previewList.length % tempRow == 0) {
      top = '';
    }
    if (nextStep == 'left' || direction == 'left') {
      left = '';
    }
    if (nextStep == 'right' || direction == 'right') {
      right = '';
    }
  
    console.log('nextStep:', nextStep);
    console.log('!nextStep:', !nextStep);
    console.log('direction:', direction);
    console.log('!direction:', !direction);
  
    if (!nextStep && !direction) {
      console.log('inside chk, 786');
      left = right = top = '';
    }
  
    const data = {top, left, right};
    console.log('779', JSON.stringify(data, null, 1));
    return data;
  };
  
  const dispatchToNativeSDK = data => {
    NativeModules.Bridge.dispatchDataToNativeSide(JSON.stringify(data));
  };



  const base64ToJpg = async base64Image => {
    const random_uuid = uuid.v4();
    const filename = `${random_uuid}`;
  
    const dirHome = RNFS.DocumentDirectoryPath;
    const dirPictures = `${dirHome}/capturedImages/`;
    const dirThumbnails = `${dirHome}/thumbnails`;
  
    const path = dirPictures + `${filename}.jpg`;
    //const path_thumbnail = RNFS.DocumentDirectoryPath + `/${filename}_thumbnail.jpg`;
    await RNFS.mkdir(dirPictures);
    await RNFS.writeFile(path, base64Image, 'base64');
    const thumbnail = await ImageResizer.createResizedImage(
      path,
      50,
      50,
      'JPEG',
      75,
      0,
      null,
    );
    return {filepath: path, thumbnail: thumbnail.uri};
  };
  