(ns addfocus.masterview
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [tools.time :as time]))

(defn add-task []
  (r/with-let
    [empty-task {:task ""
                 :time 25
                 :important false
                 :urgent false}
     task (r/atom empty-task)
     saved-task (r/atom {})
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
       [:div.column.col-12
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
        (if (= @saved-task @task)
          [:div.saved "Task saved!"]
          [:<>
           [:button
            {:on-click #(do (rf/dispatch [:new-task @task true])
                            (reset! saved-task @task))} 
            "Start now"]
           [:button
            {:on-click #(do (rf/dispatch [:new-task @task])
                            (reset! saved-task @task))} 
            "Do later"]])]]]
     [:div.handle "Add New"]]))

(defn task-list [{:keys [name sub-to]}]
  [:div.task-list
   [:h5 name]
   [:ul
    (for
     [x @(rf/subscribe [sub-to])]
      [:<> {:key x}
       [:li
        {:on-click #(rf/dispatch [:active-task (:id x)])}
        (:task x)]])]])

(defn active-task [active]
  (r/with-let
    [start (r/atom (time/now))
     running (r/atom false)]
    [:div.active-task
     [:div.task-name "- " (:task (:task active))]
     [:div.start (time/unix->time (:start-time active))]
     [:div.end (time/unix->time (:end-time active))]
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
     [:div.buttons
      (case (:state active)
        :ready [:i.fa.fa-play-circle-o
                {:on-click #(rf/dispatch [:run-active-task])}]
        :running [:i.fa.fa-pause-circle-o
                  {:on-click #(rf/dispatch [:pause-active-task])}]
        :paused [:<>
                 [:i.fa.fa-play-circle-o
                  {:on-click #(rf/dispatch [:resume-active-task])}]
                 [:i.fa.fa-times-circle-o
                  {:on-click #(rf/dispatch [:run-active-task])}]]
        :completed [:div.completed "Completed"])]]))

;start timer - set start and end times / start green bar
;pause timer - pause green bar / remove end time 
;end timer - play notification sound / add minutes 


(defn index []
  (r/with-let
    [settings (r/atom false)]
    [:<>
     [add-task]
     [:ul.down-buttons
      [:li
       {:on-click #(reset! settings (not @settings))}
       [:span.litext "Settings"]
       [:i.fa.fa-gears]]
      [:li
       [:span.litext "Feedback"]
       [:i.fa.fa-commenting-o]]
      [:li
       [:span.litext "People"]
       [:i.fa.fa-download]]
      [:li
       [:span.litext "Hello"]
       [:i.fa.fa-camera]]]
     [:div#settings
      {:class (if @settings "open" "")}
      [:div.close-button
       {:on-click #(reset! settings false)}
       [:i.fa.fa-close]]
      [:div.header [:h1 "Settings"]]]
     [:div.corner.tl
      [:div.icon
       [:i.fa.fa-list]]
      [:div [task-list
             {:name "Important & Urgent"
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

     [active-task @(rf/subscribe [:active-task])]]))