(ns addfocus.subs
  (:require
   [re-frame.core :as rf]
   [tools.time :as time]
   [addfocus.helpers :refer [get-task]]))

; ---------------------------------------------------------------------------- ;
;                                   Task subs                                  ;
; ---------------------------------------------------------------------------- ;

(rf/reg-sub
 :all-tasks
 (fn [db _]
   (:tasks db)))

(rf/reg-sub
 :task/iu
 :<- [:all-tasks]
 (fn [tasks _]
   (filter #(and (:important %) (:urgent %)) tasks)))

(rf/reg-sub
 :task/i
 :<- [:all-tasks]
 (fn [tasks _]
   (filter #(and (:important %) (not (:urgent %))) tasks)))

(rf/reg-sub
 :task/u
 :<- [:all-tasks]
 (fn [tasks _]
   (filter #(and (not (:important %)) (:urgent %)) tasks)))

(rf/reg-sub
 :task/trivia
 :<- [:all-tasks]
 (fn [tasks _]
   (filter #(and (not (:important %)) (not (:urgent %))) tasks)))

; -------------------------------- Active task ------------------------------- ;

(rf/reg-sub
 :active-task
 (fn [db _]
   (cond
     (> 99 (:id (:active-task db)))
     (merge (:active-task db)
            {:task (get-task (:id (:active-task db)) [])})
     :else
     (merge (:active-task db)
            {:task (first (filter
                           #(= (:id (:active-task db))
                               (:id %))
                           (:tasks db)))}))))

; ---------------------------------------------------------------------------- ;
;                                 History subs                                 ;
; ---------------------------------------------------------------------------- ;

(rf/reg-sub
 :all-history
 (fn [db _]
   (:history db)))

(defn day-filter [day list]
  (filter #(<
            (time/midnight day)
            (:date %)
            (time/next-midnight day))
          list))

(defn hour-filter [day hour list]
  (filter #(<
            (+ (time/midnight day) (* hour time/h-ms))
            (:date %)
            (+ (time/midnight day) (* (+ 1 hour) time/h-ms)))
          list))

(defn sum-work-minutes [day hour list]
  (let [data (hour-filter day hour list)]
    (reduce + (map #(:duration %) data))))

(rf/reg-sub
 :history-data
 :<- [:all-history]
 (fn [history _]
   (into {}
         (for [ago (reverse (range -7 1))]
           (let [day (+ (time/now) (* ago time/d-ms))
                 day-data (day-filter day @(rf/subscribe [:all-history]))
                 d (time/unix->datemap day)]
             [ago
              (into {} (for [hour (range 24)]
                         [hour
                          (sum-work-minutes day hour day-data)]))])))))

(rf/reg-sub
 :bonus-data
 :<- [:history-data]
 (fn [history-data _]
   (let [week-minutes (reduce +
                              (for [x history-data]
                                (reduce +
                                        (for [y (second x)] (second y)))))
         week-multiplier (+ 1 (/ week-minutes 2500))
         hour-fn (fn [h]
                   (+ 1 (/
                         (reduce +
                                 (for [x history-data]
                                   (+ (get (second x) h)
                                      (get (second x)
                                           (if (= h 23) 0 (+ 1 h))))))
                         1000)))
         hour-bonus (into {}
                          (for [h (range 24)]
                            [h (* (hour-fn h) week-multiplier) 2]))]
     {:week-minutes week-minutes
      :hour-bonus hour-bonus})))

(rf/reg-sub
 :history-chart-data
 :<- [:history-data]
 (fn [history _]
   (into []
         (for [ago (reverse (range -7 1))]
           (let [day (+ (time/now) (* ago time/d-ms))
                 day-data (get history ago)
                 d (time/unix->datemap day)]
             {:name (if (= ago 0)
                      "Today"
                      (str (:day d) " " (time/short-month-name (:month d))))
              :data (for [hour (range 24)]
                      {:x (str (time/s-zero hour) :00)
                       :y (get day-data hour)})})))))

(defn abs [n] (max n (- n)))
(defn distance [x y] (abs (- (if (< x y) (- y x) (- x y)) 12)))
(rf/reg-sub
 :history-data-random
 (fn [_ _]
   (into []
         (for
          [day (reverse ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"])]
           {:name day
            :data (into []
                        (for [x (range 24)]
                          {:x (str x) :y (+ (rand-int 24) (* 3 (distance 12 x)))}))}))))

;Steps
;Filter day data
;Get day name
;Loop day data -> get hour string, get work duration
