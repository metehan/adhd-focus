(ns addfocus.masterview
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [tools.time :as time]
   ["apexcharts" :as charts]))

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
      [:label.form-label "Duration: " [:strong (:time @task)] " minutes"]
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
        [:span
         {:on-click #(rf/dispatch [:active-task (:id x)])}
         (:task x)]
        [:i.fa.fa-edit]
        [:i.fa.fa-trash-o
         {:on-click #(rf/dispatch [:delete-task (:id x)])}]]])]])

(defn active-task [active]
  [:div.active-task
   [:div.task-name "- " (:task (:task active))]
   [:div.start (time/unix->time (:start-time active))]
   [:div.end (time/unix->time (:end-time active))]
   [:article
    [:div.chart
     [:div {:class (str "bar bar-0 lightGray-face " 
                        (if (:break (:task active)) "lime" "cyan"))}
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
      :running (if (not (:break (:task active)))
                 [:i.fa.fa-pause-circle-o
                  {:on-click #(rf/dispatch [:pause-active-task])}])
      :paused [:<>
               [:i.fa.fa-play-circle-o
                {:on-click #(rf/dispatch [:resume-active-task])}]
               [:i.fa.fa-times-circle-o
                {:on-click #(rf/dispatch [:run-active-task])}]]
      :completed [:div.completed
                  ;[:h6 "Task completed and saved to history."]
                  [:button {:on-click #(rf/dispatch [:cancel-session])}
                   " ▶ Start again"]
                  [:button {:on-click #(rf/dispatch [:cancel-session])}
                   " ✖ Delete"]
                  [:hr]
                  [:h6 "Break timer"]
                  [:button.break
                   {:on-click #(rf/dispatch [:break-timer 3])}
                   "3"]
                  [:button.break
                   {:on-click #(rf/dispatch [:break-timer 5])}
                   "5"]
                  [:button.break
                   {:on-click #(rf/dispatch [:break-timer 10])}
                   "10"]
                  [:button.break
                   {:on-click #(rf/dispatch [:break-timer 20])}
                   "20"]]
      :break-completed [:div.completed 
                        [:button.break
                         {:on-click #(rf/dispatch [:break-timer 5])}
                         "5"]])]])

(defn history [series]
  (let [data (clj->js {:chart {:type "heatmap"}
                       :series series})
        ref-chart (atom nil)]
    (r/create-class
     {:reagent-render
      (fn []
        [:div
         [:h6 (:week-minutes @(rf/subscribe [:bonus-data])) " minutes"]
         [:div#chart {:ref (fn [e] (reset! ref-chart e))}]
         [:p "Hello"]])
      :component-did-mount
      (fn []
        (.render
         (charts. @ref-chart data)))})))


(defn index []
  (r/with-let
    [settings (r/atom false)
     modal (r/atom :none)
     modal-route (fn [page]
                   (if (= page @modal)
                     (reset! modal :none)
                     (reset! modal page)))]
    [:<>
     [add-task]
     [:ul.down-buttons
      [:li
       {:on-click #(modal-route :settings)}
       [:span.litext "Settings"]
       [:i.fa.fa-gears]]
      [:li
       {:on-click #(modal-route :history)}
       [:span.litext "History "]
       [:i.fa.fa-history]]
      [:li
       [:span.litext "Feedback"]
       [:i.fa.fa-commenting-o]]
      [:li
       {:on-click #(rf/dispatch [:save-temp-history])}
       [:span.litext "About"]
       [:i.fa.fa-info]]]

     [:div#settings.modal
      {:class (if (= :settings @modal) "open" "")}
      [:div.close-button
       {:on-click #(modal-route :none)}
       [:i.fa.fa-close]]
      [:div.header [:h1 "Settings"]]]

     (case @modal
       :history      [:div.modal.open
                      [:div.close-button
                       {:on-click #(modal-route :none)}
                       [:i.fa.fa-close]]
                      [:div.header [:h1 "History"]]
                      [history @(rf/subscribe [:history-chart-data])]]
       :info [:div.modal.open
              [:div.close-button
               {:on-click #(modal-route :none)}
               [:i.fa.fa-close]]
              [:div.header [:h1 "Info"]]
              [:div "Info will be here"]]
       nil)

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