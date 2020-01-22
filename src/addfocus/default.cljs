(ns addfocus.default
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [addfocus.masterview :as masterview]
   [addfocus.events]
   [addfocus.subs]))

(defn counter
  "Given a counter id and its current value, render it as an interactive widget."
  [id]
  (let [counter-value @(rf/subscribe [::counter id])]
    [:div {:on-click #(rf/dispatch [:inc-counter id])
           :style    {:padding    20
                      :margin     "10px 0"
                      :background "rgba(0,0,0,0.05)"
                      :cursor     "pointer"}}
     (str "Counter " (name id) ": ")
     counter-value]))


(defn ^:export render []
  (r/render [masterview/index] 
            (js/document.getElementById "app")))

(defn ^:export init []
  (rf/dispatch-sync [:initialize])
  (render))

