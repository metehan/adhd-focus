(ns addfocus.events
  (:require
   [re-frame.core :as rf]))

(rf/reg-event-db :initialize
                 (constantly {::counters {"A" 0
                                          "B" 0
                                          "C" 0}}))

(rf/reg-event-db :inc-counter
                 (fn [db [_ counter-id]]
                   (update-in db [::counters counter-id] inc)))


