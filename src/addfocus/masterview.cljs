(ns addfocus.masterview
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [tools.time :as time]))

(defn add-task []
  (r/with-let
    [task (r/atom {:task ""
                   :time 25
                   :important false
                   :urgent false})
     pick-time (fn [x] (swap! task assoc :time x))]
    [:div.add-task
     [:div.invisible-handle]
     [:div.task-form
      [:label.form-label "Task"]
      [:input.text-input
       {:type "text"
        :on-change #(swap! task assoc :task (.. % -target -value))}]
      [:label.form-label "Time " (:time @task)]
      [:div.time-selector
       (for [x [1 2 3 5 8 10 15 20 25 30 40 50 60]]
         [:label {:key x}
          [:input {:on-click #(pick-time x) :type "radio", :name "time"}]
          [:span (str x)]])]
      [:div.clearfix]
      [:div.columns.col-gapless
       [:div.column.col-6
        [:button.start-now "Start now"]]
       [:div.column.col-6.text-right
        [:div.switchers
         [:label.switch
          [:input.check
           {:on-change #(swap! task assoc
                               :important (not (:important @task)))
            :type "checkbox" :name "i"}]
          [:span "Important"]]
         [:label.switch
          [:input.check
           {:on-change #(swap! task assoc
                               :urgent (not (:urgent @task)))
            :type "checkbox" :name "u"}]
          [:span "Urgent"]]]
        [:div.clearfix]
        [:button
         {:on-click #(rf/dispatch [:new-task @task])} "Do later"]]]]
     [:div.handle "Add New"]]))

(defn task-list [{:keys [name sub-to]}]
  [:div.task-list
   [:h5 name]
   [:ul
    (for
     [x @(rf/subscribe [sub-to])]
      [:li
       {:on-click #(rf/dispatch [:active-task (:id x)])}
       (:task x)])]])

(defn active-task [task]
  (r/with-let [start (r/atom (time/now))]
    [:div.active-task
     [:div.task-name "- " (:task task)]
     [:div.start (time/unix->time @start)]
     [:div.end (time/unix->time
                (+ @start (* (:time task) 60000)))]
     [:article
      [:div.chart
       [:div {:class "bar bar-0 lime"}
        [:div {:class "face top"}
         [:div.growing-bar]]
        [:div {:class "face side-0"}
         [:div.growing-bar]]
        [:div {:class "face floor"}
         [:div.growing-bar]]
        [:div {:class "face side-a"}]
        [:div {:class "face side-b"}]
        [:div {:class "face side-1"}
         [:div.growing-bar]]]]]
     [:button#timer-button
      {:on-click
       (fn [] (
               (.timerBar js/window (:time task) 0 #(rf/dispatch [:active-task 10]))
               (reset! start (time/now))))}
      "start"]
     [:button
      {:on-click
       (fn [] (.stopTimerBar js/window))}
      "stop"]]))

;start timer - set start and end times / start green bar
;pause timer - pause green bar / remove end time 
;end timer - play notification sound / add minutes 


(defn index []
  [:<>
   [add-task]
   [:div.corner.tl
    [:div.icon
     [:i.fa.fa-list]]
    [:div [task-list
           {:name "Important / Urgent"
            :sub-to :task/iu}]]]

   [:div.corner.tr
    [:div.icon
     [:i.fa.fa-list]]
    [:div [task-list
           {:name "Important"
            :sub-to :task/i}]]]

   [:div.corner.bl
    [:div.icon
     [:i.fa.fa-list]]
    [:div [task-list
           {:name "Urgent"
            :sub-to :task/u}]]]

   [:div.corner.br
    [:div.icon
     [:i.fa.fa-list]]
    [:div [task-list
           {:name "Trivial"
            :sub-to :task/trivia}]]]

   [active-task @(rf/subscribe [:active-task])]])