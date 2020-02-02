(ns addfocus.events
  (:require
   [re-frame.core :as rf]))

(rf/reg-event-db 
 :initialize
 (constantly 
  {:tasks
   [
    {:id 10 :task "Save statistics of work"
     :time 12 :important true  :urgent true }
    {:id 11 :task "Create slot machine for gifts"
     :time 45 :important true  :urgent true }
    {:id 12 :task "Publish the app"
     :time 25 :important false :urgent true }
    {:id 13 :task "Complete i forgot the sentence"
     :time 60 :important false :urgent true }
    {:id 14 :task "ADHD focused to do app"
     :time 5 :important true  :urgent false}
    {:id 15 :task "Create sub projects for todo lists"
     :time 2 :important true  :urgent false}
    {:id 16 :task "Tagging system maybe?"
     :time 4 :important false :urgent false}
    {:id 17 :task "Save tasks to cloud"
     :time 40 :important false :urgent false}
    ]
   :active-task 17
   :id_counter 17}
  ))

(rf/reg-event-db 
 :active-task
 (fn [db [_ task-id]]
   (assoc db :active-task task-id)))

(rf/reg-fx
 :save-to-cloud
 (fn [tasks]
   (.saveToRemoteData js/window
    "tasks" (.stringify js/JSON (clj->js tasks)))))

(rf/reg-fx
 :save-to-cloud-text
 (fn [[name text]]
   (.saveToRemoteText js/window name text)))


(rf/reg-event-fx
 :new-task
 (fn [{:keys [db]} [_ task]]
   (let
    [id (+ (:id_counter db) 1)
     newdb (-> db
               (assoc :id_counter id)
               (assoc :tasks (conj (:tasks db) (assoc task :id  id))))]
     {:db newdb
      :save-to-cloud (:tasks newdb)})))

