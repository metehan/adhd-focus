(ns addfocus.helpers)

(defn get-task [id tasks]
  (cond
    (= id 0) {:id 0
              :time 25
              :important false
              :urgent false
              :task "25 Minute Focus"}
    (< id 99) {:id id
               :time id
               :ignore true
               :break true
               :task (str id " Minute Break")}
    :else (first (filter #(= id (:id %)) tasks))))

(defn exp->lvl [exp]
  (cond 
    (< exp 13300) 1
    (< exp 320000)
    (.floor js/Math (/ (.log js/Math (/ (- exp 2500) 10000))
                       (.log js/Math 1.08)))
    :else
    (.floor js/Math (+ 44 (/ (- exp 300000) 20000)))))

(defn lvl->exp [lvl]
  (if
    (< lvl 45) (.ceil js/Math (+ 2500 (* 10000 (.pow js/Math 1.08 lvl))))
    (+ 300000 (* 20000 (- lvl 44)))))