(function () {

  var remoteStorage = new RemoteStorage({
    changeEvents: { local: true, window: true, remote: true, conflicts: true }
  });


  function init() {
    remoteStorage.access.claim('addfocus', 'rw');
    remoteStorage.setApiKeys({
      dropbox: '86gt09zt4ykmr3w'
    });
    var widget = new Widget(remoteStorage);
    widget.attach('app');
    remoteStorage.addfocus.init();

    remoteStorage.addfocus.on('change', function (event) {
      if (event.newValue && (!event.oldValue)) {
        console.log('Change from ' + event.origin + ' (add)', event);
      }
    });
  }
  document.addEventListener('DOMContentLoaded', init);
})();