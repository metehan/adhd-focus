(ns addfocus.masterview)

(defn index []
  [:div {:id "app"}
   [:div {:class "add-task"}
    [:div {:class "invisible-handle"}]
    [:div {:class "task-form"}
     [:label {:class "form-label"} "Task"]
     [:input {:class "text-input", :type "text"}]
     [:label {:class "form-label"} "Time"]
     [:div {:class "time-selector"}
      [:label
       [:input {:type "radio", :id "1", :name "time"}]
       [:span " 1"]]
      [:label
       [:input {:type "radio", :id "1", :name "time"}]
       [:span " 2"]]
      [:label
       [:input {:type "radio", :id "1", :name "time"}]
       [:span " 3"]]
      [:label
       [:input {:type "radio", :id "1", :name "time"}]
       [:span " 5"]]
      [:label
       [:input {:type "radio", :id "1", :name "time"}]
       [:span " 8"]]
      [:label
       [:input {:type "radio", :id "1", :name "time"}]
       [:span "10"]]
      [:label
       [:input {:type "radio", :id "1", :name "time"}]
       [:span "15"]]
      [:label
       [:input {:type "radio", :id "1", :name "time"}]
       [:span "20"]]
      [:label
       [:input {:type "radio", :id "1", :name "time"}]
       [:span "25"]]
      [:label
       [:input {:type "radio", :id "1", :name "time"}]
       [:span "30"]]
      [:label
       [:input {:type "radio", :id "1", :name "time"}]
       [:span "40"]]
      [:label
       [:input {:type "radio", :id "1", :name "time"}]
       [:span "50"]]
      [:label
       [:input {:type "radio", :id "1", :name "time"}]
       [:span "60"]]]
     [:div {:class "clearfix"}]
     [:div {:class "columns col-gapless"}
      [:div {:class "column col-6"}
       [:button {:class "start-now"} "Start now"]]
      [:div {:class "column col-6 text-right"}
       [:div {:class "switchers"}
        [:label {:class "switch"}
         [:input {:class "check", :type "checkbox", :name "i"}]
         [:span]]
        [:label {:class "switch"}
         [:input {:class "check", :type "checkbox", :name "u"}]
         [:span]]]
       [:div {:class "clearfix"}]
       [:button "Do later"]]]]
    [:div {:class "handle"} "Add New"]]
   [:div {:class "corner tl"}
    [:div {:class "icon"}
     [:i {:class "fa fa-list"}]]
    [:div {:class "add-item"} " Hello cats"]]
   [:div {:class "corner tr"}
    [:div {:class "icon"}
     [:i {:class "fa fa-list"}]]
    [:div {:class "add-item"}
     [:ul
      [:li "Buy a sniper"]
      [:li "Rent a hellicopter"]
      [:li "Hire a hunter"]
      [:li "Plan the trip"]
      [:li "Find the cats"]
      [:li "Negotiate with them"]
      [:li "Kiss them with love"]
      [:li "Dig a hole"]]]]
   [:div {:class "corner bl"}
    [:div {:class "icon"}
     [:i {:class "fa fa-list"}]]
    [:div {:class "add-item"} " Hello cats"]]
   [:div {:class "corner br"}
    [:div {:class "icon"}
     [:i {:class "fa fa-list"}]]
    [:div {:class "add-item"} " Hello cats"]]
   [:div {:class "active-task"}
    [:div {:class "task-name"} "- Kiss all Australian cats"]
    [:div {:class "start"} "17:23"]
    [:div {:class "end"} "18:13"]
    [:article
     [:div {:class "chart"}
      [:div {:class "bar bar-0 lime"}
       [:div {:class "face top"}
        [:div {:class "growing-bar"}]]
       [:div {:class "face side-0"}
        [:div {:class "growing-bar"}]]
       [:div {:class "face floor"}
        [:div {:class "growing-bar"}]]
       [:div {:class "face side-a"}]
       [:div {:class "face side-b"}]
       [:div {:class "face side-1"}
        [:div {:class "growing-bar"}]]]]]]])

