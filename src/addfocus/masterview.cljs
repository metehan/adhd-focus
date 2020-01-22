(ns addfocus.masterview
  (:require
   [re-frame.core :as rf]))

(defn add-task []
  [:div.add-task
   [:div.invisible-handle]
   [:div.task-form
    [:label.form-label "Task"]
    [:input.text-input {:type "text"}]
    [:label.form-label "Time"]
    [:div.time-selector
     [:label
      [:input {:type "radio", :name "time"}]
      [:span " 1"]]
     [:label
      [:input {:type "radio", :name "time"}]
      [:span " 2"]]
     [:label
      [:input {:type "radio", :name "time"}]
      [:span " 3"]]
     [:label
      [:input {:type "radio", :name "time"}]
      [:span " 5"]]
     [:label
      [:input {:type "radio", :name "time"}]
      [:span " 8"]]
     [:label
      [:input {:type "radio", :name "time"}]
      [:span "10"]]
     [:label
      [:input {:type "radio", :name "time"}]
      [:span "15"]]
     [:label
      [:input {:type "radio", :name "time"}]
      [:span "20"]]
     [:label
      [:input {:type "radio", :name "time"}]
      [:span "25"]]
     [:label
      [:input {:type "radio", :name "time"}]
      [:span "30"]]
     [:label
      [:input {:type "radio", :name "time"}]
      [:span "40"]]
     [:label
      [:input {:type "radio", :name "time"}]
      [:span "50"]]
     [:label
      [:input {:type "radio", :name "time"}]
      [:span "60"]]]
    [:div.clearfix]
    [:div.columns.col-gapless
     [:div.column.col-6
      [:button.start-now "Start now"]]
     [:div.column.col-6.text-right
      [:div.switchers
       [:label.switch
        [:input.check {:type "checkbox", :name "i"}]
        [:span]]
       [:label.switch
        [:input.check {:type "checkbox", :name "u"}]
        [:span]]]
      [:div.clearfix]
      [:button "Do later"]]]]
   [:div.handle "Add New"]])

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
  [:div.active-task
   [:div.task-name "- " (:task task)]
   [:div.start "17:23"]
   [:div.end "18:13"]
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
       [:div.growing-bar]]]]]])

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