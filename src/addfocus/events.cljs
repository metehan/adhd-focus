(ns addfocus.events
  (:require
   [re-frame.core :as rf]
   [tools.time :as time]
   [addfocus.helpers :refer [get-task exp->lvl lvl->exp]]))

; ----------------------------- Default Database ----------------------------- ;

(rf/reg-event-db
 :initialize
 (constantly
  {:tasks []
   :history []
   :player {:level 1
            :exp 13300
            :coins 5}
   :active-task {:id 0
                 :state :ready
                 :start-time (time/now)
                 :paused-at (time/now)
                 :pause-duration 0
                 :paused-times 0
                 :end-time (+ (time/now) 1500000)}}))

(rf/dispatch [:timer-completed])

; --------------------------------- Load data -------------------------------- ;

(rf/reg-event-db
 :load-data
 (fn [db [_ context data]]
   (assoc db context data)))

;(for [x [["a" "b"] ["x" "y"]]] (first x))
;(.log js/console [["a" "b"] ["x" "y"]])
(for [x [["aa" {:wtf "xx"}] ["vv" {:s "ucks"}]]] x)

(rf/reg-fx
 :save-to-cloud
 (fn [updates]
   (doall (for [x updates]
            (.saveRemoteData js/window (first x) (clj->js (second x)))))))

; ------------------------------- Add new task ------------------------------- ;

(rf/reg-event-fx
 :new-task
 (fn [{:keys [db]} [_ task start-now]]
   (let
    [id (time/now)
     newdb (assoc db :tasks (conj (:tasks db) (assoc task :id  id)))]
     {:db newdb
      :save-to-cloud {"tasks" (:tasks newdb)}
      :dispatch-later [(if start-now {:ms 10 :dispatch [:active-task id]})
                       (if start-now {:ms 500 :dispatch [:run-active-task]})]})))

; ------------------------------- Delete a task ------------------------------ ;

(rf/reg-event-fx
 :delete-task
 (fn [{:keys [db]} [_ id]]
   (let
    [newdb (assoc db :tasks (filter #(not= id (:id %)) (:tasks db)))]
     {:db newdb
      :save-to-cloud {"tasks" (:tasks newdb)}})))

; --------------------------- Start the break timer -------------------------- ;

(rf/reg-event-fx
 :break-timer
 (fn [{:keys [db]} [_ timer]]
   (let [id (if (< 0 timer 100) timer 5)]
     {:dispatch-later [{:ms 10 :dispatch [:active-task id]}
                       {:ms 500 :dispatch [:run-active-task]}]})))

; --------------------------- Choose an active task -------------------------- ;

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

; ---------------------- Start timer for the active task --------------------- ;

(rf/reg-event-db
 :run-active-task
 (fn [db _]
   (let
    [task (get-task (:id (:active-task db)) (:tasks db))]
     (do
       (.timerBar js/window
                  (:time task) 0
                  (if (:break  task)
                    #(rf/dispatch [:break-completed])
                    #(rf/dispatch [:timer-completed])))
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

; --------------------------- Pause the active task -------------------------- ;

(rf/reg-event-db
 :pause-active-task
 (fn [db _]
   (do
     (.stopTimerBar js/window)
     (->
      db
      (assoc-in [:active-task :state] :paused)
      (assoc-in [:active-task :paused-at] (time/now))))))

; -------------------------- Resume the active task -------------------------- ;

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

; ------------------------- Task completion callback ------------------------- ;

(defn calculate-bonus
  []
  (let [at @(rf/subscribe [:active-task])
        bonus-data @(rf/subscribe [:bonus-data])
        task-point (* (:time (:task at))
                      (case [(:important (:task at)) (:urgent (:task at))]
                        [true true] 4
                        [true false] 3.5
                        [false true] 2
                        1))
        hour (.getHours (time/obj (:start-time at)))]
    {:point task-point
     :exp (int (* task-point (get (:hour-bonus bonus-data) hour)))}))

(rf/reg-event-fx
 :timer-completed
 (fn [{:keys [db]} _]
   (let
    [bonus (calculate-bonus)
     history-item {:date (:start-time (:active-task db))
                   :task-id (:id (:active-task db))
                   :exp (:exp bonus)
                   :point (:point bonus)
                   :duration (:time (get-task
                                     (:id (:active-task db))
                                     (:tasks db)))}
     newdb (-> db
               (update-in [:player :exp] #(+ (:exp bonus) %))
               (update-in [:player :coins] #(+ (:point bonus) %))
               (assoc-in [:active-task :state] :completed)
               (assoc :history (conj (:history db) history-item)))]
     {:db newdb
      :dispatch [:calculate-level]
      :save-to-cloud {"history" (:history newdb)
                      "player" (:player newdb)}})))

(rf/reg-event-fx
 :calculate-level
 (fn [{:keys [db]} _]
   (let [level (exp->lvl (:exp (:player db)))]
     (if (> level (:level (:player db)))
       {:db (assoc-in db [:player :level] level)}
       {}))))

; ------------------------- Break completion callback ------------------------ ;

(rf/reg-event-db
 :break-completed
 (fn [db _]
   (assoc-in db [:active-task :state] :break-completed)))

(rf/reg-event-fx
 :cancel-session
 (fn [{:keys [db]} _]
   (let
    [bonus (calculate-bonus)
     newdb (-> db
               (assoc :history
                      (filter
                       #(not= (:start-time (:active-task db)) (:date %))
                       (:history db)))
               (update-in [:player :exp] #(- % (:exp bonus)))
               (update-in [:player :coins] #(- % (:point bonus))))]
     {:db newdb
      :save-to-cloud {"history" (:history newdb)
                      "player" (:player newdb)}
      :dispatch [:active-task (:id (:active-task db))]})))

; ---------------------------------------------------------------------------- ;
;                    Temporary development related functions                   ;
; ---------------------------------------------------------------------------- ;

(defn random-history-last-week []
  (let [start-time (time/midnight (- (time/now)
                                     (* 7 time/d-ms)))]
    (into []
          (for [x (range 100)]
            (let [date (+ start-time
                          (* x (/ (* 7 time/d-ms) 100))
                          (rand-int (/ (* 7 time/d-ms) 100)))]
              {:date date
               :task-id 1581010679462
               :duration (rand-int 20)})))))

(rf/reg-event-fx
 :save-temp-history
 (fn [{:keys [db]} _]
   (let
    [history (random-history-last-week)
     newdb (-> db (assoc :history history))]
     {:db newdb
      :save-to-cloud ["history" (:history newdb)]})))