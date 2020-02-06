
window.connectRemote = function() {
  const remoteStorage = new RemoteStorage({
    changeEvents: {
      local: true,
      window: true,
      remote: true,
      conflicts: true
    }
  });

    // Init remote storage //
    remoteStorage.setApiKeys({
      dropbox: '86gt09zt4ykmr3w'
    });
    remoteStorage.access.claim('addfocus', 'rw');

    // Create & Add connection widget
  const widget = new Widget(remoteStorage, {modalBackdrop: false});
    widget.attach('settings');

    //Create scope for remote storage
    window.rsClient = remoteStorage.scope('/addfocus/');

    //Helper functions
    const log = function(){
      console.log("Data sent to remote storage.")
    }

    window.saveRemoteData = function (name, content, callback) {
      window.rsClient.storeFile(
        'application/json', 
        name + '.json', 
        JSON.stringify(content)
      ).then(() => {if (typeof callback !== 'undefined'){callback()}});

    }

    window.loadRemoteData = function (name, callback) {
      window.rsClient.getFile(name + '.json')
        .then(file => {callback(JSON.parse(file.data))});
    }

  // TODO: When connected loadRemoteData 
  //remoteStorage.on('connected', function () {
    // Storage account has been connected, let’s roll!
  //});

   //window.saveRemoteText = function (name, content, callback) {
   //  window.rsClient.storeFile('text/plain', name + '.txt', content)
   //    .then(() => {if (typeof callback !== 'undefined') { callback() } });
   //}
  }