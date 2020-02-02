(ns tools.timerbar)

(defn update-bar [percent]
  (let [grower (.getElementsByClassName js/document "growing-bar")]
    (for [x (js->clj grower)] 
      (set! (.-width (.-style x)) (str percent "%")))))