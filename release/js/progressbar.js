let startingTime = 0
let endingTime = 1
let completeCallback = function(){}
let visualUpdates
let grower
let completeTimeout = setTimeout(function () {completeCallback() }, 1000)
let completionSound = document.getElementById("timer-completion")
let startSound = document.getElementById("timer-start")
let masterVolume = 0.6
completionSound.volume = masterVolume
startSound.volume = masterVolume

let updateVisuals = function () {
  const totalTime = endingTime - startingTime
  const passedTime = Date.now() - startingTime
  const percent = passedTime / totalTime * 100

  if (percent < 100) {
    for (i = 0; i < grower.length; i++) {
      grower[i].style.width = percent.toFixed(2) + "%"
    }
  } else {
    for (i = 0; i < grower.length; i++) {
      grower[i].style.width = "100%"
    }
    clearInterval(visualUpdates)
  }
}

window.timerBar = function (minutes, start, callback) {
  clearTimeout(completeTimeout)
  grower = document.getElementsByClassName("growing-bar")

  startingTime = Date.now() - start * 60 * 1000
  endingTime = startingTime + minutes * 60 * 1000

  const totalTime = endingTime - startingTime
  const passedTime = Date.now() - startingTime
  const percent = (passedTime / totalTime * 100).toFixed(2)

  startSound.play()

  for (i = 0; i < grower.length; i++) {
    grower[i].style.width = percent + "%"
  }

  visualUpdates = setInterval(() => {
    if (!document.hidden) {updateVisuals()}}, 100)

  completeCallback = callback;
  completeTimeout = setTimeout(() => {
    completionSound.play()
    completeCallback()
  }, endingTime - Date.now())
}

window.stopTimerBar = function () {
  clearTimeout(completeTimeout)
  clearInterval(visualUpdates)
}

window.clearTimerBar = function () {
  for (i = 0; i < grower.length; i++) {
    grower[i].style.width = "0%"
  }
}