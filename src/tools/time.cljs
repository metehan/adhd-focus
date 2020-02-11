(ns tools.time)

(defn now [] (.now js/Date))
(defn obj 
  ([] (obj (now)))
  ([ut] (js/Date. (if (< ut 10000000000) (* 1000 ut) ut))))

(defn datemap
  ([] (datemap (now)))
  ([ut] 
   (let [date (obj ut)]
     {:year (.getFullYear date)
      :month (.getMonth date)
      :day (.getDate date)
      :hour (.getHours date)
      :minute (.getMinutes date)
      :second (.getSeconds date)
      :weekday (.getDay date)})))

(defn unix->datemap [ut] (datemap ut))
(defn datemap->unix [dm]
  ;new Date (year, month, day, hours, minutes, seconds, milliseconds);
  (.parse js/Date 
          (js/Date. (:year dm) 
                    (:month dm) 
                    (:day dm) 
                    (:hour dm) 
                    (:minute dm) 
                    (:second dm)))
  )

(def s-ms 1000)
(def m-ms 60000)
(def h-ms 3600000)
(def d-ms 86400000)
(defn round [x] (.round js/Math x))


(defn midnight
  ([] (midnight (now)))
  ([unixtime] (- (doto (js/Date. unixtime) (.setHours 0 0 0 1)) 1)))

(defn next-midnight
  ([] (+ (midnight) d-ms))
  ([unixtime] (+ (midnight unixtime) d-ms))
  )

(defn sec-of-day
  ([] (sec-of-day (now)))
  ([unixtime]
   (round (/ (- unixtime (midnight unixtime)) s-ms))))

(defn min-of-day
  ([] (min-of-day (now)))
  ([unixtime]
   (round (/ (- unixtime (midnight unixtime)) m-ms))))

;Leftpads 0 to hours lower than 10 
(defn s-zero [a] (if (> 10 a) (str 0 a) (str a)))
(defn unix->time
  "Returns hours and minutes as string"
  [unixtime]
  (let [date (js/Date. unixtime)]
    (str (s-zero (.getHours date)) ":" (s-zero (.getMinutes date)))))

(defn todaytime->unix [hour minute]
  (+ (midnight) (* hour 3600000) (* minute 60000)))

(defn diff
  ([a b] (diff a b m-ms))
  ([a b unit] (round (/ (- b a) unit))))


(defn days-in-month [year month]
  (let [date (js/Date. year month 0)]
    (.getDate date)))

(defn weekday 
  ([year month day]
   (let [date (js/Date. year month day)]
     (.getDay date)))
  ([unixtime]
   (let [date (js/Date. unixtime)]
     (.getDay date))))

;we will mark holidays in the future
;(defn is-holiday [year month day])

(defn make-calendar [year month]
  (let [days (days-in-month year month)
        prev-end (days-in-month year (- month 1))
        first-day (weekday year (- month 1) 1)]
    (take 35
          (concat
           (reverse 
            (for [d (range (- first-day 1))] 
              {:day (- prev-end d)
               :weekday (mod (- first-day d 1) 7)
               :month "prev"}))
           (drop 1 
                 (for [d (range (+ days 1))]
                   {:day d,
                    :weekday (mod (+ (- d 1) first-day) 7) 
                    :month "this"}))
           (drop 1
                 (for [d (range 7)]
                   {:day d
                    :weekday (mod (+ (- d 1) days first-day) 7)
                    :month  "next"}))))))

(defn month-name [month]
 (case month
    0 "Ocak"
    1 "Şubat"
    2 "Mart"
    3 "Nisan"
    4 "Mayıs"
    5 "Haziran"
    6 "Temmuz"
    7 "Ağustos"
    8 "Eylül"
    9 "Ekim"
   10 "Kasım"
   11 "Aralık"))

(defn short-month-name [month]
 (case month
    0 "Jan"
    1 "Feb"
    2 "Mar"
    3 "Apr"
    4 "May"
    5 "Jun"
    6 "Jul"
    7 "Agu"
    8 "Sep"
    9 "Oct"
   10 "Nov"
   11 "Dec"))

(defn weekday-name [weekday]
 (case weekday
    0 "Mon"
    1 "Tue"
    2 "Wed"
    3 "Thu"
    4 "Fri"
    5 "Sat"
    6 "Sun"))

(defn nicedate [unixtime type]
  (if (number? unixtime)
   (let [d (unix->datemap unixtime)]
     (case type
       :short-date (str (:day d) "/" 
                        (:month d) "/" 
                        (:year d))
       :fixed-short-date (str (s-zero (:day d)) "/" 
                              (s-zero (:month d)) "/" 
                              (:year d))
       :named-date (str (:day d) " " 
                        (month-name (:month d)) " " 
                        (:year d))
       :date-time (str (s-zero (:day d)) "/"
                           (s-zero (:month d)) "/"
                           (:year d) " - "
                           (s-zero (:hour d)) ":"
                           (s-zero (:minute d)))
       :fixed-named-date (str (s-zero (:day d)) " " 
                              (short-month-name (:month d)) " " 
                              (:year d))
       :fixed-hiccup [:span.date [:span.d (s-zero (:day d))]
                      [:span.m (short-month-name (:month d))]
                      [:span.y (:year d)]]))))