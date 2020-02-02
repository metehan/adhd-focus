

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
    const widget = new Widget(remoteStorage);
    widget.attach('app');

    //Create scope for remote storage
    window.rsClient = remoteStorage.scope('/addfocus/');

    //Helper functions
    const log = function(){
      console.log("Data sent to remote storage.")
    }

    window.saveToRemoteData = function (name, content, callback = log) {
      window.rsClient.storeFile('application/json', name + '.json', content)
        .then(callback());

    }

    window.saveToRemoteText = function (name, content, callback = log) {
      window.rsClient.storeFile('text/plain', name + '.txt', content)
        .then(callback());
    }

    //SAVE DATA DEMO

    // List all items in the "foo/" category/folder
    //window.rsClient.getListing('')
    //  .then(listing => console.log(listing));

    // Write some text to "addfocus/bam.json"
    //const content = '{"name":"somesink","amount": "1 lt"}'
    //window.rsClient.storeFile('application/json', 'bam.json', content)
    //  .then(() => console.log("data has been saved"));
