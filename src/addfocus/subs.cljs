(ns addfocus.subs
  (:require 
   [re-frame.core :as rf]))

(rf/reg-sub
 :all-tasks
 (fn [db _]
    (:tasks db)))

(rf/reg-sub 
 :task/iu
 :<- [:all-tasks]
 (fn [tasks [_ counter-id]]
   (filter #(and (:important %) (:urgent %)) tasks)))

(rf/reg-sub 
 :task/i
  :<- [:all-tasks]
 (fn [tasks [_ counter-id]]
   (filter #(and (:important %) (not (:urgent %))) tasks)))

(rf/reg-sub 
 :task/u
  :<- [:all-tasks]
 (fn [tasks [_ counter-id]]
   (filter #(and (not (:important %)) (:urgent %)) tasks)))

(rf/reg-sub 
 :task/trivia
  :<- [:all-tasks]
 (fn [tasks [_ counter-id]]
   (filter #(and (not (:important %)) (not (:urgent %))) tasks)))

(rf/reg-sub
 :active-task
 (fn [db _]
   (first (filter #(= (:active-task db) (:id %)) (:tasks db)))))