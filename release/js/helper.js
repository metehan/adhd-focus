//window.timerWorker = new Worker("timer-worker.js")

window.timerBar = function(minutes, start, callback) {
  const target = minutes * 60 * 1000;
  const grower = document.getElementsByClassName("growing-bar");
  clearInterval(window.timerInterval)

  let passed = start * 60 * 1000;
  let percent =  passed / target * 100;;

  for (i = 0; i < grower.length; i++) {
    grower[i].style.width = percent.toString() + "%";
  }

    window.timerInterval = setInterval(function () {
      if (percent < 100) {
        passed = passed + 1000
        percent = passed / target * 100;
        for (i = 0; i < grower.length; i++) {
          grower[i].style.width = percent.toString() + "%";
        }
      }else {
        clearInterval(window.timerInterval)
        callback()
      }
    }, 1000);

window.stopTimerBar = function(){
  clearInterval(window.timerInterval)
}
}