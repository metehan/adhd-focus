(ns addfocus.default
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [addfocus.masterview :as masterview]
   [addfocus.events]
   [addfocus.subs]))

(defn load-data [context]
  (.loadRemoteData
   js/window
   context
   (fn [data]
     (rf/dispatch
      [:load-data (keyword context) (js->clj data :keywordize-keys true)])))
  )

(defn ^:export render []
  (r/render [masterview/index]
            (js/document.getElementById "app")))

(defn ^:export init []
  (do
    (rf/dispatch-sync [:initialize])
    (render)
    (.connectRemote js/window)
    (load-data "tasks")
    (load-data "history")))

