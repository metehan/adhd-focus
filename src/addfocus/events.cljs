(ns addfocus.events
  (:require
   [re-frame.core :as rf]))

(rf/reg-event-db 
 :initialize
 (constantly 
  {:tasks
   [
    {:id 10 :task "Write some code"
     :time 12 :important true  :urgent true }
    {:id 11 :task "Code Clojurescript"
     :time 45 :important true  :urgent true }
    {:id 12 :task "Publish the app"
     :time 25 :important false :urgent true }
    {:id 13 :task "Code 1 hour"
     :time 60 :important false :urgent true }
    {:id 14 :task "Call ADHD expert"
     :time 5 :important true  :urgent false}
    {:id 15 :task "Call pizza"
     :time 2 :important true  :urgent false}
    {:id 16 :task "Make coffee"
     :time 4 :important false :urgent false}
    {:id 17 :task "Cook something nice"
     :time 40 :important false :urgent false}
    ]
   :active-task 14}
  ))

(rf/reg-event-db 
 :active-task
 (fn [db [_ task-id]]
   (assoc db :active-task task-id)))


