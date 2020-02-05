(ns addfocus.events
  (:require
   [re-frame.core :as rf]
   [tools.time :as time]))

(rf/reg-event-db
 :initialize
 (constantly
  {:tasks []
   :active-task {:id 0
                 :state :ready
                 :start-time (time/now)
                 :paused-at (time/now)
                 :pause-duration 0
                 :paused-times 0
                 :end-time (time/now)}}))

(rf/reg-event-db
 :load-data
 (fn [db [_ context data]]
   (assoc db context data)))

(rf/reg-fx
 :save-to-cloud
 (fn [tasks]
   (.saveRemoteData js/window
                    "tasks" (clj->js tasks))))

(rf/reg-event-fx
 :new-task
 (fn [{:keys [db]} [_ task start-now]]
   (let
    [id (time/now)
     newdb (-> db
               (assoc :id_counter id)
               (assoc :tasks (conj (:tasks db) (assoc task :id  id))))]
     {:db newdb
      :save-to-cloud (:tasks newdb)
      :dispatch-later [(if start-now {:ms 10 :dispatch [:active-task id]})
                       (if start-now {:ms 500 :dispatch [:run-active-task]})]})))

(defn get-task [id tasks]
  (first (filter #(= id (:id %)) tasks)))

(rf/reg-event-db
 :active-task
 (fn [db [_ task-id]]
   (do 
     (.stopTimerBar js/window)
     (.clearTimerBar js/window)
     (assoc db :active-task
            {:id task-id
             :state :ready
             :start-time (time/now)
             :paused-at (time/now)
             :pause-duration 0
             :paused-times 0
             :end-time (+ (time/now)
                          (* 60000 (:time (get-task task-id (:tasks db)))))}))))

(rf/reg-event-db
 :run-active-task
 (fn [db _]
   (let
    [task (get-task (:id (:active-task db)) (:tasks db))]
     (do
       (.timerBar js/window
                  (:time task) 0
                  #(rf/dispatch [:timer-completed]))
       (->
        db
        (assoc-in [:active-task :state] :running)
        (assoc-in [:active-task :start-time] (time/now))
        (assoc-in [:active-task :end-time]
                  (+ (time/now)
                     (:pause-duration (:active-task db))
                     (* 60000
                        (:time (get-task (:id (:active-task db))
                                         (:tasks db)))))))))))


(rf/reg-event-db
 :pause-active-task
 (fn [db _]
   (do
     (.stopTimerBar js/window)
     (->
      db
      (assoc-in [:active-task :state] :paused)
      (assoc-in [:active-task :paused-at] (time/now))))))

(rf/reg-event-db
 :resume-active-task
 (fn [db _]
   (let [active (:active-task db)
         task (get-task (:id active) (:tasks db))
         paused-total (+ (:pause-duration active)
                         (- (time/now) (:paused-at active)))
         worked-total (- (- (time/now) (:start-time active))
                         paused-total)
         start-minute (/ worked-total 60000)
         end-time (+ (+ (:start-time active) paused-total)
                     (* 60000 (:time task)))]
     (do
       (.timerBar js/window
                  (:time task) start-minute
                  #(rf/dispatch [:timer-completed]))
       (->
        db
        (assoc-in  [:active-task :state] :running)
        (assoc-in  [:active-task :pause-duration] paused-total)
        (assoc-in  [:active-task :end-time] end-time)
        (update-in [:active-task :paused-times] inc))))))


(rf/reg-event-db
 :timer-completed
 (fn [db _]
   (assoc-in db [:active-task :state] :completed)))
