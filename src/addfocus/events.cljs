(ns addfocus.events
  (:require
   [re-frame.core :as rf]
   [tools.time :as time]))

(rf/reg-event-db
 :initialize
 (constantly
  {:tasks []
   :history []
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
 (fn [[file tasks]]
   (.saveRemoteData js/window
                    file (clj->js tasks))))

(rf/reg-event-fx
 :new-task
 (fn [{:keys [db]} [_ task start-now]]
   (let
    [id (time/now)
     newdb (assoc db :tasks (conj (:tasks db) (assoc task :id  id)))]
     {:db newdb
      :save-to-cloud ["tasks" (:tasks newdb)]
      :dispatch-later [(if start-now {:ms 10 :dispatch [:active-task id]})
                       (if start-now {:ms 500 :dispatch [:run-active-task]})]})))

(rf/reg-event-fx
 :delete-task
 (fn [{:keys [db]} [_ id]]
   (let
    [newdb (assoc db :tasks (filter #(not= id (:id %)) (:tasks db)))]
     {:db newdb
      :save-to-cloud ["tasks" (:tasks newdb)]})))

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

(rf/reg-event-fx
 :timer-completed
 (fn [{:keys [db]} _]
   (let
    [history-item {:date (:start-time (:active-task db))
           :task-id (:id (:active-task db))
           :duration (:time (get-task (:id (:active-task db)) (:tasks db)))}
     newdb (-> db 
               (assoc-in [:active-task :state] :completed)
               (assoc :history (conj (:history db) history-item)))]
     {:db newdb
      :save-to-cloud ["history" (:history newdb)]})))

(rf/reg-event-fx
 :cancel-session
 (fn [{:keys [db]} _]
   (let
    [newdb (assoc db :history 
                  (filter 
                   #(not= (:start-time (:active-task db)) (:date %)) 
                   (:history db)))]
     {:db newdb
      :save-to-cloud ["history" (:history newdb)]
      :dispatch [:active-task (:id (:active-task db))]})))