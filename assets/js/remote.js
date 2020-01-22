(function () {

  var remoteStorage = new RemoteStorage({
    changeEvents: { 
      local: true, 
      window: true, 
      remote: true, 
      conflicts: true}
  });


  const init = function () {
    // Init remote storage //
    remoteStorage.setApiKeys({
      dropbox: '86gt09zt4ykmr3w'
    });
    remoteStorage.access.claim('addfocus', 'rw');

    // Create & Add connection widget
    var widget = new Widget(remoteStorage);
    widget.attach('app');
    
    
    //SAVE DATA DEMO
    const client = remoteStorage.scope('/addfocus/');
    
    // List all items in the "foo/" category/folder
    client.getListing('')
    .then(listing => console.log(listing));
    
    // Write some text to "addfocus/bam.json"
    const content = '{"name":"smozie","amount": "1 lt"}'
    client.storeFile('application/json', 'bam.json', content)
    .then(() => console.log("data has been saved"));
  }
  
  document.addEventListener('DOMContentLoaded', init);
})();