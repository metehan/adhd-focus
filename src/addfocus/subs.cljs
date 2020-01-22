(ns addfocus.subs
  (:require 
   [re-frame.core :as rf]))


(rf/reg-sub ::counter
            (fn [db [_ counter-id]]
              (get-in db [::counters counter-id])))

(rf/reg-sub ::counter-ids
            (fn [db _]
              (-> (get db ::counters)
                  (keys))))